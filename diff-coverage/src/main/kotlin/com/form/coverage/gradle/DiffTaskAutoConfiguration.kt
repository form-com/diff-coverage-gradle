package com.form.coverage.gradle

import org.gradle.api.file.FileCollection
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun DiffCoverageTask.collectFileCollectionOrThrow(
    sourceType: ConfigurationSourceType
): FileCollection {
    val (collectionSource, fileCollection) = collectFileCollectionOrAutoconfigure(sourceType)
    return if (fileCollection.isEmpty) {
        throwMissedConfigurationException(collectionSource, sourceType)
    } else {
        logger.debug(
            "{}({}) was configured from {}",
            sourceType.sourceConfigurationPath,
            sourceType.resourceName,
            collectionSource.pluginName
        )
        fileCollection
    }
}

private fun throwMissedConfigurationException(
    collectionSource: FileCollectionSource,
    sourceType: ConfigurationSourceType
): Nothing {
    val errorMessage = if (collectionSource == FileCollectionSource.DIFF_COVERAGE) {
        "'${sourceType.sourceConfigurationPath}' file collection is empty."
    } else {
        "'${sourceType.sourceConfigurationPath}' is not configured."
    }
    throw IllegalArgumentException(errorMessage)
}

private fun DiffCoverageTask.collectFileCollectionOrAutoconfigure(
    configurationType: ConfigurationSourceType
): Pair<FileCollectionSource, FileCollection> {
    val configurationSource: ConfigurationSource = obtainConfigurationSource(configurationType)
    val customConfigurationSource: FileCollection? = configurationSource.customConfigurationSource(diffCoverageReport)
    return if (customConfigurationSource != null) {
        FileCollectionSource.DIFF_COVERAGE to customConfigurationSource
    } else {
        logger.debug(
            "{} is not configured. Attempting to autoconfigure from JaCoCo...",
            configurationType.sourceConfigurationPath
        )
        FileCollectionSource.JACOCO to jacocoTestReportsSettings(configurationSource.autoconfigurationMapper)
    }
}

private fun DiffCoverageTask.jacocoTestReportsSettings(
    jacocoSettings: (JacocoReport) -> FileCollection
): FileCollection {
    return listOf(project).union(project.subprojects).asSequence()
        .map { it.tasks.findByName(DiffCoverageTask.JACOCO_REPORT_TASK) }
        .filterNotNull()
        .onEach { logger.debug("Found JaCoCo configuration in gradle project '{}'", it.project.name) }
        .map { jacocoSettings(it as JacocoReport) }
        .fold(project.files() as FileCollection) { aggregated, nextCollection ->
            aggregated.plus(nextCollection)
        }
}

private fun obtainConfigurationSource(
    configurationSourceType: ConfigurationSourceType
): ConfigurationSource {
    return when (configurationSourceType) {
        ConfigurationSourceType.CLASSES -> ConfigurationSource(JacocoReport::getAllClassDirs) {
            it.classesDirs
        }
        ConfigurationSourceType.SOURCES -> ConfigurationSource(JacocoReport::getAllSourceDirs) {
            it.srcDirs
        }
        ConfigurationSourceType.EXEC -> ConfigurationSource(JacocoReport::getExecutionData) {
            it.jacocoExecFiles
        }
    }
}

private class ConfigurationSource(
    val autoconfigurationMapper: (JacocoReport) -> FileCollection,
    val customConfigurationSource: (ChangesetCoverageConfiguration) -> FileCollection?
)

internal enum class ConfigurationSourceType(
    val sourceConfigurationPath: String,
    val resourceName: String
) {
    CLASSES("diffCoverageReport.classesDirs", ".class files"),
    SOURCES("diffCoverageReport.srcDirs", "sources"),
    EXEC("diffCoverageReport.jacocoExecFiles", ".exec files")
}

private enum class FileCollectionSource(val pluginName: String) {
    JACOCO("JaCoCo"), DIFF_COVERAGE("Diff-Coverage")
}
