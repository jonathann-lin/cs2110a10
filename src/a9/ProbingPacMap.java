package a9;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A map with keys of type 'K' and values of type `V`, implemented using a hash table with linear
 * probing.
 */
public class ProbingPacMap<K, V> implements PacMap<K, V> {

    /**
     * Represents an association of a key `key` (of type `K`) with a value `value` (of type `V`).
     */
    private record Entry<K, V>(K key, V value) {

    }

    /**
     * Represents a tombstone. If an entry at index `i` is removed, element `i` will be replaced by
     * a reference to this object. Tombstones count toward the load factor, and are cleared when the
     * hash table is resized.
     */
    @SuppressWarnings("rawtypes")
    private static final Entry TOMBSTONE = new Entry<>(null, null);

    /**
     * The initial capacity of the hash table for new instances of `ProbingPacMap`.
     */
    private static final int INITIAL_CAPACITY = 16;

    /**
     * The maximum load factor (inclusive) that is allowed in the `entries` hash table. If the load
     * factor ever exceeds this maximum, then the hash table length must be immediately doubled to
     * reduce the load factor. Must have `0 < maxLoadFactor < 1`.
     */
    public static final double MAX_LOAD_FACTOR = 0.5;

    /**
     * The probing hash table backing this map. Indices (i.e., buckets) that don't current store an
     * entry (possibly a TOMBSTONE) are `null`. If this map contains an entry with a key whose hash
     * code maps to index `i`, then the (unique) entry containing that key is reachable via linear
     * search starting at index `i` (wrapping around the array if necessary) without encountering
     * `null`.
     */
    private Entry<K, V>[] entries;

    private void assertInv() {
        assert 0 < MAX_LOAD_FACTOR;
        assert MAX_LOAD_FACTOR < 1;
        assert loadFactor() <= MAX_LOAD_FACTOR;
        assert size() >= 0;
    }

    /**
     * Stores current number of keys currently associated with values in this map. In other words,
     * stores the current number of elements in the map. Requires that size >= 0. Requires
     * size/entries.length <= MAX_LOAD_FACTOR.
     */
    private int size;

    private int tombstoneNum;

    /**
     * Create a new empty `ProbingPacMap`.
     */
    @SuppressWarnings("unchecked")
    public ProbingPacMap() {
        entries = new Entry[INITIAL_CAPACITY];
        size = 0;
        tombstoneNum = 0;
    }

    /**
     * Returns the number of keys currently associated with values in this map. Runs in O(1) time.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the current load factor of the hash table backing this map. Runs in O(1) time.
     */
    private double loadFactor() {
        return (double) (size+tombstoneNum) / entries.length;
    }


    /**
     * Returns the hash value of 'key' in 'arr'.
     * Requires that key and arr are not null.
     */
    private int hashValue(K key, Entry<K, V>[] arr) {
        assert key != null;
        assert arr != null;
        return Math.abs(key.hashCode() % arr.length);
    }

    /**
     * Doubles the length of entries and copies over all elements into correct buckets based on
     * their new hash values.
     */

    private void resize() {
        Entry<K, V>[] newEntries = (Entry<K, V>[]) new Entry[entries.length * 2];
        Iterator<K> it = this.iterator();
        while (it.hasNext()) {
            Entry<K, V> e = entries[findEntry(it.next())];
            newEntries[findFreeIndex(e.key, newEntries)] = e;
        }
        entries = newEntries;
        tombstoneNum = 0;
        assertInv();
    }

    /**
     * If `key` is a key in this map, return the index in `entries` for this key. Otherwise, returns
     * the first index of a `null` or tombstone entry in the table at or after the index
     * corresponding to the key's hash code (wrapping around).
     */
    private int findEntry(K key) {
        int startIndex = hashValue(key, entries);
        int firstTombstone = -1;
        for (int i = 0; i < entries.length; i++) {
            int index = (startIndex + i) % entries.length;
            if (entries[index] == null) {
                return firstTombstone == -1 ? index : firstTombstone;
            } else if (entries[index] == TOMBSTONE) {
                if (firstTombstone == -1) {
                    firstTombstone = index;
                }
            } else if (entries[index].key.equals(key)) {
                return index;
            }
        }
        return firstTombstone;//Only triggers if key is not in there and map is full
    }

    @Override
    public boolean containsKey(K key) {
        int index = findEntry(key);
        if (entries[index] == null || entries[index] == TOMBSTONE) {
            return false;
        }
        return entries[index].key.equals(key);
    }


    @Override
    public V get(K key) {
        if (!containsKey(key)) {
            throw new NoSuchElementException();
        }
        return entries[findEntry(key)].value;
    }

    @Override
    public void put(K key, V value) {
        int index = findEntry(key);
        if (entries[index] == TOMBSTONE){
            tombstoneNum --;
            size ++;
        }
        else if(entries[index] == null){
            size ++;
        }
        else{ //replacing an existing thing, so change nothing

        }

        entries[index] = new Entry<>(key, value);
        if (loadFactor() > MAX_LOAD_FACTOR) {
            resize();
        }

        assertInv();
    }

    /**
     * < Returns the first index where key can be inserted in arr. In other words, finds first empty
     * index at or after the hash value of key, with wraparound.
     */
    private int findFreeIndex(K key, Entry<K, V>[] arr) {
        int index = hashValue(key, arr);
        while (arr[index] != null) {
            index = (index + 1) % arr.length;
        }
        return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(K key) {
        if (!containsKey(key)) {
            throw new NoSuchElementException();
        }
        V value = entries[findEntry(key)].value();
        entries[findEntry(key)] = TOMBSTONE;
        size--;
        tombstoneNum++;

        assertInv();


        return value;


    }

    @Override
    public Iterator<K> iterator() {
        return new ProbingPacMapIterator();
    }

    /**
     * An iterator over the keys in this hash table. This map must not be structurally modified
     * while any such iterators are alive.
     */
    private class ProbingPacMapIterator implements Iterator<K> {

        /**
         * The index of the entry in `entries` containing the next value to yield, or
         * `entries.length` if all values have been yielded.
         */
        private int iNext;

        /**
         * Create a new iterator over this dictionary's keys.
         */
        ProbingPacMapIterator() {
            iNext = 0;
            findNext();
        }

        /**
         * Set `iNext` to the first index `i` not less than the current value of `iNext` such that
         * `entries[i] != null` and 'entries[i] != TOMBSTONE', or set it to `entries.length` if
         * there are no remaining non-null and non-tombstone entries.  Note that if `iNext` is
         * already the index of a non-null and non-tombstone entry, then it will not be changed.
         */
        private void findNext() {
            while (iNext < entries.length && (entries[iNext] == null
                    || entries[iNext] == TOMBSTONE)) {
                iNext += 1;
            }
        }

        @Override
        public boolean hasNext() {
            return iNext < entries.length;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            K ans = entries[iNext].key;
            iNext += 1;
            findNext();
            return ans;
        }
    }
}