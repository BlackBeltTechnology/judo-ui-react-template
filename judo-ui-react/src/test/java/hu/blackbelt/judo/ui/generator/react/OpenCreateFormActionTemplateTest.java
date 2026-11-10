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
import hu.blackbelt.judo.meta.ui.data.DataFactory;
import hu.blackbelt.judo.meta.ui.data.RelationKind;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mirrors the runtime's {@code LinkRenderer.stagesCreateLocally}: a create through an eager
 * link of a saved owner is staged only into a composition; any other relation is created
 * through its own {@code ~create} endpoint, because the server refuses a new object nested
 * into a non-composition relation of the owner's update payload.
 */
class OpenCreateFormActionTemplateTest {

    @Test
    void eagerNonCompositionLinkOnViewCreatesThroughTheRelation() {
        assertTrue(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.AGGREGATION, true)));
        assertTrue(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.ASSOCIATION, true)));
    }

    @Test
    void eagerCompositionLinkStaysADraft() {
        assertFalse(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.COMPOSITION, true)));
    }

    @Test
    void lazyLinkAndFormPageKeepTheirFlow() {
        assertFalse(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.AGGREGATION, false)));
        assertFalse(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.FORM, RelationKind.AGGREGATION, true)));
    }

    @Test
    void tableCreateKeepsItsFlow() {
        PageDefinition page = UiFactory.eINSTANCE.createPageDefinition();
        PageContainer container = UiFactory.eINSTANCE.createPageContainer();
        container.setType(PageContainerType.VIEW);
        page.setContainer(container);
        Table table = UiFactory.eINSTANCE.createTable();
        container.getChildren().add(table);
        ButtonGroup group = UiFactory.eINSTANCE.createButtonGroup();
        table.setTableActionButtonGroup(group);
        Action action = createAction(page, group);

        assertFalse(UiActionsHelper.isLinkCreatePersistedOnSavedOwner(page, action));
    }

    @Test
    void templateDecidesDraftAndRefreshByTheRule() throws IOException {
        String template = read("/actor/src/pages/actions/OpenCreateFormAction.fragment.hbs");

        assertTrue(template.contains("{{# if (isLinkCreatePersistedOnSavedOwner page action) }}"));
        assertTrue(template.contains("const stagesDraft = !!isDraft || !(data as { __signedIdentifier?: string }).__signedIdentifier;"));
        assertTrue(template.contains("isDraft: {{# if (isLinkCreatePersistedOnSavedOwner page action) }}stagesDraft{{ else }}"));
        assertTrue(template.contains("ownerValidation: stagesDraft ? validate : undefined,"));
        // A persisted create reloads the owner even while it carries staged edits (e.g. after an unset).
        assertTrue(template.contains("if (result === 'submit'{{# unless (isLinkCreatePersistedOnSavedOwner page action) }} && !editMode{{/ unless }})"));
        // A selector reports that its result was created (not picked), so the caller can tell them apart.
        assertTrue(template.contains("onSubmit([returnedData], 'submit', true);"));
    }

    @Test
    void eagerNonCompositionSetSelectorFollowsTheSameRule() {
        assertTrue(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.AGGREGATION, true, true)));
        assertFalse(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.COMPOSITION, true, true)));
        assertFalse(isLinkCreatePersistedOnSavedOwner(page(PageContainerType.VIEW, RelationKind.AGGREGATION, false, true)));
    }

    @Test
    void setSelectorReloadsTheSavedOwnerAfterASelectorCreate() throws IOException {
        String template = read("/actor/src/pages/actions/OpenSetSelectorAction.fragment.hbs");

        // Mirrors the runtime's handleSelectorCreate: the relation's ~create already linked the
        // target, so it is adopted by a reload instead of being staged into the owner.
        assertTrue(template.contains("if (openCreated && !isDraft && (data as { __signedIdentifier?: string }).__signedIdentifier) {"));
        assertTrue(template.contains("await refresh(true);"));
    }

    private static String read(String path) throws IOException {
        try (InputStream input = OpenCreateFormActionTemplateTest.class.getResourceAsStream(path)) {
            assertNotNull(input);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Object[] page(PageContainerType type, RelationKind kind, boolean eager) {
        return page(type, kind, eager, false);
    }

    private static Object[] page(PageContainerType type, RelationKind kind, boolean eager, boolean setSelector) {
        PageDefinition page = UiFactory.eINSTANCE.createPageDefinition();
        PageContainer container = UiFactory.eINSTANCE.createPageContainer();
        container.setType(type);
        page.setContainer(container);
        Link link = UiFactory.eINSTANCE.createLink();
        link.setIsEager(eager);
        RelationType relation = DataFactory.eINSTANCE.createRelationType();
        relation.setRelationKind(kind);
        link.setDataElement(relation);
        container.getChildren().add(link);
        ButtonGroup group = UiFactory.eINSTANCE.createButtonGroup();
        link.setActionButtonGroup(group);
        return new Object[] { page, setSelector ? createAction(page, group, UiFactory.eINSTANCE.createOpenSetSelectorActionDefinition())
                : createAction(page, group) };
    }

    private static boolean isLinkCreatePersistedOnSavedOwner(Object[] pageAndAction) {
        return UiActionsHelper.isLinkCreatePersistedOnSavedOwner((PageDefinition) pageAndAction[0], (Action) pageAndAction[1]);
    }

    private static Action createAction(PageDefinition page, ButtonGroup group) {
        return createAction(page, group, UiFactory.eINSTANCE.createOpenCreateFormActionDefinition());
    }

    private static Action createAction(PageDefinition page, ButtonGroup group, ActionDefinition definition) {
        Button button = UiFactory.eINSTANCE.createButton();
        button.setActionDefinition(definition);
        group.getButtons().add(button);
        Action action = UiFactory.eINSTANCE.createAction();
        action.setActionDefinition(definition);
        page.getActions().add(action);
        return action;
    }
}
