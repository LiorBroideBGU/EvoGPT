import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonParserTest {

    

    @Test
    public void testParseReaderWithLegacyStrictness() {
        try {
            // Create a JsonReader with LEGACY_STRICT strictness
            JsonReader jsonReader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
            jsonReader.setStrictness(Strictness.LEGACY_STRICT);
            
            // Call parseReader with the JsonReader
            JsonElement result = JsonParser.parseReader(jsonReader);
            
            // Assert that the result is not null and is of type JsonObject
            assert result != null;
            assert result.isJsonObject();
            assert result.getAsJsonObject().get("key").getAsString().equals("value");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testParseReaderWithMalformedJson() {
        try {
            // Create a malformed JSON string
            JsonReader jsonReader = new JsonReader(new StringReader("{key:value}")); // Missing quotes
            
            // Call parseReader and expect a JsonSyntaxException
            JsonElement result = JsonParser.parseReader(jsonReader);
        } catch (JsonSyntaxException e) {
            // Expected exception
            assert e.getMessage().contains("Expected string");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testParseReader_ValidJson_co() {
        try {
            StringReader reader = new StringReader("{\"key\": \"value\"}");
            JsonElement result = JsonParser.parseReader(reader);
            assertNotNull(result);
            assertTrue(result.isJsonObject());
            assertEquals("value", result.getAsJsonObject().get("key").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParseReader_InvalidJson() {
        try {
            StringReader reader = new StringReader("{\"key\": \"value\",}"); // Invalid JSON
            JsonParser.parseReader(reader);
            fail("Expected JsonSyntaxException to be thrown");
        } catch (JsonSyntaxException e) {
            // Expected exception
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParseReader_EmptyJson() {
        try {
            StringReader reader = new StringReader(""); // Empty JSON
            JsonElement result = JsonParser.parseReader(reader);
            assertNotNull(result);
            assertTrue(result.isJsonNull());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParseReader_MalformedJson() {
        try {
            StringReader reader = new StringReader("malformed json"); // Malformed JSON
            JsonParser.parseReader(reader);
            fail("Expected JsonSyntaxException to be thrown");
        } catch (JsonSyntaxException e) {
            // Expected exception
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParseReader_JsonReader() {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader("{\"key\": \"value\"}"));
            JsonElement result = JsonParser.parseReader(jsonReader);
            assertNotNull(result);
            assertTrue(result.isJsonObject());
            assertEquals("value", result.getAsJsonObject().get("key").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParseReader_JsonReader_InvalidJson() {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader("{\"key\": \"value\",}")); // Invalid JSON
            JsonParser.parseReader(jsonReader);
            fail("Expected JsonSyntaxException to be thrown");
        } catch (JsonSyntaxException e) {
            // Expected exception
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParse_ValidJson_co() {
        try {
            String json = "{\"key\": \"value\"}";
            JsonElement result = new JsonParser().parse(json);
            assertNotNull(result);
            assertTrue(result.isJsonObject());
            assertEquals("value", result.getAsJsonObject().get("key").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParse_Reader() {
        try {
            StringReader reader = new StringReader("{\"key\": \"value\"}");
            JsonElement result = new JsonParser().parse(reader);
            assertNotNull(result);
            assertTrue(result.isJsonObject());
            assertEquals("value", result.getAsJsonObject().get("key").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testParse_JsonReader() {
        try {
            JsonReader jsonReader = new JsonReader(new StringReader("{\"key\": \"value\"}"));
            JsonElement result = new JsonParser().parse(jsonReader);
            assertNotNull(result);
            // assertTrue(result.isJsonObject());
            assertEquals("value", result.getAsJsonObject().get("key").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}