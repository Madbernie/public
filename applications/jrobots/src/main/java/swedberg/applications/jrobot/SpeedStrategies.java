package swedberg.applications.jrobot;

import java.util.Random;

public class SpeedStrategies {
    private static final int MAX = 20;

    public static SpeedStrategy balanced() { return new BalancedStrategy(); }
    public static SpeedStrategy aggressive() { return new AggressiveStrategy(); }
    public static SpeedStrategy cautious() { return new CautiousStrategy(); }
    public static SpeedStrategy adaptive() { return new AdaptiveStrategy(); }
    public static SpeedStrategy evasive() { return new EvasiveStrategy(); }
    public static SpeedStrategy sniper() { return new SniperStrategy(); }
    public static SpeedStrategy randomWalk() { return new RandomWalkStrategy(); }

    private static class BalancedStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if ("FLEE".equals(ctx.modeName)) return clamp(12 + 6 * ctx.aggression, ctx.targetSpeed);
            if ("FOLLOW".equals(ctx.modeName)) {
                double base = 8 + 4 * ctx.aggression;
                if (ctx.targetDistance > 450) return clamp(ctx.targetSpeed + 2, base);
                if (ctx.targetDistance < 200) return clamp(4 + 4 * ctx.aggression, ctx.targetSpeed);
                return clamp(base, ctx.targetSpeed);
            }
            if ("SHOOT".equals(ctx.modeName)) return clamp(4 + 4 * ctx.aggression, ctx.targetSpeed);
            return 8;
        }

        @Override
        public String name() { return "Balanserad"; }

        private int clamp(double v, double cap) {
            return (int)Math.max(2, Math.min(MAX, Math.min(v, cap + 4)));
        }
    }

    private static class AggressiveStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if ("FLEE".equals(ctx.modeName)) {
                if (ctx.health > 40) return MAX;
                return clamp(14, ctx.targetSpeed);
            }
            if ("SHOOT".equals(ctx.modeName)) {
                if (ctx.targetDistance < 200) return 6;
                return MAX;
            }
            return clamp(MAX, ctx.targetSpeed + 4);
        }

        @Override
        public String name() { return "Aggressiv"; }

        private int clamp(double v, double cap) {
            return (int)Math.max(2, Math.min(MAX, Math.min(v, cap + 6)));
        }
    }

    private static class CautiousStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if ("FLEE".equals(ctx.modeName)) return MAX;
            if ("SHOOT".equals(ctx.modeName)) return 4;
            if ("FOLLOW".equals(ctx.modeName)) return clamp(6, ctx.targetSpeed);
            return 6;
        }

        @Override
        public String name() { return "F\u00f6rsiktig"; }

        private int clamp(double v, double cap) {
            return (int)Math.max(2, Math.min(MAX, Math.min(v, cap)));
        }
    }

    private static class AdaptiveStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if (ctx.targetSpeed < 1) return 8;
            double speed = ctx.targetSpeed;
            if ("FLEE".equals(ctx.modeName)) speed += 4;
            if ("SHOOT".equals(ctx.modeName)) speed = Math.max(4, speed - 2);
            if (ctx.aggression < 0.5) speed *= 0.7;
            if (speed < 2) speed = 2;
            if (speed > MAX) speed = MAX;
            return (int)speed;
        }

        @Override
        public String name() { return "Adaptiv"; }
    }

    private static class EvasiveStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if ("FLEE".equals(ctx.modeName)) return MAX;
            if ("SHOOT".equals(ctx.modeName)) {
                return ctx.targetDistance < 300 ? 14 : 8;
            }
            return 12;
        }

        @Override
        public String name() { return "Undvikande"; }
    }

    private static class SniperStrategy implements SpeedStrategy {
        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if ("SHOOT".equals(ctx.modeName)) return 2;
            if ("FOLLOW".equals(ctx.modeName)) {
                if (ctx.targetDistance > 500) return MAX;
                if (ctx.targetDistance < 200) return clamp(ctx.targetSpeed + 2, 10);
                return 8;
            }
            if ("FLEE".equals(ctx.modeName)) return MAX;
            return 6;
        }

        @Override
        public String name() { return "Krypskytt"; }

        private int clamp(double v, double cap) {
            return (int)Math.max(2, Math.min(MAX, Math.min(v, cap)));
        }
    }

    private static class RandomWalkStrategy implements SpeedStrategy {
        private final Random rand = new Random();
        private int hold;
        private int cached;

        @Override
        public int desiredSpeed(SpeedContext ctx) {
            if (hold-- > 0) return cached;
            hold = 10 + rand.nextInt(20);
            if ("FLEE".equals(ctx.modeName)) {
                cached = 14 + rand.nextInt(7);
            } else if ("SHOOT".equals(ctx.modeName)) {
                cached = 2 + rand.nextInt(ctx.targetDistance < 300 ? 16 : 10);
            } else {
                cached = 2 + rand.nextInt(18);
            }
            return cached;
        }

        @Override
        public String name() { return "Slumpvis"; }
    }
}
