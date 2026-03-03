import com.google.gson.FieldAttributes;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {
    
    private static Field getTestField(String fieldName) throws NoSuchFieldException {
        return TestClass.class.getDeclaredField(fieldName);
    }
    
    private static class TestClass {
        @Deprecated
        private String testField;
        public int anotherField;
    }
    
    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = getTestField("testField");
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
            Field field = getTestField("testField");
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
            Field field = getTestField("testField");
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
            Field field = getTestField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(String.class, fieldAttributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            Field field = getTestField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Deprecated annotation = fieldAttributes.getAnnotation(Deprecated.class);
            assertNotNull(annotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = getTestField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertTrue(annotations.size() > 0);
            assertTrue(annotations.stream().anyMatch(a -> a instanceof Deprecated));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = getTestField("anotherField");
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
            Field field = getTestField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}