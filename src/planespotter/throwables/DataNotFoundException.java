package planespotter.throwables;

import planespotter.display.SearchModels;

import javax.swing.*;

// TODO neuen DIalog erstellen!! wenn kein flug oder so gefunden wurde // neuer Konstructor mit param dialog
public class DataNotFoundException extends Exception {

    // exception message
    static final String MESSAGE = "db-data couldn't be found!";

    /**
     * constructor, is called when this exception is thrown
     */
    public DataNotFoundException () {
        new DataNotFoundException("");
    }

    /**
     * second constructor with string param
     *
     * @param msg is the exception message
     */
    public DataNotFoundException (String msg) {
        super(MESSAGE + "\n" + msg);
        var errorMsgPane = SearchModels.errorMsgPane(msg);
        errorMsgPane.setVisible(true);
    }

}
