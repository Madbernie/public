# Notes

## Design decisions

- Game ticks at 20ms (~50 FPS) via `javax.swing.Timer`
- Robots have 150 HP, max speed 20, turn rate 5°/tick
- Projectile damage: 5 HP, Grenade blast damage: 2 HP
- Collision damage: robot-robot 5 HP, robot-obstacle 1 HP, wall-bounce 1 HP
- Hearts spawn every 1000 ticks, heal 5 HP
- Game auto-restarts 1000 ticks after game over

## Known quirks

- AI targets are estimated via scan raycasts — no wall-penetration
- `RobotUser` tracks per-robot `PositionTracker` instances, `RobotAI` only tracks one
- Strategy profiles are scored and selected automatically by `StrategyDatabase`
- `GameLogger` falls back to `System.out` if file logging fails

## Package structure

- `swedberg.applications.jrobot` — all game classes
- `swedberg.framework.utilities` — `PositionTracker`, `StringHelper`
