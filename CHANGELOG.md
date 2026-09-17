# Changelog

## Unreleased

### NEW

### UPDATE

### FIXED

## 1.5.1 - 2026-09-16

### UPDATE

- Plugin compatibility no longer capped with an `untilBuild` upper bound, so it stays installable on future IDE versions without a manual bump
- Build now targets Java 23 via Gradle toolchain (`jvmToolchain(23)`) instead of manually set `sourceCompatibility`/`targetCompatibility`/`jvmTarget`
