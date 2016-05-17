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

The project currently uses 'webpack' to process .scss files, combine, minify, etc.
To get it to constantly watch for changes:

`./gradlew webpackWatch`

When `webpackWatch` is running, you can just change a JS, CSS, or SCSS file and then refresh the browser.

## Deploying to Prod

Set up the Elastic Beanstalk CLI: http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html

If that's done correctly, you can just use `eb deploy`
