package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /**
     * MUI puts the field test ID on the checkbox's span wrapper, which cannot be checked,
     * so the canonical {@code field::<id>::input} role has to reach the inner input — the
     * same gap the numeric widget had, and the shape the runtime emits.
     */
    @Test
    void checkboxEmitsTheCanonicalNativeInputId() throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/widget-fragments/checkbox.hbs")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildFieldTestId('{{ getElementId child }}', 'input')"));
        }
    }

    /**
     * MUI X's accessible field structure renders a date editor as a contenteditable
     * section list and hides the value in a 1x1 input, so the canonical
     * {@code field::<id>::input} role would address an element that nothing can be
     * typed or pasted into. Every date-like widget therefore keeps the single-input
     * structure, matching the runtime's date inputs.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/actor/src/containers/widget-fragments/dateinput.hbs",
            "/actor/src/containers/widget-fragments/datetimeinput.hbs",
            "/actor/src/containers/widget-fragments/timeinput.hbs",
            "/actor/src/components/dialog/FilterDialog.tsx.hbs",
            "/actor/src/components/table/SingleValueFilterComponent.tsx.hbs"
    })
    void dateWidgetsUseTheSingleInputStructure(String resource) throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(resource)) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            long pickers = countOccurrences(template, "<DatePicker")
                    + countOccurrences(template, "<DateTimePicker")
                    + countOccurrences(template, "<TimePicker");
            assertTrue(pickers > 0, resource + " declares no date picker");
            assertEquals(
                    pickers,
                    countOccurrences(template, "enableAccessibleFieldDOMStructure={false}"),
                    resource + " must opt every picker out of the sectioned DOM structure");
        }
    }

    private static long countOccurrences(String text, String token) {
        long count = 0;
        int index = text.indexOf(token);
        while (index >= 0) {
            count += 1;
            index = text.indexOf(token, index + token.length());
        }
        return count;
    }
}
