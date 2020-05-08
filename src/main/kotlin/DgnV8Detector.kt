package com.peeveen.tika.detect.dgn

import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.tika.detect.Detector
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * DGN v8 files are Microsoft Compound Documents (like pre-2007 Office files). Internally, these files are
 * like a ZIP, with a filesystem-like structure. This class reports a file as being a DGN v8 file if:
 *    a) it is a Microsoft Compound Document, and
 *    b) it contains a set of files with names that SEEM to be common to all DGN v8 files.
 */
class DgnV8Detector : Detector {
    override fun detect(input: InputStream, metadata: Metadata): MediaType {
        // Check for DGN8, Microsoft Compound Document, containing telltale files.
        // Apache POI is the boy for that job.

        // We are obliged to leave the input stream at the same position as we got it.
        val tikaInputStream = input as TikaInputStream
        val inputLength = tikaInputStream.length.toInt()
        input.mark(inputLength)
        try {
            // Unfortunately, POI has other ideas. So we need access to the whole file (for
            // POI purposes) but we don't want to allow POI to do any annoying mark/reset/close
            // operations that will interfere with Tika. Only way I can see around this is to
            // read the whole file into memory or transfer it to a temporary file. For now, I'm
            // going down the memory route.
            val dgnBytes = input.readNBytes(inputLength)
            val poiDoc = POIFSFileSystem(ByteArrayInputStream(dgnBytes))
            // If we find all the "signature" filenames, then we're happy.
            if (poiDoc.root.filterIsInstance<DocumentEntry>().map { it.name }.containsAll(DGN_V8_SIGNATURE_FILES))
                return DGN_V8_MEDIA_TYPE
        } catch (e: Exception) {
            // Thrown if:
            //   a) document is not a Microsoft Compound Document, or ...
            //   b) IS a Microsoft Compound Document, but is recognised by POI as a known Office format.
            // In either case, it ain't a DGN.
        } finally {
            input.reset()
        }
        // If a Detector fails to detect a format, it must return this.
        return MediaType.OCTET_STREAM
    }
}

// Filenames found in a DGN v8 compound file that will convince us that it is a DGN v8.
private val DGN_V8_SIGNATURE_FILES = setOf("Dgn~Mf", "Dgn~S", "Dgn~H", "\u0005SummaryInformation")

// Not easy to find, but the official MIME types of DGN files are mentioned on this Bentley page:
// https://communities.bentley.com/products/projectwise/content_management/w/wiki/5617/5617
val DGN_V7_MEDIA_TYPE = MediaType("image", "vnd.dgn")
val DGN_V8_MEDIA_TYPE = MediaType(DGN_V7_MEDIA_TYPE, mapOf(Pair("ver", "8")))
