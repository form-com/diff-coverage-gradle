[![](https://jitpack.io/v/form-com/diff-coverage-gradle.svg)](https://jitpack.io/#form-com/diff-coverage-gradle)

Setup your project with the plugin:
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.form-com.diff-coverage-gradle:diff-coverage:0.6.0'
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.form.diff-coverage'

diffCoverageReport {
    diffSource.file = ${PATH_TO_DIFF_FILE} // or `diffSource.url = ${URL_TO_DIFF_FILE}`. Required. 

    jacocoExecFiles = file("/path/to/jacoco/exec/file") // Optional. default `build/jacoco/test.exec`
    srcDirs = file("/path/to/sources")  // Optional. Default "src/main/java/"
    classesDirs = file("/path/to/compiled/classes") // Optional 

    reports {
        html = true // Optional. default `false`
        fullCoverageReport = true // Optional. default `false`. Generates full report
        reportDir = "/path/dir/to/store/reports" // Optional. Default - jacoco report dir
    }

    violationRules {
        minBranches = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        minLines = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        minInstructions = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        failOnViolation = true // Optional. default `false`
    }
}
```

Diff file creation example
```
diffCoverageReport {
    afterEvaluate {
        diffSource.url =  createDiffUrl()
    }
    ...
}
```
The function
```
import java.nio.file.Files

...

// Takes changes from the the current branch and specified 'diffBase'. If 'diffBase' is not specified then 'HEAD' will be used.
ext.createDiffUrl = { ->
    def diffBase = project.hasProperty('diffBase') ? project.diffBase : 'HEAD'
    logger.warn("Computing coverage for changes between $diffBase and current state (including uncommitted)")

    def file = Files.createTempFile(URLEncoder.encode(project.name, 'UTF-8'), '.diff').toFile()
    file.withOutputStream { out ->
        exec {
            commandLine 'git', 'diff', '--no-color', '--minimal', diffBase
            standardOutput = out
        }
    }
    return file.toURI().toURL()
}
```

Execute:
```
./gradlew clean diffCoverage -PdiffBase=develop
```
