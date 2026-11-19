package com.vamvisions.verbeclair.core.domain.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user closed the first steps card (OB-1). An app setting, not user data: it is never
 * exported, an import never changes it, and a reinstall clears it.
 */
interface FirstStepsPreferences {

    val closed: Flow<Boolean>

    suspend fun close()
}
