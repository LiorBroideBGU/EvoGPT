import com.google.gson.FormattingStyle;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormattingStyleTest {


    @Test
    public void testWithNewlineEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            FormattingStyle newStyle = style.withNewline("\n");
            assertNotNull(newStyle);
            assertEquals("\n", newStyle.getNewline());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWithIndentEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            FormattingStyle newStyle = style.withIndent("    ");
            assertNotNull(newStyle);
            assertEquals("    ", newStyle.getIndent());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWithSpaceAfterSeparatorsEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            FormattingStyle newStyle = style.withSpaceAfterSeparators(false);
            assertNotNull(newStyle);
            assertFalse(newStyle.usesSpaceAfterSeparators());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetNewlineEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.COMPACT;
            assertEquals("", style.getNewline());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetIndentEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            assertEquals("  ", style.getIndent());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUsesSpaceAfterSeparatorsEnhanced() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            assertTrue(style.usesSpaceAfterSeparators());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWithNewline() {
        try {
            FormattingStyle style = FormattingStyle.COMPACT;
            FormattingStyle newStyle = style.withNewline("\n");
            assertEquals("Newline should be \\n", "\n", newStyle.getNewline());

            newStyle = style.withNewline("\r\n");
            assertEquals("Newline should be \\r\\n", "\r\n", newStyle.getNewline());

            // Test illegal newline
            try {
                style.withNewline("invalid");
                fail("Expected IllegalArgumentException for invalid newline");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Only combinations of \\n and \\r are allowed in newline."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWithIndent() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            FormattingStyle newStyle = style.withIndent("    ");
            assertEquals("Indent should be 4 spaces", "    ", newStyle.getIndent());

            newStyle = style.withIndent("\t");
            assertEquals("Indent should be a tab", "\t", newStyle.getIndent());

            // Test illegal indent
            try {
                style.withIndent("invalid indent!");
                fail("Expected IllegalArgumentException for invalid indent");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Only combinations of spaces and tabs are allowed in indent."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testWithSpaceAfterSeparators() {
        try {
            FormattingStyle style = FormattingStyle.COMPACT;
            FormattingStyle newStyle = style.withSpaceAfterSeparators(true);
            assertTrue("Should have space after separators", newStyle.usesSpaceAfterSeparators());

            newStyle = style.withSpaceAfterSeparators(false);
            assertFalse("Should not have space after separators", newStyle.usesSpaceAfterSeparators());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetNewline() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            assertEquals("Get newline should return \\n", "\n", style.getNewline());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetIndent() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            assertEquals("Get indent should return 2 spaces", "  ", style.getIndent());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testUsesSpaceAfterSeparators() {
        try {
            FormattingStyle style = FormattingStyle.PRETTY;
            assertTrue("Should use space after separators", style.usesSpaceAfterSeparators());

            FormattingStyle compactStyle = FormattingStyle.COMPACT;
            assertFalse("Should not use space after separators", compactStyle.usesSpaceAfterSeparators());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}