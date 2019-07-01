package com.worldapp.coverage.configuration

import com.worldapp.coverage.Report
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import java.io.File

fun ChangesetCoverageConfiguration.toReport(jacocoPluginExtension: JacocoPluginExtension): Report {
    return Report(
            reportConfiguration.html,
            reportDir
                    ?.let(::File)
                    ?: File(jacocoPluginExtension.reportsDir, "diffCoverage"),

            violationRules.failOnViolation,
            listOf(buildRules(violationRules))
    )
}

private fun buildRules(
        violationRules: ViolationRules
): Rule {
    return sequenceOf(
            ICoverageNode.CounterEntity.INSTRUCTION to violationRules.minInstructions,
            ICoverageNode.CounterEntity.BRANCH to violationRules.minBranches,
            ICoverageNode.CounterEntity.LINE to violationRules.minLines
    ).filter {
        it.second > 0.0
    }.map {
        Limit().apply {
            setCounter(it.first.name)
            minimum = it.second.toString()
        }
    }.toList().let {
        Rule().apply {
            limits = it
        }
    }
}