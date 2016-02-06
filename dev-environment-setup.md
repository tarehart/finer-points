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


## Launching the Server

Just run `./gradlew bootRun` and you should be able to connect at localhost:8080.

## Debugging

You can attach a java remote debugger on port 5005, as configured in the build.gradle file.

## Rapid Iteration

### Java

Exit the `bootRun` process (Ctrl+C) and re-run it.

### JS / CSS

The project currently uses 'Gulp' to process .scss files, combine, minify, etc.
There are two different methods of getting it to constantly watch for changes.

1. Command line
    1. Open a terminal and execute `./gradlew gulp_watch`
2. Or, if you prefer, via IntelliJ
    1. Setup gulp task runner in Intellij: https://www.jetbrains.com/idea/help/using-gulp-task-runner.html
        - When that page tells you to install node, you can skip that step. You should already have a version in ~/.gradle/nodejs/.
          This directory should have been created during the gradle build.
    2. Open gulpfile.js, right click on the "watch" task and run it.

When `watch` is running, you can just change a JS, CSS, or SCSS file and then refresh the browser.
