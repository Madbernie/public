plugins {
    application
}

description = "Mother — empty canvas window"

application {
    mainClass = "swedberg.applications.mother.Mother"
}

dependencies {
    implementation(project(":framework"))
}
