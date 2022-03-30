package planespotter;

import planespotter.display.GUI_alt;

public class Main {

    /**
     * Project Main-Method
     * @param args -> null
     */
    public static void main (String[] args) {
        // TODO: Controller Initialisation
        Controller.initialize();


        // TODO: new Init Frame
        Controller.openWindow(GUI_alt.class, null);
    }

}
