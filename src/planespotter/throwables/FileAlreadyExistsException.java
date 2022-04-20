package planespotter.throwables;

public class FileAlreadyExistsException extends Exception {

    private final String message = "Die config.cfg-Datei existiert bereits!";

    public FileAlreadyExistsException () {
        super();
    }

    public String getMessage () { return message; }


}
