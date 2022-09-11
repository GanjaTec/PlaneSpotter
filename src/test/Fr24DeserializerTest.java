package test;

import org.junit.jupiter.api.Test;
import planespotter.constants.Areas;
import planespotter.dataclasses.Fr24Frame;
import planespotter.model.nio.Fr24Deserializer;
import planespotter.model.nio.Fr24Supplier;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Fr24DeserializerTest {

    @Test
    void setFilter() {
        Fr24Deserializer deserializer = new Fr24Deserializer();
        String[] testInput = {"DUKE", "FORTE"};
        deserializer.setFilter(testInput);
        String[] strings = deserializer.getFilters()
                .stream()
                .toArray(String[]::new);
        assertArrayEquals(strings, testInput);

    }

    @Test
    void deserialize() {
        assertDoesNotThrow(() -> {
            // testing deserialize of a HttpResponse
            HttpResponse<String> response;
            try {
                response = new Fr24Supplier(0, Areas.CGN_LANDESCHNEISE).sendRequest(2);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new Error("Assertion failed because of the HttpResponse!", e);
            }
            Fr24Deserializer deserializer = new Fr24Deserializer();
            Stream<Fr24Frame> data = deserializer.deserialize(response);
            if (data.findAny().isEmpty()) {
                throw new Error("Assertion failed because of empty data Deque!");
            }
        });
    }
}