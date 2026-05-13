package com.example.work_in_progress.util

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for [DataUtil] utility functions, covering priority conversion,
 * name formatting, and date validation.
 */
class DataUtilTest {

    // getPriority(String) tests

    @Test
    fun getPriority_string_none() {
        val result = DataUtil.getPriority("NONE")
        Assert.assertEquals(Priority.NONE, result)
    }

    @Test
    fun getPriority_string_low() {
        val result = DataUtil.getPriority("LOW")
        Assert.assertEquals(Priority.LOW, result)
    }

    @Test
    fun getPriority_string_medium() {
        val result = DataUtil.getPriority("MEDIUM")
        Assert.assertEquals(Priority.MEDIUM, result)
    }

    @Test
    fun getPriority_string_high() {
        val result = DataUtil.getPriority("HIGH")
        Assert.assertEquals(Priority.HIGH, result)
    }

    @Test
    fun getPriority_string_lowercaseInput() {
        val result = DataUtil.getPriority("low")
        Assert.assertEquals(Priority.LOW, result)
    }

    @Test
    fun getPriority_string_mixedCaseInput() {
        val result = DataUtil.getPriority("Medium")
        Assert.assertEquals(Priority.MEDIUM, result)
    }

    @Test
    fun getPriority_string_withWhitespace() {
        val result = DataUtil.getPriority("  HIGH  ")
        Assert.assertEquals(Priority.HIGH, result)
    }

    @Test
    fun getPriority_string_invalidValue_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.getPriority("INVALID")
        }
    }

    // getPriority(Int) tests

    @Test
    fun getPriority_int_0() {
        val result = DataUtil.getPriority(0)
        Assert.assertEquals(Priority.NONE, result)
    }

    @Test
    fun getPriority_int_1() {
        val result = DataUtil.getPriority(1)
        Assert.assertEquals(Priority.LOW, result)
    }

    @Test
    fun getPriority_int_2() {
        val result = DataUtil.getPriority(2)
        Assert.assertEquals(Priority.MEDIUM, result)
    }

    @Test
    fun getPriority_int_3() {
        val result = DataUtil.getPriority(3)
        Assert.assertEquals(Priority.HIGH, result)
    }

    @Test
    fun getPriority_int_greaterThan3_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.getPriority(4)
        }
    }

    @Test
    fun getPriority_int_negative_throws() {
        Assert.assertThrows(IndexOutOfBoundsException::class.java) {
            DataUtil.getPriority(-1)
        }
    }

    // getPriorityName tests

    @Test
    fun getPriorityName_0_returnsNone() {
        val result = DataUtil.getPriorityName(0)
        Assert.assertEquals("None", result)
    }

    @Test
    fun getPriorityName_1_returnsLow() {
        val result = DataUtil.getPriorityName(1)
        Assert.assertEquals("Low", result)
    }

    @Test
    fun getPriorityName_2_returnsMedium() {
        val result = DataUtil.getPriorityName(2)
        Assert.assertEquals("Medium", result)
    }

    @Test
    fun getPriorityName_3_returnsHigh() {
        val result = DataUtil.getPriorityName(3)
        Assert.assertEquals("High", result)
    }

    // getPriorityValue tests

    @Test
    fun getPriorityValue_noneString() {
        val result = DataUtil.getPriorityValue("NONE")
        Assert.assertEquals(0, result)
    }

    @Test
    fun getPriorityValue_lowString() {
        val result = DataUtil.getPriorityValue("LOW")
        Assert.assertEquals(1, result)
    }

    @Test
    fun getPriorityValue_mediumString() {
        val result = DataUtil.getPriorityValue("MEDIUM")
        Assert.assertEquals(2, result)
    }

    @Test
    fun getPriorityValue_highString() {
        val result = DataUtil.getPriorityValue("HIGH")
        Assert.assertEquals(3, result)
    }

    // validateDate tests

    @Test
    fun validateDate_validDate_1_1_2025() {
        DataUtil.validateDate("1/1/2025")
    }

    @Test
    fun validateDate_validDate_12_31_2025() {
        DataUtil.validateDate("12/31/2025")
    }

    @Test
    fun validateDate_validDate_singleDigitMonth() {
        DataUtil.validateDate("3/15/2025")
    }

    @Test
    fun validateDate_validDate_singleDigitDay() {
        DataUtil.validateDate("12/5/2025")
    }

    @Test
    fun validateDate_validDate_bothSingleDigit() {
        DataUtil.validateDate("2/7/2025")
    }

    @Test
    fun validateDate_invalidFormat_noSlash_throws() {
        Assert.assertThrows(Exception::class.java) {
            DataUtil.validateDate("01012025")
        }
    }

    @Test
    fun validateDate_invalidFormat_dashes_throws() {
        Assert.assertThrows(Exception::class.java) {
            DataUtil.validateDate("01-01-2025")
        }
    }

    @Test
    fun validateDate_invalidMonth_0_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("0/15/2025")
        }
    }

    @Test
    fun validateDate_invalidMonth_13_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("13/15/2025")
        }
    }

    @Test
    fun validateDate_invalidDay_0_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("5/0/2025")
        }
    }

    @Test
    fun validateDate_invalidDay_32_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("5/32/2025")
        }
    }
}
