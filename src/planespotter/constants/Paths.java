package planespotter.constants;

import planespotter.controller.Controller;
import planespotter.util.Utilities;

/**
 * @name Paths
 * @author jml04
 * @author Bennet
 * @author Lukas
 * @version 1.0
 * @description
 * Contains all (absolute) paths to various resource,
 * root directory is the project directory
 */
public final class Paths {

    /**
     * absolute root-path, project folder, given by System property 'user.dir'
     * initialized with the working directory when the program is started
     */
    @SuppressWarnings(value = "Does not work yet")
    public static final String ROOT_PATH = Controller.ROOT_PATH;

    /**
     * src-path
     * contains all project sources
     */
    public static final String SRC_PATH = "src\\";

    /**
     * libs-path
     * contains all external libraries
     */
    public static final String LIBS_PATH = SRC_PATH + "libs\\";

    /**
     * code path
     * contains all project code
     */
    public static final String CODE_PATH = SRC_PATH + "planespotter\\";

    /**
     * resource-path
     * contains all project resources
     */
    public static final String RESOURCE_PATH = CODE_PATH + "resource\\";

    /**
     * image-path
     * contains all used images
     */
    public static final String IMAGE_PATH = RESOURCE_PATH + "img\\";

}
