package com.form.coverage.configuration

import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import java.nio.file.Paths

open class ChangesetCoverageConfiguration(
        var jacocoExecFiles: FileCollection? = null,
        var classesDirs: FileCollection? = null,
        var srcDirs: FileCollection? = null,
        val diffSource: DiffSourceConfiguration = DiffSourceConfiguration(),
        val reportConfiguration: ReportsConfiguration = ReportsConfiguration(),
        val violationRules: ViolationRules = ViolationRules()
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
        var file: String = "",
        var url: String = "",
        val git: GitConfiguration = GitConfiguration()
) {
    override fun toString(): String {
        return "DiffSourceConfiguration(file='$file', url='$url', git=$git)"
    }
}

open class GitConfiguration(var diffBase: String = "") {
    fun compareWith(diffBase: String) {
        this.diffBase = diffBase
    }

    override fun toString(): String {
        return "GitConfiguration(diffBase='$diffBase')"
    }
}

open class ReportsConfiguration(
        var html: Boolean = false,
        var baseReportDir: String = Paths.get("build", "reports", "jacoco").toString(),
        var fullCoverageReport: Boolean = false
) {
    override fun toString(): String {
        return "ReportsConfiguration(html=$html, baseReportDir='$baseReportDir', fullCoverageReport=$fullCoverageReport)"
    }
}

open class ViolationRules(
        var minLines: Double = 0.0,
        var minBranches: Double = 0.0,
        var minInstructions: Double = 0.0,
        var failOnViolation: Boolean = false
) {
    override fun toString(): String {
        return "ViolationRules(" +
                "minLines=$minLines, " +
                "minBranches=$minBranches, " +
                "minInstructions=$minInstructions, " +
                "failOnViolation=$failOnViolation" +
                ")"
    }
}
