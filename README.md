# Fullbright DamageMeter Zoom Plus

![Minecraft Version](https://img.shields.io/badge/Minecraft-26.2-brightgreen)
![Loader](https://img.shields.io/badge/Loader-Fabric-blue)
![Version](https://img.shields.io/badge/Release-1.6.1--pre.6-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

Fullbright DamageMeter Zoom Plus is a client-side Fabric mod for Minecraft 26.2.
It combines fullbright, zoom, mob health display, and damage meter features in one mod.

## Features

| Key | Function | Description |
| :-- | :-- | :-- |
| B | Fullbright | Toggles the night-vision style fullbright effect on and off. |
| C | Zoom | Hold to zoom in. Scroll the mouse wheel while zooming to go much further in or back out. |
| H | Mob HP Mode | Cycles through BossBar, Labels, Both, and Off. |
| J | Damage Meter | Toggles floating damage numbers. |

## Release Notes

- Updated for Minecraft 26.2
- Fullbright is restored with the new render pipeline
- Zoom now reacts directly to mouse wheel in both directions
- Mouse movement keeps a usable feel even at maximum zoom
- Zoom-out now continues past the default view instead of stopping early
- Zoom now hooks into the camera FOV calculation for a much deeper possible zoom
- Pre-release version bumped to 1.6.1-pre.6

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API.
3. Put the mod jar into the `mods` folder.
4. Start the game and test the keys above.

## Build

```bash
./gradlew build
```

## License

MIT
