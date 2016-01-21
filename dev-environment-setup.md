# Development Environment Setup Steps

1. Install Neo4j Community Edition: http://neo4j.com/download/
    - Launch the server, connect to localhost:7474, and choose an arbitrary password
2. Create an application.properties file in this directory with values for:
    - googleClientId=( look up here: https://console.developers.google.com/project/node-stand/apiui/credential )
    - googleClientSecret=( lookup here: https://console.developers.google.com/project/node-stand/apiui/credential )
    - neo4jUrl=http://localhost:7474
    - neo4jUsername=neo4j
    - neo4jPassword=( you'll be prompted to choose one when connecting to localhost:7474 )
3. Create an Intellij project at the root directory
4. On the command line, execute `./gradlew idea` which will configure your Intellij project with the right dependencies.


## Rapid Iteration on JS / CSS

1. Setup gulp task runner in Intellij: https://www.jetbrains.com/idea/help/using-gulp-task-runner.html
    - When that page tells you to install node, choose the version found in ~/.gradle/nodejs/. This directory should
    have been created during the gradle build. At time of writing, the proper download for windows is node-v0.10.22-x86.msi
2. Open gulpfile.js, right click on the "watch" task and run it. This will keep your scripts and css up to date.