import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FieldAttributesTest {

    private static Field getField(String fieldName) throws NoSuchFieldException {
        return TestClass.class.getDeclaredField(fieldName);
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = getField("testField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertEquals(TestClass.class, fieldAttributes.getDeclaringClass());
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
            assertNotNull(fieldAttributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            Field field = getField("testField");
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
            Field field = getField("annotatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            assertNotNull(fieldAttributes.getAnnotation(TestAnnotation.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = getField("annotatedField");
            FieldAttributes fieldAttributes = new FieldAttributes(field);
            Collection<Annotation> annotations = fieldAttributes.getAnnotations();
            assertNotNull(annotations);
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
            assertEquals(true, fieldAttributes.hasModifier(Modifier.PRIVATE));
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

    private static class TestClass {
        private String testField;
        
        @TestAnnotation
        private String annotatedField;
    }

    private @interface TestAnnotation {}
}