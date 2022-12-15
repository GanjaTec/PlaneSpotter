package planespotter.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import planespotter.dataclasses.ConnectionSource;
import planespotter.model.io.FileWizard;
import planespotter.throwables.IllegalInputException;
import planespotter.throwables.KeyCheckFailedException;
import planespotter.util.Utilities;

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
 * The {@link ConnectionManager} class represents a Manager, that holds all {@link ConnectionSource}s,
 * which are created by the user and contains functions to manage them
 */
public class ConnectionManager {

    // map for all connections with custom as key
    @NotNull private final Map<String, ConnectionSource> connections;

    // current selected conn
    @Nullable private ConnectionSource selectedConn;

    /**
     * constructs a new {@link ConnectionManager} with an empty connection map
     */
    public ConnectionManager(@Nullable String filename) {
        Map<String, ConnectionSource> cons;
        if (filename == null || filename.isBlank()) {
            cons = new HashMap<>(0);
        } else {
            try {
                cons = FileWizard.getFileWizard().readConsJson(filename);
                //cons = FileWizard.getFileWizard().readConnections(filename);
            } catch (Throwable e) { // we don't want an exception in initializer
                e.printStackTrace();
                cons = new HashMap<>(0);
            }
        }
        this.connections = cons;
        this.selectedConn = null;
    }

    /**
     * adds a {@link ConnectionSource} with custom name
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
        ConnectionSource conn = new ConnectionSource(key, uri, false);
        connections.put(conn.name, conn);
    }

    /**
     * adds a {@link ConnectionSource} with custom name
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
     * adds a new {@link ConnectionSource} to the connections map
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
     * gets a {@link ConnectionSource} from the connection map by key
     *
     * @param key is the key {@link String}
     * @return Connection with the specified key or null, if no {@link ConnectionSource} exists for this key
     */
    @Nullable
    public ConnectionSource get(@NotNull String key) {
        return connections.get(key);
    }

    /**
     * sets the current selected {@link ConnectionSource}
     *
     * @param selected is the current selected {@link ConnectionSource}, nullable
     */
    public void setSelectedConn(@Nullable String selected) {
        selectedConn = selected == null ? null : get(selected);
    }

    /**
     * removes a {@link ConnectionSource} from the connection map
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
     * getter for all {@link ConnectionSource} in a {@link Collection}
     *
     * @return {@link Collection} of {@link ConnectionSource}s, the connection map values
     */
    @NotNull
    public Collection<ConnectionSource> getConnections() {
        return connections.values();
    }

    /**
     * getter for the current selected {@link ConnectionSource}
     *
     * @return the current selected {@link ConnectionSource} or null if there is no
     */
    @Nullable
    public ConnectionSource getSelectedConn() {
        return selectedConn;
    }

}
