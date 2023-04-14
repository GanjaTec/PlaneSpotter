package planespotter.util;

import org.jetbrains.annotations.Nullable;
import planespotter.controller.Controller;
import planespotter.display.UserInterface;
import planespotter.display.models.DevToolsView;
import planespotter.model.io.DBToCSV;
import planespotter.throwables.DataNotFoundException;

import java.io.IOException;
import java.sql.SQLException;

public final class DevTools {

    public static final int SEPARATE_TABLES = 0;
    public static final int COMBINE_TABLES = 1;

    private static Throwable lastError = null;


    /**
     * saves the Database into CSV file(s)
     *
     * @param saveMode is the save mode,
     *                 0 for separated table CSV's,
     *                 1 for one combined CSV
     * @see DevTools constants
     */
    public static boolean saveDBAsCSV(int saveMode, String dirName) {
        UserInterface ui = Controller.getInstance().getUI();
        ui.showDevToolsView();
        try {
            switch (saveMode) {
                case SEPARATE_TABLES -> DBToCSV.dbToCSV(dirName, true);
                case COMBINE_TABLES -> DBToCSV.dbToCSV(dirName, false);
                default -> throw new IllegalArgumentException("Invalid save mode, see DevTools constants for valid modes.");
            }
            return true;
        } catch (IOException | DataNotFoundException | SQLException e) { // TODO: 01.02.2023 catchThemAll
            e.printStackTrace();
            setLastError(e);
            return false;
        } finally {
            DevToolsView dtools = ui.getDevToolsView();
            if (dtools != null) {
                ui.getDevToolsView().setLoading(false);
            }
        }
    }

    @Nullable
    public static Throwable getLastError() {
        return lastError;
    }

    private static void setLastError(Throwable e) {
        lastError = e;
    }
}
