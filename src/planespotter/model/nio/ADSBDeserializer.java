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
import java.util.stream.Stream;

public class ADSBDeserializer implements AbstractDeserializer<HttpResponse<String>> {

    @Override
    @NotNull
    public Stream<? extends Frame> deserialize(@NotNull HttpResponse<String> data) {
        String body = data.body();
        JsonElement requestElement = JsonParser.parseString(body);
        if (requestElement instanceof JsonObject jsonObject) {
            Gson gson = new Gson();
            long now = jsonObject.getAsJsonPrimitive("now").getAsLong();
            Deque<ADSBFrame> frames = new ArrayDeque<>();
            jsonObject.getAsJsonArray("aircraft")
                    .iterator()
                    .forEachRemaining(e -> {
                        JsonObject jo = e.getAsJsonObject();
                        jo.addProperty("timestamp", now);

                        //System.out.println(jo);
                        ADSBFrame adsbFrame = gson.fromJson(e, ADSBFrame.class);
                        frames.add(adsbFrame);
                    });
            return Stream.iterate(frames.poll(), f -> frames.poll());
        }
        throw new InvalidDataException("request data is invalid, please check input!");
    }
}
