package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.model.ExceptionHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class HttpSupplier implements Supplier {

    // static HttpClient instance, fixed thread overhead
    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Nullable private ExceptionHandler exceptionHandler;

    protected HttpSupplier() {
        this(null);
    }

    protected HttpSupplier(@Nullable ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @NotNull
    public abstract HttpResponse<String> sendRequest(int timeoutSec) throws IOException, InterruptedException;

    @Nullable
    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    public void setExceptionHandler(@NotNull ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
