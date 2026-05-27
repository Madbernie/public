package swedberg.applications.jrobot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StrategyDatabase {
    private static final List<StrategyProfile> profiles = new ArrayList<>();
    private static int nextProfileIndex;
    private static final Path savePath = Paths.get(
        System.getProperty("user.home"), ".jrobot_strategy.dat");

    static {
        if (!load()) {
            profiles.add(new StrategyProfile(8, 80, 60, 80, 8, 60));
            profiles.add(new StrategyProfile(12, 100, 80, 100, 10, 60));
            profiles.add(new StrategyProfile(16, 120, 90, 120, 12, 30));
            profiles.add(new StrategyProfile(20, 140, 110, 140, 14, 60));
            profiles.add(new StrategyProfile(20, 160, 130, 160, 16, 30));
        }
    }

    public static StrategyProfile selectProfile() {
        StrategyProfile best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (StrategyProfile p : profiles) {
            double s = p.score();
            if (s > bestScore) { bestScore = s; best = p; }
        }
        if (best != null && best.gamesPlayed > 0) {
            return best;
        }
        int idx = nextProfileIndex % profiles.size();
        nextProfileIndex++;
        return profiles.get(idx);
    }

    public static void recordResult(StrategyProfile profile,
        boolean won, double healthRemaining, long survivalTicks) {
        profile.recordResult(won, healthRemaining, survivalTicks);
        save();
    }

    private static boolean load() {
        if (!Files.exists(savePath)) return false;
        try (BufferedReader r = Files.newBufferedReader(savePath)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 8) continue;
                StrategyProfile p = new StrategyProfile(
                    Integer.parseInt(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]));
                p.gamesPlayed = Integer.parseInt(parts[6]);
                p.totalScore = Double.parseDouble(parts[7]);
                profiles.add(p);
            }
            return !profiles.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private static void save() {
        try (BufferedWriter w = Files.newBufferedWriter(savePath)) {
            for (StrategyProfile p : profiles) {
                w.write(String.format("%d,%f,%f,%d,%d,%d,%d,%f%n",
                    p.cruiseSpeed, p.wallMargin, p.obstacleMargin,
                    p.fleeDist, p.dodgeInterval, p.scanStep,
                    p.gamesPlayed, p.totalScore));
            }
        } catch (IOException e) {
            // silent
        }
    }
}
