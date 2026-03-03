import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonArrayTest {


    @Test
    public void testDeepCopyEnhanced() {
        try {
            JsonArray original = new JsonArray();
            original.add(new JsonPrimitive("test"));
            JsonArray copy = original.deepCopy();
            assertNotSame(original, copy);
            assertEquals(original.size(), copy.size());
            assertEquals(original.get(0).getAsString(), copy.get(0).getAsString());

            // Test deep copy of an empty array
            JsonArray emptyArray = new JsonArray();
            JsonArray emptyCopy = emptyArray.deepCopy();
            assertNotSame(emptyArray, emptyCopy);
            assertEquals(emptyArray.size(), emptyCopy.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddBoolean() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(true);
            assertEquals(1, jsonArray.size());
            assertTrue(jsonArray.get(0).getAsBoolean());

            jsonArray.add((Boolean) null);
            assertEquals(2, jsonArray.size());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddCharacter() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add('a');
            assertEquals(1, jsonArray.size());
            assertEquals('a', jsonArray.get(0).getAsCharacter());

            jsonArray.add((Character) null);
            assertEquals(2, jsonArray.size());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddNumber() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(123);
            assertEquals(1, jsonArray.size());
            assertEquals(123, jsonArray.get(0).getAsInt());

            jsonArray.add((Number) null);
            assertEquals(2, jsonArray.size());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddString() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("Hello");
            assertEquals(1, jsonArray.size());
            assertEquals("Hello", jsonArray.get(0).getAsString());

            jsonArray.add((String) null);
            assertEquals(2, jsonArray.size());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddJsonElement() {
        try {
            JsonArray jsonArray = new JsonArray();
            JsonElement element = new JsonPrimitive("element");
            jsonArray.add(element);
            assertEquals(1, jsonArray.size());
            assertEquals(element, jsonArray.get(0));

            jsonArray.add((JsonElement) null);
            assertEquals(2, jsonArray.size());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSetEnhanced() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(new JsonPrimitive("original"));
            JsonElement replaced = jsonArray.set(0, new JsonPrimitive("modified"));
            assertEquals("original", replaced.getAsString());
            assertEquals("modified", jsonArray.get(0).getAsString());

            JsonElement nullElement = jsonArray.set(0, null);
            assertEquals("modified", nullElement.getAsString());
            assertEquals(JsonNull.INSTANCE, jsonArray.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testEqualsEnhanced() {
        try {
            JsonArray jsonArray1 = new JsonArray();
            JsonArray jsonArray2 = new JsonArray();
            assertTrue(jsonArray1.equals(jsonArray2));

            jsonArray1.add(new JsonPrimitive("test"));
            assertFalse(jsonArray1.equals(jsonArray2));

            jsonArray2.add(new JsonPrimitive("test"));
            assertTrue(jsonArray1.equals(jsonArray2));

            jsonArray2.add(new JsonPrimitive("another"));
            assertFalse(jsonArray1.equals(jsonArray2));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testDeepCopy() {
        try {
            JsonArray original = new JsonArray();
            original.add(new JsonPrimitive("test"));
            JsonArray copy = original.deepCopy();

            Assert.assertNotSame(original, copy);
            Assert.assertEquals(original.size(), copy.size());
            Assert.assertEquals(original.get(0), copy.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAddAll() {
        try {
            JsonArray array1 = new JsonArray();
            array1.add(new JsonPrimitive("one"));
            array1.add(new JsonPrimitive("two"));

            JsonArray array2 = new JsonArray();
            array2.add(new JsonPrimitive("three"));

            array1.addAll(array2);

            Assert.assertEquals(3, array1.size());
            Assert.assertEquals(new JsonPrimitive("three"), array1.get(2));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSet() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("first"));
            array.add(new JsonPrimitive("second"));

            JsonElement oldElement = array.set(0, new JsonPrimitive("updated"));
            Assert.assertEquals(new JsonPrimitive("first"), oldElement);
            Assert.assertEquals(new JsonPrimitive("updated"), array.get(0));
            Assert.assertEquals("second", array.get(1).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testRemoveJsonElement() {
        try {
            JsonArray array = new JsonArray();
            JsonElement element = new JsonPrimitive("toRemove");
            array.add(element);
            array.add(new JsonPrimitive("stay"));

            boolean removed = array.remove(element);
            Assert.assertTrue(removed);
            Assert.assertEquals(1, array.size());
            Assert.assertFalse(array.contains(element));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testRemoveByIndex() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("first"));
            array.add(new JsonPrimitive("second"));

            JsonElement removedElement = array.remove(0);
            Assert.assertEquals(new JsonPrimitive("first"), removedElement);
            Assert.assertEquals(1, array.size());
            Assert.assertEquals(new JsonPrimitive("second"), array.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testContains() {
        try {
            JsonArray array = new JsonArray();
            JsonElement element = new JsonPrimitive("test");
            array.add(element);

            Assert.assertTrue(array.contains(element));
            Assert.assertFalse(array.contains(new JsonPrimitive("notPresent")));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSize() {
        try {
            JsonArray array = new JsonArray();
            Assert.assertEquals(0, array.size());

            array.add(new JsonPrimitive("test"));
            Assert.assertEquals(1, array.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testIsEmpty() {
        try {
            JsonArray array = new JsonArray();
            Assert.assertTrue(array.isEmpty());

            array.add(new JsonPrimitive("test"));
            Assert.assertFalse(array.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testIterator() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("first"));
            array.add(new JsonPrimitive("second"));

            Iterator<JsonElement> iterator = array.iterator();
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(new JsonPrimitive("first"), iterator.next());
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(new JsonPrimitive("second"), iterator.next());
            Assert.assertFalse(iterator.hasNext());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGet() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("test"));

            Assert.assertEquals(new JsonPrimitive("test"), array.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsNumber() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(42));

            Assert.assertEquals(42, array.getAsNumber());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsString() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("Hello"));

            Assert.assertEquals("Hello", array.getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsDouble() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(3.14));

            Assert.assertEquals(3.14, array.getAsDouble(), 0.001);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsBigDecimal() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(new BigDecimal("123.45")));

            Assert.assertEquals(new BigDecimal("123.45"), array.getAsBigDecimal());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsBigInteger() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(new BigInteger("123456789")));

            Assert.assertEquals(new BigInteger("123456789"), array.getAsBigInteger());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsFloat() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(2.5f));

            Assert.assertEquals(2.5f, array.getAsFloat(), 0.001);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsLong() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(123456789L));

            Assert.assertEquals(123456789L, array.getAsLong());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsInt() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(42));

            Assert.assertEquals(42, array.getAsInt());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsByte() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive((byte) 1));

            Assert.assertEquals((byte) 1, array.getAsByte());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsCharacter() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive('c'));

            Assert.assertEquals('c', array.getAsCharacter());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsShort() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive((short) 10));

            Assert.assertEquals((short) 10, array.getAsShort());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAsBoolean() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(true));

            Assert.assertTrue(array.getAsBoolean());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAsList() {
        try {
            JsonArray array = new JsonArray();
            JsonElement element = new JsonPrimitive("test");
            array.add(element);

            List<JsonElement> list = array.asList();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals(element, list.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testEquals() {
        try {
            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            array1.add(new JsonPrimitive("test"));
            array2.add(new JsonPrimitive("test"));

            Assert.assertTrue(array1.equals(array2));
            Assert.assertFalse(array1.equals(new JsonArray()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHashCode() {
        try {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive("test"));
            int expectedHashCode = array.asList().hashCode();
            Assert.assertEquals(expectedHashCode, array.hashCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}