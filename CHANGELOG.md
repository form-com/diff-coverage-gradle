# Diff-Coverage Gradle plugin Changelog

## [0.10.0]
### Added

### Changed
- `compareWith` and `failIfCoverageLessThan` are changed to infix function 
  - <details>
    <summary>Usage example</summary>
    
     ```kotlin
    // kotlin gradle dsl 
    configure<com.form.coverage.gradle.ChangesetCoverageConfiguration> {
        // both are correct
        diffSource.git compareWith "HEAD"
        diffSource.git.compareWith("HEAD")
        
        // both are correct
        violationRules failIfCoverageLessThan 0.7
        violationRules.failIfCoverageLessThan(0.7)
    }
    
    ```
    </details>

### Fixed



## [0.9.2]
### Fixed
- redirects handling for `diffSource.url`


## [0.9.1]
### Fixed
- Fixed incorrect diff generation by JGit [#34](https://github.com/form-com/diff-coverage-gradle/issues/34)


## [0.9.0]
### Added
- autoconfiguration of `jacocoExecFiles`, `classesDirs`, `srcDirs` if JaCoCo plugin is applied and custom values are not set [#24](https://github.com/form-com/diff-coverage-gradle/issues/24)
- source file collection `diffCoverageReport.srcDirs` as `diffCoverage` task input [#28](https://github.com/form-com/diff-coverage-gradle/issues/28)
### Changed
- fail build if any of `jacocoExecFiles`, `classesDirs` or `srcDirs` is not configured and cannot be autoconfigured from JaCoCo plugin [#29](https://github.com/form-com/diff-coverage-gradle/issues/29)
### Fixed
- error message if provided Git revision doesn't exist [#19](https://github.com/form-com/diff-coverage-gradle/issues/19)
- Diff Coverage task fail when only csv or xml report is enabled [#26](https://github.com/form-com/diff-coverage-gradle/issues/26)

## [0.8.1]
### Fixed
- parsing of diff files that contains paths with special characters [#22](https://github.com/form-com/diff-coverage-gradle/issues/22)

## [0.8.0]
### Fixed
- compatibility with gradle 6.7.1 [#14](https://github.com/form-com/diff-coverage-gradle/issues/14)
- inability to create Diff Coverage outputs when report dir isn't created [#16](https://github.com/form-com/diff-coverage-gradle/issues/16)
### Added
- configuration function `failIfCoverageLessThan` that reduces Diff Coverage configuration verbosity [#17](https://github.com/form-com/diff-coverage-gradle/issues/17)
```groovy
diffCoverageReport {
     violationRules.failIfCoverageLessThan 0.9
     
     // configuration above do the same as configuration below
     violationRules {
         minBranches = 0.9
         minLines = 0.9
         minInstructions = 0.9
         failOnViolation = true
     }
}
```

## [0.7.2]
### Fixed
- compatibility with Gradle v7 [#15](https://github.com/form-com/diff-coverage-gradle/issues/15)

## [0.7.1]
### Fixed
- NPE for projects containing classes out of package [#10](https://github.com/form-com/diff-coverage-gradle/issues/10)

## [0.7.0]
### Added
- JGit as diff source [#7](https://github.com/form-com/diff-coverage-gradle/issues/7)
- support of csv diff report [#8](https://github.com/form-com/diff-coverage-gradle/issues/8)
- support of xml diff report [#8](https://github.com/form-com/diff-coverage-gradle/issues/8)

## [0.6.0]
first public release :birthday:
