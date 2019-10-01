Setup your project with the plugin:
```
buildscript {
    repositories {
        maven { url "http://nexus.t1.tenet/nexus/content/repositories/public/" }
    }
    dependencies {
        classpath 'com.worldapp.coverage:diff-coverage:0.5.0'
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.worldapp.diff-coverage'

diffCoverageReport {
    diffSource.file = ${PATH_TO_DIFF_FILE} // or `diffSource.url = ${URL_TO_DIFF_FILE}`. Required. 

    jacocoExecFiles = file("/path/to/jacoco/exec/file") // Optional. default `build/jacoco/test.exec`
    srcDirs = file("/path/to/sources")  // Optional. Default "src/main/java/"
    classesDirs = file("/path/to/compiled/classes") // Optional 
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

Computable diff
```
diffCoverageReport {
    afterEvaluate {
        diffSource.url =  createDiffUrl()
    }
}

ext.createDiffUrl = { ->
    // getting merge request diff(for teamcity build)
    def vcsRootUrlParameter = 'vcsroot.url'
    def mergeBranchProperty = "teamcity.build.branch"
    if(project.hasProperty(vcsRootUrlParameter) && project.hasProperty(mergeBranchProperty)) {
        def projectIdMatcher = project.property(vcsRootUrlParameter) =~ /http:\/\/gitlab\.t1\.tenet\/(.+)\.git/
        def buildBranchMatcher = project.property(mergeBranchProperty) =~ /merge-requests\/(\d+)(\/head)?/

        if (buildBranchMatcher.find() && projectIdMatcher.find()) {
            return "http://gitlab.t1.tenet/${projectIdMatcher.group(1)}/merge_requests/${buildBranchMatcher.group(1)}.diff"
        }
    }

    // getting diff of the last commit. Can be used locally and by a teamcity build
    def file = Files.createTempFile(URLEncoder.encode(project.name, "UTF-8"), ".diff").toFile()
    def outputStream = file.newOutputStream()
    exec {
        commandLine 'git', 'diff', '@^..@'
        standardOutput = outputStream
    }
    outputStream.close()
    return file.toURI().toURL()
}
```

Execute:
```
./gradlew clean diffCoverage
```
