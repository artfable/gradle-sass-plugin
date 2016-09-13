package com.github.artfable.gradle

import io.bit3.jsass.importer.Import
import io.bit3.jsass.importer.Importer
import org.gradle.api.logging.Logger
import java.io.File
import java.util.*

/**
 * Importer that deletes duplicates during importing scss files
 *
 * @author artfable
 * 12.09.16
 */
class NonDuplicateImporter(val logger: Logger) : Importer {
    val duplicates: MutableSet<String> = HashSet()

    override fun apply(url: String, previous: Import?): MutableCollection<Import>? {
        previous?: throw IllegalStateException("NonDuplicateImporter can't be first")

        val srcFile: File = File(previous.absoluteUri.toString())
        var resolvedUrl: String = url
        var importPath: String = srcFile.parent + File.separator + url + ".scss"
        var importFile: File = File(importPath)

        if (!importFile.exists()) {
            val separatorIndex: Int = url.lastIndexOf('/')
            if (separatorIndex < 0) {
                resolvedUrl = '_' + resolvedUrl
            } else {
                resolvedUrl = resolvedUrl.substring(0, separatorIndex) + "/_" + resolvedUrl.substring(separatorIndex + 1)
            }
            importPath = srcFile.parent + File.separator + resolvedUrl + ".scss"
            importFile = File(importPath)
            if (!importFile.exists()) {
                throw ImportException("Couldn't find import [$url]")
            }
        }
        if (duplicates.contains(importFile.canonicalPath)) {
            logger.info("Skip ${importFile.canonicalPath}")
            return ArrayList()
        } else {
            duplicates.add(importFile.canonicalPath)
            return null
        }
    }
}

class ImportException : Throwable {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}