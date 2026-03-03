import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class FieldAttributesTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {}

    private static Field getTestField() throws NoSuchFieldException {
        return TestClass.class.getDeclaredField("testField");
    }

    private static class TestClass {
        @TestAnnotation
        private String testField;
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
            assertEquals("Field name should match", "testField", fieldAttributes.getName());
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
            assertEquals("Declared type should match", String.class, fieldAttributes.getDeclaredType());
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
            assertEquals("Declared class should match", String.class, fieldAttributes.getDeclaredClass());
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
            TestAnnotation annotation = fieldAttributes.getAnnotation(TestAnnotation.class);
            assertNotNull("Should find the TestAnnotation", annotation);
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
            assertEquals("Should have one annotation", 1, annotations.size());
            assertTrue("Should contain TestAnnotation", annotations.contains(field.getAnnotation(TestAnnotation.class)));
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
            assertTrue("Field should be private", fieldAttributes.hasModifier(Modifier.PRIVATE));
            assertFalse("Field should not be public", fieldAttributes.hasModifier(Modifier.PUBLIC));
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
            assertEquals("toString should match field's toString", field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}