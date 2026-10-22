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

class TabTemplateTest {

    private static final String TAB_CONTROLLER_TEMPLATE =
            "/actor/src/containers/widget-fragments/tabcontroller.hbs";

    /**
     * A tab is addressed as {@code tabs::<controller>::<tab>}, and the tab part
     * must be the modeled element's canonical id, exactly like every other
     * canonical selector. Emitting the tab wrapper's xmi:id produced a model
     * path that the runtime engine never emits.
     */
    @Test
    void tabsAreAddressedByTheirCanonicalElementId() throws IOException {
        String template = loadTemplate(TAB_CONTROLLER_TEMPLATE);

        assertEquals(1, count(template, "id: '{{ getElementId tab.element }}',"));
        assertEquals(0, count(template, "id: '{{ getXMIID tab }}',"));
    }

    private static String loadTemplate(String path) throws IOException {
        try (InputStream input = TabTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing template resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static int count(String value, String token) {
        return (value.length() - value.replace(token, "").length()) / token.length();
    }
}
