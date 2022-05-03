---
name: Bug report
about: Create a report to help us improve
title: ''
labels: ''
assignees: SurpSG

---

**Describe the bug**
A clear and concise description of what the bug is.

**Desktop (please complete the following information):**
 - OS: [e.g. Windows 10]
 - Gradle version: [e.g. 7.4.2]
 - Diff Coverage plugin version [e.g. 0.9.3]

**To Reproduce**
If possible, provide your configuration of the plugin, for example:
```groovy
diffCoverageReport {
    diffSource.file = 'diff.patch'

    violationRules.failIfCoverageLessThan 0.9
}
```

**Expected behavior**
A clear and concise description of what you expected to happen.

**Logs**
If applicable, add stacktrace, Gradle output.

**Additional context**
Add any other context about the problem here.
