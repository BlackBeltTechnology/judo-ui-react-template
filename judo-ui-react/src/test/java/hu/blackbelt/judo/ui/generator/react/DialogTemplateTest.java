package hu.blackbelt.judo.ui.generator.react;

/*-
 * #%L
 * JUDO UI React
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

class DialogTemplateTest {

    private static final String CONFIRMATION_DIALOG_TEMPLATE =
            "/actor/src/components/dialog/ConfirmationDialog.tsx.hbs";

    private static final String CRUD_DIALOG_TEMPLATE = "/actor/src/hooks/useCRUDDialog.tsx.hbs";

    /**
     * The bulk operation dialog confirms before it runs anything, so it is
     * addressed through the shared `confirmation` dialog identity rather than
     * through hand-written per-widget ids.
     */
    @Test
    void bulkOperationDialogUsesTheCanonicalConfirmationIdentity() throws IOException {
        String template = loadTemplate(CRUD_DIALOG_TEMPLATE);

        assertEquals(1, count(template, "const CONFIRMATION_DIALOG_ID = 'confirmation';"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'title')"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'cancel')"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'confirm')"));
        assertEquals(0, count(template, "use-crud-dialog"));
    }

    @Test
    void confirmationDialogUsesTheCanonicalRuntimeDialogIdentity() throws IOException {
        String template = loadTemplate(CONFIRMATION_DIALOG_TEMPLATE);

        assertEquals(1, count(template, "const CONFIRMATION_DIALOG_ID = 'confirmation';"));
        assertEquals(1, count(template, "buildDialogTestId(CONFIRMATION_DIALOG_ID)"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'title')"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'content')"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'cancel')"));
        assertEquals(1, count(template, "buildDialogRoleTestId(CONFIRMATION_DIALOG_ID, 'confirm')"));
        assertEquals(0, count(template, "buildDialogTestId(id)"));
        assertEquals(0, count(template, "buildDialogRoleTestId(id,"));
    }

    /**
     * Clearing a collection removes every element in one click, so it asks first.
     * Unsetting a single relation deliberately does not: it breaks one link and is
     * reversible before saving.
     */
    @Test
    void clearingACollectionAsksForConfirmation() throws IOException {
        String template = loadTemplate("/actor/src/pages/actions/ClearAction.fragment.hbs");

        assertEquals(1, count(template, "openConfirmDialog("));
        assertEquals(1, count(template, "judo.modal.confirm.confirm-clear"));
    }

    /**
     * Deleting several selected rows destroys data, so it asks first on every
     * table. The lazy path already confirms through its bulk CRUD dialog; the
     * eager path used to drop the selection from the draft without asking.
     */
    @Test
    void bulkDeletingRowsAsksForConfirmation() throws IOException {
        String template = loadTemplate("/actor/src/pages/actions/BulkDeleteAction.fragment.hbs");

        assertEquals(1, count(template, "openConfirmDialog("));
        assertEquals(1, count(template, "judo.modal.confirm.confirm-bulk-delete"));
    }

    private static String loadTemplate(String path) throws IOException {
        try (InputStream input = DialogTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing template resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int count(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }
}
