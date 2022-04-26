package planespotter.throwables;

public class JFrameNotFoundException extends Exception {

    private final String message = "FrameNotFoundException: Frame coldn't be found!";

    public JFrameNotFoundException () {
        super();
    }

    public String getMessage() {
        return message;
    }
}
