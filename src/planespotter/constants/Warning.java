package planespotter.constants;

/**
 * @name Warning
 * @author jml04
 * @version 1.0
 * @description
 * enum Warning contains all Warnings with warning-message-strings
 */
public enum Warning {
    LIVE_DATA_NOT_FOUND("No Live-Data found!"),
    NO_DATA_FOUND("Couldn't find any data!"),
    SQL_ERROR("Seems like there is a problem with the database, please contact an admin!"),
    UNKNOWN_ERROR("Unknown error occurred, please contact an admin!"),
    TIMEOUT("Timeout! Task takes more time than expected! \nIf the Program is lagging, try to restart it!"),
    REJECTED_EXECUTION("Execution rejected! This shouldn't happen normally!"),
    ILLEGAL_INPUT("Illegal Input! \nAn expression/character you used, is not allowed!"),
    NUMBER_EXPECTED("INT expected! please enter a valid number (e.g. 1, 525, -11,..."),
    FILE_ALREADY_EXISTS("File already exists, please choose another name!"),
    INVALID_DATA("Some data is invalid, try again."),
    FILE_NOT_FOUND("File couldn't be found, check Path and try again!"),
    NOT_SUPPORTED_YET("This feature is not supported yet!");

    // warning string instance field
    private final String message;

    // private enum constructor
    Warning(String msg) {
        this.message = msg;
    }

    // message string getter
    public final String message() {
        return this.message;
    }
}
