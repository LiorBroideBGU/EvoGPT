import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

public class JsonArrayTest {

    @Test
    public void testDeepCopy() {
        try {
            JsonArray original = new JsonArray();
            original.add("test");
            JsonArray copy = original.deepCopy();

            assertNotSame(original, copy);
            assertEquals(original.size(), copy.size());
            assertEquals(original.get(0).getAsString(), copy.get(0).getAsString());

            // Test deep copy with nested elements
            original.add(new JsonArray());
            copy = original.deepCopy();
            assertNotSame(original.get(1), copy.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }






    @Test
    public void testAddAll() {
        try {
            JsonArray jsonArray1 = new JsonArray();
            jsonArray1.add("first");
            JsonArray jsonArray2 = new JsonArray();
            jsonArray2.add("second");
            jsonArray1.addAll(jsonArray2);
            assertEquals(2, jsonArray1.size());
            assertEquals("second", jsonArray1.get(1).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSet() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("first");
            jsonArray.set(0, new JsonPrimitive("second"));
            assertEquals("second", jsonArray.get(0).getAsString());

            JsonElement oldElement = jsonArray.set(0, null);
            assertSame(JsonNull.INSTANCE, jsonArray.get(0));
            assertEquals("second", oldElement.getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testRemoveElement() {
        try {
            JsonArray jsonArray = new JsonArray();
            JsonElement element = new JsonPrimitive("toRemove");
            jsonArray.add(element);
            jsonArray.remove(element);
            assertEquals(0, jsonArray.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testRemoveByIndex() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("first");
            jsonArray.add("second");
            JsonElement removedElement = jsonArray.remove(0);
            assertEquals("first", removedElement.getAsString());
            assertEquals(1, jsonArray.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testContains() {
        try {
            JsonArray jsonArray = new JsonArray();
            JsonElement element = new JsonPrimitive("exists");
            jsonArray.add(element);
            assertTrue(jsonArray.contains(element));
            assertFalse(jsonArray.contains(new JsonPrimitive("notExists")));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSize() {
        try {
            JsonArray jsonArray = new JsonArray();
            assertEquals(0, jsonArray.size());
            jsonArray.add("test");
            assertEquals(1, jsonArray.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testIsEmpty() {
        try {
            JsonArray jsonArray = new JsonArray();
            assertTrue(jsonArray.isEmpty());
            jsonArray.add("test");
            assertFalse(jsonArray.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGet() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("test");
            assertEquals("test", jsonArray.get(0).getAsString());

            // Test out of bounds
            try {
                jsonArray.get(1);
                fail("Should have thrown IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // Expected
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }













    @Test
    public void testAsList() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("item1");
            List<JsonElement> list = jsonArray.asList();
            assertEquals(1, list.size());
            assertEquals("item1", list.get(0).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testEquals() {
        try {
            JsonArray jsonArray1 = new JsonArray();
            jsonArray1.add("item1");
            JsonArray jsonArray2 = new JsonArray();
            jsonArray2.add("item1");
            assertTrue(jsonArray1.equals(jsonArray2));

            jsonArray2.add("item2");
            assertFalse(jsonArray1.equals(jsonArray2));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHashCode() {
        try {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add("item1");
            int hashCode = jsonArray.hashCode();
            assertTrue(hashCode != 0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}