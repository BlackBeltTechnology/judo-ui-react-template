package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Filtering is a dialog on both engines. Its selector vocabulary must be the
 * canonical `table::<id>::filter-panel` family so a single engine-neutral spec
 * drives React and runtime alike.
 */
class FilterTemplateTest {

    private String template(String path) throws IOException {
        try (InputStream input = FilterTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void theFilterDialogCarriesTheCanonicalFilterPanelRoles() throws IOException {
        String dialog = template("/actor/src/components/dialog/FilterDialog.tsx.hbs");

        assertTrue(dialog.contains("buildFilterPanelTestId(tableTestId)"));
        assertTrue(dialog.contains("buildFilterPanelColumnInputTestId(tableTestId)"));
        // Operator and value are row-scoped: several rows may filter the same attribute.
        assertTrue(dialog.contains("buildFilterPanelOperatorInputTestId(tableTestId, index)"));
        assertTrue(dialog.contains("buildFilterPanelValueInputTestId(tableTestId, index)"));
    }

    /**
     * The value role must target the element that receives focus and keystrokes,
     * not a wrapper, so an engine-neutral spec can type into it directly.
     */
    @Test
    void theFilterValueRoleIsCarriedByTheFocusableInput() throws IOException {
        String dialog = template("/actor/src/components/dialog/FilterDialog.tsx.hbs");

        assertTrue(dialog.contains("inputProps=\\{{ 'data-testid': buildFilterPanelValueInputTestId(tableTestId, index) }}"));
        assertTrue(!dialog.contains("<div data-testid={buildFilterPanelValueInputTestId(tableTestId, index)}>"));
    }

    /**
     * Several filter rows may target the SAME attribute (`name LIKE x` AND
     * `name NOT_EQUAL y`), so a row is addressable by its index, not by its
     * attribute name. Runtime emits `::row::<n>::operator` / `::value` /
     * `::remove`; React must use the identical vocabulary.
     */
    @Test
    void eachFilterRowIsAddressableByItsCanonicalRowIndex() throws IOException {
        String helpers = template("/actor/src/utilities/transfer-id.ts.hbs");

        assertTrue(helpers.contains("export function buildFilterPanelRowTestId(tableId: TestIdKey, index: number): string {"));
        assertTrue(helpers.contains("::row::${index}"));
        assertTrue(helpers.contains("export function buildFilterPanelOperatorInputTestId(tableId: TestIdKey, index?: number): string {"));
        assertTrue(helpers.contains("export function buildFilterPanelValueInputTestId(tableId: TestIdKey, index?: number): string {"));

        String dialog = template("/actor/src/components/dialog/FilterDialog.tsx.hbs");
        assertTrue(dialog.contains("buildFilterPanelOperatorInputTestId(tableTestId, index)"));
        assertTrue(dialog.contains("buildFilterPanelValueInputTestId(tableTestId, index)"));
        assertTrue(dialog.contains("buildFilterPanelRowTestId(tableTestId, index)"));
    }

    /**
     * Every operator option must carry a canonical role named by the backend
     * operator (`like`, `notEqual`, `matches`), because the visible label is
     * translated and cannot be matched by an engine-neutral spec. The template
     * previously reused one `field::<id>::value` ID for every option.
     */
    @Test
    void eachOperatorOptionCarriesACanonicalRoleNamedByTheBackendOperator() throws IOException {
        String helpers = template("/actor/src/utilities/transfer-id.ts.hbs");
        assertTrue(helpers.contains("export function buildFilterPanelOperatorOptionTestId("));
        assertTrue(helpers.contains("return `${buildFilterPanelOperatorInputTestId(tableId, index)}::${operation}`;"));

        String dialog = template("/actor/src/components/dialog/FilterDialog.tsx.hbs");
        assertTrue(dialog.contains("buildFilterPanelOperatorOptionTestId(tableTestId, index, item)"));
        // The old shared per-option ID must be gone.
        assertTrue(!dialog.contains("<MenuItem className=\"filter-operation-item\" data-testid={buildFieldTestId(id, 'value')}"));
    }

    @Test
    void theFilterDialogIdentityIsThreadedThroughTheDialogPlumbing() throws IOException {
        assertTrue(template("/actor/src/components-api/dialog/FilterDialog.ts.hbs").contains("tableTestId: string;"));
        assertTrue(template("/actor/src/components-api/dialog/DialogContext.ts.hbs").contains("tableTestId: string"));
        assertTrue(template("/actor/src/components/dialog/DialogProvider.tsx.hbs").contains("tableTestId={tableTestId}"));
        assertTrue(template("/actor/src/pages/actions/FilterAction.fragment.hbs").contains("tableTestId"));

        for (String path : new String[] {
                "/actor/src/components/table/EagerTable.tsx.hbs",
                "/actor/src/components/table/LazyTable.tsx.hbs" }) {
            assertTrue(template(path).contains("await filterAction(toolBarAction.id, tableTestId);"));
        }
    }
}
