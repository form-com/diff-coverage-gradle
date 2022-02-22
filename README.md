# Diff coverage gradle plugin 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle.svg)](https://jitpack.io/#form-com/diff-coverage-gradle) 
![CI](https://github.com/form-com/diff-coverage-gradle/workflows/CI/badge.svg) 
[![codecov](https://codecov.io/gh/form-com/diff-coverage-gradle/branch/develop/graph/badge.svg)](https://codecov.io/gh/form-com/diff-coverage-gradle)
[![GitHub issues](https://img.shields.io/github/issues/form-com/diff-coverage-gradle)](https://github.com/form-com/diff-coverage-gradle/issues)
[![GitHub stars](https://img.shields.io/github/stars/form-com/diff-coverage-gradle?style=flat-square)](https://github.com/form-com/diff-coverage-gradle/stargazers) 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle/month.svg)](https://jitpack.io/#form-com/diff-coverage-gradle)

`Diff coverage` is JaCoCo extension that computes code coverage of new/modified code based on a provided [diff](https://en.wikipedia.org/wiki/Diff#Unified_format). 
The diff content can be provided via path to patch file, URL or using embedded git(see [parameters description](#Parameters-description)).   

Why should I use it?
* forces each developer to be responsible for its own code quality(see [diffCoverage task](#gradle-task-description))
* helps to increase total code coverage(especially useful for old legacy projects)
* reduces time of code review(you don't need to waste your time to track what code is covered)

## Installation

### Compatibility
| Diff Coverage plugin | Gradle              |
|----------------------|---------------------|
| **0.9.+**            | **6.7.1** - **7.4** |
| **0.10.0**           | **6.7.1** - **7.4** |

### Add plugin dependency  

<details open>

<summary><b>Groovy</b></summary>

```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.form-com.diff-coverage-gradle:diff-coverage:0.9.2'
    }
}
```

</details>
<details>
<summary><b>Kotlin</b></summary>

```kotlin
buildscript {
    repositories {
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.form-com.diff-coverage-gradle:diff-coverage:0.9.2")
    }
}
```

</details>

### Apply `JaCoCo` and `Diff Coverage` plugins
* `JaCoCo` is used to collect coverage data
* `Diff Coverage` is used to generate diff report

<details open>
<summary><b>Groovy</b></summary>

```groovy
apply plugin: 'jacoco'
apply plugin: 'com.form.diff-coverage'
```

</details>
<details>
<summary><b>Kotlin</b></summary>

```kotlin
plugins {
    jacoco
}
apply(plugin = "com.form.diff-coverage")
```

</details>

## Configuration

<details open>
<summary><b>Groovy</b></summary>

```groovy
diffCoverageReport {
    diffSource.file = ${PATH_TO_DIFF_FILE} 

    violationRules.failIfCoverageLessThan 0.9
    
    reports {
        html = true
    }
}
```

</details>
<details>
<summary><b>Kotlin</b></summary>

```kotlin
configure<com.form.coverage.gradle.ChangesetCoverageConfiguration> {
    diffSource.file = ${PATH_TO_DIFF_FILE}

    violationRules.failIfCoverageLessThan(0.9)
    reports {
        html = true
    }
}
```

</details>

<details>
<summary>Full example</summary> 

```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.form-com.diff-coverage-gradle:diff-coverage:0.9.2'
    }
}

apply plugin: 'java'
apply plugin: 'jacoco'
apply plugin: 'com.form.diff-coverage'

diffCoverageReport {
    diffSource {
        git.compareWith 'refs/remotes/origin/develop'
    }

    violationRules.failIfCoverageLessThan 0.9

    reports {
        html = true
        xml = true
        csv = true
    }
}
diffCoverage.dependsOn += check
```  

</details>

## Execute

```shell
./gradlew check diffCoverage
```

## Parameters description
```groovy
diffCoverageReport {
    diffSource { // Required. Only one of `file`, `url` or git must be spesified
        file = 'path/to/file.diff' //  Path to diff file 
        url = 'http://domain.com/file.diff' // URL to retrieve diff by
        git.compareWith 'refs/remotes/origin/develop' // Compares current HEAD and all uncommited with provided branch, revision or tag 
    }
    jacocoExecFiles = files('/path/to/jacoco/exec/file.exec') // Required. By default exec files are taken from jacocoTestReport configuration if any
    srcDirs = files('/path/to/sources')  // Required. By default sources are taken from jacocoTestReport configuration if any
    classesDirs = files('/path/to/compiled/classes') // Required. By default classes are taken from jacocoTestReport configuration if any

    reports {
        html = true // Optional. default `false`
        xml = true // Optional. default `false`
        csv = true // Optional. default `false`
        reportDir = 'dir/to/store/reports' // Optional. Default 'build/reports/jacoco/diffCoverage'
    }

    violationRules.failIfCoverageLessThan 0.9 // Optional. The function sets all coverage metrics to a single value, sets failOnViolation to true
    
    // configuration below is equivalent to the configuration above
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
  <summary><b>JaCoCo HTML report</b></summary> 
  <img src="https://user-images.githubusercontent.com/8483470/77781534-a61e0780-704d-11ea-871e-879fb45757cd.png" width=500 alt="JaCoCo HTML report"/>        
</details>

