package planespotter.controller;

import com.google.gson.*;
import org.jetbrains.annotations.TestOnly;
import planespotter.constants.Areas;
import planespotter.dataclasses.Frame;
import planespotter.model.Supplier;

import java.net.http.HttpResponse;
import java.util.*;

@TestOnly
public class Test {

    // TEST-MAIN
    // FIXME: 04.05.2022 callsigns und planetypes sind beide noch in "" (Bsp: "A320" statt A320)
    // FIXME: 05.05.2022 planetypes werden in getAllPlanetypes doppelt ausgegeben!
    public static void main(String[] args) throws Exception {
        Supplier supplier = new Supplier(0, Areas.S_GER);
        var frames = new Test().deserialize(supplier.fr24get());
        frames.forEach(Frame::printValues);
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
    public Deque<Frame> deserialize (HttpResponse<String> response) {
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

    private JsonArray parseJsonArray (HttpResponse<String> resp) {
        var jsa = new JsonArray();
        resp.body().lines()
                .filter(x -> x.length() != 1)
                .map(this::unwrap)
                .map(this::parseJsonObject)
                .forEach(jsa::add);
        return jsa;
    }

    //TODO check o.getAsJsonArray für ganzes objekt
    private JsonObject parseJsonObject (String line) {
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

    private Number parseOrDefault (String toParse, Class<? extends Number> target) {
        boolean notBlank = !toParse.isBlank();
        if (target == Integer.class || target == int.class) {
            return notBlank ? Integer.parseInt(toParse) : -1;
        } else if (target == Double.class || target == double.class) {
            return notBlank ? Double.parseDouble(toParse) : -1.0;
        }
        return null;
    }

    private String unwrap (String line) {
        String out;
        if (line.startsWith("{")) {
            out = line.substring(43);
        }  else {
            out = line.substring(12);
        }
        return out.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("\"", "");
    }

}
