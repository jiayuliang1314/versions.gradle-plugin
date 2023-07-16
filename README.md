# versions.gradle-plugin
Gradle plugin to discover dependency updates and format versions.gradle

./gradlew :app:dependencies --configuration prodReleaseRuntimeClasspath > dependices.txt

Please use like 
versions-gradle-checker-3.0.jar /admin/project/demo/version.gradle /admin/project/demo/dependices.txt /admin/project/demo/report.html



gradle dependencies

+, -, | and \ are just used to draw the tree - it's a kind of ASCII art.
When it comes to (*) and -> please refer to this question and answer.
(*) - is used to indicate that particular dependency is described somewhere else in the tree
-> - is used to point the dependency that wins in version conflict.