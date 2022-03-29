package planespotter;

import planespotter.display.Init;

public class Main {

    /**
     * Project Main-Method
     * @param args -> null
     */
    public static void main (String[] args) {
        // TODO: Controller Initialisation
        Controller.initialize();

        // TODO: new Init Frame
        Controller.openWindow(Init.class, null);
    }

}
