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

import hu.blackbelt.judo.meta.ui.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the three-mode behavior of {@code Table.checkboxSelection} and the
 * bulk-button collapse in {@link UiWidgetHelper#tableButtonVisibilityConditions}.
 *
 * The truth table for own-page context is:
 *
 *   checkboxSelection | tableHasAnyBulkAction | checkboxSelectionForOwnPage |
 *                     |                       | multiSelectAllowedForOwnPage|
 *   ------------------+-----------------------+-----------------------------+
 *   null  (ENABLED)   | false                 | true                        |
 *   null  (ENABLED)   | true                  | true                        |
 *   ENABLED           | false                 | true                        |
 *   ENABLED           | true                  | true                        |
 *   DISABLED          | false                 | false                       |
 *   DISABLED          | true                  | false                       |
 *   AUTO              | false                 | false                       |
 *   AUTO              | true                  | true                        |
 *
 * Selector-mode behavior is template-level (a runtime {@code isSelector} branch in
 * {@code containers/components/table/index.tsx.hbs}) and is not testable from Java;
 * it is covered by the itest snapshots in {@code RelationTest}.
 */
public class CheckboxSelectionTest {

    private static final UiFactory FACTORY = UiFactory.eINSTANCE;

    private static Table emptyTable() {
        Table t = FACTORY.createTable();
        // Tables in real models always have a TableActionButtonGroup, but the helper
        // must also be null-safe; we test both shapes explicitly.
        return t;
    }

    private static Table tableWithEmptyButtonGroup() {
        Table t = FACTORY.createTable();
        t.setTableActionButtonGroup(FACTORY.createButtonGroup());
        return t;
    }

    /**
     * On {@link ActionDefinition} only {@code isBulk} is a stored EAttribute.
     * {@code getIsBulkDeleteAction()}, {@code getIsBulkRemoveAction()}, and
     * {@code getIsBulkCallOperationAction()} are derived from the EClass identity
     * (e.g. {@code this instanceof BulkDeleteActionDefinition}). The factory takes
     * care of the latter; Tatami sets the former during model build and we mirror it
     * here in fixtures so {@link UiWidgetHelper#tableButtonVisibilityConditions}
     * sees a realistic shape.
     */
    private static BulkDeleteActionDefinition newBulkDeleteAction() {
        BulkDeleteActionDefinition ad = FACTORY.createBulkDeleteActionDefinition();
        ad.setIsBulk(true);
        return ad;
    }

    private static BulkRemoveActionDefinition newBulkRemoveAction() {
        BulkRemoveActionDefinition ad = FACTORY.createBulkRemoveActionDefinition();
        ad.setIsBulk(true);
        return ad;
    }

    private static BulkCallOperationActionDefinition newBulkCallOperationAction() {
        BulkCallOperationActionDefinition ad = FACTORY.createBulkCallOperationActionDefinition();
        ad.setIsBulk(true);
        return ad;
    }

    private static Table tableWithBulkDelete() {
        Table t = tableWithEmptyButtonGroup();
        Button b = FACTORY.createButton();
        b.setActionDefinition(newBulkDeleteAction());
        t.getTableActionButtonGroup().getButtons().add(b);
        return t;
    }

    private static Table tableWithBulkRemove() {
        Table t = tableWithEmptyButtonGroup();
        Button b = FACTORY.createButton();
        b.setActionDefinition(newBulkRemoveAction());
        t.getTableActionButtonGroup().getButtons().add(b);
        return t;
    }

    private static Table tableWithBulkCallOperation() {
        Table t = tableWithEmptyButtonGroup();
        Button b = FACTORY.createButton();
        b.setActionDefinition(newBulkCallOperationAction());
        t.getTableActionButtonGroup().getButtons().add(b);
        return t;
    }

    private static Table tableWithNonBulkOnly() {
        Table t = tableWithEmptyButtonGroup();
        Button b = FACTORY.createButton();
        // DeleteActionDefinition is intentionally non-bulk (isBulk left at default false).
        b.setActionDefinition(FACTORY.createDeleteActionDefinition());
        t.getTableActionButtonGroup().getButtons().add(b);
        return t;
    }

    // ---- row-action button-group fixtures ----

    /**
     * Adds a row action button (with the given action definition) to the table's
     * {@code rowActionButtonGroup}, creating the group if missing.
     */
    private static Table withRowAction(Table t, ActionDefinition ad) {
        if (t.getRowActionButtonGroup() == null) {
            t.setRowActionButtonGroup(FACTORY.createButtonGroup());
        }
        Button b = FACTORY.createButton();
        b.setActionDefinition(ad);
        t.getRowActionButtonGroup().getButtons().add(b);
        return t;
    }

    private static Table tableWithEmptyRowActionGroup() {
        Table t = tableWithEmptyButtonGroup();
        t.setRowActionButtonGroup(FACTORY.createButtonGroup());
        return t;
    }

    private static Table tableWithRowDelete() {
        return withRowAction(tableWithEmptyButtonGroup(), FACTORY.createRowDeleteActionDefinition());
    }

    private static Table tableWithRowParameterless() {
        return withRowAction(tableWithEmptyButtonGroup(), FACTORY.createParameterlessCallOperationActionDefinition());
    }

    private static Table tableWithRowInputForm() {
        return withRowAction(tableWithEmptyButtonGroup(), FACTORY.createInputFormCallOperationActionDefinition());
    }

    private static Table tableWithRowInputSelector() {
        return withRowAction(tableWithEmptyButtonGroup(), FACTORY.createInputSelectorCallOperationActionDefinition());
    }

    private static Table tableWithRowOpenPage() {
        return withRowAction(tableWithEmptyButtonGroup(), FACTORY.createOpenPageActionDefinition());
    }

    // ---- tableHasAnyBulkAction ----

    @Test
    void tableHasAnyBulkAction_nullTable_false() {
        assertFalse(UiTableHelper.tableHasAnyBulkAction(null));
    }

    @Test
    void tableHasAnyBulkAction_nullButtonGroup_false() {
        assertFalse(UiTableHelper.tableHasAnyBulkAction(emptyTable()));
    }

    @Test
    void tableHasAnyBulkAction_emptyButtonGroup_false() {
        assertFalse(UiTableHelper.tableHasAnyBulkAction(tableWithEmptyButtonGroup()));
    }

    @Test
    void tableHasAnyBulkAction_nonBulkOnly_false() {
        assertFalse(UiTableHelper.tableHasAnyBulkAction(tableWithNonBulkOnly()));
    }

    @Test
    void tableHasAnyBulkAction_bulkDelete_true() {
        assertTrue(UiTableHelper.tableHasAnyBulkAction(tableWithBulkDelete()));
    }

    @Test
    void tableHasAnyBulkAction_bulkRemove_true() {
        assertTrue(UiTableHelper.tableHasAnyBulkAction(tableWithBulkRemove()));
    }

    @Test
    void tableHasAnyBulkAction_bulkCallOperation_true() {
        assertTrue(UiTableHelper.tableHasAnyBulkAction(tableWithBulkCallOperation()));
    }

    // ---- checkboxSelectionForOwnPage ----

    @Test
    void checkboxSelectionForOwnPage_nullEnumNoBulk_true() {
        Table t = tableWithEmptyButtonGroup();
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_nullEnumWithBulk_true() {
        Table t = tableWithBulkDelete();
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_enabledNoBulk_true() {
        Table t = tableWithEmptyButtonGroup();
        t.setCheckboxSelection(CheckboxSelection.ENABLED);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_enabledWithBulk_true() {
        Table t = tableWithBulkDelete();
        t.setCheckboxSelection(CheckboxSelection.ENABLED);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_disabledNoBulk_false() {
        Table t = tableWithEmptyButtonGroup();
        t.setCheckboxSelection(CheckboxSelection.DISABLED);
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_disabledWithBulk_false() {
        Table t = tableWithBulkDelete();
        t.setCheckboxSelection(CheckboxSelection.DISABLED);
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoNoBulk_false() {
        Table t = tableWithEmptyButtonGroup();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoWithBulkDelete_true() {
        Table t = tableWithBulkDelete();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoWithBulkRemove_true() {
        Table t = tableWithBulkRemove();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoWithBulkCallOperation_true() {
        Table t = tableWithBulkCallOperation();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoNonBulkOnly_false() {
        Table t = tableWithNonBulkOnly();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    // ---- multiSelectAllowedForOwnPage (mirrors checkboxSelectionForOwnPage today) ----

    @Test
    void multiSelectAllowedForOwnPage_mirrorsCheckboxSelectionForOwnPage_disabled() {
        Table t = tableWithBulkDelete();
        t.setCheckboxSelection(CheckboxSelection.DISABLED);
        assertEquals(
                UiTableHelper.checkboxSelectionForOwnPage(t),
                UiTableHelper.multiSelectAllowedForOwnPage(t));
        assertFalse(UiTableHelper.multiSelectAllowedForOwnPage(t));
    }

    @Test
    void multiSelectAllowedForOwnPage_mirrorsCheckboxSelectionForOwnPage_auto() {
        Table withBulk = tableWithBulkDelete();
        withBulk.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.multiSelectAllowedForOwnPage(withBulk));

        Table withoutBulk = tableWithEmptyButtonGroup();
        withoutBulk.setCheckboxSelection(CheckboxSelection.AUTO);
        assertFalse(UiTableHelper.multiSelectAllowedForOwnPage(withoutBulk));
    }

    // ---- UiWidgetHelper.tableButtonVisibilityConditions collapse ----

    @Test
    void tableButtonVisibilityConditions_disabledTableCollapsesBulkButtonToFalse() {
        Table table = tableWithBulkDelete();
        table.setCheckboxSelection(CheckboxSelection.DISABLED);
        Button bulkButton = table.getTableActionButtonGroup().getButtons().get(0);
        PageContainer container = FACTORY.createPageContainer();

        assertEquals("false",
                UiWidgetHelper.tableButtonVisibilityConditions(bulkButton, table, container));
    }

    @Test
    void tableButtonVisibilityConditions_enabledTableKeepsSelectionExpression() {
        Table table = tableWithBulkDelete();
        table.setCheckboxSelection(CheckboxSelection.ENABLED);
        Button bulkButton = table.getTableActionButtonGroup().getButtons().get(0);
        PageContainer container = FACTORY.createPageContainer();

        String result = UiWidgetHelper.tableButtonVisibilityConditions(bulkButton, table, container);
        // Pre-existing expression survives unchanged for the ENABLED case.
        assertEquals("selectionModel.ids.size > 0", result);
    }

    // ---- isRowActionBatchable ----

    @Test
    void isRowActionBatchable_null_false() {
        assertFalse(UiTableHelper.isRowActionBatchable(null));
    }

    @Test
    void isRowActionBatchable_rowDelete_true() {
        assertTrue(UiTableHelper.isRowActionBatchable(FACTORY.createRowDeleteActionDefinition()));
    }

    @Test
    void isRowActionBatchable_parameterless_true() {
        assertTrue(UiTableHelper.isRowActionBatchable(FACTORY.createParameterlessCallOperationActionDefinition()));
    }

    @Test
    void isRowActionBatchable_inputForm_false() {
        assertFalse(UiTableHelper.isRowActionBatchable(FACTORY.createInputFormCallOperationActionDefinition()));
    }

    @Test
    void isRowActionBatchable_inputSelector_false() {
        assertFalse(UiTableHelper.isRowActionBatchable(FACTORY.createInputSelectorCallOperationActionDefinition()));
    }

    @Test
    void isRowActionBatchable_openPage_false() {
        assertFalse(UiTableHelper.isRowActionBatchable(FACTORY.createOpenPageActionDefinition()));
    }

    // ---- tableHasAnyBatchableRowAction (STRICT variant) ----

    @Test
    void tableHasAnyBatchableRowAction_nullTable_false() {
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(null));
    }

    @Test
    void tableHasAnyBatchableRowAction_nullRowGroup_false() {
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(emptyTable()));
    }

    @Test
    void tableHasAnyBatchableRowAction_emptyRowGroup_false() {
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(tableWithEmptyRowActionGroup()));
    }

    @Test
    void tableHasAnyBatchableRowAction_rowDeleteOnly_true() {
        assertTrue(UiTableHelper.tableHasAnyBatchableRowAction(tableWithRowDelete()));
    }

    @Test
    void tableHasAnyBatchableRowAction_parameterlessOnly_true() {
        assertTrue(UiTableHelper.tableHasAnyBatchableRowAction(tableWithRowParameterless()));
    }

    @Test
    void tableHasAnyBatchableRowAction_rowDeleteAndParameterless_true() {
        Table t = tableWithRowDelete();
        withRowAction(t, FACTORY.createParameterlessCallOperationActionDefinition());
        assertTrue(UiTableHelper.tableHasAnyBatchableRowAction(t));
    }

    @Test
    void tableHasAnyBatchableRowAction_rowDeletePlusInputForm_falseByVeto() {
        Table t = tableWithRowDelete();
        withRowAction(t, FACTORY.createInputFormCallOperationActionDefinition());
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(t));
    }

    @Test
    void tableHasAnyBatchableRowAction_rowDeletePlusInputSelector_falseByVeto() {
        Table t = tableWithRowDelete();
        withRowAction(t, FACTORY.createInputSelectorCallOperationActionDefinition());
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(t));
    }

    @Test
    void tableHasAnyBatchableRowAction_inputFormOnly_false() {
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(tableWithRowInputForm()));
    }

    @Test
    void tableHasAnyBatchableRowAction_inputSelectorOnly_false() {
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(tableWithRowInputSelector()));
    }

    @Test
    void tableHasAnyBatchableRowAction_openPageOnly_false() {
        // Neutral category: no batchable action means false even though no veto fires.
        assertFalse(UiTableHelper.tableHasAnyBatchableRowAction(tableWithRowOpenPage()));
    }

    @Test
    void tableHasAnyBatchableRowAction_rowDeletePlusOpenPage_true() {
        Table t = tableWithRowDelete();
        withRowAction(t, FACTORY.createOpenPageActionDefinition());
        // OpenPage is neutral: doesn't enable, doesn't veto.
        assertTrue(UiTableHelper.tableHasAnyBatchableRowAction(t));
    }

    // ---- checkboxSelectionForOwnPage AUTO + row-action matrix ----

    @Test
    void checkboxSelectionForOwnPage_autoRowDeleteOnly_true() {
        Table t = tableWithRowDelete();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoParameterlessOnly_true() {
        Table t = tableWithRowParameterless();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoRowDeletePlusInputForm_falseByVeto() {
        Table t = tableWithRowDelete();
        withRowAction(t, FACTORY.createInputFormCallOperationActionDefinition());
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoBulkAndInputFormRow_trueByBulk() {
        // Explicit BulkX still wins regardless of row-action veto.
        Table t = tableWithBulkDelete();
        withRowAction(t, FACTORY.createInputFormCallOperationActionDefinition());
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void checkboxSelectionForOwnPage_autoOpenPageRowOnly_false() {
        Table t = tableWithRowOpenPage();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        // No batchable, no bulk → false.
        assertFalse(UiTableHelper.checkboxSelectionForOwnPage(t));
    }

    @Test
    void multiSelectAllowedForOwnPage_autoRowDeleteOnly_true() {
        Table t = tableWithRowDelete();
        t.setCheckboxSelection(CheckboxSelection.AUTO);
        assertTrue(UiTableHelper.multiSelectAllowedForOwnPage(t));
    }

    @Test
    void tableButtonVisibilityConditions_autoWithoutBulkUnreachable() {
        // AUTO without bulk action means no bulk button exists, so the branch is
        // unreachable in practice. We still document the helper's null-safety on a
        // hypothetical orphan bulk button attached to an AUTO-no-bulk table by
        // exercising the collapse path.
        Table table = tableWithEmptyButtonGroup();
        table.setCheckboxSelection(CheckboxSelection.AUTO);
        Button orphan = FACTORY.createButton();
        orphan.setActionDefinition(newBulkDeleteAction());
        // Not added to the button group, so tableHasAnyBulkAction(table) == false,
        // therefore multiSelectAllowedForOwnPage(table) == false, therefore collapse.
        PageContainer container = FACTORY.createPageContainer();

        assertEquals("false",
                UiWidgetHelper.tableButtonVisibilityConditions(orphan, table, container));
    }
}
