package io.airlift.airline.util;

import java.util.*;

/**
 * @author sstrohschein
 *         <br>Date: 29.08.15
 *         <br>Time: 11:39
 */
public final class CollectionUtils
{
    private CollectionUtils() {}

    public static <E> List<E> asList(Iterable<E> iterable) {
        if(iterable instanceof Collection) {
            return new ArrayList<>((Collection<E>)iterable);
        } else {
            List<E> list = new ArrayList<>();
            for(E element: iterable) {
                list.add(element);
            }
            return list;
        }
    }

    public static <K, V> Map<K, V> asSingleEntryMap(K key, V value) {
        Map<K, V> map = new HashMap<>(1);
        map.put(key, value);
        return map;
    }

    public static <V> Set<V> asSingleEntrySet(V value) {
        Set<V> set = new HashSet<>(1);
        set.add(value);
        return set;
    }
}