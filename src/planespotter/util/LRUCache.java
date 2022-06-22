package planespotter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @name LRUCache
 * @author jml04
 * @version 1.0
 * @param <K> is the Key class
 * @param <V> is the Value class
 *
 * Class LRUCache represents a Cache with Least-Recently-Used strategy and any key and value.
 * @see CacheElement
 */
public class LRUCache<K, V> {

    private final Map<K, CacheElement<V>> cache;
    private final int maxSize;

    public LRUCache(final int maxElements) {
        this.cache = new LinkedHashMap<>(maxElements); // TODO ist was ist hier die beste Collection??
        this.maxSize = maxElements;
    }

    public boolean isFull() {
        return this.cache.size() == this.maxSize;
    }

    public boolean isAlmostFull() {
        return this.cache.size() < this.maxSize - MathUtils.divide(maxSize, 4);
    }

    public synchronized boolean put(K key, V source) {
        if (this.isFull()) {
            if (!this.removeLeastRecentlyUsed()) {
                System.out.println("Cache is full of Seniors!");
                return false;
            }
        }
        this.cache.put(key, new CacheElement<>(source));
        return true;
    }

    // TODO: 20.06.2022 womit soll ein cache object identifiziert werden? String name?
    public synchronized V get(K key) {
        var cacheElement = this.cache.getOrDefault(key, null);
        if (cacheElement != null) {
            if (cacheElement.incrementUseCount() % 4 == 0) {
                cacheElement.incrementGeneration();
            }
            this.cache.replace(key, cacheElement);
            return cacheElement.getSource();
        }
        return null;
    }

    public V getOrDefault(K key, V orElse) {
        var get = this.get(key);
        return (get != null) ? get : orElse;
    }

    private synchronized boolean removeLeastRecentlyUsed() {
        var entrySet = this.cache.entrySet();
        if (entrySet.isEmpty()) {
            return false;
        }
        var minUseCount = new AtomicInteger(Integer.MAX_VALUE);
        var success = new AtomicBoolean(false);
        var key = new AtomicReference<K>();

        //CacheElement<V> value;
        //int useCount;

        // TODO: 22.06.2022 test if parallelStream is needed!!
        // replaced for with parallelStream.forEach
        entrySet.parallelStream()
                .forEach(entry -> {
            var value = entry.getValue();
            int useCount = value.getUseCount();
            if (useCount < minUseCount.get() /*&& value.getGeneration() < 4*/) {
                minUseCount.set(useCount);
                key.set(entry.getKey());
                success.set(true);
            }
        });
        if (key.get() != null) {
            this.remove(key.get());
        }
        return success.get();
    }

    public synchronized void remove(@NotNull K key) {
        this.cache.remove(key);
    }

    public synchronized void remove(@Range(from = 0, to = 4) byte... generations) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * @name CacheElement
     * @version 1.0
     * @param <V> is the Cache Value Class
     *
     * Inner class CacheElement is the Value class for the Cache-HashMap, which contains the Value class V,
     *           that is the actual Value class the user works with, the CacheElement has just a few more values,
     *           where they can be managed in the cache-HashMap
     * @see LRUCache
     */
    private static class CacheElement<V> {

        public static final byte BABY = 0,
                                 CHILD = 1,
                                 ADULT = 2,
                                 SENIOR  = 3;

        private final V source;
        private byte generation;
        private int useCount;

        private CacheElement(V source) {
            this.source = source;
            this.generation = 0;
            this.useCount = 0;
        }

        private V getSource() {
            return this.source;
        }

        private byte getGeneration() {
            return this.generation;
        }

        private int incrementUseCount() {
            return this.useCount++;
        }

        public int getUseCount() {
            return this.useCount;
        }

        private void incrementGeneration() {
            if (this.generation < SENIOR) {
                this.generation++;
            }
        }
    }

}
