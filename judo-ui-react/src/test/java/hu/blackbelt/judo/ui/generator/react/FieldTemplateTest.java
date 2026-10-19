package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
     *
     * <p>The role has to be carried by an expression that also compiles. Unlike
     * {@code TextField}, whose {@code inputProps} is the permissive
     * {@code InputBaseComponentProps}, the checkbox's input slot is typed as a plain
     * {@code InputHTMLAttributes<HTMLInputElement>}, so a bare {@code data-testid} in an
     * object literal is rejected by the excess-property check. Spreading it in is the
     * one form that needs neither a cast nor an extra type import in every container
     * that happens to contain a checkbox. {@code inputProps} itself is deprecated in
     * MUI 7 in favour of {@code slotProps.input}.
     */
    @Test
    void checkboxEmitsTheCanonicalNativeInputId() throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/widget-fragments/checkbox.hbs")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildFieldTestId('{{ getElementId child }}', 'input')"));
            assertTrue(template.contains(
                    "slotProps={ { input: { ...{ 'data-testid': buildFieldTestId('{{ getElementId child }}', 'input') } } } }"),
                    "the input role must be spread into slotProps.input so the generated container compiles");
            assertFalse(template.contains("inputProps={"),
                    "inputProps is deprecated in MUI 7 and its type rejects a literal data-testid");
        }
    }

    /**
     * A multiline text widget renders a real {@code <textarea>}, so it carries the
     * canonical {@code field::<id>::input} role like every other typable field. The role
     * must not hang off the optional {@code maxLength} block — a multiline attribute
     * without a declared maximum is still typed into.
     */
    @Test
    void textareaEmitsTheCanonicalNativeInputId() throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/widget-fragments/textarea.hbs")) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("'data-testid': buildFieldTestId('{{ getElementId child }}', 'input'),"),
                    "the multiline widget must carry the canonical input role");
            int role = template.indexOf("'data-testid': buildFieldTestId('{{ getElementId child }}', 'input'),");
            int guard = template.indexOf("{{# if child.attributeType.dataType.maxLength }}\n        inputProps");
            assertTrue(guard < 0 || role < guard || guard == -1,
                    "the input role must not be nested inside the optional maxLength guard");
        }
    }

    /**
     * Disabling the accessible field DOM structure leaves a date widget with one real
     * {@code <input>}, so the canonical {@code field::<id>::input} role has to reach it —
     * the runtime exposes the same role on its date inputs, and the E2E suite fills
     * dates through it. The role rides on the text field's {@code inputProps}, whose
     * {@code InputBaseComponentProps} type accepts arbitrary attributes.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/actor/src/containers/widget-fragments/dateinput.hbs",
            "/actor/src/containers/widget-fragments/datetimeinput.hbs",
            "/actor/src/containers/widget-fragments/timeinput.hbs"
    })
    void dateWidgetsEmitTheCanonicalNativeInputId(String resource) throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(resource)) {
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains(
                    "inputProps: { 'data-testid': buildFieldTestId('{{ getElementId child }}', 'input') },"),
                    resource + " must carry the input role on the picker's text field");
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
