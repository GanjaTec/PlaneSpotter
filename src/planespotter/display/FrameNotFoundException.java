package planespotter.display;

public class FrameNotFoundException extends Exception {

    private final String message = "FrameNotFoundException: Frame coldn't be found!";

    public FrameNotFoundException () {
        super();
    }

    public String getMessage() {
        return message;
    }
}
