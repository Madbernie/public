package swedberg.applications.jrobot;

import java.util.Arrays;
import java.util.List;

public class TargetingStrategies {

    public static TargetingStrategy lastHit() {
        return new TargetingStrategy() {
            @Override
            public String selectTarget(String current, List<TargetMemory.Record> targets) {
                TargetMemory.Record newest = null;
                for (TargetMemory.Record r : targets) {
                    if (newest == null || r.lastSeenTick > newest.lastSeenTick)
                        newest = r;
                }
                return newest != null ? newest.name : current;
            }

            @Override
            public String name() { return "Senast sedd"; }
        };
    }

    public static TargetingStrategy focused() {
        return new TargetingStrategy() {
            @Override
            public String selectTarget(String current, List<TargetMemory.Record> targets) {
                for (TargetMemory.Record r : targets) {
                    if (r.name.equals(current)) return current;
                }
                TargetMemory.Record newest = null;
                for (TargetMemory.Record r : targets) {
                    if (newest == null || r.lastSeenTick > newest.lastSeenTick)
                        newest = r;
                }
                return newest != null ? newest.name : current;
            }

            @Override
            public String name() { return "L\u00e5st"; }
        };
    }

    public static TargetingStrategy weakest() {
        return new TargetingStrategy() {
            @Override
            public String selectTarget(String current, List<TargetMemory.Record> targets) {
                TargetMemory.Record chosen = null;
                for (TargetMemory.Record r : targets) {
                    if (chosen == null || r.hitCount > chosen.hitCount)
                        chosen = r;
                }
                return chosen != null ? chosen.name : current;
            }

            @Override
            public String name() { return "Svagast"; }
        };
    }

    public static TargetingStrategy mostDangerous() {
        return new TargetingStrategy() {
            @Override
            public String selectTarget(String current, List<TargetMemory.Record> targets) {
                double bx = 600, by = 450;
                TargetMemory.Record closest = null;
                double best = Double.MAX_VALUE;
                for (TargetMemory.Record r : targets) {
                    if (r.name.equals(current) && targets.size() > 1) continue;
                    double dist = Math.hypot(r.x - bx, r.y - by);
                    if (closest == null || dist < best) { best = dist; closest = r; }
                }
                if (closest != null) return closest.name;
                TargetMemory.Record newest = null;
                for (TargetMemory.Record r : targets) {
                    if (newest == null || r.lastSeenTick > newest.lastSeenTick)
                        newest = r;
                }
                return newest != null ? newest.name : current;
            }

            @Override
            public String name() { return "Farligast"; }
        };
    }

    public static TargetingStrategy scatter() {
        return new TargetingStrategy() {
            @Override
            public String selectTarget(String current, List<TargetMemory.Record> targets) {
                int count = targets.size();
                if (count <= 1) return current;
                TargetMemory.Record[] arr = targets.toArray(new TargetMemory.Record[0]);
                Arrays.sort(arr, (a, b) -> Integer.compare(a.hitCount, b.hitCount));
                return arr[0].name;
            }

            @Override
            public String name() { return "Spridd"; }
        };
    }
}
