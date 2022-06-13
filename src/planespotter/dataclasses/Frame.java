package planespotter.dataclasses;

import java.lang.reflect.Field;
import java.util.Arrays;

public abstract class Frame {

    public void printValues(Object classs) {
        Field[] fields = classs.getClass().getFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                System.out.println(field.getName() + ": " + field.get(field));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

}
