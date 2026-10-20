package com.example.cycletracker.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdaterVersionTest {

    @Test
    fun newerVersionsAreDetected() {
        assertTrue(Updater.isNewer("1.1", "1.0"))
        assertTrue(Updater.isNewer("v1.0.1", "1.0"))
        assertTrue(Updater.isNewer("1.10", "1.9"))
        assertTrue(Updater.isNewer("2.0", "1.99.99"))
    }

    @Test
    fun sameOrOlderVersionsAreNotNewer() {
        assertFalse(Updater.isNewer("1.0", "1.0"))
        assertFalse(Updater.isNewer("v1.0", "1.0.0"))
        assertFalse(Updater.isNewer("1.0", "1.1"))
        assertFalse(Updater.isNewer("1.9", "1.10"))
    }

    @Test
    fun suffixesAreIgnored() {
        assertFalse(Updater.isNewer("1.1-beta", "1.1"))
        assertTrue(Updater.isNewer("1.2-rc1", "1.1"))
    }
}
