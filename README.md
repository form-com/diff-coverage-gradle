Setup your project with the plugin:
```
buildscript {
    repositories {
        maven { url "http://nexus.t1.tenet/nexus/content/repositories/public/" }
    }
    dependencies {
        classpath 'com.worldapp.coverage:diff-coverage:${VERSION}'
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.worldapp.diff-coverage'

diffCoverageReport {
    diffFile = ${PATH_TO_DIFF_FILE} // required

    jacocoExecFile = "/path/to/jacoco/exec/file" // Optional. default `build/jacoco/test.exec`
    srcDir = "/path/to/sources" // Optional. Default "src/main/java/"
    classesDir = "/path/to/compiled/classes" // Optional
    reportDir = "/path/dir/to/store/reports" // Optional. Default - jacoco report dir

    reports {
        html = true // Optional. default `false`
    }

    violationRules {
        minBranches = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        minLines = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        minInstructions = 0.9 // Optional. default `0.0`. When less `0.1` then skipped
        failOnViolation = true // Optional. default `false`
    }
}
```

Execute:
```
./gradlew clean diffCoverage
```
