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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The relation selector overlay must be addressable through the field that owns it,
 * matching the runtime contract `field::&lt;linkId&gt;::selector` with the
 * `title`, `cancel` and `confirm` roles. Without this, a cross-engine test cannot
 * drive relation selection without branching on the engine.
 *
 * Scope: the set selector, whose owner is a link field. The add selector is owned by a
 * table rather than a field, so its canonical owner identity is a separate question and
 * is deliberately not asserted here.
 */
class SelectorDialogTemplateTest {

    private static final String DIALOG_TEMPLATE = "/actor/src/containers/dialog.tsx.hbs";
    private static final String DIALOG_TYPES_TEMPLATE = "/actor/src/dialogs/types.ts.hbs";
    private static final String DIALOG_HOOKS_TEMPLATE = "/actor/src/dialogs/hooks.tsx.hbs";
    private static final String SET_SELECTOR_ACTION = "/actor/src/pages/actions/OpenSetSelectorAction.fragment.hbs";
    private static final String TABLE_TEMPLATE = "/actor/src/containers/components/table/index.tsx.hbs";

    @Test
    void selectorDialogIsAddressableThroughTheOwningField() throws IOException {
        String template = loadTemplate(DIALOG_TEMPLATE);

        assertEquals(1, count(template, "buildFieldTestId(selectorOwnerId, 'selector')"),
                "The selector overlay must carry the owning field's canonical selector root");
        assertEquals(1, count(template, "buildFieldTestId(selectorOwnerId, 'selector::title')"));
        assertEquals(1, count(template, "buildFieldTestId(selectorOwnerId, 'selector::cancel')"));
        assertEquals(1, count(template, "buildFieldTestId(selectorOwnerId, 'selector::confirm')"));
        assertTrue(loadTemplate(TABLE_TEMPLATE).contains("buildTableTestId(testIdOverride ?? '{{ getElementId table }}')"),
                "Selector tables must use the owning field id so row and cell selectors match runtime");
    }

    @Test
    void selectorOwnerIsThreadedFromTheActionToTheDialog() throws IOException {
        assertTrue(loadTemplate(DIALOG_TYPES_TEMPLATE).contains("selectorOwnerId?: string;"),
                "Dialog props must accept the owning field id");
        assertTrue(loadTemplate(DIALOG_HOOKS_TEMPLATE).contains("selectorOwnerId"),
                "The dialog hook must forward the owning field id");
        assertTrue(loadTemplate(SET_SELECTOR_ACTION).contains("selectorOwnerId: '{{ getElementId link }}'"),
                "The set selector action must name the field that owns the selector");
    }

    private static String loadTemplate(String path) throws IOException {
        try (InputStream input = SelectorDialogTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing template resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int count(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }
}
