package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.model.io.FileWizard;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.KeyCheckFailedException;
import planespotter.util.Utilities;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @name ConnectionManager
 * @author jml04
 * @version 1.0
 *
 * @description
 * The {@link ConnectionManager} class represents a Manager, that holds all {@link Connection}s,
 * which are created by the user and contains functions to manage them
 */
public class ConnectionManager {

    // map for all connections with custom as key
    @NotNull private final Map<String, Connection> connections;

    // current selected conn
    @Nullable private Connection selectedConn;

    /**
     * constructs a new {@link ConnectionManager} with an empty connection map
     */
    public ConnectionManager(@Nullable String filename) {
        Map<String, Connection> cons;
        if (filename == null || filename.isBlank()) {
            cons = new HashMap<>(0);
        } else {
            try {
                cons = FileWizard.getFileWizard().readConnections(filename);
            } catch (IOException e) {
                e.printStackTrace();
                cons = new HashMap<>(0);
            }
        }
        this.connections = cons;
        this.selectedConn = null;
    }

    /**
     * adds a {@link Connection} with custom name
     * and {@link URI} to the connection map
     *
     * @param key is the connection name, must be unique
     * @param uri is the {@link URI}, which represents the connection address
     * @throws KeyCheckFailedException if the key is blank or already exists
     */
    // TODO: 08.09.2022 URI check for security (user-input)
    public void add(@NotNull String key, @NotNull URI uri) throws KeyCheckFailedException, IllegalInputException {
        checkKey(key);
        Utilities.checkUri(uri);
        Connection conn = new Connection(key, uri, false);
        connections.put(conn.name, conn);
    }

    /**
     * adds a {@link Connection} with custom name
     * and URI-String where an {@link URI} is created from,
     * to the connection map
     *
     * @param key is the connection name, must be unique
     * @param uri is the URI-Strings (converted to URI here), which represents the connection address
     * @throws KeyCheckFailedException if the key is blank or already exists
     */
    public void add(@NotNull String key, @NotNull String uri) throws KeyCheckFailedException, IllegalInputException {
        add(key, URI.create(uri));
    }

    /**
     * adds a new {@link Connection} to the connections map
     *
     * @param key is the connection key, must be unique
     * @param host is the connection host, must not be blank
     * @param port is the connection port, must not be blank
     * @param path is the connection path, optional
     * @throws KeyCheckFailedException if the key is not unique
     * @throws IllegalInputException if the {@link URI} is illegal
     */
    public void add(@NotNull String key, @NotNull String host, @NotNull String port, @Nullable String path) throws KeyCheckFailedException, IllegalInputException {
        String uri = Utilities.createURI("http", host, port, path, null, null);
        add(key,  uri);
    }

    /**
     * gets a {@link Connection} from the connection map by key
     *
     * @param key is the key {@link String}
     * @return Connection with the specified key or null, if no {@link Connection} exists for this key
     */
    @Nullable
    public Connection get(@NotNull String key) {
        return connections.get(key);
    }

    /**
     * sets the current selected {@link Connection}
     *
     * @param selected is the current selected {@link Connection}, nullable
     */
    public void setSelectedConn(@Nullable String selected) {
        selectedConn = selected == null ? null : get(selected);
    }

    /**
     * removes a {@link Connection} from the connection map
     *
     * @param key is the key to remove
     */
    public void remove(@NotNull String key) {
        connections.remove(key);
    }

    /**
     * checks a key {@link String} and throws if it's not unique
     *
     * @param key is the key {@link String} to check
     * @throws KeyCheckFailedException if the key is not unique
     */
    private void checkKey(@NotNull String key) throws KeyCheckFailedException {
        if (key.isBlank()) {
            throw new KeyCheckFailedException("Connection name must not be blank!");
        } else if (connections.containsKey(key)) {
            throw new KeyCheckFailedException("Connection name already exists, please choose another name!");
        }
    }

    /**
     * disconnects all connections
     */
    public void disconnectAll() {
        getConnections().forEach(conn -> conn.setConnected(false));
    }

    /**
     * getter for all {@link Connection} in a {@link Collection}
     *
     * @return {@link Collection} of {@link Connection}s, the connection map values
     */
    @NotNull
    public Collection<Connection> getConnections() {
        return connections.values();
    }

    /**
     * getter for the current selected {@link Connection}
     *
     * @return the current selected {@link Connection} or null if there is no
     */
    @Nullable
    public Connection getSelectedConn() {
        return selectedConn;
    }

    /**
     * @name Connection
     * @version 1.0
     *
     * @description
     * The connection class represents a Connection with custom name and {@link URI}
     */
    public static class Connection {

        // connection name (unique)
        @NotNull public final String name;

        // connection URI
        @NotNull public final URI uri;

        // 'mix with Fr24-Data' flag
        private boolean mixWithFr24;

        // 'connected' flag
        private transient boolean connected;

        /**
         * constructs a new {@link Connection}
         *
         * @param name is the connection name
         * @param uri is the conection URI {@link String}
         * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
         */
        public Connection(@NotNull String name, @NotNull String uri, boolean mixWithFr24) {
            this(name, URI.create(uri), false, mixWithFr24);
        }

        /**
         * constructs a new {@link Connection}
         *
         * @param name is the connection name
         * @param uri is the conection URI
         * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
         */
        public Connection(@NotNull String name, @NotNull URI uri, boolean mixWithFr24) {
            this(name, uri, false, mixWithFr24);
        }

        /**
         * constructs a new {@link Connection}
         *
         * @param name is the connection name
         * @param uri is the conection URI
         * @param connected indicates if the {@link Connection} should be connected directly, usually false
         * @param mixWithFr24 indicates if the ADSB data should be mixed with Fr24 data
         */
        public Connection(@NotNull String name, @NotNull URI uri, boolean connected, boolean mixWithFr24) {
            this.name = name;
            this.uri = uri;
            this.connected = connected;
            this.mixWithFr24 = mixWithFr24;
        }

        /**
         * sets a {@link Connection} connected
         *
         * @param c indicates if the {@link Connection} should be connected
         */
        public void setConnected(boolean c) {
            connected = c;
        }

        /**
         * getter for 'connected' flag
         *
         * @return true if this {@link Connection} is connected
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
}
