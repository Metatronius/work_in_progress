// Copyright (c) 2026 Metatronius. All rights reserved.

package com.example.work_in_progress.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [DataUtil] on actual Android device/emulator.
 * Verifies utility functions work correctly in Android runtime environment.
 */
@RunWith(AndroidJUnit4::class)
class DataUtilInstrumentedTest {

    @Test
    fun getPriority_stringConversionWorks() {
        val result = DataUtil.getPriority("MEDIUM")
        Assert.assertEquals(Priority.MEDIUM, result)
    }

    @Test
    fun getPriority_intConversionWorks() {
        val result = DataUtil.getPriority(2)
        Assert.assertEquals(Priority.MEDIUM, result)
    }

    @Test
    fun getPriorityName_returnsCorrectLabel() {
        val names = listOf(
            DataUtil.getPriorityName(0),
            DataUtil.getPriorityName(1),
            DataUtil.getPriorityName(2),
            DataUtil.getPriorityName(3)
        )
        Assert.assertEquals(listOf("None", "Low", "Medium", "High"), names)
    }

    @Test
    fun validateDate_acceptsValidDates() {
        // Should not throw
        DataUtil.validateDate("1/1/2025")
        DataUtil.validateDate("12/31/2025")
        DataUtil.validateDate("5/15/2025")
    }

    @Test
    fun validateDate_rejectsInvalidMonth() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("13/1/2025")
        }
    }

    @Test
    fun validateDate_rejectsInvalidDay() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("5/32/2025")
        }
    }

    @Test
    fun validateDate_rejectsZeroMonth() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("0/15/2025")
        }
    }

    @Test
    fun validateDate_rejectsZeroDay() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            DataUtil.validateDate("5/0/2025")
        }
    }

    @Test
    fun dateFormat_matchesValidDates() {
        Assert.assertTrue(DataUtil.dateFormat.matches("1/1/2025"))
        Assert.assertTrue(DataUtil.dateFormat.matches("12/31/2025"))
        Assert.assertTrue(DataUtil.dateFormat.matches("5/5/2025"))
    }

    @Test
    fun dateFormat_rejectsInvalidFormats() {
        Assert.assertFalse(DataUtil.dateFormat.matches("01-01-2025"))
        Assert.assertFalse(DataUtil.dateFormat.matches("2025-01-01"))
        Assert.assertFalse(DataUtil.dateFormat.matches("1/1/25"))
    }

    @Test
    fun getPriorityValue_mapsCorrectly() {
        Assert.assertEquals(0, DataUtil.getPriorityValue("NONE"))
        Assert.assertEquals(1, DataUtil.getPriorityValue("LOW"))
        Assert.assertEquals(2, DataUtil.getPriorityValue("MEDIUM"))
        Assert.assertEquals(3, DataUtil.getPriorityValue("HIGH"))
    }
}
