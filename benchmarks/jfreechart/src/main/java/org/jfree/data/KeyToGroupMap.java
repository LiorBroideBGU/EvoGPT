

package org.jfree.data;

import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.Args;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


public class KeyToGroupMap<K extends Comparable<K>, G extends Comparable<G>> 
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -2228169345475318082L;

    
    private G defaultGroup;

    
    private List<G> groups;

    
    private Map<K, G> keyToGroupMap;

    
    public KeyToGroupMap() {
        this((G) "Default Group"); // FIXME
    }

    
    public KeyToGroupMap(G defaultGroup) {
        Args.nullNotPermitted(defaultGroup, "defaultGroup");
        this.defaultGroup = defaultGroup;
        this.groups = new ArrayList<>();
        this.keyToGroupMap = new HashMap<>();
    }

    
    public int getGroupCount() {
        return this.groups.size() + 1;
    }

    
    public List<G> getGroups() {
        List<G> result = new ArrayList<>();
        result.add(this.defaultGroup);
        for (G group : this.groups) {
            if (!result.contains(group)) {
                result.add(group);
            }
        }
        return result;
    }

    
    public int getGroupIndex(G group) {
        int result = this.groups.indexOf(group);
        if (result < 0) {
            if (this.defaultGroup.equals(group)) {
                result = 0;
            }
        }
        else {
            result = result + 1;
        }
        return result;
    }

    
    public G getGroup(K key) {
        Args.nullNotPermitted(key, "key");
        G result = this.defaultGroup;
        G group = this.keyToGroupMap.get(key);
        if (group != null) {
            result = group;
        }
        return result;
    }

    
    public void mapKeyToGroup(K key, G group) {
        Args.nullNotPermitted(key, "key");
        G currentGroup = getGroup(key);
        if (!currentGroup.equals(this.defaultGroup)) {
            if (!currentGroup.equals(group)) {
                int count = getKeyCount(currentGroup);
                if (count == 1) {
                    this.groups.remove(currentGroup);
                }
            }
        }
        if (group == null) {
            this.keyToGroupMap.remove(key);
        }
        else {
            if (!this.groups.contains(group)) {
                if (!this.defaultGroup.equals(group)) {
                    this.groups.add(group);
                }
            }
            this.keyToGroupMap.put(key, group);
        }
    }

    
    public int getKeyCount(G group) {
        Args.nullNotPermitted(group, "group");
        int result = 0;
        for (G g : this.keyToGroupMap.values()) {
            if (group.equals(g)) {
                result++;
            }
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyToGroupMap)) {
            return false;
        }
        KeyToGroupMap<K, G> that = (KeyToGroupMap) obj;
        if (!Objects.equals(this.defaultGroup, that.defaultGroup)) {
            return false;
        }
        if (!this.keyToGroupMap.equals(that.keyToGroupMap)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.defaultGroup);
        hash = 83 * hash + Objects.hashCode(this.keyToGroupMap);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        KeyToGroupMap<K, G> result = (KeyToGroupMap) super.clone();
        result.defaultGroup
            = (G) KeyToGroupMap.clone(this.defaultGroup);
        result.groups = (List<G>) KeyToGroupMap.clone(this.groups);
        result.keyToGroupMap = (Map<K, G>) KeyToGroupMap.clone(this.keyToGroupMap);
        return result;
    }

    
    private static Object clone(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> c = object.getClass();
        Object result = null;
        try {
            Method m = c.getMethod("clone", (Class[]) null);
            if (Modifier.isPublic(m.getModifiers())) {
                try {
                    result = m.invoke(object, (Object[]) null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (NoSuchMethodException e) {
            result = object;
        }
        return result;
    }

    
    private static Collection clone(Collection list)
        throws CloneNotSupportedException {
        Collection result = null;
        if (list != null) {
            try {
                Collection clone = list.getClass().getDeclaredConstructor().newInstance();
                for (Object o : list) {
                    clone.add(KeyToGroupMap.clone(o));
                }
                result = clone;
            }
            catch (Exception e) {
                throw new CloneNotSupportedException("Exception.");
            }
        }
        return result;
    }

}
