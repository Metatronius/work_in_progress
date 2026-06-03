package com.example.work_in_progress.util

import org.junit.Assert
import org.junit.Test

/** Unit tests for [DataUtil] edge cases and boundary conditions for date validation. */
class DataUtilEdgeCasesTest {

    @Test
    fun validateDate_veryOldDate_year1900_succeeds() {
        DataUtil.validateDate("12/25/1900")
    }

    @Test
    fun validateDate_veryOldDate_year1970_succeeds() {
        DataUtil.validateDate("01/01/1970")
    }

    @Test
    fun validateDate_farFutureDate_year2100_succeeds() {
        DataUtil.validateDate("12/31/2100")
    }

    @Test
    fun validateDate_farFutureDate_year2999_succeeds() {
        DataUtil.validateDate("06/15/2999")
    }

    @Test
    fun validateDate_monthBoundary_january_succeeds() {
        DataUtil.validateDate("01/15/2026")
    }

    @Test
    fun validateDate_monthBoundary_december_succeeds() {
        DataUtil.validateDate("12/31/2026")
    }

    @Test
    fun validateDate_dayBoundary_day1_succeeds() {
        DataUtil.validateDate("05/01/2026")
    }

    @Test
    fun validateDate_dayBoundary_day31_succeeds() {
        DataUtil.validateDate("05/31/2026")
    }

    @Test
    fun validateDate_februaryDay29_succeeds() {
        // Note: validateDate does not check leap year validity, only format and month/day ranges
        DataUtil.validateDate("02/29/2024")
    }

    @Test
    fun validateDate_februaryDay29_nonLeapYear_alsoSucceeds() {
        // validateDate does not validate actual calendar constraints
        DataUtil.validateDate("02/29/2023")
    }

    @Test
    fun validateDate_invalidMonth_month0_fails() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("00/15/2026")
        }
    }

    @Test
    fun validateDate_invalidMonth_month13_fails() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("13/15/2026")
        }
    }

    @Test
    fun validateDate_invalidDay_day0_fails() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("05/00/2026")
        }
    }

    @Test
    fun validateDate_invalidDay_day32_fails() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("05/32/2026")
        }
    }

    @Test
    fun validateDate_whitespaceOnlyString_fails() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("   ")
        }
    }

    @Test
    fun validateDate_singleDigitMonth_succeeds() {
        DataUtil.validateDate("5/20/2026")
    }

    @Test
    fun validateDate_singleDigitDay_succeeds() {
        DataUtil.validateDate("05/5/2026")
    }

    @Test
    fun validateDate_bothSingleDigit_succeeds() {
        DataUtil.validateDate("5/5/2026")
    }

    @Test
    fun validateDate_aprilDay30_succeeds() {
        DataUtil.validateDate("04/30/2026")
    }

    @Test
    fun validateDate_aprilDay31_alsoSucceeds() {
        // validateDate allows any day 1-31, doesn't check specific month constraints
        DataUtil.validateDate("04/31/2026")
    }

    @Test
    fun getPriority_lowercaseNone_succeeds() {
        val priority = DataUtil.getPriority("none")
        assert(priority == Priority.NONE)
    }

    @Test
    fun getPriority_mixedCaseLow_succeeds() {
        val priority = DataUtil.getPriority("LoW")
        assert(priority == Priority.LOW)
    }

    @Test
    fun getPriority_trimmedWhitespace_succeeds() {
        val priority = DataUtil.getPriority("  MEDIUM  ")
        assert(priority == Priority.MEDIUM)
    }

    @Test
    fun getPriority_invalidString_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.getPriority("INVALID")
        }
    }

    @Test
    fun getPriority_int_boundary0_succeeds() {
        val priority = DataUtil.getPriority(0)
        assert(priority == Priority.NONE)
    }

    @Test
    fun getPriority_int_boundary3_succeeds() {
        val priority = DataUtil.getPriority(3)
        assert(priority == Priority.HIGH)
    }

    @Test
    fun getPriority_int_negative_throws() {
        Assert.assertThrows(Exception::class.java) {
            DataUtil.getPriority(-1)
        }
    }

    @Test
    fun getPriority_int_tooLarge_throws() {
        Assert.assertThrows(Exception::class.java) {
            DataUtil.getPriority(4)
        }
    }

    @Test
    fun getPriorityName_emptyString_handlesGracefully() {
        Assert.assertThrows(Exception::class.java) {
            DataUtil.getPriorityName(-1)
        }
    }

    @Test
    fun getPriorityValue_allPriorities_returnsCorrectOrdinal() {
        assert(DataUtil.getPriorityValue("NONE") == 0)
        assert(DataUtil.getPriorityValue("LOW") == 1)
        assert(DataUtil.getPriorityValue("MEDIUM") == 2)
        assert(DataUtil.getPriorityValue("HIGH") == 3)
    }
}
