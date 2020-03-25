Setup your project with the plugin:
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.form.coverage:diff-coverage:0.6.0'
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

The function below can work in two mods: `current & 'diffBase' branches diff` and `merge request`
* `current & 'diffBase' branches diff` - works by default. Can be run locally. Takes changes from the the current branch and specified 'diffBase'.
    If 'diffBase' is not specified then 'HEAD' will be used.
* `merge request` - Teamcity only mode. Takes properties from merge-request build and computes changes provided by the merge request

Usage
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
