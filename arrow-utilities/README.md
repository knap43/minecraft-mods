# Arrow Utilities

NeoForge 1.21.1 mod. Arrows that hit a mob stay visibly lodged in it, and every one of them drops as an item when the mob dies.

- Works on any living entity except players, vanilla or modded; arrows sit on the mob's actual model parts and follow baby scaling.
- Arrows never fall out on their own and are saved with the world.
- On death, from any cause, every lodged arrow drops as itself: tipped arrows keep their potion, spectral arrows stay spectral. The killing shot drops too.
- Arrows carry over when a mob converts (zombie → drowned, villager → witch, …).
- Players keep vanilla behaviour.
- Compatible with [2D Projectiles](https://www.curseforge.com/minecraft/mc-mods/twod-projectiles): lodged arrows are drawn through the same vanilla hook it flattens, so they appear 2D automatically.

Install on both client and server.

## Build & run (Arch)

```fish
sudo pacman -S --needed jdk21-openjdk
./gradlew runClient   # test in a dev client
./gradlew build       # → build/libs/arrowutilities-1.0.0.jar
```

Drop the jar into `~/.minecraft/mods` of a NeoForge 1.21.1 profile.

## Notes

- Piercing arrows pass through and never lodge, as in vanilla.
- Lodged arrows render as plain arrows regardless of type; the dropped item is exact.
