package planespotter.display.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import planespotter.dataclasses.*;
import planespotter.display.UserInterface;
import planespotter.util.Utilities;

import javax.swing.*;
import java.awt.*;

import static planespotter.constants.DefaultColor.DEFAULT_SEARCH_ACCENT_COLOR;

public class InfoPane extends JPanel {

    @NotNull
    public static InfoPane of(@NotNull Component parent, @NotNull Flight flight, @NotNull DataPoint dataPoint) {
        Airport src = flight.src(), dest = flight.dest();
        Plane plane = flight.plane();
        Airline airline = plane.airline();
        Position pos = dataPoint.pos();
        String srcName = src.name();
        String destName = dest.name();
        String airlName = airline.name();
        String airlCountry = airline.country();
        String[] listData = new String[] {
                "Flight-ID: " + flight.id(),
                "Callsign: " + flight.callsign(),
                "Flight-Nr.: " + flight.flightNr(),
                "Start-Airport: " + src.iataTag() + "(" + ((srcName != null) ? srcName : "N/A") + ")",
                "Destination-Airport: " + dest.iataTag() + "(" + ((destName != null) ? destName : "N/A") + ")",
                "Plane-ID: " + plane.id(),
                "Planetype: " + plane.planeType(),
                "ICAO: " + plane.icao(),
                "Registration: " + plane.registration(),
                "Airline: " + airline.iataTag() + "(" + ((airlName != null) ? airlName : "N/A") + ", " + ((airlCountry != null) ? airlCountry : "N/A") + ")",
                "\n",
                "Current Altitude: " + Utilities.feetToMeters(dataPoint.altitude()) + " meters",
                "Current Speed: " + Utilities.knToKmh(dataPoint.speed()) + " km/h",
                "Current Heading: " + dataPoint.heading() + "°",
                "Current Position: " + pos.lat() + ", " + pos.lon(),
                "Current Squawk-Code: " + dataPoint.squawk(),
                "Current Timestamp: " + dataPoint.timestamp()
        };
        return new InfoPane(parent, listData);
    }


    private final JList<String> infoList;

    public InfoPane(@NotNull Component parent) {
        this(parent, (String[]) null);
    }

    public InfoPane(@NotNull Component parent, @Nullable String... listData) {
        super();
        super.setLayout(null);
        super.setOpaque(false);
        super.setBounds(0, 0, 270, parent.getHeight());

        this.infoList = (listData == null) ? new JList<>() : new JList<>(listData);
        this.infoList.setFocusable(false);
        this.infoList.setBorder(BorderFactory.createLineBorder(DEFAULT_SEARCH_ACCENT_COLOR.get()));
        this.infoList.setFont(UserInterface.DEFAULT_FONT.deriveFont(12f));
        this.infoList.setCellRenderer(new Renderer());
        this.infoList.setBounds(10, 10, super.getWidth() - 20, 330);
        super.add(this.infoList);
    }

    public void setListData(@NotNull String... data) {
        this.infoList.setListData(data);
    }

    @NotNull
    public JList<String> getInfoList() {
        return this.infoList;
    }


    private static class Renderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(@NotNull JList<?> list, @NotNull Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            // do cell customisation
        }
    }

}
