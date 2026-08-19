package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTemplateTest {

    @Test
    void textInputEmitsTheCanonicalNativeInputId() throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/widget-fragments/textinput.hbs")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("'data-testid': buildFieldTestId('{{ getElementId child }}', 'input')"));
        }
    }

    /**
     * The numeric widget carried only the wrapper role, so its focusable input
     * had no `field::<id>::input` and a shared spec could not type into it. The
     * runtime emits the role for both widget kinds.
     */
    @Test
    void numericInputEmitsTheCanonicalNativeInputId() throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/widget-fragments/numericinput.hbs")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("'data-testid': buildFieldTestId('{{ getElementId child }}', 'input')"));
        }
    }
}
