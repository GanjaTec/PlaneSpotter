package planespotter.model;

import planespotter.controller.Scheduler;
import planespotter.dataclasses.DataPoint;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.proto.ProtoDeserializer;
import planespotter.throwables.NoAccessException;
import planespotter.util.Utilities;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LiveMap {
    // frames, which will be inserted later (first loaded into the view)
    private static final ConcurrentLinkedQueue<Frame> insertLater;
    // static initializer
    static {
        insertLater = new ConcurrentLinkedQueue<>();
    }

    public static Vector<DataPoint> directLiveData(Scheduler scheduler) {
        var deserializer = new ProtoDeserializer();
        var frames = deserializer.runSuppliers(deserializer.getAllAreas(), scheduler);
        insertLater.addAll(frames);
        return frames.stream()
                .map(Utilities::parseDataPoint)
                .collect(Collectors.toCollection(Vector::new));
    }

    /**
     *
     * @return true, if insertLater.size() is greater or equals 1000, else false
     *         if true, another Method gets ac
     */
    protected static boolean canInsert() {
        return (insertLater.size() > 999);
    }

    protected static Queue<Frame> pollFromQueue() {
        var counter = new AtomicInteger();
        var frames = new ConcurrentLinkedQueue<Frame>();

        insertLater.parallelStream()
                .forEach(frame -> {
                    if (counter.get() <= 1000) {
                        frames.add(insertLater.poll());
                        counter.getAndIncrement();
                    }
                });
        return frames;
    }

}
