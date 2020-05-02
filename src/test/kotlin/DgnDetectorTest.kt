package com.stevenfrew.tika.detect.dgn.test

import com.stevenfrew.tika.detect.dgn.DGN_V7_MEDIA_TYPE
import com.stevenfrew.tika.detect.dgn.DGN_V8_MEDIA_TYPE
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class DgnDetectorTest {
    /**
     * Asks Tika to detect the format of every file found in the given resource folder.
     * The format that Tika returns is compared against the given list of formats.
     * The test will fail if the result of that match does not agree with the shouldMatch parameter.
     */
    private fun testFolderContents(resourceFolderName: String, formats: Array<MediaType>, shouldMatch: Boolean = true) {
        val folderUri = DgnDetectorTest::class.java.classLoader.getResource(resourceFolderName)?.toURI()
        if (folderUri != null) {
            val notDgnFolder = File(folderUri)
            val walker = notDgnFolder.walk()
            val tika = Tika()
            val typeStrings = formats.map { it.toString() }
            for (file in walker.iterator().asSequence()
                .filter { it.isFile && !it.isHidden }) {
                val detectedType = tika.detect(file)
                assertEquals(
                    typeStrings.contains(detectedType),
                    shouldMatch,
                    "The format of the file \"${file.name}\" was not detected correctly."
                )
            }
        }
    }

    @Test
    fun detectNotDGNs() {
        testFolderContents("notdgn", arrayOf(DGN_V7_MEDIA_TYPE, DGN_V8_MEDIA_TYPE), false)
    }

    @Test
    fun detectDGN7s() {
        testFolderContents("dgn/dgn7", arrayOf(DGN_V7_MEDIA_TYPE))
    }

    @Test
    fun detectDGN8s() {
        testFolderContents("dgn/dgn8", arrayOf(DGN_V8_MEDIA_TYPE))
    }
}