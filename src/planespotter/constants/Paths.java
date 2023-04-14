package planespotter.constants;

import planespotter.controller.Controller;
import planespotter.util.Utilities;

/**
 * @name Paths
 * @author jml04
 * @author Bennet
 * @author Lukas
 * @version 1.0
 *
 * @description
 * Contains all (relative, except ROOT_PATH) paths to various resource,
 * root directory is the project directory
 */
public final class Paths {

    /**
     * absolute root-path, project folder, given by System property 'user.dir'
     * initialized with the working directory when the program is started
     */
    @SuppressWarnings(value = "Does not work yet")
    public static final String ROOT = (Controller.ROOT_PATH == null) ? Utilities.getAbsoluteRootPath() : Controller.ROOT_PATH;

    /**
     * src-path
     * contains all project sources
     */
    public static final String SOURCE = "src\\";

    /**
     * python-helper-path
     * contains all python-helper-scripts
     */
    public static final String PY_RUNTIME_HELPER = "python-helper\\runtime-helper\\";

    /**
     * libs-path
     * contains all external libraries
     */
    public static final String LIBS = SOURCE + "libs\\";

    /**
     * license-path
     * contains all license files
     */
    public static final String LICENSE = SOURCE + "license\\";

    /**
     * code path
     * contains all project code
     */
    public static final String CODE = SOURCE + "planespotter\\";

    /**
     * resource-path
     * contains all project resources
     */
    public static final String RESOURCE = CODE + "resource\\";

    /**
     * Bitmap-history path
     * contains all history Bitmaps
     */
    public static final String BMP_HISTORY = RESOURCE + "bmphistory\\";

    /**
     * Database CSV exports
     */
    public static final String DB_CSV = RESOURCE + "DatabaseCSV\\";

    /**
     * image-path
     * contains all used images
     */
    public static final String IMAGES = RESOURCE + "img\\";

    /**
     * CUDA-path
     * contains .cu and .ptx files for GPU-tasks
     */
    public static final String CUDA = CODE + "cuda\\";

}
