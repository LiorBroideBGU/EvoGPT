import com.google.gson.FieldNamingPolicy;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class FieldNamingPolicyTest {

    @Test
    public void testIdentity() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("testField", FieldNamingPolicy.IDENTITY.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCase() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("TestField", FieldNamingPolicy.UPPER_CAMEL_CASE.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseWithSpaces() {
        try {
            Field field = TestClass.class.getDeclaredField("testFieldWithSpaces");
            assertEquals("Test Field With Spaces", FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCaseWithUnderscores() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("TEST_FIELD", FieldNamingPolicy.UPPER_CASE_WITH_UNDERSCORES.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithUnderscores() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test_field", FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDashes() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test-field", FieldNamingPolicy.LOWER_CASE_WITH_DASHES.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDots() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test.field", FieldNamingPolicy.LOWER_CASE_WITH_DOTS.translateName(field));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class TestClass {
        public String testField;
        public String testFieldWithSpaces;
    }
}