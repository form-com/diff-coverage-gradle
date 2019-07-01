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
import java.io.File

class ReportGenerator(
        projectDirectory: File,
        jacocoExec: String,
        bin: String,
        src: String,
        private val codeUpdateInfo: CodeUpdateInfo
) {

    private val title: String = projectDirectory.name

    private val executionDataFile: File = File(jacocoExec)
    private val classesDirectory: File = File(bin)
    private val sourceDirectory: File = File(src)

    fun create(
            report: Report
    ) {
        val execFileLoader = ExecFileLoader().apply {
            load(executionDataFile)
        }

        val bundleCoverage = analyzeStructure(execFileLoader)

        createVisitor(report).run {
            visitInfo(
                    execFileLoader.sessionInfoStore.infos,
                    execFileLoader.executionDataStore.contents
            )

            visitBundle(
                    bundleCoverage,
                    DirectorySourceFileLocator(sourceDirectory, "utf-8", 4)
            )

            visitEnd()
        }
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

            analyzer.analyzeAll(classesDirectory)
            return builder.getBundle(title)
        }
    }
}
