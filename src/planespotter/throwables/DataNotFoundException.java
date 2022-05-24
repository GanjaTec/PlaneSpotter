package planespotter.throwables;

import planespotter.controller.Controller;

// TODO neuen DIalog erstellen!! wenn kein flug oder so gefunden wurde // neuer Konstructor mit param dialog
public class DataNotFoundException extends Exception {

    // exception message
    final String MESSAGE = "db-data couldn't be found!";

    /**
     * constructor, is called when this exception is thrown
     */
    public DataNotFoundException () {
        this(null, false);
    }

    /**
     * constructor only with error message
     *
     * @param msg is the error message
     */
    public DataNotFoundException (String msg) {
        this(msg, false);
    }


    /**
     * (main) constructor with string and boolean params
     *
     * @param msg is the exception message
     * @param doLog says if the exception should be logged in the console
     */
    public DataNotFoundException (String msg, boolean doLog) {
        if (doLog) {
            var ctrl = Controller.getInstance();
            if (ctrl != null) {
                ctrl.getLogger().errorLog(MESSAGE + "\n" + msg, this);
            }
        } else {
            if (msg == null) {
                this.printStackTrace();
            }
        }
        /*var errorMsgPane = new SearchModels().errorMsgPane(msg);
        errorMsgPane.setVisible(true);*/
    }

    @Override
    public void printStackTrace () {
        var stringBuilder = new StringBuilder();
        var stackTrace = super.getStackTrace();
        stringBuilder.append(MESSAGE);
        for (var el : stackTrace) {
            stringBuilder.append("\n at ").append(el);
        }
        var logger = Controller.getLogger();
        var out = stringBuilder.toString();
        logger.errorLog(out, this);
        super.printStackTrace();
    }

    @Override
    public String getMessage() {
        return this.MESSAGE;
    }
}
