package planespotter.model;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyComplex;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import planespotter.constants.Paths;
import planespotter.controller.Controller;
import sun.misc.Unsafe;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

/**
 * @name PyAdapter
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link PyAdapter} class represents a python-adapter which is able to
 * run python scripts and functions, implementation not finished yet
 */
public class PyAdapter {

    // the python interpreter (IDLE) which runs py
    private static final PythonInterpreter py;

    static {
        init();
        py = new PythonInterpreter();
        py.setOut(System.out);
        py.setErr(System.err);
    }

    private static void init() {
        Properties jvmProps = System.getProperties();
        Properties pyProps = new Properties();
        pyProps.put("python.import.site", "false");
        PythonInterpreter.initialize(jvmProps, pyProps, new String[0]);

    }

    /**
     * runs a specific function in python, given as a string and returns the result
     *
     * @param func is the function that python should evaluate, as string,
     *             must be in the following form:
     *
     *                      result = <function>       // will return result
     *                      <function>                // will return null
     *
     *             null-return can be ignored, but it's recommended to
     *             catch the null-return to prevent a {@link NullPointerException}
     *
     * @param <R> is the returned object's type, can be ignored
     * @return result of the function, or null if the function type is void
     * @see planespotter.model.PyAdapter
     * @see org.python.util.PythonInterpreter
     */
    public static <R> R runFunction(String func) {
        py.exec(func);
        return getResult();
    }

    /**
     *
     *
     * @param filename is the script-filename
     * @param params are the function params // does not work yet
     * @param <R> is the returned object's type
     * @return result variable in python-script, if it's not None/null, else null
     * @see planespotter.model.PyAdapter
     * @see org.python.util.PythonInterpreter
     */
    public static <R> R runScript(String filename, String... params) {
        int counter = 0;
        for (var in : params) {
            var ps = new PyString(in);
            py.set("var" + counter, ps);
            counter++;
        }
        py.execfile(filename);
        return getResult();
    }

    /**
     * returns the result of a function called before
     *
     * @param <R> is the result class type
     * @return the result of the function called before
     */
    @Nullable
    private static <R> R getResult() {
        try {
            return (R) py.get("result");
        } catch (Exception all) {
            return null;
        }
    }


}
