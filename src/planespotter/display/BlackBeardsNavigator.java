package planespotter.display;

import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.model.Utilities;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.GUIConstants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.GUIConstants.LINE_BORDER;


/**
 * @name BlackBeardsNavigator
 * @author jml04
 * @version 1.1
 *
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class BlackBeardsNavigator {

    /**
     * constructor
     */
    public BlackBeardsNavigator () {
    }

    /**
     * creates a map with a flight route from a specific flight
     */
    public JMapViewer createTrackingMap(Flight flight, boolean showPoints)
            throws DataNotFoundException {
        var viewer = new GUISlave().mapViewer();
        var dps = Controller.getInstance().loadedData;
        int size = dps.size(),
            counter = 0,
            altitude,
            heading;
        Position dpPos;
        DataPoint lastdp = null;
        CustomMapMarker newMarker;
        Coordinate coord1, coord2;
        MapPolygonImpl line;
        Color markerColor;
        var polys = new ArrayList<MapPolygon>(size);
        var markers = new ArrayList<MapMarker>(size);
        for (var dp : dps) {
            dpPos = dp.pos();
            altitude = dp.altitude();
            heading = dp.heading();
            markerColor = this.colorByAltitude(altitude);
            if (counter > 0) {
                if (dp.flightID() == lastdp.flightID() // check if the data points belong to eachother
                        && dp.timestamp() >= lastdp.timestamp()) {
                    coord1 = Position.toCoordinate(dpPos);
                    coord2 = Position.toCoordinate(lastdp.pos());
                    line = new MapPolygonImpl(coord1, coord2, coord1);
                    line.setColor(markerColor);
                    polys.add(line);
                }
            }
            if (showPoints) {
                newMarker = new CustomMapMarker(new Coordinate(dpPos.lat(), dpPos.lon()), heading);
                newMarker.setBackColor(markerColor);
                markers.add(newMarker);
            }
            counter++;
            lastdp = dp;
        }

        if (!dps.isEmpty()) {
            if (showPoints) {
                viewer.setMapMarkerList(markers);
            }
            viewer.setMapPolygonList(polys);
            new TreePlantation().createFlightInfo(flight);
        } else throw new DataNotFoundException("Couldn't create Flight Route for this flightID!");
        return viewer;
    }

    /**
     * creates a map with all flights from the given list
     */
    public JMapViewer createLiveMap() {
        var gsl = new GUISlave();
        var viewer = gsl.mapViewer();
        var data = Utilities.parsePositionVector(Controller.getInstance().loadedData);
        CustomMapMarker newMarker;
        for (var pos : data) {
            newMarker = new CustomMapMarker(new Coordinate(pos.lat(), pos.lon()), 90); // FIXME: 19.05.2022
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
            viewer.addMapMarker(newMarker);
        }
        return viewer;
    }

    public JMapViewer createSignificanceMap(HashMap<Airport, Integer> significanceMap) {
        var viewer = new GUISlave().mapViewer();
        var markers = new LinkedList<MapMarker>();
        var atomRadius = new AtomicInteger();
        var atomCoord = new AtomicReference<Coordinate>();
        significanceMap.keySet()
                .forEach(ap -> {
                    int lvl = significanceMap.get(ap);
                    if (lvl > 9) {
                        atomRadius.set(lvl / 100);
                        atomCoord.set(Position.toCoordinate(ap.pos()));
                        var mark = new MapMarkerCircle(atomCoord.get(), atomRadius.get());
                        mark.setColor(Color.RED);
                        mark.setBackColor(null);
                        markers.add(mark);
                    }
                });
        viewer.setMapMarkerList(markers);
        return viewer;
    }

    /**
     * @param altitude is the altitude from the given DataPoint
     * @return a specific color, depending on the altitude
     */
    Color colorByAltitude(int altitude) {
        long meters = Utilities.feetToMeters(altitude);
        int maxHeight = 15000;
        int r = 255,
            g = 0,
            b = 0;
        int factor = (255 / 50);
        for (long i = 0; i < maxHeight;) {
            if (meters <= i) {
                return new Color(r, g, b);
            } else {
                if (i < 7000) {
                    g += factor * 2;
                } else {
                    if (r > 0) {
                        r -= factor * 4;
                        if (r < 0) { // gebraucht?
                            r = 0;
                        }
                    } else {
                        b += factor * 4;
                    }
                }
                i += 300;
            }
        }
        return new Color(r, g, 255);
    }

    /**
     * @return a map prototype (JMapViewer)
     */
    JMapViewer defaultMapViewer(JPanel parent) {
        var viewer = new JMapViewer(new MemoryTileCache());
        var mapController = new DefaultMapController(viewer);
        var mapType = UserSettings.getCurrentMapSource();
        mapController.setMovementMouseButton(1);
        viewer.setBounds(parent.getBounds());
        viewer.setBorder(LINE_BORDER);
        viewer.setZoomControlsVisible(false);
        viewer.setTileSource(mapType);
        viewer.setVisible(true);

        return viewer;
    }

    /**
     * @param marker is the map marker coordinate
     * @param clicked is the clicked coordinate
     * @return true, if clicked coord is equals marker coord, with tolarance
     */
    public boolean isMarkerHit(Coordinate marker, ICoordinate clicked) {
        int zoom = new GUISlave().mapViewer().getZoom();
        double tolerance = 0.008 * zoom; // // FIXME: 23.04.2022 falsche formel (exponential?)
        return (clicked.getLat() < marker.getLat() + tolerance &&
                clicked.getLat() > marker.getLat() - tolerance &&
                clicked.getLon() < marker.getLon() + tolerance &&
                clicked.getLon() > marker.getLon() - tolerance);
    }

    /**
     * @param clicked is the marker not to reset
     * @return resetted list of all map markers
     */
    public List<MapMarker> resetTrackingMarkers(MapMarker clicked) {
        var mapMarkers = new ArrayList<MapMarker>();
        var markerList = new GUISlave().mapViewer().getMapMarkerList();
        for (var m : markerList) {
            var marker = new CustomMapMarker(m.getCoordinate(), 90); // FIXME: 19.05.2022
            if (m == clicked) {
                marker.setColor(Color.WHITE);
            } else {
                marker.setColor(Color.BLACK);
            }
            marker.setBackColor(m.getBackColor());
            mapMarkers.add(marker);
        }
        return mapMarkers;
    }

}
