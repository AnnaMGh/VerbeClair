package com.vamvisions.verbeclair.core.domain.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestScoreTest {

    @Test
    fun twoOfThreeRoundsTo67() = assertEquals(67, scorePercent(2, 3))

    @Test
    fun halfRoundsUp() = assertEquals(80, scorePercent(159, 200)) // 79.5 %

    @Test
    fun eightOfTen() = assertEquals(80, scorePercent(8, 10))

    @Test
    fun accentOnlyAnswersCountAsCorrect() {
        val lines = List(9) { LineResult.CORRECT } + LineResult.CORRECT_NO_ACCENTS
        assertEquals(100, lastTestScore(lines))
    }

    @Test
    fun failedLinesDoNotCount() {
        val lines = List(8) { LineResult.CORRECT } + List(2) { LineResult.FAILED }
        assertEquals(80, lastTestScore(lines))
    }

    @Test
    fun noCheckedLineHasNoScore() {
        assertNull(scorePercent(0, 0))
        assertNull(lastTestScore(emptyList()))
    }

    @Test
    fun boundaries() {
        assertEquals(0, scorePercent(0, 7))
        assertEquals(100, scorePercent(7, 7))
        assertEquals(1, scorePercent(1, 200)) // 0.5 % rounds up
    }
}
