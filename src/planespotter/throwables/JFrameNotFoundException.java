package planespotter.throwables;

public class JFrameNotFoundException extends Exception {

    private static final String message = "FrameNotFoundException: Frame coldn't be found!";

    public JFrameNotFoundException () {
        super(message);
    }

    public String getMessage() {
        return message;
    }
}
