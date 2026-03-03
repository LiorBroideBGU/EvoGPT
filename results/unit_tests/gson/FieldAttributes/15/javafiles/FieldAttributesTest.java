import org.junit.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {}

    public static class TestClass {
        @TestAnnotation
        public int testField;
    }

    private static Field getField(String fieldName) throws NoSuchFieldException {
        return TestClass.class.getDeclaredField(fieldName);
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
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
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            assertEquals("testField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            assertEquals(int.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            assertEquals(int.class, fieldAttributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            TestAnnotation annotation = fieldAttributes.getAnnotation(TestAnnotation.class);
            assertNotNull(annotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertEquals(1, annotations.size());
            assertTrue(annotations.contains(fieldAttributes.getAnnotation(TestAnnotation.class)));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            assertTrue(fieldAttributes.hasModifier(Modifier.PUBLIC));
            assertFalse(fieldAttributes.hasModifier(Modifier.STATIC));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getField("testField"));
            assertEquals("public int com.google.gson.FieldAttributesTest$TestClass.testField", fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}