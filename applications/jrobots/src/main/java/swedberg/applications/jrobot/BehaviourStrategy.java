package swedberg.applications.jrobot;

public class BehaviourStrategy {
    private final String name;
    private final ScanStrategy scan;
    private final SpeedStrategy speed;
    private final TargetingStrategy targeting;
    private final double maxAggression;

    public BehaviourStrategy(String name, ScanStrategy scan, SpeedStrategy speed,
        double maxAggression) {
        this(name, scan, speed, TargetingStrategies.lastHit(), maxAggression);
    }

    public BehaviourStrategy(String name, ScanStrategy scan, SpeedStrategy speed,
        TargetingStrategy targeting, double maxAggression) {
        this.name = name;
        this.scan = scan;
        this.speed = speed;
        this.targeting = targeting;
        this.maxAggression = maxAggression;
    }

    public String name() { return name; }
    public ScanStrategy scan() { return scan; }
    public SpeedStrategy speed() { return speed; }
    public TargetingStrategy targeting() { return targeting; }
    public double maxAggression() { return maxAggression; }

    public static BehaviourStrategy AGGRESSIV = new BehaviourStrategy("Aggressiv",
        ScanStrategies.predictiveScan(), SpeedStrategies.aggressive(),
        TargetingStrategies.weakest(), 1.0);

    public static BehaviourStrategy BALANSERAD = new BehaviourStrategy("Balanserad",
        ScanStrategies.sectorReacquisition(), SpeedStrategies.balanced(), 0.7);

    public static BehaviourStrategy DEFENSIV = new BehaviourStrategy("Defensiv",
        ScanStrategies.soundLocalization(), SpeedStrategies.evasive(),
        TargetingStrategies.mostDangerous(), 0.4);

    public static BehaviourStrategy KRYPSKYTT = new BehaviourStrategy("Krypskytt",
        ScanStrategies.prioritySweep(), SpeedStrategies.sniper(),
        TargetingStrategies.focused(), 0.5);

    public static BehaviourStrategy JAKT = new BehaviourStrategy("Jakt",
        ScanStrategies.allInOne(), SpeedStrategies.adaptive(),
        TargetingStrategies.weakest(), 0.9);

    public static BehaviourStrategy FOERSIKTIG = new BehaviourStrategy("F\u00f6rsiktig",
        ScanStrategies.adaptiveResolution(), SpeedStrategies.cautious(), 0.3);

    public static BehaviourStrategy OFOERUTSAEBAR = new BehaviourStrategy("Of\u00f6ruts\u00e4gbar",
        ScanStrategies.adaptiveResolution(), SpeedStrategies.randomWalk(),
        TargetingStrategies.scatter(), 0.6);

    public static BehaviourStrategy[] ALL = {
        AGGRESSIV, BALANSERAD, DEFENSIV, KRYPSKYTT, JAKT, FOERSIKTIG, OFOERUTSAEBAR
    };
}
