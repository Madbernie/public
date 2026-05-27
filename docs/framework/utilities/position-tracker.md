# PositionTracker

Tracks the estimated position of a target based on scan hits. Used by robot AI to predict movement.

## Methods

| Method | Description |
|--------|-------------|
| `recordHit(angle, distance, originX, originY)` | Records a scan hit and updates position estimate |
| `reset()` | Clears the estimate |
| `hasEstimate()` | Returns `true` if a position has been estimated |
| `hasVelocity()` | Returns `true` if enough data exists to estimate velocity |
| `getEstX()` / `getEstY()` | Estimated target coordinates |
| `getPrevEstX()` / `getPrevEstY()` | Previous estimated coordinates |
| `distanceTo(x, y)` | Distance from a point to the estimated position |
| `angleTo(x, y)` | Angle from a point to the estimated position |
| `predictAngle(x, y, leadTicks)` | Predicted angle with lead for firing |
| `estimateSpeed()` | Estimated speed of the target |

## Build

```
.\gradlew :framework:build
```
