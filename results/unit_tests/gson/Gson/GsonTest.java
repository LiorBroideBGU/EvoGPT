import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GsonTest {


    @Test
    public void testGetAdapterWithNullType() {
        try {
            Gson gson = new Gson();
            // Expecting a NullPointerException, so we catch it and assert it
            gson.getAdapter((TypeToken<Object>) null);
        } catch (NullPointerException e) {
            // Expected exception
            return;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        // If we reach here, the expected exception was not thrown
        throw new AssertionError("Expected NullPointerException was not thrown.");
    }

    @Test
    public void testGetAdapterWithCachedType() {
        try {
            Gson gson = new Gson();
            TypeToken<String> typeToken = TypeToken.get(String.class);
            gson.getAdapter(typeToken);
            gson.getAdapter(typeToken); // Call again to hit the cached path
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetAdapterWithNewFactory() {
        try {
            Gson gson = new Gson();
            TypeToken<String> typeToken = TypeToken.get(String.class);
            Method method = Gson.class.getDeclaredMethod("getAdapter", TypeToken.class);
            method.setAccessible(true);
            Object result = method.invoke(gson, typeToken);
            assertNotNull(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToJsonTreeWithNull() {
        try {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree(null);
            assertNotNull(jsonElement);
            assertEquals(JsonNull.INSTANCE, jsonElement);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToJsonWithNull() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(null);
            assertNotNull(json);
            assertEquals("null", json);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testToJsonWithAppendable() {
        try {
            Gson gson = new Gson();
            StringWriter writer = new StringWriter();
            gson.toJson("test", writer);
            assertNotNull(writer.toString());
            assertEquals("\"test\"", writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFromJsonWithNull() {
        try {
            Gson gson = new Gson();
            String json = null;
            String result = gson.fromJson(json, String.class);
            assertEquals(null, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFromJsonWithReader() {
        try {
            Gson gson = new Gson();
            String json = "{\"name\":\"test\"}";
            StringReader reader = new StringReader(json);
            Map<String, String> result = gson.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            assertNotNull(result);
            assertEquals("test", result.get("name"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFromJsonWithJsonElement() {
        try {
            Gson gson = new Gson();
            JsonElement jsonElement = JsonNull.INSTANCE;
            String result = gson.fromJson(jsonElement, String.class);
            assertEquals(null, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testNewBuilder() {
        try {
            Gson gson = new Gson();
            assertNotNull(gson.newBuilder());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testExcluder() {
        try {
            Gson gson = new Gson();
            assertNotNull(gson.excluder());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testFieldNamingStrategy() {
        try {
            Gson gson = new Gson();
            assertNotNull(gson.fieldNamingStrategy());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testSerializeNulls() {
        try {
            Gson gson = new Gson();
            assertFalse(gson.serializeNulls());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHtmlSafe() {
        try {
            Gson gson = new Gson();
            assertTrue(gson.htmlSafe());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAdapterWithTypeToken() {
        try {
            Gson gson = new Gson();
            TypeToken<String> typeToken = TypeToken.get(String.class);
            TypeAdapter<String> adapter = gson.getAdapter(typeToken);
            assertNotNull(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAdapterWithClass() {
        try {
            Gson gson = new Gson();
            TypeAdapter<String> adapter = gson.getAdapter(String.class);
            assertNotNull(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToJsonTreeWithObject() {
        try {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("test");
            assertNotNull(jsonElement);
            assertEquals("test", jsonElement.getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToJsonTreeWithObjectAndType() {
        try {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree("test", String.class);
            assertNotNull(jsonElement);
            assertEquals("test", jsonElement.getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToJsonWithObject() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson("test");
            assertNotNull(json);
            assertEquals("\"test\"", json);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testFromJsonWithStringAndClass() {
        try {
            Gson gson = new Gson();
            String json = "\"test\"";
            String result = gson.fromJson(json, String.class);
            assertEquals("test", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testFromJsonWithStringAndType() {
        try {
            Gson gson = new Gson();
            String json = "\"test\"";
            String result = gson.fromJson(json, String.class);
            assertEquals("test", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testFromJsonWithReaderAndClass() {
        try {
            Gson gson = new Gson();
            String json = "\"test\"";
            StringReader reader = new StringReader(json);
            String result = gson.fromJson(reader, String.class);
            assertEquals("test", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testFromJsonWithReaderAndType() {
        try {
            Gson gson = new Gson();
            String json = "\"test\"";
            StringReader reader = new StringReader(json);
            String result = gson.fromJson(reader, String.class);
            assertEquals("test", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            Gson gson = new Gson();
            String result = gson.toString();
            assertNotNull(result);
            assertTrue(result.contains("serializeNulls:"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}