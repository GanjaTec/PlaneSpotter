package planespotter.constants;

import planespotter.dataclasses.Airline;

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
    // default airport
    // ...


}
