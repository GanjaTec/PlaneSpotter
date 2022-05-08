package planespotter.constants;

import planespotter.dataclasses.*;

import java.util.HashMap;

/**
    @name DefaultObject
    @author @all
    @version 1.0

    DefaultObject is the default given object when an object of "dataclasses" should be null or
    when an Exception is thrown
 */
public final class DefaultObject {

    /**
     *  constant default objects for dataclasses-objects
     */
    // default airline
    public static final Airline DEFAULT_AIRLINE = new Airline(-1, "None", "None");
    // default position
    public static final Position DEFAULT_POSITION = new Position(0d, 0d);
    // default airport
    public static final Airport DEFAULT_AIRPORT = new Airport(-1, "None", "None", DEFAULT_POSITION);
    // default plane
    public static final Plane DEFAULT_PLANE = new Plane(-1, "None", "None", "None", "None", DEFAULT_AIRLINE);
    // default flight
    public static final Flight DEFAULT_FLIGHT = new Flight(-1, DEFAULT_AIRPORT, DEFAULT_AIRPORT, "None", DEFAULT_PLANE, "None", new HashMap<>());


}
