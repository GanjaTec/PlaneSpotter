package planespotter.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.RECORD_COMPONENT, ElementType.MODULE})
public @interface GitIgnore {

    // TODO: 22.11.2022 implement function for adding the target name to .gitignore

}
