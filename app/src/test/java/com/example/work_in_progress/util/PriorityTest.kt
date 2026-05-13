package com.example.work_in_progress.util

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for the [Priority] enum, covering ordinal values and enum entries.
 */
class PriorityTest {

    @Test
    fun priority_noneOrdinal() {
        Assert.assertEquals(0, Priority.NONE.ordinal)
    }

    @Test
    fun priority_lowOrdinal() {
        Assert.assertEquals(1, Priority.LOW.ordinal)
    }

    @Test
    fun priority_mediumOrdinal() {
        Assert.assertEquals(2, Priority.MEDIUM.ordinal)
    }

    @Test
    fun priority_highOrdinal() {
        Assert.assertEquals(3, Priority.HIGH.ordinal)
    }

    @Test
    fun priority_entriesCount() {
        Assert.assertEquals(4, Priority.entries.size)
    }

    @Test
    fun priority_entriesInOrder() {
        Assert.assertEquals(Priority.NONE, Priority.entries[0])
        Assert.assertEquals(Priority.LOW, Priority.entries[1])
        Assert.assertEquals(Priority.MEDIUM, Priority.entries[2])
        Assert.assertEquals(Priority.HIGH, Priority.entries[3])
    }

    @Test
    fun priority_noneToString() {
        Assert.assertEquals("NONE", Priority.NONE.toString())
    }

    @Test
    fun priority_lowToString() {
        Assert.assertEquals("LOW", Priority.LOW.toString())
    }

    @Test
    fun priority_valueOf_none() {
        Assert.assertEquals(Priority.NONE, Priority.valueOf("NONE"))
    }

    @Test
    fun priority_valueOf_low() {
        Assert.assertEquals(Priority.LOW, Priority.valueOf("LOW"))
    }

    @Test
    fun priority_valueOf_medium() {
        Assert.assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"))
    }

    @Test
    fun priority_valueOf_high() {
        Assert.assertEquals(Priority.HIGH, Priority.valueOf("HIGH"))
    }

    @Test
    fun priority_valueOf_invalid_throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            Priority.valueOf("INVALID")
        }
    }
}
