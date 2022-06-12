package planespotter.constants;

/**
 * @name Areas
 * @author Lukas
 * @version 1.0
 *
 * class areas contains all map areas as complicated coordinate-strings
 */
public final class Areas { // TODO zu Enum machen (die einzel Strings) mit String area Bsp.: URKAINE("...").get() oder so

	// World Areas
	public static final String AMERICA = "84.512%2C-66.357%2C-162.169%2C-23.303";
	public static final String EURASIA = "85.052%2C-63.86%2C-41.935%2C-170.256";

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


	public static synchronized String[] getWorldAreas() {
		return new String[] { AMERICA, EURASIA };
	}

	public static synchronized String[] getAllAreas() {
		int eastLength = EASTERN_FRONT.length;
		int gerLength = GERMANY.length;
		int itaSwiAuLength = ITA_SWI_AU.length;
		final int length = eastLength + gerLength + itaSwiAuLength;
		var areas = new String[length];
		int a = 0, b = 0, c = 0; // counter
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

}
	
