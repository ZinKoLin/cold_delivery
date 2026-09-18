package com.colddelivery.app.data.local.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSeederPolicyTest {
    @Test fun debugBuildsMaySeedDemoBusinessData() {
        assertTrue(DemoSeeder.shouldSeedDemoBusinessData(isDebug = true))
    }

    @Test fun releaseBuildsNeverSeedDemoBusinessData() {
        assertFalse(DemoSeeder.shouldSeedDemoBusinessData(isDebug = false))
    }
}