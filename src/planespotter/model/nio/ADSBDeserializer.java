package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Frame;

import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.stream.Stream;

public class ADSBDeserializer implements AbstractDeserializer<HttpResponse<String>> {

    @Override
    @NotNull
    public Stream<? extends Frame> deserialize(@NotNull HttpResponse<String> data) {
        return null;
    }
}
