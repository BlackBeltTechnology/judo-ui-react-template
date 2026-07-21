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

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.getButtonActionType;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.normalizeButtonActionType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the pure-function core of {@link UiActionsHelper#getButtonActionType(org.eclipse.emf.ecore.EObject)}.
 *
 * <p>The EMF-backed entry point {@code getButtonActionType(EObject)} is exercised end-to-end
 * by the integration tests (snapshot regeneration). Here we cover only the pure string
 * normalization in {@link UiActionsHelper#normalizeButtonActionType(String)}, which is a
 * byte-exact port of the runtime's {@code getButtonActionType} in
 * {@code @judo/test-ids/src/element.ts}.
 */
public class UiActionsHelperTest {

    @Test
    void normalizeButtonActionType_producesNormalizedStringForKnownSubclasses() {
        assertEquals("opencreateform", normalizeButtonActionType("OpenCreateFormActionDefinition"));
        assertEquals("opensetselector", normalizeButtonActionType("OpenSetSelectorActionDefinition"));
        assertEquals("openaddselector", normalizeButtonActionType("OpenAddSelectorActionDefinition"));
        assertEquals("openpage", normalizeButtonActionType("OpenPageActionDefinition"));
        assertEquals("rowopenpage", normalizeButtonActionType("RowOpenPageActionDefinition"));
        assertEquals("rowdelete", normalizeButtonActionType("RowDeleteActionDefinition"));
        assertEquals("calloperation", normalizeButtonActionType("CallOperationActionDefinition"));
        assertEquals("refresh", normalizeButtonActionType("RefreshActionDefinition"));
    }

    @Test
    void normalizeButtonActionType_stripsUiPrefixIfPresent() {
        // Runtime's @type carries a "ui:" prefix; EClass.getName() does not.
        // The port preserves the .replace("ui:", "") call so both inputs normalize identically.
        assertEquals("opencreateform", normalizeButtonActionType("ui:OpenCreateFormActionDefinition"));
    }

    @Test
    void normalizeButtonActionType_returnsEmptyStringForNull() {
        assertEquals("", normalizeButtonActionType(null));
    }

    @Test
    void getButtonActionType_returnsEmptyStringForNullButton() {
        assertEquals("", getButtonActionType(null));
    }
}
