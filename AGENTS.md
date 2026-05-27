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
│       ├── utilities/StringHelper.java
│       └── utilities/PositionTracker.java
├── applications/                # Application projects (one subdirectory each)
│   ├── hello-world/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/swedberg/applications/hello_world/HelloWorld.java
│   └── jrobots/
│       ├── build.gradle.kts
│       └── src/main/java/swedberg/applications/jrobot/
└── docs/                        # All documentation
    ├── applications/
    │   ├── hello-world/hello-world.md
    │   └── jrobots/jrobots.md
    └── framework/
        └── utilities/string-helper.md
```

## Conventions

See full documentation:

- **Coding rules** — `docs/coding-rules.md`
- **Java** — `docs/java.md`
- **Gradle** — `docs/gradle.md`

## Commands

See `docs/gradle.md` for the full command table.

## Adding a new application

See `docs/gradle.md` for module setup instructions.

## Adding a new framework module

See `docs/gradle.md` for module setup instructions.

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
  - `docs/applications/jrobots/`
- `docs/jrobots/` — supporting docs for jrobots
  - `changelog.md`
  - `notes.md`
  - `requirements.md`
- `docs/framework/` — one subdirectory per module
  - `docs/framework/utilities/`
    - `string-helper.md`
    - `position-tracker.md`
- `docs/coding-rules.md` — coding conventions
- `docs/java.md` — Java version, style, testing
- `docs/gradle.md` — build system, commands, module setup
- `docs/quickstart.md` — quick-start guide for AI agents
