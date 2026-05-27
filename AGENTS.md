# AGENTS.md

This file is used to document agent configuration, instructions, and notes for AI assistants working in this repository.

## Project Description

A multi-module Gradle project with a shared framework library and standalone applications.

- **Group:** `com.example`
- **Version:** `1.0.0`
- **License:** Apache 2.0
- **Java:** 23
- **Gradle:** 8.13 (wrapper-based)
- **Testing:** JUnit 5 (Jupiter 5.11.4)

### Module layout

```
public/
├── settings.gradle.kts          # Root project config
├── build.gradle.kts             # Shared build config (Java 23, JUnit 5)
├── .gitignore
├── AGENTS.md
├── LICENSE                      # Apache 2.0
├── gradlew / gradlew.bat       # Gradle wrapper
├── gradle/wrapper/              # Wrapper JAR + properties
├── scripts/                     # Start scripts (one per application)
├── framework/                   # Shared framework library
│   └── src/main/java/swedberg/framework/
│       └── utilities/StringHelper.java
├── applications/                # Application projects (one subdirectory each)
│   └── hello-world/
│       ├── build.gradle.kts
│       └── src/main/java/swedberg/applications/hello_world/HelloWorld.java
└── docs/                        # All documentation
    ├── applications/
    │   └── hello-world/hello-world.md
    └── framework/
        └── utilities/string-helper.md
```

## Conventions

- All documentation **must always** be written in English.
- No files in the repository may contain absolute paths.
- Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) **must** be committed.
- Only build artifacts (`.gradle/`, `build/`) are gitignored.

## Commands

| Command | Description |
|---------|-------------|
| `.\gradlew build` | Build all projects |
| `.\gradlew :framework:build` | Build only framework |
| `.\gradlew :framework:test` | Run framework tests |
| `.\gradlew :applications:hello-world:build` | Build hello-world |
| `.\gradlew :applications:hello-world:run` | Run hello-world |
| `.\gradlew test` | Run all tests |
| `.\gradlew :framework:test --rerun-tasks` | Force re-run framework tests |
| `.\scripts\hello-world.bat` | Start script for HelloWorld |

## Adding a new application

1. Create `applications/<name>/` with its own `build.gradle.kts` (apply the `application` plugin and set `mainClass`).
2. Add `include("applications:<name>")` to `settings.gradle.kts`.
3. Create a start script `scripts/<name>.bat` (and `<name>` for Unix) that runs `.\gradlew :applications:<name>:run`.
4. Create documentation at `docs/applications/<name>/`.
5. Add a line for the new application in the Documentation section of this file.

## Adding a new framework module

1. Create the package under `framework/src/main/java/swedberg/framework/`.
2. Create documentation at `docs/framework/<module>/`.
3. If the module needs its own `build.gradle.kts`, add `include("framework:<module>")` to `settings.gradle.kts`.
4. Add a line for the new module in the Documentation section of this file.

## Maintaining documentation

When updating documentation:

- Never use absolute paths.
- Use relative paths or Gradle project references (e.g. `:applications:hello-world:run`).
- Use `.\gradlew` for Windows commands, `./gradlew` for Unix.
- Add new module/application documentation under the correct `docs/` subdirectory.
- Keep this file's **Documentation** section in sync with the actual `docs/` tree.
- When adding or renaming a module or application, update all affected documentation files.

## Notes

- ...

## Documentation

All documentation lives under `docs/`:

- `docs/applications/` — one subdirectory per application
  - `docs/applications/hello-world/`
- `docs/framework/` — one subdirectory per module
  - `docs/framework/utilities/`
