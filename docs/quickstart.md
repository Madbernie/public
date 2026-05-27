# Quick Start for AI Agents

## First steps

```
.\gradlew build          # Build everything
.\gradlew :framework:test  # Run framework tests
.\scripts\hello-world.bat  # Run HelloWorld
.\scripts\jrobots.bat      # Run JRobot game
```

## Project at a glance

```
public/
├── framework/           # Shared library (utilities, helpers)
├── applications/        # Standalone apps, one subdirectory each
│   ├── hello-world/
│   └── jrobots/
├── scripts/             # Start scripts (one .bat + one Unix per app)
├── docs/                # All documentation
├── build.gradle.kts     # Shared config (Java 23, JUnit 5)
├── settings.gradle.kts  # Module includes
├── gradlew / gradlew.bat
└── .gitignore
```

## Adding an application

1. Create `applications/<name>/build.gradle.kts` with `application` plugin and `mainClass`
2. Add `include("applications:<name>")` to `settings.gradle.kts`
3. Add source under `applications/<name>/src/main/java/`
4. Create start scripts in `scripts/`
5. Add documentation in `docs/applications/<name>/`

## Adding to framework

- Add classes under `framework/src/main/java/swedberg/framework/<module>/`
- Document under `docs/framework/<module>/`

## Key docs

| File | Contents |
|------|----------|
| `AGENTS.md` | Project overview, conventions, docs tree |
| `docs/coding-rules.md` | Documentation rules, git conventions |
| `docs/java.md` | Java version, style, testing |
| `docs/gradle.md` | Build system, commands, module setup |
| `docs/applications/<name>/` | Per-application documentation |

## Conventions at a glance

- All documentation in English
- No absolute paths in any file
- Gradle wrapper files committed
- Only `.gradle/` and `build/` are gitignored
- One start script per application in `scripts/`
