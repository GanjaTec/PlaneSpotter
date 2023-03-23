package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static planespotter.util.Time.*;

public class SimpleBenchmark {

    private static final String PREFIX = "[Benchmark] ";

    public static void benchmark(@NotNull Runnable target) {
        benchmark(target, null);
    }

    public static void benchmark(@NotNull Runnable target, @Nullable String tag) {
        long start, millis, secs;

        start = nowMillis();
        try {
            target.run();
            millis = elapsedMillis(start);
            secs = elapsedSeconds(start);
            tag = tag == null ? "" : "'" + tag + "'";
            System.out.println(PREFIX + "Benchmark " + tag + " successfully done!");
            System.out.println(PREFIX + "Elapsed Time: " + millis + " ms / " + secs + " s");
        } catch (Exception all) {
            System.out.println(PREFIX + "Exception occurred while benchmarking " + tag + "!");
            all.printStackTrace();
        }

    }

}
