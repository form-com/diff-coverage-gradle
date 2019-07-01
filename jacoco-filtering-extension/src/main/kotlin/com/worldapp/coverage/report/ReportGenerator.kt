package com.worldapp.coverage.report

import com.worldapp.coverage.filters.ModifiedLinesFilter
import com.worldapp.coverage.violation.createViolationCheckVisitor
import com.worldapp.diff.CodeUpdateInfo
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.internal.analysis.FilteringAnalyzer
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.html.HTMLFormatter
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
            reportDirectory: File,
            rules: List<Rule>
    ) {
        val execFileLoader = ExecFileLoader().apply {
            load(executionDataFile)
        }

        val bundleCoverage = analyzeStructure(execFileLoader)

        val htmlVisitor = HTMLFormatter().createVisitor(FileMultiReportOutput(reportDirectory))
        MultiReportVisitor(listOf(
                htmlVisitor,
                createViolationCheckVisitor(true, rules)
        ))
                .run {
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
