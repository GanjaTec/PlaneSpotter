package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.ModeSReply;
import planespotter.dataclasses.Frame;
import planespotter.throwables.InvalidDataException;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@TestOnly
public class ProtoDeserializer implements AbstractDeserializer<String> {

    @Override
    @NotNull
    public Stream<? extends Frame> deserialize(@NotNull String rawData) {
        ModeSDecoder decoder = new ModeSDecoder();
        try {
            ModeSReply reply = decoder.decode(rawData);
            System.out.println(reply.toString());
            System.out.println(Arrays.toString(reply.getIcao24()));
        } catch (BadFormatException | UnspecifiedFormatError e) {
            throw new InvalidDataException("Couldn't decode data, check input String!", e);
        }
        throw new UnsupportedOperationException();
    }
}
