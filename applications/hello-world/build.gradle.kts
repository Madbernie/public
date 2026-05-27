plugins {
    application
}

description = "Hello World application"

application {
    mainClass = "swedberg.applications.hello_world.HelloWorld"
}

dependencies {
    implementation(project(":framework"))

    // Application-specific dependencies go here
}
