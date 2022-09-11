package planespotter.model.nio;

import com.google.gson.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import planespotter.dataclasses.Fr24Frame;
import planespotter.throwables.Fr24Exception;
import planespotter.throwables.InvalidDataException;

import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Stream;

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
public class Fr24Deserializer implements Deserializer<HttpResponse<String>> {

    @NotNull private static final Gson gson = new Gson();

    // filter manager, contains all data filters
    @NotNull private FilterManager filterManager;

    /**
     * Fr24-Deserializer-constructor
     */
    public Fr24Deserializer() {
        this.filterManager = new FilterManager();
    }

    /**
     * sets filter-expressions for deserializer
     *
     * @param filter are the expressions to set as filters
     */
    public void setFilter(@NotNull String... filter) {
        filterManager.set(filter);
    }

    /**
     * getter for {@link FilterManager} object which contains filters and filter-functions
     *
     * @return the deserializer's {@link FilterManager} object
     */
    @NotNull
    public FilterManager getFilters() {
        return filterManager;
    }

    /**
     * deserializes incoming {@link HttpResponse} that contains Fr24-Data
     * from json to {@link Fr24Frame}-{@link ArrayDeque},
     * now improved with a {@link JsonParser} and a faster stream
     * that filters the {@link JsonObject}-entries for valid frames
     * and maps them into frames
     *
     * @param response is the HttpResponse to deserialize
     * @return ArrayDeque of deserialized Frames
     */
    @Override
    @NotNull
    public Stream<Fr24Frame> deserialize(@NotNull HttpResponse<String> response) {
        JsonElement element = JsonParser.parseString(response.body());
        if (element instanceof JsonObject obj) {
            return obj.entrySet()
                    .stream()
                    .filter(e -> {
                        String key = e.getKey();
                        JsonElement value = e.getValue();
                        boolean unnecessary = key.equals("full_count") || key.equals("version");
                        return !unnecessary && filterBy(value.toString(), filterManager.getFilters());
                    })
                    // Fr24 is using a JsonArray of values here instead of a JsonObject so we have to
                    // parse the weird JsonArray to JsonObject here to make it readable for Gson.fromJsonm,
                    .map(e -> {
                        try {
                            return gson.fromJson(e.getValue(), Fr24Frame.class);
                        } catch (JsonSyntaxException syntax) {
                            JsonObject jo = parseJsonObject(e.getValue().toString());
                            return gson.fromJson(jo, Fr24Frame.class);
                        }
                    });
        }
        throw new Fr24Exception("Input Json is invalid, check request and response!");
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
    private boolean filterBy(@NotNull String input, @Nullable List<String> filter) {
        // checking for null filter
        if (filter == null || filter.size() == 0) {
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
        line = line.replaceAll("\"", "")
                   .replaceAll("\\[", "")
                   .replaceAll("]", "");
        String[] cols = line.split(",");
        JsonObject o = new JsonObject();
        try {
            o.addProperty("icaoaddr", cols[0] == null ? "NONE" : cols[0]);
            o.addProperty("lat", parseOrElse(cols[1], 0.0));
            o.addProperty("lon", parseOrElse(cols[2], 0.0));
            o.addProperty("heading", parseOrElse(cols[3], -1));
            o.addProperty("altitude", parseOrElse(cols[4], -1));
            o.addProperty("groundspeed", parseOrElse(cols[5], -1));
            o.addProperty("squawk", parseOrElse(cols[6], 40401));
            o.addProperty("tailnumber", cols[7]);
            o.addProperty("planetype", cols[8]);
            o.addProperty("registration", cols[9]);
            o.addProperty("timestamp", parseOrElse(cols[10], -1L));
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
        try {
            if (orElse instanceof Integer) {
                return notBlank ? Integer.parseInt(toParse) : orElse;
            } else if (orElse instanceof Long) {
                return notBlank ? Long.parseLong(toParse) : orElse;
            } else if (orElse instanceof Double) {
                return notBlank ? Double.parseDouble(toParse) : orElse;
            }
        } catch (NumberFormatException ignored) {
        }
        return orElse;
    }

}
