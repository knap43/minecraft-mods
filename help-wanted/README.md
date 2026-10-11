# Help Wanted

NeoForge 1.21.1 mod. Craft a **Help Wanted Board** (any sign + paper + emerald), place it near your base, and every few in-game days an unemployed villager arrives — as long as an unclaimed bed lies within range. Right-click the board to see when the next applicant is due.

## Build & run (Arch)

```fish
sudo pacman -S --needed jdk21-openjdk
./gradlew runClient   # test in a dev client
./gradlew build       # → build/libs/helpwanted-1.0.0.jar
```

Drop the jar into `~/.minecraft/mods` of a NeoForge 1.21.1 profile.

## Config

`<world>/serverconfig/helpwanted-server.toml`

| Key | Default | Meaning |
|---|---|---|
| `arrivalIntervalDays` | `3.0` | In-game days between applicants |
| `bedSearchRadius` | `48` | Blocks from the board to search for a free bed |
