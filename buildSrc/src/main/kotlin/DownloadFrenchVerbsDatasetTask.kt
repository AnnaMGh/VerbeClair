import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest

/**
 * Downloads the pinned `french-verbs-dataset-v<N>.json.gz` release asset from the private
 * VAMVisionsKMP repository and checks it against the SHA-256 pinned in `gradle.properties`.
 * The token is not an input: it never reaches a cache key, and a missing token only fails this task.
 */
abstract class DownloadFrenchVerbsDatasetTask : DefaultTask() {
    @get:Input abstract val datasetVersion: Property<String>
    @get:Input abstract val sha256: Property<String>
    @get:Input abstract val repository: Property<String>
    @get:Internal abstract val token: Property<String>
    @get:OutputFile abstract val datasetFile: RegularFileProperty

    @TaskAction
    fun download() {
        val token = token.orNull?.takeIf { it.isNotBlank() } ?: throw GradleException(
            "Missing $DATASET_TOKEN_PROPERTY in local.properties (fine-grained token, Contents read-only on " +
                "VAMVisionsKMP). In CI, set the $DATASET_TOKEN_VARIABLE environment variable.",
        )
        val tag = "french-verbs-dataset-v${datasetVersion.get()}"
        val assetName = "$tag.json.gz"
        val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()

        val release = client.send(
            HttpRequest.newBuilder(URI("https://api.github.com/repos/${repository.get()}/releases/tags/$tag"))
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        when (release.statusCode()) {
            200 -> Unit
            401, 403 -> throw GradleException(
                "GitHub rejected $DATASET_TOKEN_PROPERTY (HTTP ${release.statusCode()}). The token may be expired " +
                    "or lack Contents read-only access to VAMVisionsKMP: create a new one and replace it in local.properties.",
            )
            404 -> throw GradleException(
                "Release $tag not found in ${repository.get()} (HTTP 404). Check frenchVerbsDataset.version, " +
                    "and that $DATASET_TOKEN_PROPERTY has access to VAMVisionsKMP.",
            )
            else -> throw GradleException("Could not read release $tag: HTTP ${release.statusCode()}")
        }
        @Suppress("UNCHECKED_CAST")
        val assets = (JsonSlurper().parseText(release.body()) as Map<String, Any?>)["assets"] as List<Map<String, Any?>>
        val assetUrl = assets.firstOrNull { it["name"] == assetName }?.get("url") as String?
            ?: throw GradleException("Release $tag has no asset $assetName")

        // The asset URL redirects to a signed download URL that must be fetched without the token.
        val redirect = client.send(
            HttpRequest.newBuilder(URI(assetUrl))
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/octet-stream")
                .build(),
            HttpResponse.BodyHandlers.discarding(),
        )
        val downloadUri = when (redirect.statusCode()) {
            301, 302, 303, 307, 308 -> URI(redirect.headers().firstValue("Location").get())
            else -> throw GradleException("Could not download $assetName: HTTP ${redirect.statusCode()}")
        }
        val target = datasetFile.get().asFile
        val partial = File(target.parentFile, "${target.name}.part")
        target.parentFile.mkdirs()
        val response = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
            .send(HttpRequest.newBuilder(downloadUri).build(), HttpResponse.BodyHandlers.ofFile(partial.toPath()))
        if (response.statusCode() != 200) {
            partial.delete()
            throw GradleException("Could not download $assetName: HTTP ${response.statusCode()}")
        }

        val actual = MessageDigest.getInstance("SHA-256").digest(partial.readBytes())
            .joinToString("") { "%02x".format(it) }
        if (!actual.equals(sha256.get(), ignoreCase = true)) {
            partial.delete()
            throw GradleException(
                "Checksum does not match for $assetName: expected ${sha256.get()} (frenchVerbsDataset.sha256), got $actual.",
            )
        }
        partial.renameTo(target) || throw GradleException("Could not move $partial to $target")
    }

    companion object {
        const val DATASET_TOKEN_PROPERTY = "frenchVerbsDataset.githubToken"
        const val DATASET_TOKEN_VARIABLE = "FRENCH_VERBS_DATASET_TOKEN"
    }
}
