package planespotter.dataclasses;

import org.jetbrains.annotations.NotNull;
import planespotter.model.ConnectionManager;

import java.net.URI;

/**
 * @version 1.0
 * @name Connection
 * @description The connection class represents a Connection with custom name and {@link URI}
 */
public class ConnectionSource {

    // connection name (unique)
    @NotNull
    public final String name;

    // connection URI
    @NotNull
    public final URI uri;

    // 'mix with Fr24-Data' flag
    private boolean mixWithFr24;

    // 'connected' flag
    private transient boolean connected;

    /**
     * constructs a new {@link ConnectionSource}
     *
     * @param name        is the connection name
     * @param uri         is the conection URI {@link String}
     * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
     */
    public ConnectionSource(@NotNull String name, @NotNull String uri, boolean mixWithFr24) {
        this(name, URI.create(uri), false, mixWithFr24);
    }

    /**
     * constructs a new {@link ConnectionSource}
     *
     * @param name        is the connection name
     * @param uri         is the conection URI
     * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
     */
    public ConnectionSource(@NotNull String name, @NotNull URI uri, boolean mixWithFr24) {
        this(name, uri, false, mixWithFr24);
    }

    /**
     * constructs a new {@link ConnectionSource}
     *
     * @param name        is the connection name
     * @param uri         is the conection URI
     * @param connected   indicates if the {@link ConnectionSource} should be connected directly, usually false
     * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
     */
    public ConnectionSource(@NotNull String name, @NotNull URI uri, boolean connected, boolean mixWithFr24) {
        this.name = name;
        this.uri = uri;
        this.connected = connected;
        this.mixWithFr24 = mixWithFr24;
    }

    /**
     * sets a {@link ConnectionSource} connected
     *
     * @param c indicates if the {@link ConnectionSource} should be connected
     */
    public void setConnected(boolean c) {
        connected = c;
    }

    /**
     * getter for 'connected' flag
     *
     * @return true if this {@link ConnectionSource} is connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * getter for 'mix with Fr24 data' flag
     *
     * @return true if 'mix with Fr24 data' is enabled, else false
     */
    public boolean isMixWithFr24() {
        return mixWithFr24;
    }

    /**
     * sets the 'mix with Fr24 data' flag
     *
     * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
     */
    public void setMixWithFr24(boolean mixWithFr24) {
        this.mixWithFr24 = mixWithFr24;
    }

}
