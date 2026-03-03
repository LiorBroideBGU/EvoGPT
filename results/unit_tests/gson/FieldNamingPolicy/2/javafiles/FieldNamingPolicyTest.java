import com.google.gson.FieldNamingPolicy;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.assertEquals;

public class FieldNamingPolicyTest {

    @Test
    public void testIdentity() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.IDENTITY.translateName(field);
            assertEquals("testField", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCase() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.UPPER_CAMEL_CASE.translateName(field);
            assertEquals("TestField", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseWithSpaces() {
        try {
            Field field = TestClass.class.getDeclaredField("testFieldWithSpaces");
            String result = FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES.translateName(field);
            assertEquals("Test Field With Spaces", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCaseWithUnderscores() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.UPPER_CASE_WITH_UNDERSCORES.translateName(field);
            assertEquals("TEST_FIELD", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithUnderscores() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
            assertEquals("test_field", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDashes() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_DASHES.translateName(field);
            assertEquals("test-field", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDots() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_DOTS.translateName(field);
            assertEquals("test.field", result);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class TestClass {
        private String testField;
        private String testFieldWithSpaces;
    }
}