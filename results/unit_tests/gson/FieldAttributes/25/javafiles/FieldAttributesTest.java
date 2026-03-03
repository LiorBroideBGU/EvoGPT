import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.Assert.*;

public class FieldAttributesTest {

    private static Field getTestField() throws NoSuchFieldException {
        return TestClass.class.getDeclaredField("testField");
    }

    private static class TestClass {
        @Deprecated
        public int testField;
    }

    @Test
    public void testGetDeclaringClass() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
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
            FieldAttributes attributes = new FieldAttributes(field);
            assertEquals("Field name should be 'testField'", "testField", attributes.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredType() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            assertEquals("Declared type should be int", int.class, attributes.getDeclaredType());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetDeclaredClass() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            assertEquals("Declared class should be int", int.class, attributes.getDeclaredClass());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotation() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            Deprecated deprecatedAnnotation = attributes.getAnnotation(Deprecated.class);
            assertNotNull("Should find Deprecated annotation", deprecatedAnnotation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetAnnotations() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            Collection<Annotation> annotations = attributes.getAnnotations();
            assertFalse("Annotations collection should not be empty", annotations.isEmpty());
            assertTrue("Annotations should contain Deprecated", annotations.contains(deprecatedAnnotation()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static Annotation deprecatedAnnotation() {
        return TestClass.class.getDeclaredField("testField").getAnnotation(Deprecated.class);
    }

    @Test
    public void testHasModifier() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            assertTrue("Field should be public", attributes.hasModifier(Modifier.PUBLIC));
            assertFalse("Field should not be static", attributes.hasModifier(Modifier.STATIC));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testToString() {
        try {
            Field field = getTestField();
            FieldAttributes attributes = new FieldAttributes(field);
            assertEquals("toString should match field's toString", field.toString(), attributes.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}