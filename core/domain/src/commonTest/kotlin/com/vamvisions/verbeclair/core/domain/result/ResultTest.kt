package com.vamvisions.verbeclair.core.domain.result

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ResultTest {

    private val success: Result<Int, DataError.Local> = Result.Success(VALUE)
    private val failure: Result<Int, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)

    @Test
    fun mapTransformsASuccess() {
        assertThat(success.map { it * 2 }).isEqualTo(Result.Success(VALUE * 2))
    }

    @Test
    fun mapKeepsAFailure() {
        assertThat(failure.map { it * 2 }).isEqualTo(Result.Error(DataError.Local.DISK_FULL))
    }

    @Test
    fun onSuccessRunsOnlyForASuccess() {
        val seen = mutableListOf<Int>()
        success.onSuccess { seen += it }
        failure.onSuccess { seen += it }
        assertThat(seen).isEqualTo(listOf(VALUE))
    }

    @Test
    fun onFailureRunsOnlyForAFailure() {
        val seen = mutableListOf<DataError.Local>()
        success.onFailure { seen += it }
        failure.onFailure { seen += it }
        assertThat(seen).isEqualTo(listOf(DataError.Local.DISK_FULL))
    }

    private companion object {
        const val VALUE = 2
    }
}
