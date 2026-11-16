package com.vamvisions.verbeclair.core.domain.result

import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTest {

    private val success: Result<Int, DataError.Local> = Result.Success(VALUE)
    private val failure: Result<Int, DataError.Local> = Result.Error(DataError.Local.DISK_FULL)

    @Test
    fun mapTransformsASuccess() {
        assertEquals(Result.Success(VALUE * 2), success.map { it * 2 })
    }

    @Test
    fun mapKeepsAFailure() {
        assertEquals(Result.Error(DataError.Local.DISK_FULL), failure.map { it * 2 })
    }

    @Test
    fun onSuccessRunsOnlyForASuccess() {
        val seen = mutableListOf<Int>()
        success.onSuccess { seen += it }
        failure.onSuccess { seen += it }
        assertEquals(listOf(VALUE), seen)
    }

    @Test
    fun onFailureRunsOnlyForAFailure() {
        val seen = mutableListOf<DataError.Local>()
        success.onFailure { seen += it }
        failure.onFailure { seen += it }
        assertEquals(listOf(DataError.Local.DISK_FULL), seen)
    }

    private companion object {
        const val VALUE = 2
    }
}
