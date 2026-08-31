package com.pablo.ruiz.babyloading.feature.tracking

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedTrackingSourceContractTest {
    @Test
    fun productionCaptureFlowDoesNotMaterializeWholePhotoByteArrays() {
        val sourceRoot = File(
            "src/main/java/com/pablo/ruiz/babyloading/feature/tracking",
        )
        assertTrue(sourceRoot.isDirectory)

        val productionSource = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .joinToString(separator = "\n") { it.readText() }

        assertFalse(productionSource.contains("readBytes("))
        assertFalse(productionSource.contains("writeBytes("))
        assertFalse(productionSource.contains("ByteArray"))
    }
}
