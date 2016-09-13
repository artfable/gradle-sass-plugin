package com.github.artfable.gradle

import groovy.lang.Closure
import io.bit3.jsass.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.StopExecutionException
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * @author artfable
 * 29.08.16
 */
class GradleLibsassPlugin : Plugin<Project> {
    val CHARSET: String = "UTF-8"

    override fun apply(project: Project) {
        val config = project.extensions.create("sass", GradleLibsassPluginExtension::class.java)
        val task = project.task("compileSass")

        task.doFirst { task ->
            for (group in config.groups) {
                val sourceDir: File = File(group.sourceDir)
                val outputDir: File = File(group.outputDir)

                if (!sourceDir.isDirectory) {
                    throw StopExecutionException("SourceDir should be a directory")
                }
                if (outputDir.isFile) {
                    throw StopExecutionException("OutputDir shouldn't be a file!")
                }

                if (!outputDir.isDirectory && !outputDir.mkdirs()) {
                    throw StopExecutionException("Couldn't create outputDir")
                }

                compileGroup(sourceDir, outputDir, config, project.logger)
            }
        }
    }

    private fun compileGroup(sourceDir: File, outputDir: File, config: GradleLibsassPluginExtension, logger: Logger) {
        val compiler: Compiler = Compiler()

        for (file in sourceDir.listFiles { file, name -> !name.startsWith('_') && name.endsWith(".scss") }) {
            val options: Options = Options()
            options.includePaths.add(sourceDir)
            options.outputStyle = OutputStyle.COMPACT
            if (config.optimisation) {
                options.importers.add(NonDuplicateImporter(logger))
            }

            val outFile: File = File(outputDir.absolutePath + File.separator + file.name.replace(".scss", ".css"))
            outFile.delete()
            if (!outFile.createNewFile()) {
                if (config.ignoreFailures) {
                    logger.error("Skip ${file.name} because couldn't create output file")
                    continue
                } else {
                    throw StopExecutionException("Couldn't create output file (${file.name})")
                }
            }

            compileFile(file, outFile, options, compiler, config.ignoreFailures, logger)
        }
    }

    private fun compileFile(file: File, outputFile: File, options: Options, compiler: Compiler, ignoreFailures: Boolean, logger: Logger) {
        try {
            val output: Output = compiler.compileFile(file.toURI(), outputFile.toURI(), options)
            outputFile.writeText(output.css, Charset.forName(CHARSET))
            logger.debug("${outputFile.name} compiled")
        } catch (e: CompilationException) {
            if (ignoreFailures) {
                logger.error("Couldn't compile file '${file.name}'", e)
            } else {
                throw GradleException("Couldn't compile file '${file.name}'", e) // can't put exception into StopExecutionException's constructor
            }
        }
    }
}

open class GradleLibsassPluginExtension() {
    val groups: MutableList<GradleLibsassPluginGroup> = ArrayList()
    var optimisation: Boolean = true
    var ignoreFailures: Boolean = false

    fun group(closure: Closure<Any>) {
        val group: GradleLibsassPluginGroup = GradleLibsassPluginGroup()
        closure.delegate = group
        closure.call()
        groups.add(group)
    }
}

open class GradleLibsassPluginGroup() {
    var sourceDir: String? = null
    var outputDir: String? = null

    override fun toString(): String {
        return "GradleLibsassPluginGroup(sourceDir=$sourceDir, outputDir=$outputDir)"
    }
}