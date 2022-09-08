package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.model.io.FileWizard;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.KeyCheckFailedException;
import planespotter.util.Utilities;

import java.io.IOException;
import java.io.Serializable;
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

    public void add(@NotNull String key, @NotNull String host, @NotNull String port, @Nullable String path) throws KeyCheckFailedException, IllegalInputException {
        add(key, "http://" + host + ":" + port + "/" + (path == null ? "" : path));
    }

    @Nullable
    public Connection get(@NotNull String key) {
        return connections.get(key);
    }

    public void setSelectedConn(@Nullable String selected) {
        selectedConn = selected == null ? null : get(selected);
    }

    public void remove(@NotNull String key) {
        connections.remove(key);
    }

    private void checkKey(@NotNull String key) throws KeyCheckFailedException {
        if (key.isBlank()) {
            throw new KeyCheckFailedException("Connection name must not be blank!");
        } else if (connections.containsKey(key)) {
            throw new KeyCheckFailedException("Connection name already exists, please choose another name!");
        }
    }

    @NotNull
    public Collection<Connection> getConnections() {
        return connections.values();
    }

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

        @NotNull public final String name;

        @NotNull public final URI uri;

        private transient boolean connected;

        public Connection(@NotNull String name, @NotNull String uri) {
            this(name, URI.create(uri), false);
        }

        public Connection(@NotNull String name, @NotNull URI uri, boolean connected) {
            this.name = name;
            this.uri = uri;
            this.connected = connected;
        }

        public void setConnected(boolean c) {
            connected = c;
        }

        public boolean isConnected() {
            return connected;
        }
    }
}
