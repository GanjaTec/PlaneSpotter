package planespotter;

import planespotter.display.GUI;

public class Main {

    /**
     * Project Main-Method
     * @param args -> null
     */
    public static void main (String[] args) {

        // TODO: new GUI frame
        Controller.openWindow(GUI.class, null);
    }

}
