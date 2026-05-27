package swedberg.applications.jrobot;

import java.util.List;

public class ScanStrategies {

    public static ScanStrategy sectorReacquisition() {
        return new SectorReacquisitionStrategy();
    }

    public static ScanStrategy predictiveScan() {
        return new PredictiveScanStrategy();
    }

    public static ScanStrategy adaptiveResolution() {
        return new AdaptiveResolutionStrategy();
    }

    public static ScanStrategy prioritySweep() {
        return new PrioritySweepStrategy();
    }

    public static ScanStrategy soundLocalization() {
        return new SoundLocalizationStrategy();
    }

    public static ScanStrategy allInOne() {
        return new AllInOneStrategy();
    }

    private static class SectorReacquisitionStrategy implements ScanStrategy {
        private static final double[] HALF_WIDTHS = { 30, 60, 120, 180 };
        private static final double[] STEP_SIZES = { 10, 15, 20, 30 };
        private int level;
        private int stepIndex;
        private double centerAngle;
        private boolean started;

        @Override
        public double nextAngle(ScanContext ctx) {
            if (!started) {
                centerAngle = normalize(ctx.hasEstimate
                    ? Math.toDegrees(Math.atan2(
                        ctx.targetEstY - ctx.robotY, ctx.targetEstX - ctx.robotX))
                    : ctx.lastAngle);
                level = 0;
                stepIndex = 0;
                started = true;
            }
            double hw = HALF_WIDTHS[level];
            double step = STEP_SIZES[level];
            int steps = (int)(hw * 2 / step);
            if (stepIndex > steps) {
                stepIndex = 0;
                level++;
            }
            if (level >= HALF_WIDTHS.length) {
                level = 0;
                stepIndex = 0;
                centerAngle = (centerAngle + 30) % 360;
            }
            double offset = -hw + stepIndex * step;
            double angle = normalize(centerAngle + offset);
            stepIndex++;
            return angle;
        }

        @Override
        public void reset() {
            started = false;
            level = 0;
            stepIndex = 0;
        }

        @Override
        public boolean isDone() {
            return level >= HALF_WIDTHS.length && stepIndex == 0;
        }

        @Override
        public String name() { return "Sektor\u00e5tertagning"; }

        private static double normalize(double a) {
            a %= 360;
            if (a < 0) a += 360;
            return a;
        }
    }

    private static class PredictiveScanStrategy implements ScanStrategy {
        private int lead;
        private boolean firstCall = true;

        @Override
        public double nextAngle(ScanContext ctx) {
            if (ctx.hasEstimate) {
                if (firstCall) { lead = 1; firstCall = false; }
                lead = (lead % 10) + 1;
                double dx = ctx.targetEstX + ctx.targetVelX * lead - ctx.robotX;
                double dy = ctx.targetEstY + ctx.targetVelY * lead - ctx.robotY;
                return normalize(Math.toDegrees(Math.atan2(dy, dx)));
            }
            return defaultSearch(ctx);
        }

        @Override
        public void reset() { firstCall = true; lead = 1; }

        @Override
        public boolean isDone() { return false; }

        @Override
        public String name() { return "Prediktiv"; }

        private static double defaultSearch(ScanContext ctx) {
            return normalize(ctx.lastAngle + 15);
        }

        private static double normalize(double a) {
            a %= 360;
            if (a < 0) a += 360;
            return a;
        }
    }

    private static class AdaptiveResolutionStrategy implements ScanStrategy {
        private double sweepAngle;
        private boolean firstCall = true;

        @Override
        public double nextAngle(ScanContext ctx) {
            if (firstCall) {
                sweepAngle = ctx.hasEstimate
                    ? Math.toDegrees(Math.atan2(
                        ctx.targetEstY - ctx.robotY, ctx.targetEstX - ctx.robotX))
                    : 0;
                firstCall = false;
            }
            double dist = ctx.hasEstimate
                ? Math.hypot(ctx.targetEstX - ctx.robotX, ctx.targetEstY - ctx.robotY)
                : 600;
            double step = Math.max(5, Math.min(60, dist / 50));
            sweepAngle = normalize(sweepAngle + step);
            return sweepAngle;
        }

        @Override
        public void reset() { firstCall = true; sweepAngle = 0; }

        @Override
        public boolean isDone() { return false; }

        @Override
        public String name() { return "Adaptiv uppl\u00f6sning"; }

        private static double normalize(double a) {
            a %= 360;
            if (a < 0) a += 360;
            return a;
        }
    }

    private static class PrioritySweepStrategy implements ScanStrategy {
        private static final int BUCKETS = 24;
        private final int[] lastScanTick = new int[BUCKETS];
        private boolean firstCall = true;

        @Override
        public double nextAngle(ScanContext ctx) {
            if (firstCall) {
                for (int i = 0; i < BUCKETS; i++) lastScanTick[i] = -i;
                firstCall = false;
            }
            int oldest = 0;
            int oldestTick = lastScanTick[0];
            for (int i = 1; i < BUCKETS; i++) {
                if (lastScanTick[i] < oldestTick) {
                    oldest = i;
                    oldestTick = lastScanTick[i];
                }
            }
            lastScanTick[oldest] = ctx.tickCount;
            double degPerBucket = 360.0 / BUCKETS;
            return oldest * degPerBucket + degPerBucket / 2;
        }

        @Override
        public void reset() {
            firstCall = true;
        }

        @Override
        public boolean isDone() { return false; }

        @Override
        public String name() { return "Prioriteringssvep"; }
    }

    private static class SoundLocalizationStrategy implements ScanStrategy {
        private final ScanStrategy fallback = new SectorReacquisitionStrategy();
        private int cooldown;

        @Override
        public double nextAngle(ScanContext ctx) {
            if (cooldown > 0) cooldown--;
            if (cooldown <= 0) {
                for (Grenade g : ctx.grenades) {
                    if (!g.isExploded()) continue;
                    double dx = g.getBlastCenterX() - ctx.robotX;
                    double dy = g.getBlastCenterY() - ctx.robotY;
                    cooldown = 8;
                    return normalize(Math.toDegrees(Math.atan2(dy, dx)));
                }
            }
            return fallback.nextAngle(ctx);
        }

        @Override
        public void reset() { fallback.reset(); cooldown = 0; }

        @Override
        public boolean isDone() { return fallback.isDone(); }

        @Override
        public String name() { return "Ljudlokalisering"; }

        private static double normalize(double a) {
            a %= 360;
            if (a < 0) a += 360;
            return a;
        }
    }

    private static class AllInOneStrategy implements ScanStrategy {
        private enum Phase { PREDICT, SECTOR, SWEEP }
        private final ScanStrategy sector = new SectorReacquisitionStrategy();
        private final ScanStrategy sweep = new PrioritySweepStrategy();
        private Phase phase = Phase.PREDICT;
        private int phaseTicks;

        @Override
        public double nextAngle(ScanContext ctx) {
            phaseTicks++;
            switch (phase) {
                case PREDICT:
                    if (ctx.hasEstimate && ctx.hasVelocity && phaseTicks < 10) {
                        double dx = ctx.targetEstX + ctx.targetVelX * 3 - ctx.robotX;
                        double dy = ctx.targetEstY + ctx.targetVelY * 3 - ctx.robotY;
                        return normalize(Math.toDegrees(Math.atan2(dy, dx)));
                    }
                    sector.reset();
                    phase = Phase.SECTOR;
                    phaseTicks = 0;
                    return sector.nextAngle(ctx);
                case SECTOR:
                    if (sector.isDone()) {
                        sweep.reset();
                        phase = Phase.SWEEP;
                        phaseTicks = 0;
                    }
                    return (phase == Phase.SECTOR) ? sector.nextAngle(ctx) : sweep.nextAngle(ctx);
                case SWEEP:
                    return sweep.nextAngle(ctx);
            }
            return sweep.nextAngle(ctx);
        }

        @Override
        public void reset() {
            phase = Phase.PREDICT;
            phaseTicks = 0;
            sector.reset();
            sweep.reset();
        }

        @Override
        public boolean isDone() { return false; }

        @Override
        public String name() { return "Allt-i-ett"; }

        private static double normalize(double a) {
            a %= 360;
            if (a < 0) a += 360;
            return a;
        }
    }
}
