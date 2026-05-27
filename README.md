# Swedberg Public

A collection of Java projects — a shared framework library and standalone applications.

## Applications

### Hello World

A simple console application that prints "Hello, World!".

```
.\scripts\hello-world.bat
```

### JRobot

A top-down 2D robot battle game. 4 robots fight in an arena with obstacles, projectiles, grenades, and healing hearts. Built with Java Swing.

```
.\scripts\jrobots.bat
```

Three AI opponents (aggressive, sniper, evasive) compete against your robot, which uses a configurable strategy system to adapt its behavior.

## Framework features

Shared utilities used across applications:

- **StringHelper** — string operations (reverse, palindrome check)
- **PositionTracker** — estimates target position and velocity from scan data (used by JRobot AI)

## Tests

- JUnit 5 tests for the framework library
- Run with: `.\gradlew test`

## Build

Requires Java 23. Everything else is handled by the Gradle wrapper:

```
.\gradlew build
```

## Documentation

See `docs/` for detailed documentation on each project.

## License

Apache 2.0
