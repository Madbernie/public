package swedberg.applications.jrobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetMemory {
    static class Record {
        String name;
        double x;
        double y;
        int lastSeenTick;
        int hitCount;
    }

    private final Map<String, Record> records = new HashMap<>();

    public void recordHit(String name, double x, double y, int tick) {
        Record r = records.get(name);
        if (r == null) {
            r = new Record();
            r.name = name;
            records.put(name, r);
        }
        r.x = x;
        r.y = y;
        r.lastSeenTick = tick;
        r.hitCount++;
    }

    public List<Record> getActiveRecords(int currentTick, int maxAge) {
        List<Record> active = new ArrayList<>();
        for (Record r : records.values()) {
            if (currentTick - r.lastSeenTick <= maxAge)
                active.add(r);
        }
        return active;
    }

    public void remove(String name) {
        records.remove(name);
    }

    public void clear() {
        records.clear();
    }
}
