package planespotter;

import planespotter.Exceptions.JFrameNotFoundException;
import planespotter.dataclasses.*;
import planespotter.display.*;
import planespotter.model.Deserializer;

import javax.swing.*;
import java.lang.module.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import static planespotter.model.Supplier.fr24get;

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

    public static void setFrameVisible (Class key, boolean visible) throws JFrameNotFoundException {
        if (framesvisible.containsKey(key))
            framesvisible.replace(key, visible);
        else throw new JFrameNotFoundException();
    }

    public static boolean getFrameVisible (Class key) throws JFrameNotFoundException {
        if (framesvisible.containsKey(key))
            return framesvisible.get(key);
        else throw new JFrameNotFoundException();
    }

    /**
     * creates DataPoint object from a Frame object
     * represents a Flight at one point
     *
     * (frame could be changed in the future)
     */
    public static DataPoint createDataPoint (Frame frame) {
        DataPoint point = new DataPoint(new Flight(0, new Airport(0, frame.getSrcAirport()),
                                            new Airport(0, frame.getDestAirport()),
                                            new Plane(0, frame.getTailnr(), frame.getPlanetype(), frame.getRegistration(),
                                                new Airline(frame.getAirline())),
                                            frame.getFlightnumber(),
                                            null),
                                        new Position(frame.getLat(), frame.getLon()),
                                        frame.getIcaoAdr(),
                                        frame.getTimestamp(),
                                        frame.getSquawk(),
                                        frame.getGroundspeed(),
                                        frame.getHeading(),
                                        frame.getAltitude());
        return point;
    }

    /**
     * @param frames is the Frame list to convert
     * @return array of DataPoints
     */
    private static DataPoint[] dataPointArray (List<Frame> frames) {
        ListIterator<Frame> it = frames.listIterator();
        DataPoint[] data = new DataPoint[frames.size()];
        int i = 0;
        while (it.hasNext()) {
            data[i] = createDataPoint(it.next());
            i++;
        }
        return data;
    }

    /**
     * creates a Frame with deserializer
     *
     */
    public static List<Frame> getFrames (){
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

    /**
     * Method to create a list of Objects the JList can work with
     * @return array of list objects
     */
    public static ListObject[] createObjectList () {
        DataPoint[] data = dataPointArray(getFrames());
        ListObject[] o = new ListObject[data.length];
        for (int i = 0; i < data.length; i++) {
            o[i] = new ListObject(data[i]);
        }
        return o;
    }

    public static String[] titleArray (ListObject[] list) {
        if (list[0] != null) {
            String[] titles = new String[list.length];
            for (int i = 0; i < list.length; i++) {
                titles[i] = list[i].getTitle();
            } return titles;
        } return null;
    }


}
