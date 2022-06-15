package planespotter.model.nio;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Fr24Frame;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;

import java.net.http.HttpResponse;
import java.util.*;

/**
 * @name ProtoDeserializer
 * @author jml04
 * @version 1.0
 *
 * Class ProtoDeserializer represents a Deserializer for Fr24-data.
 * It converts the HttpResponse to a Collection of Frames,
 * which can be used to create further dataclasses like flights, planes, etc...
 */
public class Fr24Deserializer implements AbstractDeserializer {

    /**
     * deserializes incoming http response
     * from json to fr24Frame ArrayDeque
     *
     * @param response is the HttpResponse to deserialize
     * @return ArrayDeque of deserialized Frames
     *
     * @indev
     */
    @Override
    public Deque<Fr24Frame> deserialize(HttpResponse<String> response) {
        var jsa = this.parseJsonArray(response);
        var frames = new ArrayDeque<Fr24Frame>();
        var it = jsa.iterator();
        var gson = new Gson();
        Fr24Frame fr24Frame;
        for (JsonElement j; it.hasNext(); /**/) {
            j = it.next();
            fr24Frame = gson.fromJson(j, Fr24Frame.class);
            frames.add(fr24Frame);
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
        } catch (NumberFormatException nfe) {
            throw new InvalidDataException("Invalid data to deserialize, the given Area is probably out of range!");
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
