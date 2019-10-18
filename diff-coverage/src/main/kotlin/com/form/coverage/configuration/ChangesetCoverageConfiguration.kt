package com.form.coverage.configuration

import org.gradle.api.Action
import org.gradle.api.file.FileCollection

open class ChangesetCoverageConfiguration(
        var jacocoExecFiles: FileCollection? = null,
        var classesDirs: FileCollection? = null,
        var srcDirs: FileCollection? = null,
        var reportDir: String? = null,
        var diffSource: DiffSourceConfiguration = DiffSourceConfiguration(),
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
                "classesDir=$classesDirs, " +
                "srcDir=$srcDirs, " +
                "reportDir=$reportDir, " +
                "diffSource=$diffSource, " +
                "violationRules=$violationRules)"
    }
}

open class DiffSourceConfiguration(
        var file: String = "",
        var url: String = ""
)

open class ReportsConfiguration(
        var html: Boolean = false
)

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
