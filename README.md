Setup your project with the plugin:
```
buildscript {
    repositories {
        maven { url "http://nexus.t1.tenet/nexus/content/repositories/public/" }
    }
    dependencies {
        classpath 'com.form.coverage:diff-coverage:0.5.2'
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.form.diff-coverage'

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

The function below can work in two mods: `latest commit of the current` branch and `merge request`
* `latest commit of the current` - works by default. Can be run locally. Takes changes from the latest commit to the current branch
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
    if(project.hasProperty('teamcity')) {
        Properties properties = new Properties()
        file(project.teamcity['teamcity.configuration.properties.file']).withInputStream {
            properties.load(it)
        }
        def projectIdMatcher = properties."vcsroot.url" =~ /http:\/\/gitlab\.t1\.tenet\/(.+)\.git/
        def buildBranchMatcher = properties."teamcity.build.branch" =~ /merge-requests\/(\d+)(\/head)?/

        if (buildBranchMatcher.find() && projectIdMatcher.find()) {
            return "http://gitlab.t1.tenet/${projectIdMatcher.group(1)}/merge_requests/${buildBranchMatcher.group(1)}.diff"
        }
    }

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
