import com.google.gson.FieldNamingPolicy;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Locale;
import static org.junit.Assert.*;

public class FieldNamingPolicyTest {

    @Test
    public void testIdentityTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
        } catch (Compilation Error e) {
            // Expected
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("TestField", FieldNamingPolicy.UPPER_CAMEL_CASE.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCamelCaseWithSpacesTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testFieldWithSpaces");
            assertEquals("Test Field With Spaces", FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUpperCaseWithUnderscoresTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("TEST_FIELD", FieldNamingPolicy.UPPER_CASE_WITH_UNDERSCORES.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithUnderscoresTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test_field", FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDashesTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test-field", FieldNamingPolicy.LOWER_CASE_WITH_DASHES.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testLowerCaseWithDotsTranslateName() {
        try {
            Field field = TestClass.class.getDeclaredField("testField");
            assertEquals("test.field", FieldNamingPolicy.LOWER_CASE_WITH_DOTS.translateName(field));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class TestClass {
        private String testField;
        private String testFieldWithSpaces;
    }
}