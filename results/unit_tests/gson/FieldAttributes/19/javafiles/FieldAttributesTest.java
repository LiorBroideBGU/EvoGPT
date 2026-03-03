import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {

    private static Field createField(String fieldName) throws NoSuchFieldException {
        return SampleClass.class.getDeclaredField(fieldName);
    }

    private static class SampleClass {
        public int publicField;
        private String privateField;
        protected double protectedField;
        
        @Deprecated
        public String deprecatedField;
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = createField("publicField");
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
            Field field = createField("privateField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals("privateField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            Field field = createField("protectedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(double.class, fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            Field field = createField("publicField");
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
            Field field = createField("deprecatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertNotNull(fieldAttributes.getAnnotation(Deprecated.class));
            assertNull(fieldAttributes.getAnnotation(Override.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = createField("deprecatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertTrue(annotations.size() > 0);
            assertTrue(annotations.stream().anyMatch(a -> a.annotationType() == Deprecated.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = createField("publicField");
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
            Field field = createField("publicField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}