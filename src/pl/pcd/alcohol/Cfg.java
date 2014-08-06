package pl.pcd.alcohol;

/**
 * CFG stands for ConFiGuration
 */
@SuppressWarnings("SpellCheckingInspection")
public class Cfg {
    public static final boolean DEBUG = false;
    public static final boolean localTesting = false;
    private static final String localHost = "http://192.168.0.111";
    private static final String externalHost = "http://dev.code-sharks.pl";
    public static final String URL_BASE = localTesting ? localHost : externalHost;
}
