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
        private String testField;
    }
}