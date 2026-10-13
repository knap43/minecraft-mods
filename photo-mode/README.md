# Photo Mode

NeoForge 1.21.1, client-only. Press **P** to detach the camera and fly it freely through blocks with the HUD hidden; press **P** again to snap back. Press **F2** for a screenshot, as usual.

| Key | Action |
|---|---|
| P | Enter / leave photo mode |
| WASD, Jump, Sneak | Fly (forward follows where you look) |
| Mouse | Look |
| Wheel | Fly speed |
| B | Show / hide your player |
| Z | Freeze / unfreeze the world (singleplayer; on by default) |
| G | Depth of field: autofocuses on whatever is at the centre of the frame |
| Sprint + wheel | Blur strength (with depth of field on) |
| I | Isometric view: orthographic, true isometric angle |
| ← / → | Rotate the isometric view by 90° |
| Wheel (isometric) | Zoom |

All keys are rebindable under *Controls → Key Binds → Photo Mode*. By default P is also vanilla's *Social Interactions* key; photo mode takes precedence in-game, so rebind one of them if you use that screen.

While photo mode is on, the player stands still and normal keys (attack, use, hotbar, inventory, chat, F5) are suspended. Toggles and speeds are remembered until the game closes.

## Notes

- Freezing uses the game's own tick freeze, the same as `/tick freeze`, so mobs, water and particles hold still. On a server the world keeps moving; everything else works.
- The camera can only show chunks the game has loaded around you.
- Isometric mode disables smart culling while it is on, so frame rates can dip in dense areas.

## Build (Arch)

```fish
sudo pacman -S --needed jdk21-openjdk
./gradlew runClient   # try it in a dev client
./gradlew build       # → build/libs/photomode-1.0.0.jar
```

Drop the jar into `~/.minecraft/mods` of a NeoForge 1.21.1 profile. Servers don't need it.
