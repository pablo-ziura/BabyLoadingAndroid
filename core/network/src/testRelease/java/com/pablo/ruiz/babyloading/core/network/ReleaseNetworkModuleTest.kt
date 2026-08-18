package com.pablo.ruiz.babyloading.core.network

import org.junit.Assert.fail
import org.junit.Test

class ReleaseNetworkModuleTest {
    @Test
    fun `release runtime excludes HTTP logging`() {
        assertClassIsAbsent("okhttp3.logging.HttpLoggingInterceptor")
        assertClassIsAbsent("com.pablo.ruiz.babyloading.core.network.DebugNetworkModule")
    }

    private fun assertClassIsAbsent(className: String) {
        try {
            Class.forName(className)
            fail("$className must not be present in Release.")
        } catch (_: ClassNotFoundException) {
            // Expected: the class must be absent from the release runtime.
        }
    }
}
