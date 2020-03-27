# Diff coverage gradle plugin 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle.svg)](https://jitpack.io/#form-com/diff-coverage-gradle) 
[![GitHub stars](https://img.shields.io/github/stars/form-com/diff-coverage-gradle?style=flat-square)](https://github.com/form-com/diff-coverage-gradle/stargazers) 
![CI](https://github.com/form-com/diff-coverage-gradle/workflows/CI/badge.svg) 
[![GitHub issues](https://img.shields.io/github/issues/form-com/diff-coverage-gradle)](https://github.com/form-com/diff-coverage-gradle/issues)

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
Configure `Diff Coverage` plugin
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
    
        violationRules {
            minBranches = 0.9
            minLines = 0.9
            minInstructions = 0.9
            failOnViolation = true 
        }
    }
  ```  
    
</details>


## Parameters description

## Configure violations
Violations

## Execute

```
./gradlew clean check diffCoverage -PdiffBase=master
```

## HTML report example

Diff coverage HTML report

<img src="https://user-images.githubusercontent.com/8483470/77781538-a74f3480-704d-11ea-9e39-051f1001b88a.png" width=500 />

<details>
  <summary>JaCoCo HTML report</summary> 
  <img src="https://user-images.githubusercontent.com/8483470/77781534-a61e0780-704d-11ea-871e-879fb45757cd.png" width=500 />        
</details>

