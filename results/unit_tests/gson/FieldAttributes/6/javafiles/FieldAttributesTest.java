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

    private static Field getTestField() throws NoSuchFieldException {
        return TestClass.class.getDeclaredField("testField");
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
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
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
            assertEquals("testField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
            assertEquals(int.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
            assertEquals(int.class, fieldAttributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
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
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertEquals(1, annotations.size());
            assertTrue(annotations.stream().anyMatch(a -> a instanceof TestAnnotation));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
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
            FieldAttributes fieldAttributes = new FieldAttributes(getTestField());
            assertTrue(fieldAttributes.toString().contains("testField"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}