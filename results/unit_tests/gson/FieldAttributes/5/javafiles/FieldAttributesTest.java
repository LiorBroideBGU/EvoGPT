import org.junit.Test;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = SampleClass.class.getDeclaredField("sampleField");
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
            Field field = SampleClass.class.getDeclaredField("sampleField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals("sampleField", fieldAttributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            Field field = SampleClass.class.getDeclaredField("sampleField");
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
            Field field = SampleClass.class.getDeclaredField("sampleField");
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
            Field field = SampleClass.class.getDeclaredField("annotatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            SampleAnnotation annotation = fieldAttributes.getAnnotation(SampleAnnotation.class);
            assertNotNull(annotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = SampleClass.class.getDeclaredField("annotatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertTrue(annotations.size() > 0);
            assertTrue(annotations.stream().anyMatch(a -> a instanceof SampleAnnotation));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = SampleClass.class.getDeclaredField("sampleField");
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
            Field field = SampleClass.class.getDeclaredField("sampleField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(field.toString(), fieldAttributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static class SampleClass {
        private String sampleField;

        @SampleAnnotation
        private String annotatedField;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    static @interface SampleAnnotation {}
}