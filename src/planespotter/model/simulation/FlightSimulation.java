package planespotter.model.simulation;

import org.jetbrains.annotations.NotNull;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import planespotter.constants.Warning;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.display.MapManager;
import planespotter.display.UserInterface;
import planespotter.model.io.DBOut;
import planespotter.throwables.DataNotFoundException;
import planespotter.util.Utilities;

import java.util.*;
import java.util.stream.Collectors;

public class FlightSimulation extends Simulation<DataPoint> {

    private final MapManager mapManager;
    private final Flight flight;

    public FlightSimulation(@NotNull List<DataPoint> frames, @NotNull MapManager mapManager, @NotNull Flight flight) {
        this(frames.toArray(DataPoint[]::new), mapManager, flight);
    }

    public FlightSimulation(@NotNull DataPoint[] frames, @NotNull MapManager mapManager, @NotNull Flight flight) {
        super();
        Arrays.sort(frames, Comparator.comparingLong(DataPoint::timestamp));
        setFrames(Arrays.asList(frames));
        this.mapManager = mapManager;
        this.flight = flight;
    }

    @SuppressWarnings("ConstantConditions")
    public static FlightSimulation of(String idOrCallSign) {
        UserInterface ui = Controller.getInstance().getUI();
        DBOut dbOut = DBOut.getDBOut();
        if (idOrCallSign == null || idOrCallSign.isBlank()) {
            ui.showWarning(Warning.FIELDS_NOT_FILLED, "Please enter a valid flight ID or call sign!");
            return null;
        }
        int id = -1;
        try {
            id = Integer.parseInt(idOrCallSign);
        } catch (NumberFormatException ignored) {
        }
        if (id < -1) {
            ui.showWarning(Warning.INVALID_DATA, "Invalid flight id!");
            return null;
        } else if (id > -1) {
            try {
                Flight flight = dbOut.getFlightByID(id);
                return new FlightSimulation(new ArrayList<>(flight.dataPoints().values()), ui.getMapManager(), flight);
            } catch (DataNotFoundException e) {
                ui.showWarning(Warning.INVALID_DATA, "No flight found for ID " + id + "!");
                return null;
            }
        }
        try {
            ArrayDeque<String> cslike = dbOut.getAllCallsignsLike(idOrCallSign);
            id = dbOut.getFlightIDsByCallsign(cslike.poll())
                    .stream()
                    .limit(1)
                    .mapToInt(i -> i)
                    .findFirst()
                    .orElse(-1);
        } catch (DataNotFoundException e) {
            ui.showWarning(Warning.INVALID_DATA, "No flight found for call sign " + idOrCallSign + "!");
            return null;
        }
        try {
            Flight flight = dbOut.getFlightByID(id);
            List<DataPoint> tracking = dbOut.getTrackingByFlight(id);
            return new FlightSimulation(tracking, ui.getMapManager(), flight);
        } catch (DataNotFoundException e) {
            ui.showWarning(Warning.INVALID_DATA, "No flight found for call sign " + idOrCallSign + "!");
            return null;
        }

    }

    @Override
    protected synchronized boolean processFrame() {
        if (getCurrentIndex() == getFrameCount()) {
            return false;
        }

        Stack<DataPoint> data = getFrames()
                .stream()
                .limit(incrementAndGetIndex())
                .collect(Collectors.toCollection(Stack::new));

        mapManager.createSimulationMap(data);
        return true;
    }


}
