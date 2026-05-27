package swedberg.applications.jrobot;

import java.util.List;

public interface TargetingStrategy {
    String selectTarget(String currentTarget, List<TargetMemory.Record> targets);
    String name();
}
