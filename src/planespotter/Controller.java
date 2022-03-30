package planespotter;

import planespotter.dataclasses.*;
import planespotter.display.*;

import javax.swing.*;
import java.lang.module.Configuration;
import java.util.HashMap;
import java.util.List;

import static planespotter.Supplier.fr24get;

public class Controller {

    /**
     * HashMap containing all visible frames
     */
    private static HashMap<Class, Boolean> framesvisible = new HashMap<>();
    private static Configuration cfg;

    public Controller () {}

    /**
     * * * * * * * * * * * * * * *
     * static controller methods *
     * * * * * * * * * * * * * * *
     *
     * openFrame() opens a frame
     * @param c is the Frame-Class to be opened
     * @param opener is the JFrame who opens the new Window
     */
    public static void openWindow (Class c, JFrame opener) {
        if (opener != null) opener.setVisible(false);
        if (c == Init.class) {
            new Init();
            framesvisible.put(c, true);
        } else if (c == ListView.class) {
            new ListView(opener);
            framesvisible.put(c, true);
        } else if (c == MapView.class) {
            new MapView(opener);
            framesvisible.put(c, true);
        }
    }

    /**
     *
     */
    public void addDataToList (Frame frame) {

    }

    protected static void initialize () {
        ConfigManager.loadCofnig();
        framesvisible.put(Init.class, false);
        framesvisible.put(ListView.class, false);
        framesvisible.put(MapView.class, false);
    }

    public static void setFrameVisible (Class key, boolean visible) throws FrameNotFoundException {
        if (framesvisible.containsKey(key))
            framesvisible.replace(key, visible);
        else throw new FrameNotFoundException();
    }

    public static boolean getFrameVisible (Class key) throws FrameNotFoundException {
        if (framesvisible.containsKey(key))
            return framesvisible.get(key);
        else throw new FrameNotFoundException();
    }

    /**
     * creates DataPoint object from a Frame object
     * represents a Flight at one point
     */
    public DataPoint createDataPoint (Frame frame) {
        DataPoint point = new DataPoint(new Flight(0, new Airport(0, frame.getSrcAirport(), AirportType.START),
                                            new Airport(0, frame.getDestAirport(), AirportType.DEST),
                                            new Plane(0, frame.getTailnr(), frame.getPlanetype(), frame.getRegistration(),
                                                    new Airline(frame.getAirline())),
                                            frame.getFlightnumber()),
                                        new Position(frame.getLat(), frame.getLon()),
                                        frame.getIcaoAdr(),
                                        Integer.parseInt(frame.getUnknown0()),
                                        frame.getSquawk(),
                                        frame.getGroundspeed(),
                                        frame.getHeading(),
                                        frame.getAltitude());
        return point;
    }

    /**
     * creates a Frame with deserializer
     *
     */
    public List<Frame> getFrames (){
        try {
            Deserializer ds = new Deserializer();
            List<String> list = ds.stringMagic(fr24get());
            List<Frame> frames = ds.deserialize(list);
            return frames;
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}
