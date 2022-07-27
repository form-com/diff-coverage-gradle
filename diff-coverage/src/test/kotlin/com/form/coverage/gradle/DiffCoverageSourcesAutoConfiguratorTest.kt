package com.form.coverage.gradle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder

class DiffCoverageSourcesAutoConfiguratorTest : StringSpec() {

    private val project: Project = ProjectBuilder.builder().build()
    private val emptyFileCollection: ConfigurableFileCollection = project.files()

    init {

        "get input file collection should throw when source files are not specified" {
            forAll(
                row(
                    "'diffCoverageReport.jacocoExecFiles' is not configured.",
                    DiffCoverageSourcesAutoConfigurator::obtainExecFiles
                ),
                row(
                    "'diffCoverageReport.classesDirs' is not configured.",
                    DiffCoverageSourcesAutoConfigurator::obtainClassesFiles
                ),
                row(
                    "'diffCoverageReport.srcDirs' is not configured.",
                    DiffCoverageSourcesAutoConfigurator::obtainSourcesFiles
                )
            ) { expectedError, sourceAccessor ->
                // setup
                val autoConfigurator = DiffCoverageSourcesAutoConfigurator(
                    property(ChangesetCoverageConfiguration()),
                    emptyFileCollection,
                    emptyFileCollection,
                    emptyFileCollection
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(autoConfigurator)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }

        }

        "get input file collection should throw when file collection is empty" {
            forAll(
                row(
                    "'diffCoverageReport.jacocoExecFiles' file collection is empty.",
                    DiffCoverageSourcesAutoConfigurator::obtainExecFiles
                ),
                row(
                    "'diffCoverageReport.classesDirs' file collection is empty.",
                    DiffCoverageSourcesAutoConfigurator::obtainClassesFiles
                ),
                row(
                    "'diffCoverageReport.srcDirs' file collection is empty.",
                    DiffCoverageSourcesAutoConfigurator::obtainSourcesFiles
                )
            ) { expectedError, sourceAccessor ->
                // setup
                val diffCoverageReport = ChangesetCoverageConfiguration().apply {
                    jacocoExecFiles = emptyFileCollection
                    classesDirs = emptyFileCollection
                    srcDirs = emptyFileCollection
                }
                val autoConfigurator = DiffCoverageSourcesAutoConfigurator(
                    property(diffCoverageReport),
                    emptyFileCollection,
                    emptyFileCollection,
                    emptyFileCollection
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(autoConfigurator)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }
        }

    }

    private inline fun <reified T> property(propertyValue: T): Property<T> {
        return project.objects.property(T::class.java).apply {
            set(propertyValue)
        }
    }

}
