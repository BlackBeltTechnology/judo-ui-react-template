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
     * An enumeration combo is an {@code Autocomplete} whose typable element is the inner
     * {@code <input>}; the runtime puts {@code field::<id>::input} there
     * ({@code EnumerationComboComponent} htmlInput slot). Autocomplete's own input props
     * (ref, value, handlers) must be kept, so the role is merged into {@code params.inputProps}.
     */
    @Test
    void enumerationComboEmitsTheCanonicalNativeInputId() throws IOException {
        String template = read("/actor/src/containers/widget-fragments/enumerationcombo.hbs");
        assertTrue(template.contains(
                "inputProps={ { ...params.inputProps, 'data-testid': buildFieldTestId('{{ getElementId child }}', 'input') } }"),
                "the combo's native input must carry the canonical input role");
    }

    /**
     * Date-like widgets keep MUI X's sectioned field, like the runtime on MUI X v9.
     * {@code field::<id>} sits on the text field root (the click target for keyboard
     * entry) and {@code field::<id>::input} on the hidden input holding the formatted
     * value, both through the v9-shaped nested slot props.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/actor/src/containers/widget-fragments/dateinput.hbs",
            "/actor/src/containers/widget-fragments/datetimeinput.hbs",
            "/actor/src/containers/widget-fragments/timeinput.hbs"
    })
    void dateWidgetsEmitTheCanonicalNativeInputId(String resource) throws IOException {
        String template = read(resource);
        assertTrue(template.contains(
                "htmlInput: { 'data-testid': buildFieldTestId('{{ getElementId child }}', 'input') },"),
                resource + " must carry the input role on the text field's htmlInput slot");
        assertTrue(template.contains("...{ 'data-testid': buildFieldTestId('{{ getElementId child }}') },"),
                resource + " must carry the field role on the text field root");
        assertFalse(template.contains("inputRef={autoFocusInputRef}"),
                resource + " must autofocus the field, not the aria-hidden value input");
    }

    /**
     * The template renders the same sectioned structure as the runtime, which cannot
     * opt out on MUI X v9. The legacy text-field props are gone in v9 as well.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/actor/src/containers/widget-fragments/dateinput.hbs",
            "/actor/src/containers/widget-fragments/datetimeinput.hbs",
            "/actor/src/containers/widget-fragments/timeinput.hbs",
            "/actor/src/components/dialog/FilterDialog.tsx.hbs",
            "/actor/src/components/table/SingleValueFilterComponent.tsx.hbs"
    })
    void dateWidgetsUseTheSectionedStructure(String resource) throws IOException {
        String template = read(resource);
        long pickers = countOccurrences(template, "<DatePicker")
                + countOccurrences(template, "<DateTimePicker")
                + countOccurrences(template, "<TimePicker");
        assertTrue(pickers > 0, resource + " declares no date picker");
        assertEquals(0, countOccurrences(template, "enableAccessibleFieldDOMStructure"),
                resource + " must keep MUI X's sectioned field");
        for (String legacy : new String[] {"inputProps:", "InputProps:", "InputLabelProps:"}) {
            assertFalse(template.contains(legacy), resource + " still uses the legacy text-field prop " + legacy);
        }
    }

    /** Date filter pickers carry the filter-value role on the root and {@code ::input} on the value input. */
    @Test
    void filterDialogDatePickersFollowTheFieldGrammar() throws IOException {
        String template = read("/actor/src/components/dialog/FilterDialog.tsx.hbs");
        assertFalse(template.contains("ponytail"), "the date filter ponytail must be closed");
        long pickers = countOccurrences(template, "<DatePicker") + countOccurrences(template, "<DateTimePicker");
        assertEquals(pickers, countOccurrences(template, "...{ 'data-testid': valueTestId },"),
                "every filter picker root carries the value role");
        assertEquals(pickers, countOccurrences(template, "htmlInput: { 'data-testid': `${valueTestId}::input` }"),
                "every filter picker value input carries ::input");
    }

    /** Fixed (always-shown) filters use the runtime grammar {@code table::<id>::fixed-filter::<attr>}. */
    @Test
    void fixedFilterDatePickersFollowTheFieldGrammar() throws IOException {
        String template = read("/actor/src/components/table/SingleValueFilterComponent.tsx.hbs");
        long pickers = countOccurrences(template, "<DatePicker") + countOccurrences(template, "<DateTimePicker");
        assertEquals(pickers, countOccurrences(template, "...{ 'data-testid': testId },"),
                "every fixed filter picker root carries the fixed-filter role");
        assertEquals(pickers, countOccurrences(template, "htmlInput: { 'data-testid': `${testId}::input` }"),
                "every fixed filter picker value input carries ::input");
        assertTrue(read("/actor/src/components/table/FixedTableFilters.tsx.hbs")
                .contains("testId={buildFixedFilterTestId(tableTestId, f.attributeName)}"));
    }

    private static String read(String resource) throws IOException {
        try (InputStream input = FieldTemplateTest.class.getResourceAsStream(resource)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
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
