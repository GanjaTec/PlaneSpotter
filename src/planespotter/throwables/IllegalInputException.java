package planespotter.throwables;

public class IllegalInputException extends Exception {

    public IllegalInputException() {
        this("");
    }

    public IllegalInputException(String msg) {
        super(msg);
    }

}
