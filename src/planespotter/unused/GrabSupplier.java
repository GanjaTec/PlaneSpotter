package planespotter.unused;

import planespotter.model.nio.Supplier;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;

@Deprecated(since = "Not allowed", forRemoval = true)
public class GrabSupplier implements Supplier {

    /**
     * "Copyright (c) 2014-2022 Flightradar24 AB. All rights reserved.
     * The contents of this file ['dukeRequest0'] and all derived data are the property of Flightradar24 AB for use
     * exclusively by its products and applications. Using, modifying or redistributing the data without
     * the prior written permission of Flightradar24 AB is not allowed and may result in prosecutions."
     */

    // getting duke directly from fr24-database (not allowed)
    private final String dukeRequest0 = "https://api.flightradar24.com/common/v1/search.json?fetchBy=flight&query=1I145";

    @Override
    public void supply() {
        try {
            var response = this.sendRequest();
            response.body()
                    .lines()
                    .forEach(l -> Arrays.stream(l.split(","))
                            .forEach(System.out::println));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse<String> sendRequest()
            throws IOException, InterruptedException {

        throw new UnsupportedOperationException("Not allowed!");
    }
}
