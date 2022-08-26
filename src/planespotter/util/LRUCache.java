package planespotter.util;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import planespotter.util.math.MathUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @name LRUCache
 * @author jml04
 * @version 1.0
 * @param <K> is the Key class
 * @param <V> is the Value class
 *
 * @description
 * Class LRUCache represents a Cache with Least-Recently-Used strategy and any key and value.
 * The LRU-algorithm is not finished, should probably be improved in the future.
 * @see planespotter.util.LRUCache.CacheElement
 * @see java.util.LinkedHashMap
 */
public class LRUCache<K, V> {

    // LinkedHashMap as cache collection
    @NotNull private final Map<K, CacheElement<V>> cache;

    // max cache elements
    private final int maxSize;

    // if this flag is true, least recently used elements are only removed if the cache is full,
    // else they are removed if the cache is almost full (could be improved (dynamic))
    private final boolean onlyRemoveIfFull;

    /**
     * constructor for {@link LRUCache}
     *
     * @param maxElements is the maximum element count in the cache,
     *                    cannot be changed
     * @param onlyRemoveIfFull indicates if the LRU-values should only
     *                         be removed when the cache is full,
     *                         if false, LRU-values are removed
     *                         when the cache is almost full
     */
    public LRUCache(final int maxElements, boolean onlyRemoveIfFull) {
        this.cache = new LinkedHashMap<>(maxElements);
        this.maxSize = maxElements;
        this.onlyRemoveIfFull = onlyRemoveIfFull;
    }

    /**
     * indicates if the cache is full of elements
     *
     * @return true if the cache size is equals the
     *              max element count, else false
     */
    public boolean isFull() {
        return this.cache.size() == this.maxSize;
    }

    /**
     * indicates if the cache is almost full of elements
     *
     * @return true if the cache size is bigger than (max size - (max size / 5))
     */
    public boolean isAlmostFull() {
        return this.cache.size() > this.maxSize - MathUtils.divide(maxSize, 5);
    }

    /**
     * puts a value with a key into the cache
     *
     * @param key is the key of type K
     * @param source is the value of type V
     * @return true if the new element was put in the cache successfully, else false
     */
    public synchronized boolean put(@NotNull K key, @NotNull V source) {
        if ((onlyRemoveIfFull && isFull()) || (!onlyRemoveIfFull && isAlmostFull())) {
            if (!removeLeastRecentlyUsed1(false)) {
                System.out.println("Cache is full of Seniors!");
                return false;
            }
        }
        this.cache.put(key, new CacheElement<>(source));
        return true;
    }

    /**
     * returns a value from the cache by key, if one exists
     *
     * @param key is the key paired with the value
     * @return the value paired with the key or null, if the key was not found
     */
    @Nullable
    public synchronized V get(@NotNull K key) {
        CacheElement<V> cacheElement = this.cache.getOrDefault(key, null);
        if (cacheElement != null) {
            if (++cacheElement.useCount % 4 == 0) {
                cacheElement.incrementGeneration();
            }
            cacheElement.timestamp = Time.nowMillis();
            this.cache.replace(key, cacheElement);
            return cacheElement.getSource();
        }
        return null;
    }

    /**
     * returns a value from the cache by key, if one exists
     *
     * @param key is the key paired with the value
     * @param orElse is the default value which is returned when the key was not found
     * @return the value paired with the key or the default value, if the key was not found
     */
    @NotNull
    public V getOrDefault(@NotNull K key, @NotNull V orElse) {
        V get = this.get(key);
        return (get != null) ? get : orElse;
    }

    /**
     * tries to remove the least recently used cache entry
     * by searching for the {@link CacheElement} with the oldest timestamp and
     * a low use-count, then removing the oldest unused {@link CacheElement},
     * if there is one
     *
     * @return true if an element was removed, else false
     */
    private synchronized boolean removeLeastRecentlyUsed1(boolean secondTry) {
        if (this.cache.isEmpty()) {
            return false;
        }
        Set<Map.Entry<K, CacheElement<V>>> entrySet = this.cache.entrySet();
        long lruTimestamp = Long.MAX_VALUE,
             currentTimestamp;
        K key = null;
        CacheElement<V> value;

        int avgUseCount = 0, // testing 0 as start value
                counter = 1; // is the divisor, starts at 1, not 0

        for (Map.Entry<K, CacheElement<V>> entry : entrySet) {
            value = entry.getValue();
            avgUseCount = MathUtils.divide(avgUseCount + value.useCount, counter++); //
            currentTimestamp = value.timestamp;
            if (currentTimestamp < lruTimestamp && value.useCount < avgUseCount) {
                lruTimestamp = currentTimestamp;
                key = entry.getKey();
            }
        }
        if (key == null) {
            if (secondTry) {
                return this.removeLeastRecentlyUsed0();
            }
            this.remove(false, CacheElement.BABY, CacheElement.CHILD);
            return false;
        }
        this.remove(key);
        return true;
    }

    /**
     * removes the least recently used {@link CacheElement} from
     * the cache, if there is one. Goes just by use count and
     * not by timestamp
     *
     * @return true if an element was removed, else false
     */
    private synchronized boolean removeLeastRecentlyUsed0() {
        Set<Map.Entry<K, CacheElement<V>>> entrySet = this.cache.entrySet();
        if (entrySet.isEmpty()) {
            return false;
        }
        AtomicInteger minUseCount = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<K> key = new AtomicReference<>();

        // TODO: 22.06.2022 test if parallelStream is needed!!
        // replaced for with parallelStream.forEach
        entrySet.parallelStream()
                .forEach(entry -> {
                    CacheElement<V> value = entry.getValue();
                    int useCount = value.getUseCount();
                    if (useCount < minUseCount.get() /*&& value.getGeneration() < 4*/) {
                        minUseCount.set(useCount);
                        key.set(entry.getKey());
                    }
        });
        if (key.get() == null) {
            return false;
        }
        this.remove(key.get());
        return true;
    }

    /**
     * removes a {@link CacheElement} from the cache, if it is present
     *
     * @param key is the key for the {@link CacheElement} to remove
     */
    public synchronized void remove(@NotNull K key) {
        this.cache.remove(key);
    }

    /**
     * removes {@link CacheElement}s by generations
     *
     * @param parallel indicates if the method should work parallel
     * @param generations are the generations to be removed
     */
    public synchronized void remove(boolean parallel, @Range(from = 0, to = 4) byte... generations) {
        Set<Map.Entry<K, CacheElement<V>>> entrySet = this.cache.entrySet();
        if (entrySet.isEmpty()) {
            return;
        }
        try (Stream<Map.Entry<K, CacheElement<V>>> entryStream = parallel ? entrySet.parallelStream() : entrySet.stream()) {
            entryStream // stream operations
                    .filter(e -> ArrayUtils.contains(generations, e.getValue().generation))
                    .map(Map.Entry::getKey)
                    .forEach(this::remove);
        }

    }

    /**
     * @name CacheElement
     * @version 1.0
     * @param <V> is the Cache Value Class
     *
     * @description
     * Inner class CacheElement is the Value class for the Cache-HashMap, which contains the Value class V,
     *           that is the actual Value class the user works with, the CacheElement has just a few more values,
     *           where they can be managed in the cache-HashMap
     * @see LRUCache
     */
    private static class CacheElement<V> {

        // generation byte constants
        public static final byte BABY = 0,
                                 CHILD = 1,
                                 ADULT = 2,
                                 SENIOR  = 3;

        // source object / value
        @NotNull private final V source;

        // generation
        private byte generation;

        // use-count, incremented in LRUCache.get(...)
        private int useCount;

        // timestamp from last use
        private long timestamp;

        /**
         * constructor for {@link CacheElement}
         *
         * @param source is the source object (value) of any type
         */
        private CacheElement(@NotNull V source) {
            this.source = source;
            this.generation = 0;
            this.useCount = 1;
            this.timestamp = Time.nowMillis();
        }

        /**
         * getter for the source / value object of this {@link CacheElement}
         *
         * @return the source / value object
         */
        @NotNull
        private V getSource() {
            return this.source;
        }

        /**
         * getter for the use-count of this {@link CacheElement}
         *
         * @return the use-count as an int
         */
        public int getUseCount() {
            return this.useCount;
        }

        /**
         * getter for the last-use timestamp of this {@link CacheElement}
         *
         * @return last timestamp as long
         */
        public long getTimestamp() {
            return this.timestamp;
        }

        /**
         * increments the generation of this {@link CacheElement},
         * if it is not already SENIOR
         */
        private void incrementGeneration() {
            if (this.generation < SENIOR) {
                this.generation++;
            }
        }
    }

}
