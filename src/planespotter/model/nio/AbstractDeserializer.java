package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import planespotter.dataclasses.Frame;
import planespotter.unused.Deserializer;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @name AbstractDeserializer
 * @author jml04
 * @version 1.0
 * @param <D> is the Data-Class that the deserialize-method gets,
 *
 *           for example: ...implements AbstractDeserializer<HttpResponse<String>>
 *                    or: ...implements AbstractDeserializer<String>
 *
 * @description
 * Interface AbstractDeserializer represents an abstract Deserializer which has one deserialize-Method
 * @see Fr24Deserializer
 * @see Deserializer
 * for implementations
 * @indev
 */
@FunctionalInterface
public interface AbstractDeserializer<D> {

    /**
     * This method deserializes data to a collection of Frame-extending objects.
     * The data can be of any type, it just has to be returned as a collection of Frames.
     * The output type must extend the Frame class, which could be something like {@link planespotter.dataclasses.Fr24Frame}.
     *
     * @param data is the data to deserialize, can be of any type
     * @return a collection of Frame-extending objects
     */
    @NotNull
    Stream<? extends Frame> deserialize(@NotNull D data);
}
