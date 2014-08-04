package pl.pcd.alcohol;

/**
 * CFG stands for ConFiGuration
 */
@SuppressWarnings("SpellCheckingInspection")
public class Cfg {
    public static final boolean DEBUG = false;
    public static final boolean localTestingSwitch = false;
    public static final String URL_BASE = localTestingSwitch ? "http://192.168.0.111" : "http://dev.code-sharks.pl";
}
