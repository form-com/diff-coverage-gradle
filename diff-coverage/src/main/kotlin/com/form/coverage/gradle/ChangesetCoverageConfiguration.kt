package com.form.coverage.gradle

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.nio.file.Paths

open class ChangesetCoverageConfiguration(
    @Optional @InputFiles var jacocoExecFiles: FileCollection? = null,
    @Optional @InputFiles var classesDirs: FileCollection? = null,
    @Optional @InputFiles var srcDirs: FileCollection? = null,
    @Nested val diffSource: DiffSourceConfiguration = DiffSourceConfiguration(),
    @Nested val reportConfiguration: ReportsConfiguration = ReportsConfiguration(),
    @Nested val violationRules: ViolationRules = ViolationRules()
) {

    fun reports(action: Action<in ReportsConfiguration>) {
        action.execute(reportConfiguration)
    }

    fun violationRules(action: Action<in ViolationRules>) {
        action.execute(violationRules)
    }

    fun diffSource(action: Action<in DiffSourceConfiguration>) {
        action.execute(diffSource)
    }

    override fun toString(): String {
        return "ChangesetCoverageConfiguration(" +
                "jacocoExecFiles=$jacocoExecFiles, " +
                "classesDirs=$classesDirs, " +
                "srcDirs=$srcDirs, " +
                "diffSource=$diffSource, " +
                "reportConfiguration=$reportConfiguration, " +
                "violationRules=$violationRules)"
    }
}

open class DiffSourceConfiguration(
    @Input var file: String = "",
    @Input var url: String = "",
    @Nested val git: GitConfiguration = GitConfiguration()
) {
    override fun toString(): String {
        return "DiffSourceConfiguration(file='$file', url='$url', git=$git)"
    }
}

open class GitConfiguration(@Input var diffBase: String = "") {
    infix fun compareWith(diffBase: String) {
        this.diffBase = diffBase
    }

    override fun toString(): String {
        return "GitConfiguration(diffBase='$diffBase')"
    }
}

open class ReportsConfiguration(
    @Input var html: Boolean = false,
    @Input var xml: Boolean = false,
    @Input var csv: Boolean = false,
    @Input var baseReportDir: String = Paths.get("build", "reports", "jacoco").toString(),
    @Input var fullCoverageReport: Boolean = false
) {

    override fun toString() = "ReportsConfiguration(" +
            "html=$html, " +
            "xml=$xml, " +
            "csv=$csv, " +
            "baseReportDir='$baseReportDir'"
}

open class ViolationRules(
    @Input var minLines: Double = 0.0,
    @Input var minBranches: Double = 0.0,
    @Input var minInstructions: Double = 0.0,
    @Input var failOnViolation: Boolean = false
) {
    infix fun failIfCoverageLessThan(minCoverage: Double) {
        minLines = minCoverage
        minBranches = minCoverage
        minInstructions = minCoverage
        failOnViolation = true
    }

    override fun toString(): String {
        return "ViolationRules(" +
                "minLines=$minLines, " +
                "minBranches=$minBranches, " +
                "minInstructions=$minInstructions, " +
                "failOnViolation=$failOnViolation" +
                ")"
    }
}
