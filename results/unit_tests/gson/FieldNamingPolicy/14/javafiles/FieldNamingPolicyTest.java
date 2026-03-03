import com.google.gson.FieldNamingPolicy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import org.junit.Test;

public class FieldNamingPolicyTest {

    @Test
    public void testIdentityTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.IDENTITY.translateName(field);
            assertNotNull(result);
            assertEquals("testField", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.UPPER_CAMEL_CASE.translateName(field);
            assertNotNull(result);
            assertEquals("TestField", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseWithSpacesTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testFieldWithSpaces");
            String result = FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES.translateName(field);
            assertNotNull(result);
            assertEquals("Test Field With Spaces", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCaseWithUnderscoresTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.UPPER_CASE_WITH_UNDERSCORES.translateName(field);
            assertNotNull(result);
            assertEquals("TEST_FIELD", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithUnderscoresTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
            assertNotNull(result);
            assertEquals("test_field", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDashesTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_DASHES.translateName(field);
            assertNotNull(result);
            assertEquals("test-field", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDotsTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            String result = FieldNamingPolicy.LOWER_CASE_WITH_DOTS.translateName(field);
            assertNotNull(result);
            assertEquals("test.field", result);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static class TestClass {
        private String testField;
        private String testFieldWithSpaces;
    }
}