package openblocks.codeblockutil;

import java.util.*;

/**
 * This class implements a Least Recently Used cache that stores arbitrary key/value pairs.
 */
public class LRUCache<K, V>
{
    private final int capacity;
    private final LinkedHashMap<K, V> map;

    public LRUCache(int _capacity)
    {
        this.capacity = _capacity;
        map = new LinkedHashMap<K, V>(capacity, 0.75f, true)  // access-order ordering
            {        	
				private static final long serialVersionUID = 1L;

				@Override protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
                {
                    return size() > capacity;
                }
            };
    }
    
    /**
     * @return the value associated with key, if it is in the cache (or null otherwise)
     */
    public V get(K key)
    {
        return map.get(key);
    }
    
    /**
     * Adds an entry to the cache.
     */
    public void put(K key, V value)
    {
        map.put(key, value);
    }
}
