package planespotter.controller;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import netscape.javascript.JSObject;
import planespotter.constants.Areas;
import planespotter.dataclasses.Frame;
import planespotter.model.DBOut;
import planespotter.model.Deserializer;
import planespotter.model.Supplier;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

    // TEST-MAIN
    // FIXME: 04.05.2022 callsigns und planetypes sind beide noch in "" (Bsp: "A320" statt A320)
    // FIXME: 05.05.2022 planetypes werden in getAllPlanetypes doppelt ausgegeben!
    public static void main(String[] args) throws Exception {
        var supplier = new Supplier(0, Areas.CGN_LANDESCHNEISE);
        var httpResponse = supplier.fr24get();

        var test = new Test();
        var jsa = test.parseJsonArray(httpResponse);
        var frames = new ArrayDeque<Frame>();
        for (var j : jsa) {
            var frame = new Gson().fromJson(j, Frame.class);
            frames.add(frame);
        }
        frames.stream().forEach((f) -> System.out.println(f.getCallsign() + ", " + f.getPlanetype() + ", " + f.getSrcAirport()));


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
    public ArrayDeque<Frame> deserialize (HttpResponse<String> response) {
        var test = new Test();
        var jsa = test.parseJsonArray(response);
        var frames = new ArrayDeque<Frame>();
        for (var j : jsa) {
            var frame = new Gson().fromJson(j, Frame.class);
            frames.add(frame);
        }
        return frames;
    }

    private JsonArray parseJsonArray (HttpResponse<String> resp) {
        var jsa = new JsonArray();
        var jsons = resp.body().lines()
                .map(this::unwrap)
                .map(this::parseJsonObject)
                .toList();
        jsons.stream().forEach((o) -> jsa.add(o));
        return jsa;
    }

    //TODO check o.getAsJsonArray für ganzes objekt
    private JsonObject parseJsonObject (String line) {
        var cols = line.split(",");
        var o = new JsonObject();
        if (!line.isBlank()) {
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
        } else if (line.startsWith("}")) {
            return "";
        } else {
            out = line.substring(12);
        }
        return out.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("\"", "");
    }

}
