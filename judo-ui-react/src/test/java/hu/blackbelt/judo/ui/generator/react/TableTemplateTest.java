package hu.blackbelt.judo.ui.generator.react;

/*-
 * #%L
 * JUDO UI React Frontend Generator
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTemplateTest {

    /**
     * The grid keys rows through resolveTransferId, whose chain prefers
     * __identifier over __tempId. A draft row seeded by the create template can
     * already carry an __identifier, so keying its edit mode by the raw tempId
     * addresses a row the grid does not have and crashes the page. The edit
     * mode must be keyed by the same resolved identity the grid uses.
     */
    /**
     * The container passes testIdOverride to its table child regardless of how
     * that child renders (grid, cards, tags), so every collection component's
     * props must accept it or the generated actor fails to type-check.
     */
    @Test
    void everyCollectionComponentAcceptsTheTableIdentityOverride() throws IOException {
        for (String path : new String[] {
                "/actor/src/containers/components/table/types.ts.hbs",
                "/actor/src/containers/components/cards/types.ts.hbs",
                "/actor/src/containers/components/tag/types.ts.hbs" }) {
            try (InputStream input = TableTemplateTest.class.getResourceAsStream(path)) {
                assertNotNull(input, path);
                String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(template.contains("testIdOverride?: string;"));
            }
        }
    }

    @Test
    void inlineCreateKeysEditModeByTheResolvedRowIdentity() throws IOException {
        for (String path : new String[] {
                "/actor/src/components/table/EagerTable.tsx.hbs",
                "/actor/src/components/table/LazyTable.tsx.hbs" }) {
            try (InputStream input = TableTemplateTest.class.getResourceAsStream(path)) {
                assertNotNull(input, path);
                String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

                assertTrue(template.contains("const draftRow = { ...(templateResult || {}), __tempId: tempId, __isNew: true } as unknown as T;"));
                assertTrue(template.contains("[getRowIdentifier(draftRow)]: { mode: GridRowModes.Edit },"));
            }
        }
    }

    private static final String TEMPLATE =
            "/actor/src/containers/components/table/index.tsx.hbs";
    private static final String ROW_ACTIONS_TEMPLATE =
            "/actor/src/components/table/table-row-actions.tsx.hbs";
    private static final String EAGER_TABLE_TEMPLATE =
            "/actor/src/components/table/EagerTable.tsx.hbs";
    private static final String LAZY_TABLE_TEMPLATE =
            "/actor/src/components/table/LazyTable.tsx.hbs";

    @Test
    void tableAndGridUseTheSameElementId() throws IOException {
        String template = loadTemplate(TEMPLATE);

        assertEquals(1, count(template, "buildTableTestId(testIdOverride ?? '{{ getElementId table }}')"));
        assertEquals(2, count(template, "tableTestId={testIdOverride ?? '{{ getElementId table }}'}"));
        assertEquals(0, count(template, "tableTestId={ '{{ getXMIID table }}' }"));
    }

    @Test
    void rowActionsUseCanonicalRoles() throws IOException {
        String tableTemplate = loadTemplate(TEMPLATE);
        String rowActionsTemplate = loadTemplate(ROW_ACTIONS_TEMPLATE);

        assertEquals(1, count(tableTemplate, "testId: '{{ getButtonRole button }}'"));
        assertEquals(1, count(rowActionsTemplate,
                "buildRowActionTestId(id, params.row, ROW_ACTION_ROLE_OVERFLOW)"));
        assertEquals(1, count(rowActionsTemplate,
                "data-testid={getRowActionTestId(id, params.row, a)}"));
        assertEquals(1, count(rowActionsTemplate,
                "testId: getRowActionTestId(id, params.row, action),"));
        assertEquals(0, count(rowActionsTemplate, "::button::${"));
    }

    @Test
    void modeledOpenPageRowActionsRemainVisible() throws IOException {
        String tableTemplate = loadTemplate(TEMPLATE);

        assertEquals(0, count(tableTemplate, "{{# unless button.actionDefinition.isOpenPageAction }}"));
        assertEquals(2, count(tableTemplate, "onRowClick={ actions.{{ simpleActionDefinitionName actionDefinition }} }"));
    }

    @Test
    void rowActionColumnUsesTheRuntimeInternalColumnName() throws IOException {
        String rowActionsTemplate = loadTemplate(ROW_ACTIONS_TEMPLATE);

        assertEquals(1, count(rowActionsTemplate, "field: '__actions__',"));
        assertEquals(0, count(rowActionsTemplate, "field: 'actions',"));
        assertEquals(1, count(loadTemplate(EAGER_TABLE_TEMPLATE), "pinnedColumns: { right: ['__actions__'] }"));
        assertEquals(1, count(loadTemplate(LAZY_TABLE_TEMPLATE), "pinnedColumns: { right: ['__actions__'] }"));
    }

    @Test
    void bothTableVariantsRegisterCanonicalTestIdSlots() throws IOException {
        assertCanonicalSlots(loadTemplate(EAGER_TABLE_TEMPLATE));
        assertCanonicalSlots(loadTemplate(LAZY_TABLE_TEMPLATE));
    }

    private static void assertCanonicalSlots(String template) {
        assertEquals(1, count(template, "row: TestIdRow,"));
        assertEquals(1, count(template, "cell: TestIdCell,"));
        assertEquals(1, count(template, "filterPanel: TestIdFilterPanel,"));
    }

    /**
     * Sorting is driven by clicking a column header. Without a canonical role a
     * shared spec has to reach for the MUI `.MuiDataGrid-columnHeaderTitle`
     * class, which is an implementation detail the runtime does not share.
     */
    @Test
    void columnHeadersCarryTheCanonicalHeaderRole() throws IOException {
        String template = loadTemplate(TEMPLATE);

        assertEquals(1, count(template, "renderHeader: () => ("));
        assertEquals(1, count(template,
                "buildColumnHeaderTestId(testIdOverride ?? '{{ getElementId table }}', '{{ column.attributeType.name }}')"));
    }

    private static String loadTemplate(String path) throws IOException {
        try (InputStream input = TableTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing template resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int count(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }
}
