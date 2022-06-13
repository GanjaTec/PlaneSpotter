package planespotter.dataclasses;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @name Frame
 * @author jml04
 * @author Lukas
 * @version 1.0
 *
 * abstract class Frame is a frame-superclass which should
 * have all default frame fields e.g. latitude, longitude ,etc...
 */
public abstract class Frame {

    public <E> void printValues(E o) {
        Field[] fields = o.getClass().getFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                System.out.println(field.getName() + ": " + field.get(field));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

}
