package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Frame;

import java.net.http.HttpResponse;
import java.util.Collection;

public class ADSBDeserializer implements AbstractDeserializer<HttpResponse<String>> {

    @Override
    @NotNull
    public Collection<? extends Frame> deserialize(@NotNull HttpResponse<String> data) {
        return null;
    }
}
