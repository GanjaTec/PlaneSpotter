package planespotter.model.nio;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import planespotter.dataclasses.Fr24Frame;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;

import java.net.http.HttpResponse;
import java.util.*;

/**
 * @name Fr24Deserializer
 * @author jml04
 * @version 1.0
 *
 * @description
 * Class ProtoDeserializer represents a Deserializer for Fr24-data.
 * It converts a HttpResponse to a Collection of Frames,
 * which can be used to create further dataclasses like flights, planes, etc...
 */
public class Fr24Deserializer implements AbstractDeserializer<HttpResponse<String>> {
    // filter expressions, may be null or empty
    private String[] filter;

    /**
     * Fr24-Deserializer-constructor
     */
    public Fr24Deserializer() {
        this.filter = null;
    }

    /**
     * sets filter-expressions for deserializer
     *
     * @param filter are the expressions to set as filters
     */
    public void setFilter(@Nullable String... filter) {
        this.filter = filter;
    }

    /**
     * deserializes incoming http response
     * from json to Fr24Frame ArrayDeque
     *
     * @param response is the HttpResponse to deserialize
     * @return ArrayDeque of deserialized Frames
     *
     * @indev
     */
    @Override
    @NotNull
    public Deque<Fr24Frame> deserialize(@NotNull HttpResponse<String> response) {
        JsonArray jsa = this.parseJsonArray(response, this.filter);
        ArrayDeque<Fr24Frame> frames = new ArrayDeque<>();
        Iterator<JsonElement> it = jsa.iterator();
        Gson gson = new Gson();
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
    @NotNull
    private JsonArray parseJsonArray(@NotNull HttpResponse<String> resp, @Nullable String... filter) {
        JsonArray jsa = new JsonArray(); // creating new JsonArray
        resp.body().lines() // getting stream of body-lines
                .filter(s -> s.length() != 1 && filterBy(s, filter)) // filtering out valid and filter-allowed lines
                .map(this::unwrap) // unwrapping line strings from certain expressions
                .map(this::parseJsonObject) // parsing lines to JsonObjects
                .forEach(jsa::add); // adding JsonObject to JsonArray
        return jsa;
    }

    /**
     * filters an input string by filters
     *
     * @param input is the input string which can be null
     * @param filter are the (nullable) expressions to filter for
     * @return true if the input string contains one of the filter expressions or,
     *              if the filter array is null or empty.
     *         false if no filter-match was found in the input string
     */
    private boolean filterBy(@NotNull String input, @Nullable String... filter) {
        // checking for null filter
        if (filter == null) {
            return true;
        }
        // going through filters
        for (String f : filter) {
            // checking filter for nonNull and filtering input
            if (f != null && input.contains(f)) {
                return true;
            }
        }
        // returning false if no match was found in the input string
        return false;
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
            throw new InvalidDataException("line may not be blank!");
        }
        String[] cols = line.split(",");
        JsonObject o = new JsonObject();
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
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) {
            // NumberFormatException occurs when the given area does not contain any data
            // ArrayIndexOutOfBoundsException occurs when the given data is invalid
        }
        return o;
    }

    /**
     * parses a Number-String to a Number, or a given default object
     * to a Number (Integer or Double)
     *
     * @param toParse is the Number-String, must be a single number like: 3 ; 644 ; 5.26 ; 764.363
     * @param orElse is the Number to return, if the toParse-number is blank or invalid
     * @param <N> is the Number type of orElse like Double or Integer
     * @return parsed Number or given defalut Number, if the input String is invalid
     */
    @NotNull
    private <N extends Number> Number parseOrElse(@NotNull String toParse, final N orElse) {
        boolean notBlank = !toParse.isBlank();
        if (orElse instanceof Integer) {
            return notBlank ? Integer.parseInt(toParse) : orElse;
        } else if (orElse instanceof Double) {
            return notBlank ? Double.parseDouble(toParse) : orElse;
        }
        return orElse;
    }

    /**
     * unwraps a String from expressions like '{' and the beginning JSON-expressions,
     * these shouldn't be removed, but Gson::fromJson is not able to work with the direct
     * JSON input Strings (maybe they are invalid, could be a method from Fr24 to make the data structure more complex)
     *
     * @param line is the line String to unwrap
     * @return
     */
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
