package planespotter.constants;

import org.jetbrains.annotations.NotNull;

/**
 * @name Warning
 * @author jml04
 * @version 1.0
 *
 * @description
 * enum Warning contains all Warnings with warning-message-strings
 */
public enum Warning {
    LIVE_DATA_NOT_FOUND("No Live-Data found!"),
    NO_DATA_FOUND("Couldn't find any data!"),
    FIELDS_NOT_FILLED("Some fields are not filled, please fill them and try again!"),
    SQL_ERROR("Seems like there is a problem with the database, please contact an admin!"),
    UNKNOWN_ERROR("Unknown error occurred, please contact an admin!"),
    TIMEOUT("Timeout! Task takes more time than expected! \nIf the Program is lagging, try to restart it!"),
    REJECTED_EXECUTION("Execution rejected! This shouldn't happen normally!"),
    ILLEGAL_INPUT("Illegal Input! \nAn expression/character you used, is not allowed!"),
    NUMBER_EXPECTED("Number expected, please enter a valid number!"),
    FILE_ALREADY_EXISTS("File already exists, please choose another name!"),
    INVALID_DATA("Some data is invalid, try again."),
    FILE_NOT_FOUND("File couldn't be found, check Path and try again!"),
    OUT_OF_RANGE("Input out of range!"),
    NOT_SUPPORTED_YET("This feature is not supported yet!"),
    HANDSHAKE("Handshake Exception, pausing Collector for 1 minute!"),
    NO_CONNECTION("Connection failed, please check internet connection and try again!"),
    URL_NOT_REACHABLE("a data cloud URL is not reachable, please check your internet connection!"),
    NAME_NOT_UNIQUE("Name must be unique, please change the name and try again!"),
    OUT_OF_MEMORY("OutOfMemoryError, please restart the program!");

    // warning string instance field
    @NotNull private final String message;

    // private enum constructor
    Warning(@NotNull String msg) {
        this.message = msg;
    }

    // message string getter
    @NotNull
    public final String message() {
        return this.message;
    }
}
