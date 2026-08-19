package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagsTemplateTest {

    @Test
    void tagsUseCanonicalRowsInsteadOfPositionalChipSelectors() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/Tags.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildRowTestId"));
            assertTrue(template.contains("buildRowTestId(id, option)"));
        }
    }

    @Test
    void tagsUseTheCanonicalSourceIdForFieldActions() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/containers/components/tag/index.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("id=\"{{ getElementId table }}\""));
        }
    }

    @Test
    void tagsUseTheModeledClearRole() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/Tags.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildFieldTestId(id, 'button::clear')"));
        }
    }

    /**
     * The model declares Refresh on every tag table, and the runtime renders it,
     * so a tag field must offer the same canonical refresh role. A derived tag
     * relation offers nothing else, which makes refresh its only control.
     */
    @Test
    void tagsOfferTheModeledRefreshRole() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/Tags.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildFieldTestId(id, 'button::refresh')"));
        }
    }

    @Test
    void tagRemovalUsesTheCanonicalRowActionRole() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/components/widgets/Tags.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("buildRowTestId(id, option) + '::action::remove'"));
            assertTrue(template.contains("testId={buildRowTestId(id, option) + '::action::remove'}"));
        }
    }

    @Test
    void mdiIconForwardsTheExplicitTestIdToItsRenderedIcon() throws IOException {
        try (InputStream input = TagsTemplateTest.class.getResourceAsStream(
                "/actor/src/components/MdiIcon.tsx.hbs")) {
            assertNotNull(input);
            String template = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(template.contains("testId?: string;"));
            assertTrue(template.contains("data-testid={testId}"));
            assertTrue(template.contains("onClick?: any;"));
            assertTrue(template.contains("onClick={onClick}"));
        }
    }
}
