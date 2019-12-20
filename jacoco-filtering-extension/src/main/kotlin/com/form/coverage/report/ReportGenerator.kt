package com.form.coverage.report

import com.form.coverage.report.analyzable.AnalyzableReport
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.ISourceFileLocator
import org.jacoco.report.MultiSourceFileLocator
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class ReportGenerator(
        private val projectDirectory: File,
        private val jacocoExec: Set<File>,
        classesSources: Set<File>,
        src: Set<File>
) {
    private val tabWidth: Int = 4

    private val classesSources: Set<File> = classesSources.filter(File::exists).toSet()
    private val src: Set<File> = src.filter(File::exists).toSet()

    fun create(analyzableReports: Set<AnalyzableReport>) {
        val execFileLoader = loadExecFiles()

        analyzableReports.forEach {
            create(execFileLoader, it)
        }
    }

    private fun loadExecFiles(): ExecFileLoader {
        val execFileLoader = ExecFileLoader()
        jacocoExec.forEach {
            log.debug("Loading exec data $it")
            try {
                execFileLoader.load(it)
            } catch (e: IOException) {
                throw RuntimeException("Cannot load coverage data from file: $it", e)
            }
        }
        return execFileLoader
    }

    private fun create(execFileLoader: ExecFileLoader, analyzableReport: AnalyzableReport) {
        val bundleCoverage = analyzeStructure { coverageVisitor ->
            analyzableReport.buildAnalyzer(execFileLoader.executionDataStore, coverageVisitor)
        }

        analyzableReport.buildVisitor().run {
            visitInfo(
                    execFileLoader.sessionInfoStore.infos,
                    execFileLoader.executionDataStore.contents
            )

            visitBundle(
                    bundleCoverage,
                    createSourcesLocator()
            )

            visitEnd()
        }
    }

    private fun analyzeStructure(
            createAnalyzer: (ICoverageVisitor) -> Analyzer
    ): IBundleCoverage {
        CoverageBuilder().let { builder ->

            val analyzer = createAnalyzer(builder)

            classesSources.forEach{ analyzer.analyzeAll(it) }

            return builder.getBundle(projectDirectory.name)
        }
    }

    private fun createSourcesLocator(): ISourceFileLocator {
        return src.asSequence()
                .map {
                    DirectorySourceFileLocator(it, "utf-8", 4)
                }
                .fold(MultiSourceFileLocator(tabWidth)) { accumulator, sourceLocator ->
                    accumulator.apply {
                        add(sourceLocator)
                    }
                }
    }

    companion object {
        val log = LoggerFactory.getLogger(ReportGenerator::class.java)
    }
}
