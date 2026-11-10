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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ButtonGroupTemplateTest {

    /**
     * The runtime ({@code InlineButtonGroupRenderer}) puts non-featured buttons behind a
     * trigger identified as {@code button-group::<id>::dropdown}, and each menu item
     * carries the canonical button id. The template emitted the private
     * {@code field::<xmi>-button-group::container} / {@code ::menu::item::<xmi>} pair instead.
     */
    @ParameterizedTest
    @CsvSource({
            "/actor/src/containers/widget-fragments/buttongroup.hbs, child",
            "/actor/src/containers/page.tsx.hbs, buttonGroup",
    })
    void dropdownTriggerAndItemsUseTheRuntimeIds(String path, String group) throws IOException {
        String template = loadTemplate(path);

        assertEquals(1, count(template,
                "testId={`${buildButtonGroupTestId('{{ getElementId " + group + " }}')}::dropdown`}"));
        assertEquals(1, count(template,
                "testId: buildButtonTestId('{{ getElementId button }}', '{{ getButtonActionType button }}'),"));
    }

    @Test
    void buttonGroupHelperMirrorsTheRuntimeGrammar() throws IOException {
        String helper = loadTemplate("/actor/src/utilities/transfer-id.ts.hbs");

        assertTrue(helper.contains("export function buildButtonGroupTestId(groupId: TestIdKey): string {"));
        assertTrue(helper.contains("return `button-group::${stringifyKey(groupId)}`;"));
    }

    @Test
    void containersImportTheButtonGroupHelper() throws IOException {
        assertTrue(loadTemplate("/actor/src/containers/page.tsx.hbs")
                .contains("import { buildFieldTestId, buildButtonTestId, buildButtonGroupTestId } from '~/utilities/transfer-id';"));
        assertTrue(loadTemplate("/actor/src/fragments/container/common-imports.fragment.hbs")
                .contains("buildButtonGroupTestId"));
    }

    private static String loadTemplate(String path) throws IOException {
        try (InputStream input = ButtonGroupTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing template resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int count(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }
}
