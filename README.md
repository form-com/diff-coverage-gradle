# Diff coverage gradle plugin 
[![](https://jitpack.io/v/form-com/diff-coverage-gradle.svg)](https://jitpack.io/#form-com/diff-coverage-gradle) 
![CI](https://github.com/form-com/diff-coverage-gradle/workflows/CI/badge.svg) 
[![codecov](https://codecov.io/gh/form-com/diff-coverage-gradle/branch/master/graph/badge.svg)](https://codecov.io/gh/form-com/diff-coverage-gradle)
[![GitHub issues](https://img.shields.io/github/issues/form-com/diff-coverage-gradle)](https://github.com/form-com/diff-coverage-gradle/issues)
[![GitHub stars](https://img.shields.io/github/stars/form-com/diff-coverage-gradle?style=flat-square)](https://github.com/form-com/diff-coverage-gradle/stargazers) 

`Diff coverage` is JaCoCo extension that computes code coverage of new/modified code based on a provided [diff](https://en.wikipedia.org/wiki/Diff#Unified_format) file. 

Why should I use it?
* makes each developer to be responsible of the own code quality(see Violations section)
* helps to increase total code coverage(especially useful for old legacy projects)
* reduces time during code review(you don't need to waste your time to track what code is covered)

Reports
Violations

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

    violationRules {
        minLines = 0.9
        failOnViolation = true
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

## Execute

```
./gradlew clean check diffCoverage -PdiffBase=master
```

## HTML report example
Diff coverage HTML report
![diff Coverage](https://user-images.githubusercontent.com/8483470/77778911-a7e5cc00-7049-11ea-89b3-eff3ec8ad6a7.png)
JaCoCo HTML report
![JaCoCo Full report](https://user-images.githubusercontent.com/8483470/77778976-be8c2300-7049-11ea-93d0-d0e23b34ba75.png)
