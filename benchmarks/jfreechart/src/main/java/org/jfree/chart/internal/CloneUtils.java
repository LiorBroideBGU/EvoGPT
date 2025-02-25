
package org.jfree.chart.internal;

import org.jfree.chart.api.PublicCloneable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CloneUtils {
    
    
    public static <T> T copy(T object) throws CloneNotSupportedException {
        if (object == null) {
            return null;
        }
        if (object instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) object;
            return (T) pc.clone();
        } else {
            try {
                Method method = object.getClass().getMethod("clone",
                        (Class[]) null);
                if (Modifier.isPublic(method.getModifiers())) {
                    return (T) method.invoke(object, (Object[]) null);
                } else {
                    return object;
                }
            } catch (NoSuchMethodException e) {
                return object;
            } catch (IllegalAccessException e) {
                throw new CloneNotSupportedException("Object.clone(): unable to call method.");
            } catch (InvocationTargetException e) {
                throw new CloneNotSupportedException("Object without clone() method is impossible.");
            }
        }
    }

    
    public static <T> T clone(T object) throws CloneNotSupportedException {
        if (object == null) {
            return null;
        }
        if (object instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) object;
            return (T) pc.clone();
        } else {
            try {
                Method method = object.getClass().getMethod("clone",
                        (Class[]) null);
                if (Modifier.isPublic(method.getModifiers())) {
                    return (T) method.invoke(object, (Object[]) null);
                }
            } catch (NoSuchMethodException e) {
                throw new CloneNotSupportedException("Object without clone() method is impossible.");
            } catch (IllegalAccessException e) {
                throw new CloneNotSupportedException("Object.clone(): unable to call method.");
            } catch (InvocationTargetException e) {
                throw new CloneNotSupportedException("Object without clone() method is impossible.");
            }
        }
        throw new CloneNotSupportedException("Failed to clone.");
    }

    
    public static <T> List<T>cloneList(List<T> source) {
        Args.nullNotPermitted(source, "source");
        List<T> result = new ArrayList<>();
        for (Object obj: source) {
            try {
                result.add((T) copy(obj));
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    
    public static <K, V> Map<K, V> cloneMapValues(Map<K, V> source) {
        Args.nullNotPermitted(source, "source");
        Map<K, V> result = new HashMap<>();
        for (K key : source.keySet()) {
            V value = source.get(key);
            if (value != null) {
                try {
                    result.put(key, copy(value));
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                result.put(key, null);
            }
        }
        return result;
    }
   
}
