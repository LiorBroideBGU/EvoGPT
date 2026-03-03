import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class FieldAttributesTest {

    private static class TestClass {
        public String testField;
        private int anotherField;
    }

    private static Field getField(String fieldName) throws NoSuchFieldException {
        return TestClass.class.getDeclaredField(fieldName);
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
        } catch (Compilation Error e) {
            // Expected
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetName() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals("testField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(String.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            Field field = getField("anotherField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(int.class, fieldAttributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertNull(fieldAttributes.getAnnotation(Deprecated.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertNotNull(annotations);
            assertTrue(annotations.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertTrue(fieldAttributes.hasModifier(Modifier.PUBLIC));
            assertFalse(fieldAttributes.hasModifier(Modifier.PRIVATE));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}