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
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.getButtonRole;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.normalizeButtonActionType;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.normalizeButtonRole;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.operationRole;
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

    @Test
    void normalizeButtonRole_mapsActionTypesToCanonicalRoles() {
        // Byte-exact port of the runtime's getButtonRole switch in @judo/test-ids/src/element.ts.
        assertEquals("set", normalizeButtonRole("opensetselector"));
        // The runtime maps both selector-opening types to one role, so a spec that
        // addresses `button::set` resolves on either engine.
        assertEquals("set", normalizeButtonRole("openaddselector"));
        assertEquals("create", normalizeButtonRole("opencreateform"));
        assertEquals("view", normalizeButtonRole("openpage"));
        assertEquals("view", normalizeButtonRole("rowopenpage"));
        assertEquals("delete", normalizeButtonRole("rowdelete"));
    }

    @Test
    void normalizeButtonRole_fallsBackToRawActionType() {
        assertEquals("calloperation", normalizeButtonRole("calloperation"));
        assertEquals("refresh", normalizeButtonRole("refresh"));
    }

    /**
     * A role identifies one modeled action inside its owner. The CRUD types satisfy that by
     * construction, but an operation type does not: one row can launch several operations and
     * they all report the same action type, so those roles are the modeled operation name.
     * Mirrors {@code OPERATION_ACTION_TYPES} in {@code @judo/test-ids/src/element.ts}.
     */
    @Test
    void operationRole_usesModeledOperationNameSoOneRowCanCarrySeveral() {
        assertEquals("createDarkMatter", operationRole("openoperationinputform", "createDarkMatter"));
        assertEquals("createIntergalacticDust", operationRole("openoperationinputform", "createIntergalacticDust"));
        assertEquals("talkToGod", operationRole("openoperationinputselector", "talkToGod"));
        assertEquals("bang", operationRole("parameterlesscalloperation", "bang"));
        assertEquals("destroyLife", operationRole("bulkcalloperation", "destroyLife"));
    }

    @Test
    void operationRole_sanitizesTheNameIntoOneSegment() {
        assertEquals(
                "View-Galaxy-createDarkMatter",
                operationRole("openoperationinputform", "View::Galaxy::createDarkMatter"));
    }

    @Test
    void operationRole_fallsBackToTheActionTypeWithoutAName() {
        assertEquals("openoperationinputform", operationRole("openoperationinputform", null));
        assertEquals("openoperationinputform", operationRole("openoperationinputform", ""));
        // A non-operation type keeps its canonical role even when named.
        assertEquals("view", operationRole("rowopenpage", "openGalaxy"));
    }

    @Test
    void normalizeButtonRole_returnsEmptyStringForNullOrBlank() {
        assertEquals("", normalizeButtonRole(null));
        assertEquals("", normalizeButtonRole(""));
    }

    @Test
    void getButtonRole_returnsEmptyStringForNullButton() {
        assertEquals("", getButtonRole(null));
    }
}
