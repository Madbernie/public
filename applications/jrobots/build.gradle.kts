plugins {
    application
}

description = "JRobot - Robot fighting game"

application {
    mainClass = "swedberg.games.jrobot.JRobotGUI"
}

dependencies {
    implementation(project(":framework"))
}
