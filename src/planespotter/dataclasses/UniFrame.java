package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;

public class UniFrame extends Frame {

    public static final int SIZE = 100;

    private String icao;
    private String callsign;

    /**
     * {@link Frame} super-constructor
     *
     * @param lat       is the current latitude
     * @param lon       is the current longitude
     * @param heading   is the current heading
     * @param alt       is the current altitude
     * @param speed     is the current ground speed
     * @param squawk    is the current squawk code
     * @param timestamp is the current timestamp
     */
    public UniFrame(double lat, double lon, int heading, int alt, int speed, int squawk, long timestamp, String icao, String callsign) {
        super(lat, lon, heading, alt, speed, squawk, timestamp);
        this.icao = icao;
        this.callsign = callsign;
    }

    public static <E extends Frame> UniFrame of(@NotNull E frame) {
        return new UniFrame(frame.getLat(), frame.getLon(), frame.getHeading(), frame.getAltitude(),
                frame.getGroundspeed(), frame.getSquawk(), frame.getTimestamp(), frame.getIcaoAddr(),
                frame.getCallsign());
    }

    @Override
    public void setIcaoAddr(String icao) {
        this.icao = icao;
    }

    @Override
    public String getIcaoAddr() {
        return this.icao;
    }

    @Override
    public String getCallsign() {
        return this.callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }
}
