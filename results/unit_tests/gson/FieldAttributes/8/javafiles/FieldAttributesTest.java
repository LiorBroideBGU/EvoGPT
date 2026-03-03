import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {

    private static class TestClass {
        @SuppressWarnings("unused")
        private int testField;
    }

    private static Field getTestField() throws NoSuchFieldException {
        return TestClass.class.getDeclaredField("testField");
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = getTestField();
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
            Field field = getTestField();
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
            Field field = getTestField();
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(int.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            Field field = getTestField();
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
            Field field = getTestField();
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            SuppressWarnings annotation = fieldAttributes.getAnnotation(SuppressWarnings.class);
            assertNotNull(annotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = getTestField();
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertTrue(annotations.size() > 0);
            assertTrue(annotations.stream().anyMatch(a -> a.annotationType() == SuppressWarnings.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = getTestField();
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertTrue(fieldAttributes.hasModifier(java.lang.reflect.Modifier.PRIVATE));
            assertFalse(fieldAttributes.hasModifier(java.lang.reflect.Modifier.PUBLIC));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            Field field = getTestField();
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}