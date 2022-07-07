package planespotter.display;

import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import planespotter.constants.UserSettings;
import planespotter.constants.ViewType;
import planespotter.controller.ActionHandler;
import planespotter.controller.Controller;
import planespotter.dataclasses.*;
import planespotter.display.models.HeatMapRectangle;
import planespotter.util.Utilities;
import planespotter.throwables.DataNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static planespotter.constants.GUIConstants.DEFAULT_HEAD_TEXT;
import static planespotter.constants.DefaultColor.DEFAULT_MAP_ICON_COLOR;
import static planespotter.constants.GUIConstants.LINE_BORDER;


/**
 * @name BlackBeardsNavigator
 * @author jml04
 * @version 1.1
 *
 *  manages the map data and contains methods which are executed on the mapView
 */
public final class MapManager {

    private final GUI gui;

    private volatile TreasureMap mapViewer;

    /**
     * constructor
     */
    public MapManager(GUI gui, JPanel defaultMapPanel, ActionHandler listener) {
        this.gui = gui;
        this.mapViewer = this.defaultMapViewer(defaultMapPanel);
        this.mapViewer.addKeyListener(listener);
        this.mapViewer.addMouseListener(listener);
    }

    /**
     * creates a map with a flight route from a specific flight
     */
    public TreasureMap createTrackingMap(Vector<DataPoint> dataPoints, @Nullable Flight flight, boolean showPoints, GUI gui) {

        var viewer = this.mapViewer;
        int size = dataPoints.size(),
            counter = 0,
            altitude,
            heading;
        Position dpPos;
        DataPoint lastdp = null;
        DefaultMapMarker newMarker;
        Coordinate coord1, coord2;
        MapPolygonImpl line;
        Color markerColor;
        var polys = new ArrayList<MapPolygon>(size);
        var markers = new ArrayList<MapMarker>(size);
        for (var dp : dataPoints) {
            dpPos = dp.pos();
            altitude = dp.altitude();
            heading = dp.heading();
            markerColor = Utilities.colorByAltitude(altitude);
            if (counter > 0) {
                double lon0 = lastdp.pos().lon();
                double lon1 = dp.pos().lon();
                boolean lonJump = (lon0 < -90 && lon1 > 90) || (lon0 > 90 && lon1 < -90);
                //boolean lonFit = (lon0 < 140 && lon1 > lon0) || (lon0 < -140 && lon1 < lon0);
                //double lonDiff = Math.abs(lon0) - Math.abs(lon1);
                //boolean lonDiffFit = (lonDiff < 100) && (-lonDiff > -100);
                //boolean lonFit = (lon0 + lonDiff <= 180) || (lon0 - lonDiff >= -180);

                if (dp.flightID() == lastdp.flightID() // check if the data points belong to eachother
                        && dp.timestamp() >= lastdp.timestamp()
                        && !lonJump) { // TODO: 22.06.2022 FIX lonFit
                    coord1 = Position.toCoordinate(dpPos);
                    coord2 = Position.toCoordinate(lastdp.pos());
                    line = new MapPolygonImpl(coord1, coord2, coord1);
                    line.setColor(markerColor);
                    polys.add(line);
                }
            }
            if (showPoints) {
                newMarker = new DefaultMapMarker(new Coordinate(dpPos.lat(), dpPos.lon()), heading);
                newMarker.setBackColor(markerColor);
                markers.add(newMarker);
            }
            counter++;
            lastdp = dp;
        }

        gui.disposeView();

        if (!dataPoints.isEmpty()) {
            if (showPoints) {
                viewer.setMapMarkerList(markers);
            }
            viewer.setMapPolygonList(polys);
            if (dataPoints.size() == 1 && flight != null) {
                gui.getTreePlantation().createFlightInfo(flight, gui);
            }
        }
        return viewer;
    }

    /**
     * creates a map with all flights from the given list
     */
    public TreasureMap createLiveMap(final Vector<Position> data, final TreasureMap viewer) {
        DefaultMapMarker newMarker;
        for (var pos : data) {
            newMarker = new DefaultMapMarker(new Coordinate(pos.lat(), pos.lon()), 90); // FIXME: 19.05.2022
            newMarker.setBackColor(DEFAULT_MAP_ICON_COLOR.get());
            viewer.addMapMarker(newMarker);
        }
        /*var areas = Areas.getWorldAreaRaster1D();
        var coords = new ArrayDeque<Coordinate[]>();
        for (var a : areas) {
            var sp = a.split("%2C");
            var carr = new Coordinate[] {
                    new Coordinate(0., 0.), new Coordinate(0., 0.)
            };
            for (int i = 0; i < sp.length; i++) {
                double dble = Double.parseDouble(sp[i]);
                switch (i) {
                    case 0 -> carr[0].setLat(dble);
                    case 1 -> carr[1].setLat(dble);
                    case 2 -> carr[0].setLon(dble);
                    case 3 -> carr[1].setLon(dble);
                }
            }
            coords.add(carr);
        }
        for (var cds : coords) {
            mapViewer.addMapRectangle(new MapRectangleImpl(cds[0], cds[1]));
        }*/
        return viewer;
    }

    public TreasureMap createSignificanceMap(final Map<Airport, Integer> significanceMap, final TreasureMap viewer) {
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

    public TreasureMap createPrototypeHeatMap(final HashMap<Position, Integer> heatMap, final TreasureMap viewer) {
        var markers = new LinkedList<MapMarker>();
        MapMarkerCircle newMarker;
        for (var pos : heatMap.keySet()) {
            int radius = heatMap.get(pos)/10000;
            //newMarker = new MapMarkerCircle(Position.toCoordinate(pos), radius);
            newMarker = new DefaultMapMarker.HeatMapMarker(Position.toCoordinate(pos), heatMap.get(pos)/8);
            newMarker.setColor(Color.RED);
            newMarker.setBackColor(null);
            markers.add(newMarker);
        }

        viewer.setMapMarkerList(markers);
        return viewer;
    }

    public MapManager createRasterHeatMap(final BufferedImage heatMapImg, final TreasureMap viewer) {
        var rect = new HeatMapRectangle(heatMapImg, viewer);
        viewer.addMapRectangle(rect);
        return this;
    }

    /**
     * @return a map prototype (TreasureMap)
     */
    public TreasureMap defaultMapViewer(JPanel parent) {
        var viewer = new TreasureMap();
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
        int zoom = gui.getMap().getZoom();
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
        var markerList = this.gui.getMap().getMapMarkerList();
        for (var m : markerList) {
            var marker = new DefaultMapMarker(m.getCoordinate(), 90); // FIXME: 19.05.2022
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

    /**
     * sets the TreasureMap in mapViewer
     *
     * @param map is the map to be set
     */
    public void receiveMap(TreasureMap map, String text, ViewType viewType) {
        text = (text == null) ? "" : text;
        Controller.getGUI().setCurrentViewType(viewType);
        // adding MapViewer to panel (needed?)
        this.mapViewer = map;
        var mapPanel = gui.getComponent("mapPanel");
        if (mapPanel.getComponentCount() == 0) {
            mapPanel.add(this.mapViewer);
        }
        var viewHeadTxt = (JLabel) gui.getComponent("viewHeadTxtLabel");
        viewHeadTxt.setText(DEFAULT_HEAD_TEXT + "Map-Viewer > " + text);
        // revalidating window fr24Frame to refresh everything
        mapPanel.setVisible(true);
        this.mapViewer.setVisible(true);
        Controller.getGUI().requestComponentFocus(this.mapViewer);
    }

    TreasureMap getMapViewer() {
        return this.mapViewer;
    }

}
