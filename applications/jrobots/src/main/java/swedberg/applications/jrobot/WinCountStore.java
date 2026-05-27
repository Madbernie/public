package swedberg.applications.jrobot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinCountStore {
    private static final Path savePath = Paths.get(
        System.getProperty("user.home"), ".jrobot_wins.dat");
    private static Map<String, Integer> counts;

    public static Map<String, Integer> getCounts() {
        if (counts == null) counts = load();
        return counts;
    }

    public static void increment(String name) {
        getCounts().merge(name, 1, Integer::sum);
        save();
    }

    private static Map<String, Integer> load() {
        Map<String, Integer> map = new HashMap<>();
        if (!Files.exists(savePath)) return map;
        try {
            List<String> lines = Files.readAllLines(savePath);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int comma = line.lastIndexOf(',');
                if (comma < 0) continue;
                String name = line.substring(0, comma);
                int count = Integer.parseInt(line.substring(comma + 1));
                map.put(name, count);
            }
        } catch (IOException e) {
            // silent
        }
        return map;
    }

    private static void save() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                sb.append(e.getKey()).append(',').append(e.getValue()).append('\n');
            }
            Files.writeString(savePath, sb.toString());
        } catch (IOException e) {
            // silent
        }
    }
}
