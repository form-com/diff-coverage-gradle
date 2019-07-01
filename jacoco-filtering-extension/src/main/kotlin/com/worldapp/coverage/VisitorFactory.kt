package com.worldapp.coverage

import com.worldapp.coverage.violation.createViolationCheckVisitor
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.html.HTMLFormatter
import java.io.File


data class ReportConfiguration(
        val buildHtmlReport: Boolean,
        val htmlReportOutputDir: File,

        val failOnViolation: Boolean,
        val violationRules: List<Rule>
)

fun createVisitor(
        reportConfiguration: ReportConfiguration
): IReportVisitor {
    val visitors = mutableListOf<IReportVisitor>()

    if(reportConfiguration.violationRules.isNotEmpty()) {
        visitors += createViolationCheckVisitor(
                reportConfiguration.failOnViolation,
                reportConfiguration.violationRules
        )
    }

    if(reportConfiguration.buildHtmlReport) {
        val fileMultiReportOutput = FileMultiReportOutput(reportConfiguration.htmlReportOutputDir)
        visitors += HTMLFormatter().createVisitor(fileMultiReportOutput)
    }

    return visitors.let(::MultiReportVisitor)
}