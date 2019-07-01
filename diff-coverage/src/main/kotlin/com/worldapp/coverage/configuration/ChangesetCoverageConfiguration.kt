package com.worldapp.coverage.configuration

import org.gradle.api.Action

open class ChangesetCoverageConfiguration(
        var jacocoExecFile: String = "build/jacoco/test.exec",
        var classesDir: String = "build/classes/kotlin/main",
        var srcDir: String = "src/main/java/",
        var reportDir: String? = null,
        var diffFile: String = "",
        val reportConfiguration: ReportsConfiguration = ReportsConfiguration(),
        val violationRules: ViolationRules = ViolationRules()
) {

    fun reports(action: Action<in ReportsConfiguration>) {
        action.execute(reportConfiguration)
    }

    fun violationRules(action: Action<in ViolationRules>) {
        action.execute(violationRules)
    }

    override fun toString(): String {
        return "ChangesetCoverageConfiguration(j" +
                "acocoExecFile=$jacocoExecFile, " +
                "classesDir=$classesDir, " +
                "srcDir=$srcDir, " +
                "reportDir=$reportDir, " +
                "diffFile=$diffFile, " +
                "violationRules=$violationRules)"
    }
}

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