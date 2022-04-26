package planespotter.display;

/**
 * @name UserSettings
 * @author jml04
 * @version 1.0
 *
 * class UserSettings contains the user settings which can be edited in the settings menu
 * TODO may be saved through another class like ConfigManager
 */
public class UserSettings {
    // settings variables
    private static int maxLoadedFlights = 12400; // 12400 is perfect at the moment

    /**
     * @return maxLoadedFlights, the limit of loaded flights
     */
    public static int getMaxLoadedFlights () {
        return maxLoadedFlights;
    }

    /**
     * @set the max loaded flights variable
     * @param newMax, the new flight limit
     */
    public static void setMaxLoadedFlights (int newMax) {
        maxLoadedFlights = newMax;
    }


}
