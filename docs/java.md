# Java

## Version

- **Java 23** (toolchain configured in root `build.gradle.kts`)
- Source and target compatibility set via Gradle toolchain

## Style

- Package names: `swedberg.<module>.<submodule>` (e.g. `swedberg.framework.utilities`, `swedberg.applications.hello_world`)
- Class names: PascalCase
- Method names: camelCase
- No JavaDoc required unless the method is part of a public API

## Testing

- **JUnit 5** (Jupiter 5.11.4)
- Test classes end with `*Test.java`
- Test methods use `@Test` annotation
- Tests live in `src/test/java/` mirroring the main source tree
- Run with `.\gradlew test`
