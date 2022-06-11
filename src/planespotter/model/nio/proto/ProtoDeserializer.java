package planespotter.model.nio.proto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import planespotter.a_test.Test;
import planespotter.controller.Scheduler;
import planespotter.dataclasses.Frame;
import planespotter.model.nio.AbstractDeserializer;
import planespotter.model.nio.Supplier;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @name ProtoDeserializer
 * @author jml04
 * @version 1.0
 *
 * Class ProtoDeserializer represents a Deserializer for Fr24-data.
 * It converts the HttpResponse to a Collection of Frames,
 * which can be used to create further dataclasses like flights, planes, etc...
 */
public class ProtoDeserializer extends DBManager implements AbstractDeserializer {

    /**
     * gets HttpResponse's for specific areas and deserializes its data to Frames
     *
     * @param areas are the Areas where data should be deserialized from
     * @param scheduler is the Scheduler to allow parallelism
     * @return Deque of deserialized Frames
     */
    public synchronized Deque<Frame> getFr24Frames(String[] areas, final Scheduler scheduler) {
        var concurrentDeque = new ConcurrentLinkedDeque<Frame>();
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

    /**
     * deserializes incoming http response
     * from json to frame ArrayDeque
     *
     * @param response is the HttpResponse to deserialize
     * @return ArrayDeque of deserialized Frames
     *
     * @indev
     */
    @Override
    public Deque<Frame> deserialize(HttpResponse<String> response) {
        var jsa = this.parseJsonArray(response);
        var frames = new ArrayDeque<Frame>();
        var it = jsa.iterator();
        var gson = new Gson();
        Frame frame;
        for (JsonElement j; it.hasNext(); /**/) {
            j = it.next();
            frame = gson.fromJson(j, Frame.class);
            frames.add(frame);
        }
        return frames;
    }

    /**
     * parses a HttpResponse to a JsonArray by taking the lines
     * of the response body as a stream, filtering out 1-element-lines,
     * mapping to a stream of unwrapped Strings (without [, ] or "),
     * mapping again to a stream of JsonObjects and
     * adding every element to the JsonArray.
     *
     * @param resp is the HttpResponse with String body to parse
     * @return JsonArray by HttpResponse
     */
    private JsonArray parseJsonArray(HttpResponse<String> resp) {
        var jsa = new JsonArray();
        resp.body().lines()
                .filter(x -> x.length() != 1)
                .map(this::unwrap)
                .map(this::parseJsonObject)
                .forEach(jsa::add);
        return jsa;
    }

    //TODO check o.getAsJsonArray für ganzes objekt -> könnte viel arbeit ersparen

    /**
     * parses a single line to a JsonObject by splitting the line by regex ","
     * and adding the split properties to the JsonObject.
     * Uses this::parseOrElse to parse a Number or putting in a default value.
     *
     * @param line is the line String to parse
     * @return JsonObject from line, if it's not blank
     */
    private JsonObject parseJsonObject(@NotNull String line) {
        if (line.isBlank()) {
            throw new InvalidDataException("line may not be blank / null!");
        }
        var cols = line.split(",");
        var o = new JsonObject();
        try {
            o.addProperty("icaoaddr", cols[0]);
            o.addProperty("lat", this.parseOrElse(cols[1], 0.0));
            o.addProperty("lon", this.parseOrElse(cols[2], 0.0));
            o.addProperty("heading", this.parseOrElse(cols[3], -1));
            o.addProperty("altitude", this.parseOrElse(cols[4], -1));
            o.addProperty("groundspeed", this.parseOrElse(cols[5], -1));
            o.addProperty("squawk", this.parseOrElse(cols[6], 40401));
            o.addProperty("tailnumber", cols[7]);
            o.addProperty("planetype", cols[8]);
            o.addProperty("registration", cols[9]);
            o.addProperty("timestamp", this.parseOrElse(cols[10], -1));
            o.addProperty("srcairport", cols[11]);
            o.addProperty("destairport", cols[12]);
            o.addProperty("flightnumber", cols[13]);
            o.addProperty("unknown1", cols[14]);
            o.addProperty("unknown2", cols[15]);
            o.addProperty("callsign", cols[16]);
            o.addProperty("unknown3", cols[17]);
            o.addProperty("airline", cols[18]);
        } catch (ArrayIndexOutOfBoundsException ignored) { // NumberFormatException still there??
        }
        return o;
    }

    private <N extends Number> Number parseOrElse(@NotNull String toParse, final N orElse) {
        boolean notBlank = !toParse.isBlank();
        if (orElse instanceof Integer) {
            return notBlank ? Integer.parseInt(toParse) : orElse;
        } else if (orElse instanceof Double) {
            return notBlank ? Double.parseDouble(toParse) : orElse;
        }
        return null;
    }

    private String unwrap(@NotNull final String line) {
        String out;
        try {
            if (line.startsWith("{") && line.length() > 43) { // GEHT DAS SO ? length?
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
