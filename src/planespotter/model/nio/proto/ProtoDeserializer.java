package planespotter.model.nio.proto;

import planespotter.a_test.Test;
import planespotter.constants.Areas;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.Supplier;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProtoDeserializer extends DBManager {

    public Deque<Frame> runSuppliers(Deque<String> areas, ThreadPoolExecutor exe) {
        var concurrentDeque = new ConcurrentLinkedDeque<Frame>();
        var test = new Test();
        System.out.println("Deserializing Fr24-Data...");

        while (!areas.isEmpty()) {
            exe.execute(() -> {
                var supplier = new Supplier(0, areas.poll());
                try {
                    var data = test.deserialize(supplier.fr24get());
                    while (!data.isEmpty()) {
                        concurrentDeque.add(data.poll());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        while (exe.getActiveCount() > 0) {
            System.out.print(":");
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        return concurrentDeque;
    }

    Deque<String> getAllAreas() {
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
