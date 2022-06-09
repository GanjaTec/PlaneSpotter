package planespotter.unused;

// TODO: 31.05.2022 wird die benötigt???
public class JFrameNotFoundException extends Exception {

    private static final String MESSAGE = "FrameNotFoundException: Frame coldn't be found!";

    public JFrameNotFoundException () {
        super(MESSAGE);
    }

    public String getMessage() {
        return MESSAGE;
    }
}
