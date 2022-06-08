package planespotter.constants;

public enum Warning {
    LIVE_DATA_NOT_FOUND("No Live-Data found!"),
    NO_DATA_FOUND("Couldn't find any data!"),
    SQL_ERROR("Seems like there is a problem with the database, please contact an admin!"),
    UNKNOWN_ERROR("Unknown error occurred, please contact an admin!"),
    TIMEOUT("Timeout! Task takes more time than expected! \nIf the Program is lagging, try to restart it!");

    private final String message;

    Warning(String msg) {
        this.message = msg;
    }

    public final String message() {
        return this.message;
    }
}
