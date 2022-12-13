package planespotter.model;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

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
     * @param <T> is the returned object's type, can be ignored
     * @return result of the function, or null if the function type is void
     * @see planespotter.model.PyAdapter
     * @see org.python.util.PythonInterpreter
     */
    public static <T> T runFunction(String func, Class<T> resultClass) {
        py.exec(func);
        return getResult(resultClass);
    }

    /**
     *
     *
     * @param filename is the script-filename
     * @param params are the function params // does not work yet
     * @param <T> is the returned object's type
     * @return result variable in python-script, if it's not None/null, else null
     * @see planespotter.model.PyAdapter
     * @see org.python.util.PythonInterpreter
     */
    public static <T> T runScript(String filename, Class<T> resultClass, String... params) {
        int counter = 0;
        for (var in : params) {
            var ps = new PyString(in);
            py.set("var" + counter, ps);
            counter++;
        }
        py.execfile(filename);
        return getResult(resultClass);
    }

    /**
     * returns the result of a function called before
     *
     * @param <T> is the result class type
     * @return the result of the function called before
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T getResult(Class<T> clazz) {
        try {
            return py.get("result", clazz);
        } catch (Exception all) {
            return null;
        }
    }


}
