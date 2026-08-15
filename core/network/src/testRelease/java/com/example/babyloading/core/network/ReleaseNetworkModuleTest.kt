package com.example.babyloading.core.network

import org.junit.Assert.fail
import org.junit.Test

class ReleaseNetworkModuleTest {
    @Test
    fun `release runtime excludes HTTP logging`() {
        assertClassIsAbsent("okhttp3.logging.HttpLoggingInterceptor")
        assertClassIsAbsent("com.example.babyloading.core.network.DebugNetworkModule")
    }

    private fun assertClassIsAbsent(className: String) {
        try {
            Class.forName(className)
            fail("$className must not be present in Release.")
        } catch (_: ClassNotFoundException) {
            Unit
        }
    }
}
