import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class FieldAttributesTest {
    private FieldAttributes fieldAttributes;
    private Field testField;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {}


    @Test
    public void testGetDeclaringClass() {
        try {
            assertEquals("Declaring class should match", this.getClass(), fieldAttributes.getDeclaringClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetName() {
        try {
            assertEquals("Field name should be 'exampleField'", "exampleField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            assertEquals("Declared type should be String", String.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            assertEquals("Declared class should be String", String.class, fieldAttributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            TestAnnotation annotation = fieldAttributes.getAnnotation(TestAnnotation.class);
            assertNotNull("Annotation should not be null", annotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            assertEquals("Annotations size should be 1", 1, fieldAttributes.getAnnotations().size());
            assertTrue("Annotations should contain TestAnnotation", fieldAttributes.getAnnotations().stream()
                    .anyMatch(a -> a instanceof TestAnnotation));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            assertTrue("Field should be public", fieldAttributes.hasModifier(java.lang.reflect.Modifier.PUBLIC));
            assertFalse("Field should not be static", fieldAttributes.hasModifier(java.lang.reflect.Modifier.STATIC));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            assertEquals("Field toString should match", testField.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}