package planespotter.model.nio.proto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import planespotter.a_test.Test;
import planespotter.constants.Areas;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.Supplier;
import planespotter.throwables.Fr24Exception;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static planespotter.constants.Areas.*;

public class ProtoDeserializer extends DBManager {

    public synchronized Deque<Frame> runSuppliers(String[] areas, final Scheduler scheduler) {
        var concurrentDeque = new ConcurrentLinkedDeque<Frame>();
        var test = new Test();
        System.out.println("Deserializing Fr24-Data...");

        var ready = new AtomicBoolean(false);
        var counter = new AtomicInteger(areas.length - 1);
        for (var area : areas) {
            scheduler.exec(() -> {
                var supplier = new Supplier(0, area);
                try {
                    var data = this.deserialize(supplier.fr24get());
                    while (!data.isEmpty()) {
                        concurrentDeque.add(data.poll());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (counter.get() == 0) {
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

    public String[] getAllAreas() {
        int eastLength = EASTERN_FRONT.length;
        int gerLength = GERMANY.length;
        int itaSwiAuLength = ITA_SWI_AU.length;
        final int length = eastLength + gerLength + itaSwiAuLength;
        var areas = new String[length];
        for (int addIdx = 0, a = 0, b = 0, c = 0;addIdx < length;/**/) {
            if (a < eastLength) {
                areas[addIdx] = EASTERN_FRONT[a];
                a++;
                addIdx++;
            }
            if (b < gerLength) {
                areas[addIdx] = GERMANY[b];
                b++;
                addIdx++;
            }
            if (c < itaSwiAuLength) {
                areas[addIdx] = ITA_SWI_AU[c];
                c++;
                addIdx++;
            }
        }
        return areas;
    }

    /**
     * deserializes incoming http response
     * from json to frame ArrayDeque
     *
     * @param response is the HttpResponse to deserialize
     * @return ArrayDeque of frames
     *
     * @indev
     */
    public Deque<Frame> deserialize(HttpResponse<String> response) {
        var jsa = this.parseJsonArray(response);
        var frames = new ArrayDeque<Frame>();
        var it = jsa.iterator();
        var gson = new Gson();
        Frame frame;
        for (JsonElement j; it.hasNext();) {
            j = it.next();
            frame = gson.fromJson(j, Frame.class);
            frames.add(frame);
        }
        return frames;
    }

    private JsonArray parseJsonArray(HttpResponse<String> resp) {
        var jsa = new JsonArray();
        resp.body().lines()
                .filter(x -> x.length() != 1)
                .map(this::unwrap)
                .map(this::parseJsonObject)
                .forEach(jsa::add);
        return jsa;
    }

    //TODO check o.getAsJsonArray für ganzes objekt
    private JsonObject parseJsonObject(String line) {
        if (line.isBlank()) {
            throw new IllegalArgumentException("line may not be blank / null!");
        }
        var cols = line.split(",");
        var o = new JsonObject();
        try {
            o.addProperty("icaoaddr", cols[0]);
            o.addProperty("lat", this.parseOrDefault(cols[1], double.class));
            o.addProperty("lon", this.parseOrDefault(cols[2], double.class));
            o.addProperty("heading", this.parseOrDefault(cols[3], int.class));
            o.addProperty("altitude", this.parseOrDefault(cols[4], int.class));
            o.addProperty("groundspeed", this.parseOrDefault(cols[5], int.class));
            o.addProperty("squawk", 40401);
            o.addProperty("tailnumber", cols[7]);
            o.addProperty("planetype", cols[8]);
            o.addProperty("registration", cols[9]);
            o.addProperty("timestamp", this.parseOrDefault(cols[10], int.class));
            o.addProperty("srcairport", cols[11]);
            o.addProperty("destairport", cols[12]);
            o.addProperty("flightnumber", cols[13]);
            o.addProperty("unknown1", cols[14]);
            o.addProperty("unknown2", cols[15]);
            o.addProperty("callsign", cols[16]);
            o.addProperty("unknown3", cols[17]);
            o.addProperty("airline", cols[18]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
        }
        return o;
    }

    private Number parseOrDefault(String toParse, Class<? extends Number> target) {
        boolean notBlank = !toParse.isBlank();
        if (target == Integer.class || target == int.class) {
            return notBlank ? Integer.parseInt(toParse) : -1;
        } else if (target == Double.class || target == double.class) {
            return notBlank ? Double.parseDouble(toParse) : -1.0;
        }
        return null;
    }

    private String unwrap(String line) {
        String out;
        try {
            if (line.startsWith("{")) {
                out = line.substring(43);
            } else {
                out = line.substring(12);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new Fr24Exception("Invalid Fr24-Data! caused by:\n" + e.getMessage());
        }
        return out.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("\"", "");
    }
}
