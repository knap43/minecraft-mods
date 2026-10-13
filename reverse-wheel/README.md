# Reverse Wheel

NeoForge 1.21.1, client-only. Scrolling the mouse wheel moves through the hotbar in the opposite direction to vanilla, including the spectator hotbar. Nothing else changes: scrolling in menus, chat and inventories behaves as before.

## Build (Arch)

```fish
sudo pacman -S --needed jdk21-openjdk
./gradlew build   # → build/libs/reversewheel-1.0.0.jar
```

Drop the jar into `~/.minecraft/mods` of a NeoForge 1.21.1 profile. Servers don't need it.
