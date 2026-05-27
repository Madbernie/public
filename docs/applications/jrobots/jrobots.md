# JRobot

Top-down 2D robot battle game built with Java Swing. 4 robots fight in an arena with obstacles, projectiles, grenades, and healing hearts.

## Architecture

### Game loop (`JRobotGame`)

Each tick (20ms): projectiles → grenades → hearts → robot controllers → movement → collisions → heart pickup → game-over check.

### Controllers

- **RobotAI** — state machine (EXPLORE / SEARCH / ATTACK / EVADE) with 3 presets: Aggressive, Sniper, Evasive. Uses a single `PositionTracker`.
- **RobotUser** — 4-mode FSM (CRUISE / FOLLOW / FLEE / SHOOT) with pluggable strategy system. Tracks all enemies with per-robot `PositionTracker`s.

### Strategy system

`StrategySequence` → `BehaviourStrategy` → { `ScanStrategy`, `SpeedStrategy`, `TargetingStrategy` }

- 8 behaviour presets (AGGRESSIV, BALANSERAD, DEFENSIV, etc.)
- 6 scan strategies (sector, predictive, adaptive, priority sweep, sound localization, all-in-one)
- 7 speed strategies (balanced, aggressive, cautious, adaptive, evasive, sniper, random walk)
- 5 targeting strategies (last-hit, focused, weakest, most dangerous, scatter)
- 5 `StrategyProfile`s for tuning parameters, persisted and auto-selected by score

### Entities

| Entity | Details |
|--------|---------|
| Projectile | Speed 20, damage 5, destroyed on hit |
| Grenade | Speed 6, blast radius 40, damage 2, 50-tick cooldown |
| Obstacle | 30-40px squares, blocks movement/scans/projectiles |
| Heart | Heals 5 HP, spawns every 1000 ticks |

### Persistence

| File | Location | Content |
|------|----------|---------|
| Win counts | `~/.jrobot_wins.dat` | Per-robot win totals |
| Strategy DB | `~/.jrobot_strategy.dat` | Profile scores and parameters |
| Game log | `build/logs/jrobot.log` | Timestamped game results |

## Build

```
.\gradlew :applications:jrobots:build
```

## Run

```
.\gradlew :applications:jrobots:run
```

Or using the start script:

```
.\scripts\jrobots.bat
```

## Package

- `swedberg.applications.jrobot` — all game classes (29 files)
- `swedberg.framework.utilities` — `PositionTracker` (used by AI controllers)
