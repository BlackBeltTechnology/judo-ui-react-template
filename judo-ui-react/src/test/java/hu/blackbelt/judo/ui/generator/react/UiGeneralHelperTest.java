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

import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.resolveElementId;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the pure-function core of {@link UiGeneralHelper#getElementId}.
 *
 * <p>The EMF-backed entry point {@code getElementId(EObject)} is exercised end-to-end
 * by the integration tests (snapshot regeneration). Here we cover only the pure
 * fallback logic in {@link UiGeneralHelper#resolveElementId(String, String)}, which
 * determines which id source wins ({@code sourceId} vs {@code xmi:id}).
 *
 * <p>Contract mirrors the runtime's {@code getElementTestId} in
 * {@code @judo/test-ids/src/element.ts}: prefer {@code sourceId} when present and
 * non-empty, otherwise fall back to the sanitized xmi:id, otherwise {@code "unknown"}.
 */
public class UiGeneralHelperTest {

    @Test
    void resolveElementId_prefersSourceIdWhenPresent() {
        assertEquals("psm/_alpha", resolveElementId("psm/_alpha", "ui@_beta"));
    }

    @Test
    void resolveElementId_fallsBackToXmiIdWhenSourceIdIsNull() {
        assertEquals("ui_beta", resolveElementId(null, "ui@_beta"));
    }

    @Test
    void resolveElementId_fallsBackToXmiIdWhenSourceIdIsEmpty() {
        assertEquals("ui_beta", resolveElementId("", "ui@_beta"));
    }

    @Test
    void resolveElementId_stripsAtSignsFromXmiIdOnFallback() {
        assertEquals("aBc", resolveElementId(null, "a@B@c"));
    }

    @Test
    void resolveElementId_doesNotStripAtSignsFromSourceId() {
        // sourceId is trusted verbatim — never sanitized.
        assertEquals("psm@id", resolveElementId("psm@id", "ui/_beta"));
    }

    @Test
    void resolveElementId_returnsUnknownWhenBothAbsent() {
        assertEquals("unknown", resolveElementId(null, null));
    }

    @Test
    void resolveElementId_returnsUnknownWhenSourceIdEmptyAndXmiIdNull() {
        assertEquals("unknown", resolveElementId("", null));
    }
}
