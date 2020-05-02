package com.stevenfrew.tika.dgn.detect

import org.apache.poi.poifs.filesystem.DocumentEntry
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.tika.detect.Detector
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import java.io.FilterInputStream
import java.io.InputStream

/**
 * DGN v8 files are Microsoft Compound Documents (like pre-2007 Office files). Internally, these files are
 * like a ZIP, with a filesystem-like structure. This class reports a file as being a DGN v8 file if:
 *    a) it is a Microsoft Compound Document, and
 *    b) it contains a set of files with names that SEEM to be common to all DGN v8 files.
 */
class DgnV8Detector : Detector {
    // Filenames found in a DGN v8 compound file that will convince us that it is a DGN v8.
    private val dgnV8SignatureFiles = setOf("Dgn~Mf", "Dgn~S", "Dgn~H", "\u0005SummaryInformation")

    override fun detect(input: InputStream, metadata: Metadata): MediaType {
        // Check for DGN8, Microsoft Compound Document, containing telltale files.
        // Apache POI is the boy for that job.
        try {
            val poiDoc = POIFSFileSystem(object : FilterInputStream(input) {
                // Annoyingly, POI closes the stream, and Tika does not like that, as it might
                // want to pass it to another detector. We need this dumb override to stop that.
                override fun close() {}
            })
            // If we find all the "signature" filenames, then we're happy.
            if (poiDoc.root.filterIsInstance<DocumentEntry>().map { it.name }.containsAll(dgnV8SignatureFiles))
                return DGN_V8_MEDIA_TYPE
        } catch (e: Exception) {
            // Thrown if:
            //   a) document is not a Microsoft Compound Document, or ...
            //   b) IS a Microsoft Compound Document, but is recognised by POI as a known Office format.
            // In either case, it ain't a DGN.
        }
        // If a Detector fails to detect a format, it must return this.
        return MediaType.OCTET_STREAM
    }
}

// Not easy to find, but the official MIME types of DGN files are mentioned on this Bentley page:
// https://communities.bentley.com/products/projectwise/content_management/w/wiki/5617/5617
val DGN_V7_MEDIA_TYPE = MediaType("image", "vnd.dgn")
val DGN_V8_MEDIA_TYPE = MediaType("image", "vnd.dgn", mapOf(Pair("ver", "8")))
