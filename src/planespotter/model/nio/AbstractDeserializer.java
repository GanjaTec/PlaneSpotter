package planespotter.model.nio;

import planespotter.dataclasses.Frame;

import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * @name AbstractDeserializer
 * @author jml04
 * @version 1.0
 * @param <D> is the Data-Class that the deserialize-method gets,
 *           for example: ...implements AbstractDeserializer<HttpResponse<String>>
 *                    or: ...implements AbstractDeserializer<String>
 *
 * Interface AbstractDeserializer repsesents an abstract Deserializer which has one deserialize-Method
 * @see Fr24Deserializer
 * @see planespotter.model.nio.Deserializer
 * for implementations
 * @indev
 */
public interface AbstractDeserializer<D> {

    Collection<? extends Frame> deserialize(D data);
}
