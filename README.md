Before apply you should publish the plugin to maven local
```
./gradlew publishToMavenLocal
```

Setup your project with the plugin:
```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'com.worldapp.coverage:diff-coverage:${VERSION}'
    }
}

apply plugin: 'jacoco'
apply plugin: 'com.worldapp.diff-coverage'

diffCoverageReport {
    diffFile = ${PATH_TO_DIFF_FILE}
}
```

Execute:
```
./gradlew clean diffCoverage
```
