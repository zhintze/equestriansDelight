# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
- `./gradlew build` - Build the mod (includes compiling, running tests, and creating JAR)
- `./gradlew clean build` - Clean build from scratch
- `./gradlew --refresh-dependencies` - Refresh dependency cache if libraries are missing

### Running the Mod
- `./gradlew runClient` - Launch Minecraft client with the mod loaded
- `./gradlew runServer` - Launch dedicated server with the mod loaded
- `./gradlew runGameTestServer` - Run automated game tests
- `./gradlew runData` - Generate data files (recipes, loot tables, etc.)

## Architecture

This is a NeoForge Minecraft mod for Minecraft 1.21.1 targeting Java 21. The mod uses the standard NeoForge architecture:

### Core Structure
- **Main Mod Class**: `HorseStats.java` - Entry point with `@Mod` annotation, handles registration and setup
- **Client-side Class**: `HorseStatsClient.java` - Client-only code, handles GUI and client setup
- **Configuration**: `Config.java` - Mod configuration using NeoForge's config system

### Registration System
- Uses `DeferredRegister` pattern for registering blocks, items, and creative tabs
- All registrations happen in the main mod constructor via event bus
- Items and blocks are registered together with their creative tab placements

### Configuration
- Configuration uses NeoForge's `ModConfigSpec` system
- Config file location: `config/horsestats-common.toml`
- Access config values via static methods like `Config.LOG_DIRT_BLOCK.getAsBoolean()`
- Config validation is handled automatically by the system

### Localization
- Language files in `src/main/resources/assets/horsestats/lang/`
- Uses standard Minecraft translation key format
- Config screen translations included for mod settings

### Project Properties
- Mod ID: `horsestats` (defined in `gradle.properties`)
- Base package: `com.hecookin.horsestats`
- Version and metadata managed through `gradle.properties` and injected into `neoforge.mods.toml`