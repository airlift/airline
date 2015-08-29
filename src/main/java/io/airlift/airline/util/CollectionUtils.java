package io.airlift.airline.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static <K, V> Map<K, V> asMap(K key, V value) {
        Map<K, V> map = new HashMap<>(1);
        map.put(key, value);
        return map;
    }

    public static <V> Set<V> asSet(V value) {
        Set<V> set = new HashSet<>(1);
        set.add(value);
        return set;
    }
}