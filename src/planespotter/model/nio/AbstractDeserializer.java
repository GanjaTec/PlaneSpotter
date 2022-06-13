package planespotter.model.nio;

import planespotter.dataclasses.Frame;

import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * @name AbstractDeserializer
 * @author jml04
 * @version 1.0
 *
 * Interface AbstractDeserializer repsesents an abstract Deserializer which has one deserialize-Method
 * @see Fr24Deserializer
 * @see planespotter.model.nio.Deserializer
 * for implementations
 * @indev
 */
public interface AbstractDeserializer {

    // TODO: 11.06.2022 change to Type paremeter sth. like <F extends AbstractFrame> or sth like this
    Collection<? extends Frame> deserialize(HttpResponse<String> response);
}
