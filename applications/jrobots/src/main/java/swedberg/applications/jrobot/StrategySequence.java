package swedberg.applications.jrobot;

import java.util.ArrayList;
import java.util.List;

public class StrategySequence {
    private static class Entry {
        final BehaviourStrategy strategy;
        final int duration;
        int tick;
        Entry(BehaviourStrategy s, int d) { strategy = s; duration = d; tick = 0; }
    }

    private final List<Entry> phases = new ArrayList<>();
    private int index;

    public StrategySequence add(BehaviourStrategy s, int duration) {
        phases.add(new Entry(s, duration));
        return this;
    }

    public StrategySequence add(BehaviourStrategy s) {
        phases.add(new Entry(s, 0));
        return this;
    }

    public BehaviourStrategy current() {
        return phases.isEmpty() ? BehaviourStrategy.BALANSERAD : phases.get(index).strategy;
    }

    public void advance() {
        if (phases.isEmpty()) return;
        Entry e = phases.get(index);
        if (e.duration > 0 && ++e.tick >= e.duration) {
            e.tick = 0;
            index = (index + 1) % phases.size();
        }
    }

    public void reset() {
        index = 0;
        for (Entry e : phases) e.tick = 0;
    }

    public int phaseIndex() { return index; }
    public int phaseCount() { return phases.size(); }
    public String phaseName() { return current().name(); }
}
