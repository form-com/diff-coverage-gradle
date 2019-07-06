package com.worldapp.coverage.report

import com.worldapp.coverage.Report
import com.worldapp.coverage.createVisitor
import com.worldapp.coverage.filters.ModifiedLinesFilter
import com.worldapp.diff.CodeUpdateInfo
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.internal.analysis.FilteringAnalyzer
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.ISourceFileLocator
import org.jacoco.report.MultiSourceFileLocator
import java.io.File

class ReportGenerator(
        private val projectDirectory: File,
        private val jacocoExec: Set<File>,
        classesSources: Set<File>,
        src: Set<File>,
        private val codeUpdateInfo: CodeUpdateInfo,
        private val tabWidth: Int = 4
) {

    private val classesSources: Set<File> = classesSources.filter(File::exists).toSet()
    private val src: Set<File> = src.filter(File::exists).toSet()

    fun create(
            report: Report
    ): File {
        val execFileLoader = ExecFileLoader().apply {
            jacocoExec.forEach(this::load)
        }

        val bundleCoverage = analyzeStructure(execFileLoader)

        createVisitor(report).run {
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
        return report.htmlReportOutputDir
    }

    private fun analyzeStructure(execFileLoader: ExecFileLoader): IBundleCoverage {
        CoverageBuilder().let { builder ->
            val analyzer = FilteringAnalyzer(
                    execFileLoader.executionDataStore,
                    builder,
                    codeUpdateInfo::isInfoExists
            ) { coverage ->
                ModifiedLinesFilter(codeUpdateInfo.getClassModifications(coverage.name))
            }

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
}
