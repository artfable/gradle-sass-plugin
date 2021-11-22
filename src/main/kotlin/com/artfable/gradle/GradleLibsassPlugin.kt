package com.artfable.gradle

import io.bit3.jsass.*
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.nio.charset.Charset

/**
 * @author artfable
 * 29.08.16
 */
class GradleLibsassPlugin : Plugin<Project> {
    companion object {
        const val CHARSET: String = "UTF-8"
    }

    override fun apply(project: Project) {
        val config = project.extensions.create("sass", GradleLibsassPluginExtension::class.java)
        val task = project.task("compileSass")

        task.apply {
            group = "frontend"

            doFirst {
                for (group in config.groups) {
                    val sourceDir: File =
                        File(group.sourceDir ?: throw IllegalArgumentException("Source dir should be provided"))
                    val outputDir: File =
                        File(group.outputDir ?: throw  IllegalArgumentException("Output dir should be provided"))

                    if (!sourceDir.isDirectory) {
                        throw IllegalArgumentException("SourceDir should be a directory")
                    }
                    if (outputDir.isFile) {
                        throw IllegalArgumentException("OutputDir shouldn't be a file!")
                    }

                    if (!outputDir.isDirectory && !outputDir.mkdirs()) {
                        throw IllegalArgumentException("Couldn't create outputDir")
                    }

                    compileGroup(sourceDir, outputDir, config, project.logger)
                }
            }
        }

        project.tasks.findByName("processResources")?.let {
            task.mustRunAfter(it)
        }
    }

    private fun compileGroup(sourceDir: File, outputDir: File, config: GradleLibsassPluginExtension, logger: Logger) {
        val compiler: Compiler = Compiler()

        sourceDir
            .listFiles { _, name -> !name.startsWith('_') && name.endsWith(".scss") }
            ?.forEach { file ->
                val options: Options = Options()
                options.includePaths.add(sourceDir)
                options.outputStyle = OutputStyle.COMPACT
                if (config.optimisation) {
                    options.importers.add(NonDuplicateImporter(logger))
                }

                val outFile: File = File(outputDir.absolutePath + File.separator + file.name.replace(".scss", ".css"))
                outFile.delete()
                if (outFile.createNewFile()) {
                    compileFile(file, outFile, options, compiler, config.ignoreFailures, logger)
                } else {
                    if (!config.ignoreFailures) {
                        throw IllegalArgumentException("Couldn't create output file (${file.name})")
                    }
                    logger.error("Skip ${file.name} because couldn't create output file")
                }
            }
    }

    private fun compileFile(
        file: File,
        outputFile: File,
        options: Options,
        compiler: Compiler,
        ignoreFailures: Boolean,
        logger: Logger
    ) {
        try {
            val output: Output = compiler.compileFile(file.toURI(), outputFile.toURI(), options)
            outputFile.writeText(output.css, Charset.forName(CHARSET))
            logger.debug("${outputFile.name} compiled")
        } catch (e: CompilationException) {
            if (ignoreFailures) {
                logger.error("Couldn't compile file '${file.name}'", e)
            } else {
                throw IllegalArgumentException("Couldn't compile file '${file.name}'", e)
            }
        }
    }
}

open class GradleLibsassPluginExtension {
    val groups: MutableList<GradleLibsassPluginGroup> = ArrayList()
    var optimisation: Boolean = true
    var ignoreFailures: Boolean = false

    fun group(action: Action<GradleLibsassPluginGroup>) {
        groups.add(GradleLibsassPluginGroup().apply(action::execute))
    }
}

open class GradleLibsassPluginGroup {
    var sourceDir: String? = null
    var outputDir: String? = null

    override fun toString(): String {
        return "GradleLibsassPluginGroup(sourceDir=$sourceDir, outputDir=$outputDir)"
    }
}