package planespotter.model.nio;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.ADSBFrame;
import planespotter.dataclasses.Frame;
import planespotter.throwables.InvalidDataException;

import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @name ADSBDeserializer
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link ADSBDeserializer} is a Deserializer which is able to deserialize an
 * ADSB-{@link HttpResponse} ,containing ADSB data collected with an antenna (RTL-SDR),
 * to simple {@link ADSBFrame}s which are almost normal {@link Frame}s with a few additional fields
 * @see ADSBFrame
 * @see HttpResponse
 * @see JsonParser
 */
public class ADSBDeserializer implements AbstractDeserializer<HttpResponse<String>> {

    /**
     * deserializes a {@link HttpResponse} to a {@link Stream} of {@link ADSBFrame}s
     * by parsing the response body (JSON) to a {@link JsonElement} using the {@link JsonParser}.
     * Then going through the 'aircraft' array to deserialize each aircraft-{@link JsonObject}.
     *
     * @param data is the data to deserialize, can be of any type
     * @return {@link Stream} of deserialized {@link ADSBFrame}s
     */
    @Override
    @NotNull
    public Stream<ADSBFrame> deserialize(@NotNull HttpResponse<String> data) {
        // parsing response body to java-JsonElement
        JsonElement requestElement = JsonParser.parseString(data.body());

        if (requestElement instanceof JsonObject jsonObject) {
            Gson gson = new Gson();
            long now = jsonObject.getAsJsonPrimitive("now").getAsLong();
            JsonArray jsonArray = jsonObject.getAsJsonArray("aircraft");
            int size = jsonArray.size();
            Iterator<JsonElement> aircrafts = jsonArray.iterator();
            // iterating over jsonArray objects
            return Stream.iterate(aircrafts.next(), element -> aircrafts.next())
                    .map(element -> parseElement(gson, element, now))
                    .limit(size);

        }
        throw new InvalidDataException("request data is invalid, please check input!");
    }

    /**
     * parses a {@link JsonElement} to {@link ADSBFrame} by adding the
     * 'timestamp' property to each element and using {@link Gson}.fromJson
     * to convert the element into an {@link ADSBFrame}.
     *
     * @param gson is the {@link Gson} class which contains the convert-operation
     * @param element is the {@link JsonElement} to parse
     * @param now is the now-timestamp, extracted from the JSON before
     * @return {@link ADSBFrame}, parsed from {@link JsonElement}
     */
    @NotNull
    private ADSBFrame parseElement(@NotNull Gson gson, @NotNull JsonElement element, long now) {
        JsonObject jsonObj = element.getAsJsonObject();
        float seen;
        if ((seen = jsonObj.get("seen").getAsFloat()) >= 1) {
            now -= seen;
        }
        jsonObj.addProperty("timestamp", now);
        System.out.println(element.toString());
        return gson.fromJson(element, ADSBFrame.class);
    }
}
