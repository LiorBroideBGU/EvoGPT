```
package com.google.gson;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.internal.NonNullElementWrapperList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonArrayTest {
    @Test
    public void testDeepCopy() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);
        array.add(1);
        array.add(2);
        array.add("hello");
        array.add("world");

        JsonArray copy = array.deepCopy();
        assertNotSame(array, copy);
        assertEquals(array, copy);
    }

    @Test
    public void testAddBoolean() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertEquals(2, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertFalse(array.get(1).getAsBoolean());
    }

    @Test
    public void testAddCharacter() {
        JsonArray array = new JsonArray();
        array.add('a');
        array.add('b');

        assertEquals(2, array.size());
        assertEquals('a', array.get(0).getAsCharacter());
        assertEquals('b', array.get(1).getAsCharacter());
    }

    @Test
    public void testAddNumber() {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);
        array.add(3);

        assertEquals(3, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals(2, array.get(1).getAsInt());
        assertEquals(3, array.get(2).getAsInt());
    }

    @Test
    public void testAddString() {
        JsonArray array = new JsonArray();
        array.add("hello");
        array.add("world");

        assertEquals(2, array.size());
        assertEquals("hello", array.get(0).getAsString());
        assertEquals("world", array.get(1).getAsString());
    }

    @Test
    public void testAddJsonElement() {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(true));
        array.add(new JsonPrimitive(false));

        assertEquals(2, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertFalse(array.get(1).getAsBoolean());
    }

    @Test
    public void testAddAllJsonArray() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        JsonArray other = new JsonArray();
        other.add(1);
        other.add(2);
        other.add(3);

        array.addAll(other);

        assertEquals(5, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertFalse(array.get(1).getAsBoolean());
        assertEquals(1, array.get(2).getAsInt());
        assertEquals(2, array.get(3).getAsInt());
        assertEquals(3, array.get(4).getAsInt());
    }

    @Test
    public void testSet() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        JsonPrimitive newElement = new JsonPrimitive(1);
        JsonElement oldElement = array.set(1, newElement);

        assertEquals(2, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertEquals(1, array.get(1).getAsInt());
        assertEquals(JsonNull.INSTANCE, oldElement);
    }

    @Test
    public void testRemove() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertTrue(array.remove(true));
        assertFalse(array.remove(false));

        assertEquals(1, array.size());
        assertTrue(array.get(0).getAsBoolean());
    }

    @Test
    public void testRemoveIndex() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertEquals(JsonNull.INSTANCE, array.remove(0));
        assertEquals(JsonNull.INSTANCE, array.remove(1));

        assertEquals(0, array.size());
    }

    @Test
    public void testContains() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertTrue(array.contains(true));
        assertTrue(array.contains(false));
        assertFalse(array.contains(1));
    }

    @Test
    public void testSize() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertEquals(2, array.size());
    }

    @Test
    public void testIsEmpty() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertFalse(array.isEmpty());
    }

    @Test
    public void testIterator() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        Iterator<JsonElement> iterator = array.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.next().getAsBoolean());
        assertTrue(iterator.hasNext());
        assertFalse(iterator.next().getAsBoolean());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testGet() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertTrue(array.get(0).getAsBoolean());
        assertFalse(array.get(1).getAsBoolean());
    }

    @Test
    public void testGetAsNumber() {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);

        assertEquals(1, array.getAsNumber().intValue());
        assertEquals(2, array.get(1).getAsNumber().intValue());
    }

    @Test
    public void testGetAsString() {
        JsonArray array = new JsonArray();
        array.add("hello");
        array.add("world");

        assertEquals("hello", array.getAsString());
        assertEquals("world", array.get(1).getAsString());
    }

    @Test
    public void testGetAsDouble() {
        JsonArray array = new JsonArray();
        array.add(1.0);
        array.add(2.0);

        assertEquals(1.0, array.getAsDouble(), 0.0);
        assertEquals(2.0, array.get(1).getAsDouble(), 0.0);
    }

    @Test
    public void testGetAsBigDecimal() {
        JsonArray array = new JsonArray();
        array.add(new BigDecimal("1.0"));
        array.add(new BigDecimal("2.0"));

        assertEquals(new BigDecimal("1.0"), array.getAsBigDecimal());
        assertEquals(new BigDecimal("2.0"), array.get(1).getAsBigDecimal());
    }

    @Test
    public void testGetAsBigInteger() {
        JsonArray array = new JsonArray();
        array.add(new BigInteger("1"));
        array.add(new BigInteger("2"));

        assertEquals(new BigInteger("1"), array.getAsBigInteger());
        assertEquals(new BigInteger("2"), array.get(1).getAsBigInteger());
    }

    @Test
    public void testGetAsFloat() {
        JsonArray array = new JsonArray();
        array.add(1.0f);
        array.add(2.0f);

        assertEquals(1.0f, array.getAsFloat(), 0.0f);
        assertEquals(2.0f, array.get(1).getAsFloat(), 0.0f);
    }

    @Test
    public void testGetAsLong() {
        JsonArray array = new JsonArray();
        array.add(1L);
        array.add(2L);

        assertEquals(1L, array.getAsLong());
        assertEquals(2L, array.get(1).getAsLong());
    }

    @Test
    public void testGetAsInt() {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);

        assertEquals(1, array.getAsInt());
        assertEquals(2, array.get(1).getAsInt());
    }

    @Test
    public void testGetAsByte() {
        JsonArray array = new JsonArray();
        array.add((byte) 1);
        array.add((byte) 2);

        assertEquals((byte) 1, array.getAsByte());
        assertEquals((byte) 2, array.get(1).getAsByte());
    }

    @Test
    public void testGetAsCharacter() {
        JsonArray array = new JsonArray();
        array.add('a');
        array.add('b');

        assertEquals('a', array.getAsCharacter());
        assertEquals('b', array.get(1).getAsCharacter());
    }

    @Test
    public void testEquals() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        JsonArray other = new JsonArray();
        other.add(true);
        other.add(false);

        assertTrue(array.equals(other));
    }

    @Test
    public void testNotEquals() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        JsonArray other = new JsonArray();
        other.add(true);
        other.add(true);

        assertFalse(array.equals(other));
    }

    @Test
    public void testHashCode() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        JsonArray other = new JsonArray();
        other.add(true);
        other.add(false);

        assertEquals(array.hashCode(), other.hashCode());
    }

    @Test
    public void testToString() {
        JsonArray array = new JsonArray();
        array.add(true);
        array.add(false);

        assertEquals("[true, false]", array.toString());
    }
}
```






























