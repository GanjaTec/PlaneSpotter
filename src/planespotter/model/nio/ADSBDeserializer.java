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

public class ADSBDeserializer implements AbstractDeserializer<HttpResponse<String>> {

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

            return Stream.iterate(aircrafts.next(), element -> aircrafts.next())
                    .map(element -> {
                        element.getAsJsonObject().addProperty("timestamp", now);
                        return gson.fromJson(element, ADSBFrame.class);
                    })
                    .limit(size);

        }
        throw new InvalidDataException("request data is invalid, please check input!");
    }
}
