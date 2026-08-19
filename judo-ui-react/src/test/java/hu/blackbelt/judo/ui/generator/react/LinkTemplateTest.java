package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkTemplateTest {

    @Test
    void singleRelationInputUsesTheCanonicalElementId() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/components/link/index.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("id=\"{{ getElementId link }}\""));
            assertFalse(template.contains("id=\"{{ getXMIID link }}\""));
        }
    }

    @Test
    void singleRelationInputDoesNotDuplicateTheFieldRoot() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/SingleRelationInput.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertFalse(template.contains("<Grid container data-testid={buildFieldTestId(id)}"));
        }
    }

    /**
     * The `input` role addresses the element that receives focus and keystrokes,
     * so it must be assigned through `inputProps` rather than on the MUI
     * TextField root, which renders a wrapper element.
     */
    @Test
    void relationInputRoleIsCarriedByTheFocusableInput() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/SingleRelationInput.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("'data-testid': buildFieldTestId(id, 'input')"));
            assertFalse(template.contains("data-testid={buildFieldTestId(id, 'input')}"));
        }
    }

    /**
     * The overflow trigger owns the `dropdown` role, matching the runtime
     * contract. It must not claim `button::set`, because that role addresses the
     * modeled action that opens the selector, and the same role appears on the
     * menu item the trigger reveals: a shared spec could not tell the two apart.
     */
    @Test
    void theOverflowTriggerOwnsTheDropdownRole() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/SingleRelationInput.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains(
                    "<IconButton data-testid={buildFieldTestId(id, 'dropdown')}"));
            assertFalse(template.contains(
                    "<IconButton data-testid={buildFieldTestId(id, 'button::set')}"));
            assertEquals(1, countOccurrences(template, "buildFieldTestId(id, 'dropdown')"),
                    "exactly one element may declare the dropdown role");
        }
    }

    /**
     * A field with no available actions must not leave an empty action bar behind.
     * The runtime engine omits the container entirely in that case, and a landmark
     * that never holds anything only invites specs to assert on nothing.
     */
    @Test
    void theActionBarIsOnlyRenderedWhenItHoldsSomething() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/SingleRelationInput.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("const relationActionButtons ="));
            assertTrue(template.contains("{hasRelationActions && ("));
            assertTrue(template.contains("const hasRelationActions ="));
        }
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int index = haystack.indexOf(needle); index >= 0; index = haystack.indexOf(needle, index + needle.length())) {
            count++;
        }
        return count;
    }

    /**
     * The overflow menu container is addressable on its own, matching the
     * runtime contract, so an engine-neutral test can assert the menu opened
     * before reaching for its items.
     */
    @Test
    void theOverflowMenuContainerCarriesTheCanonicalMenuRole() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/SingleRelationInput.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("data-testid={buildFieldTestId(id, 'menu')}"),
                    "the dropdown menu container must carry the canonical menu role");
            assertEquals(1, countOccurrences(template, "buildFieldTestId(id, 'menu')"),
                    "exactly one element may declare the menu role");
        }
    }

    @Test
    void linkDeleteButtonWaitsForTheDeleteLifecycle() throws IOException {
        try (InputStream input = LinkTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/components/link/index.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("await actions.{{ simpleActionDefinitionName actionDefinition }}!(value!);"));
            assertTrue(template.contains("await actions.{{ simpleActionDefinitionName unsetButton.actionDefinition }}!(value!);"));
        }
    }

}
