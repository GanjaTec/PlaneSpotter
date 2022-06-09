package planespotter.model.nio.proto;

import planespotter.a_test.Test;
import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.Supplier;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtoDeserializer extends DBManager {

    public synchronized Deque<Frame> runSuppliers(Deque<String> areas, Scheduler scheduler) {
        var concurrentDeque = new ConcurrentLinkedDeque<Frame>();
        var test = new Test();
        System.out.println("Deserializing Fr24-Data...");

        var ready = new AtomicBoolean(false);
        var counter = new AtomicInteger(areas.size() - 1);
        while (!areas.isEmpty()) {
            scheduler.exec(() -> {
                var supplier = new Supplier(0, areas.poll());
                try {
                    var data = test.deserialize(supplier.fr24get());
                    while (!data.isEmpty()) {
                        concurrentDeque.add(data.poll());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (counter.get() == 0 && areas.isEmpty()) {
                    ready.set(true);
                }
                counter.getAndDecrement();
            }, "Fr24-Deserializer");
        }
        while (!ready.get()) {
            System.out.print(":");
            try {
                TimeUnit.MILLISECONDS.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        return concurrentDeque;
    }

    public Deque<String> getAllAreas() {
        int initSize = Areas.EASTERN_FRONT.length +
                Areas.GERMANY.length +
                Areas.ITA_SWI_AU.length;
        var areas = new ArrayDeque<String>(initSize);
        Collections.addAll(areas, Areas.EASTERN_FRONT);
        Collections.addAll(areas, Areas.GERMANY);
        Collections.addAll(areas, Areas.ITA_SWI_AU);
        return areas;
    }
}
