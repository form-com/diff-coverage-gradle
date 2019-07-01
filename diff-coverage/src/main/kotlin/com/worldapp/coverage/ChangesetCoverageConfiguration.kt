package com.worldapp.coverage

import org.gradle.api.Action

open class ChangesetCoverageConfiguration(
        var jacocoExecFile: String? = "build/jacoco/test.exec",
        var classesDir: String? = "build/classes/kotlin/main",
        var srcDir: String? = "src/main/java/",
        var reportDir: String? = null,
        var diffFile: String? = "",
        val violationRules: ViolationRules = ViolationRules()
) {

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

open class ViolationRules(
        var minLines: Double = 0.0,
        var minBranches: Double = 0.0,
        var minInstructions: Double = 0.0
) {
    override fun toString(): String {
        return "ViolationRules(minLines=$minLines, minBranches=$minBranches, minInstructions=$minInstructions)"
    }
}