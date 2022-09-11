package planespotter.model.nio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.model.ExceptionHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @name HttpSupplier
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link HttpSupplier} represents a {@link Supplier} specification for Http data
 * with an abstract method for sending a request, has a static {@link HttpClient} instance
 * which is reused on every request to minimize thread count and overhead
 * @see Supplier
 * @see ADSBSupplier
 * @see Fr24Supplier
 */
public abstract class HttpSupplier implements Supplier {

    // static HttpClient instance, fixed thread overhead
    protected static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // nullable exception handler instance
    @Nullable private ExceptionHandler exceptionHandler;

    /**
     * constructs a new {@link HttpSupplier} with no {@link ExceptionHandler}
     */
    protected HttpSupplier() {
        this(null);
    }

    /**
     * constructs a new {@link HttpSupplier} with given {@link ExceptionHandler}
     *
     * @param exceptionHandler is the {@link ExceptionHandler} instance for exception handling
     */
    protected HttpSupplier(@Nullable ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * sends a {@link HttpRequest} to the (by the Supplier specified) {@link URI}
     * to receive a {@link HttpRequest} with the data
     *
     * @param timeoutSec is the request timeout in seconds
     * @return {@link HttpResponse} containing the requested data
     * @throws IOException if an I/O error occurs while sending the request
     * @throws InterruptedException if the sending is interrupted
     */
    @NotNull
    public abstract HttpResponse<String> sendRequest(int timeoutSec) throws IOException, InterruptedException;

    /**
     * getter for the {@link ExceptionHandler} instance
     *
     * @return the {@link ExceptionHandler} instance, may be null
     */
    @Nullable
    public final ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    /**
     * sets the {@link ExceptionHandler} of this {@link HttpSupplier}
     *
     * @param exceptionHandler is the new {@link ExceptionHandler} to be set
     */
    public final void setExceptionHandler(@NotNull ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
