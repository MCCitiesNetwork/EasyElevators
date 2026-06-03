# EasyElevators

Paper plugin. A configured block (default iron) on each floor acts as an elevator: jump to go up, sneak to go down.

Requires Paper **1.21** and Java **21**.

## Install

Drop `EasyElevators.jar` from `./gradlew shadowJar` into `plugins/`. Restart or reload the server.

## Usage

Place one elevator block per floor at the same X/Z. Stand on it and jump for the next floor up, or sneak for the next floor down. Non-solid blocks between floors (torches, signs, etc.) are ignored per `ignore-materials` in config.

Optional boss bar and floor arrows are configured in `config.yml`.

## Commands

| Command | Permission |
|---------|------------|
| `/easyelevators reload` | `easyelevators.reload` (default: op) |

## Config

`plugins/EasyElevators/config.yml` — block type, ignored materials, disabled worlds, sounds, boss bar, arrows. Messages use [MiniMessage](https://docs.advntr.dev/minimessage/format.html).

## Build

```bash
./gradlew shadowJar
```

Output: `build/libs/EasyElevators.jar`
