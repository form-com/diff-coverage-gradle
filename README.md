# Diff coverage gradle plugin 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle.svg)](https://jitpack.io/#form-com/diff-coverage-gradle) 
![CI](https://github.com/form-com/diff-coverage-gradle/workflows/CI/badge.svg) 
[![codecov](https://codecov.io/gh/form-com/diff-coverage-gradle/branch/master/graph/badge.svg)](https://codecov.io/gh/form-com/diff-coverage-gradle)
[![GitHub issues](https://img.shields.io/github/issues/form-com/diff-coverage-gradle)](https://github.com/form-com/diff-coverage-gradle/issues)
[![GitHub stars](https://img.shields.io/github/stars/form-com/diff-coverage-gradle?style=flat-square)](https://github.com/form-com/diff-coverage-gradle/stargazers) 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle/month.svg)](https://jitpack.io/#form-com/diff-coverage-gradle)

`Diff coverage` is JaCoCo extension that computes code coverage of new/modified code based on a provided [diff](https://en.wikipedia.org/wiki/Diff#Unified_format) file. 

Why should I use it?
* makes each developer to be responsible of the own code quality(see Violations section)
* helps to increase total code coverage(especially useful for old legacy projects)
* reduces time during code review(you don't need to waste your time to track what code is covered)

## Installation
Add plugin dependency  
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.form-com.diff-coverage-gradle:diff-coverage:0.6.0'
    }
}
```
Apply `JaCoCo`(for coverage data generation) and `Diff Coverage`(for diff report generation) plugins  
```
apply plugin: 'jacoco'
apply plugin: 'com.form.diff-coverage'
```
## Configuration
```
diffCoverageReport {
    diffSource.file = ${PATH_TO_DIFF_FILE} 
    
    jacocoExecFiles = files(jacocoTestReport.executionData)
    classesDirs = files(jacocoTestReport.classDirectories)
    srcDirs = files(jacocoTestReport.sourceDirectories)

    reports {
        html = true
    }
}
```

<details>
  <summary>Full example</summary> 
   
   
  ```
    import java.nio.file.Files

    buildscript {
        repositories {
            maven { url 'https://jitpack.io' }
        }
        dependencies {
            classpath 'com.github.form-com.diff-coverage-gradle:diff-coverage:0.6.0'
        }
    }
    
    apply plugin: 'java'
    apply plugin: 'jacoco'
    apply plugin: 'com.form.diff-coverage'

    // Generate diff file using `git diff` tool    
    ext.createDiffUrl = { ->
        def diffBase = project.hasProperty('diffBase') ? project.diffBase : 'HEAD'
        def file = Files.createTempFile(URLEncoder.encode(project.name, 'UTF-8'), '.diff').toFile()
        file.withOutputStream { out ->
            exec {
                commandLine 'git', 'diff', '--no-color', '--minimal', diffBase
                standardOutput = out
            }
        }
        return file.toURI().toURL()
    }
    
    diffCoverageReport {
        afterEvaluate {
            diffSource.url =  createDiffUrl()
        } 
        
        jacocoExecFiles = files(jacocoTestReport.executionData)
        classesDirs = files(jacocoTestReport.classDirectories)
        srcDirs = files(jacocoTestReport.sourceDirectories)
    
        reports {
            html = true
        }
    }
    diffCoverage.dependsOn += check
  ```  
    
</details>

## Execute

```
./gradlew check diffCoverage
```

## Parameters description
```
diffCoverageReport {
    diffSource {
        file = 'path/to/file.diff' // Required. Only one of `file` or `url` must be spesified 
        url = 'http://domain.com/file.diff' // Required. Only one of `file` or `url` must be spesified
    }
    jacocoExecFiles = files('/path/to/jacoco/exec/file.exec') // Required
    srcDirs = files('/path/to/sources')  // Required
    classesDirs = files('/path/to/compiled/classes') // Required

    reports {
        html = true // Optional. default `false`
        reportDir = 'dir/to/store/reports' // Optional. Default 'build/reports/jacoco/diffCoverage'
    }

    violationRules {
        minBranches = 0.9 // Optional. Default `0.0`
        minLines = 0.9 // Optional. Default `0.0`
        minInstructions = 0.9 // Optional. Default `0.0`
        failOnViolation = true // Optional. Default `false`
    }
}
```

## Gradle task description
The plugin adds a task `diffCoverage` that has no dependencies
  * loads code coverage data specified by `diffCoverageReport.jacocoExecFiles`
  * analyzes the coverage data and filters according to `diffSource.url`/`diffSource.file`
  * generates html report(if enabled: `reports.html = true`) to directory `reports.baseReportsDir`
  * checks coverage ratio if `violationRules` is specified. 
    
    Violations check is enabled if any of `minBranches`, `minLines`, `minInstructions` is greater than `0.0`.
    
    Fails the execution if the violation check is enabled and `violationRules.failOnViolation = true`

## Violations check output example

Passed:
> \>Task :diffCoverage
>
> Fail on violations: true. Found violations: 0.

Failed:
>\> Task :diffCoverage FAILED
>
>Fail on violations: true. Found violations: 2.
>
>FAILURE: Build failed with an exception.
>
>...
>
>\> java.lang.Exception: Rule violated for bundle diff-coverage-gradle: instructions covered ratio is 0.5, but expected minimum is 0.9
> 
> Rule violated for bundle diff-coverage-gradle: lines covered ratio is 0.0, but expected minimum is 0.9



## HTML report example

`Diff Coverage` plugin generates standard JaCoCo HTML report, but highlights only modified code

<img src="https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png" width=500  alt="DiffCoverage HTML report"/>

<details>
  <summary>JaCoCo HTML report</summary> 
  <img src="https://user-images.githubusercontent.com/8483470/77781534-a61e0780-704d-11ea-871e-879fb45757cd.png" width=500 alt="JaCoCo HTML report"/>        
</details>

