package com.form.coverage.gradle

import com.form.coverage.config.DiffCoverageConfig
import com.form.coverage.config.DiffSourceConfig
import com.form.coverage.config.ReportConfig
import com.form.coverage.config.ReportsConfig
import com.form.coverage.config.ViolationRuleConfig
import com.form.coverage.report.ReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

open class DiffCoverageTask @Inject constructor(
    objectFactory: ObjectFactory
) : DefaultTask() {

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @get:Input
    val projectDirProperty: Property<File> = objectFactory.property(File::class.java)

    @get:Input
    val rootProjectDirProperty: Property<File> = objectFactory.property(File::class.java)

    @get:InputFiles
    val jacocoExecFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val jacocoSourceFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @get:InputFiles
    val jacocoClassesFiles: Property<FileCollection> = objectFactory.property(FileCollection::class.java)

    @Nested
    val diffCoverageReport: Property<ChangesetCoverageConfiguration> = objectFactory.property(
        ChangesetCoverageConfiguration::class.java
    )

    @OutputDirectory
    fun getOutputDir(): File {
        return getReportOutputDir().toFile().apply {
            log.debug(
                "Diff Coverage output dir: $absolutePath, " +
                        "exists=${exists()}, isDir=$isDirectory, canRead=${canRead()}, canWrite=${canWrite()}"
            )
        }
    }

    private val sourcesConfigurator: DiffCoverageSourcesAutoConfigurator by lazy {
        DiffCoverageSourcesAutoConfigurator(
            diffCoverageReport,
            jacocoExecFiles.get(),
            jacocoClassesFiles.get(),
            jacocoSourceFiles.get()
        )
    }

    @TaskAction
    fun executeAction() {
        log.info("DiffCoverage configuration: $diffCoverageReport")
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            log.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }

        val reportGenerator = ReportGenerator(rootProjectDirProperty.get(), buildDiffCoverageConfig())
        reportGenerator.saveDiffToDir(reportDir).apply {
            log.info("diff content saved to '$absolutePath'")
        }
        reportGenerator.create()
    }

    private fun getReportOutputDir(): Path {
        return Paths.get(diffCoverageReport.get().reportConfiguration.baseReportDir).let { path ->
            if (path.isAbsolute) {
                path
            } else {
                projectDirProperty.map { it.toPath().resolve(path) }.get()
            }
        }
    }

    private fun buildDiffCoverageConfig(): DiffCoverageConfig {
        val diffCovConfig: ChangesetCoverageConfiguration = diffCoverageReport.get()
        return DiffCoverageConfig(
            reportName = projectDirProperty.map { it.name }.get(),
            diffSourceConfig = DiffSourceConfig(
                file = diffCovConfig.diffSource.file,
                url = diffCovConfig.diffSource.url,
                diffBase = diffCovConfig.diffSource.git.diffBase
            ),
            reportsConfig = ReportsConfig(
                baseReportDir = getReportOutputDir().toAbsolutePath().toString(),
                html = ReportConfig(enabled = diffCovConfig.reportConfiguration.html, "html"),
                csv = ReportConfig(enabled = diffCovConfig.reportConfiguration.csv, "report.csv"),
                xml = ReportConfig(enabled = diffCovConfig.reportConfiguration.xml, "report.xml"),
                fullCoverageReport = diffCovConfig.reportConfiguration.fullCoverageReport
            ),
            violationRuleConfig = ViolationRuleConfig(
                minBranches = diffCovConfig.violationRules.minBranches,
                minInstructions = diffCovConfig.violationRules.minInstructions,
                minLines = diffCovConfig.violationRules.minLines,
                failOnViolation = diffCovConfig.violationRules.failOnViolation
            ),
            execFiles = sourcesConfigurator.obtainExecFiles().files,
            classFiles = sourcesConfigurator.obtainClassesFiles().files,
            sourceFiles = sourcesConfigurator.obtainSourcesFiles().files
        )
    }

    companion object {
        const val JACOCO_REPORT_TASK = "jacocoTestReport"
        val log: Logger = LoggerFactory.getLogger(DiffCoverageTask::class.java)
    }

}
