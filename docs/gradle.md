# Gradle

## Version

- **Gradle 8.13** via wrapper (`gradlew` / `gradlew.bat`)
- Wrapper files **must** be committed (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`)

## Structure

Multi-project build with shared root config:

```
settings.gradle.kts    ← project includes
build.gradle.kts       ← shared Java 23 + JUnit 5 config
```

All subprojects inherit Java 23 toolchain and JUnit 5 from root.

## Adding a module

### New application

1. Create `applications/<name>/build.gradle.kts` with `application` plugin and `mainClass`
2. Add `include("applications:<name>")` to `settings.gradle.kts`
3. Create a start script in `scripts/`

### New framework module

1. Create package under `framework/src/main/java/swedberg/framework/`
2. Optionally create `framework/<module>/build.gradle.kts`
3. Add `include("framework:<module>")` to `settings.gradle.kts`

## Key commands

| Command | Description |
|---------|-------------|
| `.\gradlew build` | Build all projects |
| `.\gradlew :framework:build` | Build framework only |
| `.\gradlew :framework:test` | Run framework tests |
| `.\gradlew :applications:<name>:build` | Build a specific application |
| `.\gradlew :applications:<name>:run` | Run a specific application |
| `.\gradlew test` | Run all tests |
| `.\gradlew :framework:test --rerun-tasks` | Force re-run framework tests |

## Dependencies

- Framework: JUnit 5 only (no runtime dependencies)
- Applications: `implementation(project(":framework"))` for framework access
- All dependencies from `mavenCentral()`
