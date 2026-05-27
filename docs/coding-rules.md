# Coding Rules

## Documentation

- All documentation **must always** be written in English
- No files in the repository may contain absolute paths
- Use relative paths or Gradle project references (e.g. `:applications:hello-world:run`)
- Use `.\gradlew` for Windows commands, `./gradlew` for Unix

## Git

- Gradle wrapper files **must** be committed
- Only build artifacts (`.gradle/`, `build/`) are gitignored
- No IDE files (`.idea/`, `*.iml`, `.vscode/`) in the repo
- No OS junk (`.DS_Store`, `Thumbs.db`)

## Module conventions

- Each application gets its own `build.gradle.kts` with `application` plugin
- Framework modules live under `framework/src/main/java/swedberg/framework/`
- Application source code lives under `applications/<name>/src/main/java/`
- Start scripts go in `scripts/` (one `.bat` and one Unix script per app)
- Every application and framework module must have documentation under `docs/`
