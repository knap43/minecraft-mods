# Hold to Sprint

NeoForge 1.21.1, client-only. You sprint only while the sprint key is held: let go and you drop to walking at once, instead of carrying on until you stop moving. Double-tapping forward no longer starts a sprint.

Swimming and fast creative flight follow the same rule. If you've set *Sprint* to *Toggle* in the controls, the toggle counts as holding the key.

## Build (Arch)

```fish
sudo pacman -S --needed jdk21-openjdk
./gradlew build   # → build/libs/holdtosprint-1.0.0.jar
```

Drop the jar into `~/.minecraft/mods` of a NeoForge 1.21.1 profile. Servers don't need it.
