package planespotter.constants;

import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import planespotter.dataclasses.Position;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name Areas
 * @author Lukas
 * @version 1.0
 *
 * class areas contains all map areas as complicated coordinate-strings
 */
public final class Areas {

	/**
	 * Area-Separator-String, is used to separate the doubles in an Area-String
	 */
	private static final String SEPARATOR = "%2C";
	/**
	 * Area Structure:
	 *
	 * " {lat-topLeft} %2C {latBottomRight} %2C {lonTopLeft} %2C {lonBottomRight} "
	 *
	 * lat's go from 90 to -90
	 * lon's go from -180 to 180
	 *
	 * Bsp.: "86.0%2C-45.8%2C-120.1%2C150.5"
	 */

	//Ukraine War
	public static final String UKRAINE = "52.567%2C45.909%2C17.843%2C45.967";
	public static final String ROMANIA = "47.669%2C44.114%2C18.568%2C32.63";
	public static final String POLAND = "58.152%2C54.192%2C19.093%2C33.154";
	public static final String LITHUANIA_LATVIA = "58.238%2C54.955%2C19.085%2C33.147";
	public static final String BELARUS = "56.624%2C51.183%2C22.153%2C36.215";
	public static final String CRIMEA_BLACKSEA = "46.652%2C41.969%2C27.323%2C41.384";
	public static final String SVK_HUN_CZE_AUT = "51.146%2C45.205%2C11.581%2C25.642";
	
	public static final String[] EASTERN_FRONT = {UKRAINE, ROMANIA, POLAND, LITHUANIA_LATVIA, BELARUS, CRIMEA_BLACKSEA, SVK_HUN_CZE_AUT};
	
	//Germany
	public static final String CGN_LANDESCHNEISE = "51.055%2C50.853%2C6.544%2C7.422";
	public static final String NE_GER = "54.876%2C52.548%2C8.05%2C18.596";
	public static final String NW_GER = "54.903%2C52.539%2C3.568%2C14.114";
	public static final String UCE_GER = "53.127%2C50.662%2C7.546%2C18.092";
	public static final String UCW_GER = "52.998%2C50.526%2C2.201%2C12.747";
	public static final String LCE_GER = "51.362%2C48.799%2C5.915%2C16.461";
	public static final String LCW_GER = "51.533%2C48.979%2C1.399%2C11.945";
	public static final String S_GER= "49.728%2C47.077%2C5.772%2C16.318";
	
	public static final String[] GERMANY = {CGN_LANDESCHNEISE, NE_GER, NW_GER, UCE_GER, UCW_GER, LCE_GER, LCW_GER, S_GER};
	
	//Italy Austria Switzerland
	public static final String NORTHERN_ALPS = "49.01%2C46.32%2C5.258%2C15.803";
	public static final String ALPS = "47.773%2C45.018%2C4.588%2C15.134";
	public static final String SOUTHERN_ALPS = "46.965%2C44.169%2C4.923%2C15.469";
	public static final String UPPER_ITA = "44.98%2C42.085%2C6.027%2C16.573";
	public static final String CENTRAL_ITA = "43.526%2C40.56%2C6.902%2C17.448";
	public static final String TYRRHENIAN_SEA = "41.797%2C38.75%2C7.32%2C17.866";
	public static final String S_ITA = "41.67%2C38.617%2C10.566%2C21.112";
	public static final String SICILY = "39.289%2C36.13%2C10.139%2C20.685";
	
	public static final String[] ITA_SWI_AU = {NORTHERN_ALPS, ALPS, SOUTHERN_ALPS, UPPER_ITA, CENTRAL_ITA, TYRRHENIAN_SEA, S_ITA, SICILY};
	
	//Western EU
	public static final String GB = "";
	public static final String FRANCE = "";
	public static final String SPAIN = "";
	
	public static final String[] WESTERN_EU = {GB, FRANCE, SPAIN};


	//Northatlantic Flight Corridor
	public static final String G = "";
	public static final String Y = "";
	public static final String RMANY = "";
	public static final String NY = "";
	public static final String MANY = "";
	
	public static final String[] NAFC = {};

	/**
	 * creates a 1D-Array of Area Strings, which are created by coordinates
	 * in a 6 * 6 2D array with newArea()
	 *
	 * @return Array of Areas (whole world)
	 */
	// TODO: 02.08.2022 2. method getWorldAreaRaster1D(float gridSize) which uses this one
	public static synchronized String[] getWorldAreaRaster1D(double lonVel, double latVel) {
		double lat, lon = -180.;
		byte xLength = (byte) (360 / lonVel),
			 yLength = (byte) (180 / latVel);

		final String[][] areaRaster2D = new String[xLength][yLength]; // xLength * yLength Raster of Areas
		final String[] areaRaster1D = new String[xLength * yLength]; // 6 * 6 Raster as 1D-Array
		final AtomicInteger index = new AtomicInteger(0);

		for (byte x = 0; x < xLength; x++) {
			lat = 90.;
			for (byte y = 0; y < yLength; y++) {
				areaRaster2D[x][y] = Areas.newArea(new Position(lat, lon), new Position(lat - latVel, lon + lonVel));
				lat -= latVel;
			}
			lon += lonVel;
		}
		Arrays.stream(areaRaster2D)
				.forEach(arr -> Arrays.stream(arr)
						.forEach(area -> areaRaster1D[index.getAndIncrement()] = area));

		return areaRaster1D;
	}

	public static synchronized String[] getAllAreas() {
		int eastLength = EASTERN_FRONT.length;
		int gerLength = GERMANY.length;
		int itaSwiAuLength = ITA_SWI_AU.length;
		final int length = eastLength + gerLength + itaSwiAuLength;
		var areas = new String[length];
		int a = 0, b = 0, c = 0; // counters
		for (int addIdx = 0; addIdx < length; addIdx++) {
			if (a < eastLength) {
				areas[addIdx] = EASTERN_FRONT[a];
				a++;
			}
			if (b < gerLength) {
				areas[addIdx] = GERMANY[b];
				b++;
			}
			if (c < itaSwiAuLength) {
				areas[addIdx] = ITA_SWI_AU[c];
				c++;
			}
		}
		return areas;
	}

		/**
		 * creates an Area directly by single Coordinates
		 *
		 * @param latTopLeft is the bottom-right-latitude
		 * @param latBottomRight is the top-left-latitude
		 * @param lonTopLeft is the bottom-left-longitude
		 * @param lonBottomRight is the top-right-longitude
		 * @return new Area String, composed of the given Coordinates
		 */
		public static String newArea(@Range(from = 90, to = -90) final double latTopLeft,
									 @Range(from = 90, to = -90) final double latBottomRight,
									 @Range(from = -180, to = 180) final double lonTopLeft,
									 @Range(from = -180, to = 180) final double lonBottomRight) {
			return  latTopLeft     + SEPARATOR +
					latBottomRight + SEPARATOR +
					lonTopLeft     + SEPARATOR +
					lonBottomRight;
		}

		/**
		 * creates an Area by Positions
		 *
		 * @param topLeft is the bottom-left-position
		 * @param bottomRight is the top-right-position
		 * @return new Area String, composed of the given Positions
		 */
		public static String newArea(final Position topLeft, final Position bottomRight) {
			return newArea(topLeft.lat(), bottomRight.lat(), topLeft.lon(), bottomRight.lon());
		}

		/**
		 * creates an Area by ICoordinates / Coordinates
		 *
		 * @param topLeft is the bottom-left-coordinate
		 * @param bottomRight is the top-right-coordniate
		 * @return new Area String, composed of the given ICoordniates / Coordniates
		 */
		public static String newArea(final ICoordinate topLeft, final ICoordinate bottomRight) {
			return newArea(topLeft.getLat(), bottomRight.getLat(), topLeft.getLon(), bottomRight.getLon());
		}

}
	
