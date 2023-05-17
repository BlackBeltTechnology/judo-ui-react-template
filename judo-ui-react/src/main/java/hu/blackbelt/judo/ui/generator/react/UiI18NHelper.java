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

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.eclipse.emf.common.util.EList;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.modelName;
import static hu.blackbelt.judo.ui.generator.react.UiPageHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.*;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.restParamName;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiI18NHelper extends Helper {
    private static final String LANGUAGE_DEFAULT = "default";

    // Translations ending with these tokens are excluded because they are handled in system-level, generalized translations
    private static final String[] EXCLUDED_TRANSLATION_SUFFIXES = {
            ".LabelWrapper",
            ".Left",
            ".Right",
            ".TableCreate",
            ".LinkCreate",
            ".LinkDelete",
            ".LinkEdit",
            ".LinkView",
            ".RowView",
            ".RowRemove",
            ".RowDelete",
            ".RowEdit",
            ".PageCreate",
            ".PageEdit",
            ".PageBack",
            ".PageAdd",
            ".PageDelete",
            ".PageClear",
            ".PageFilter",
            ".PageRefresh",
            ".PageSet"
    };
    private static final Pattern TRANSLATION_KEYS_TO_SKIP = Pattern.compile(".*(" + String.join("|", EXCLUDED_TRANSLATION_SUFFIXES) + ")$");

    public static List<KeyValue<String, String>> i18nMenuTreeLabels(Application app) {
        List<KeyValue<String, String>> collector = new ArrayList<>();

        collectMenuItems(app.getNavigationController().getItems(), collector);

        return collector;
    }

    public static List<EnumerationType> i18nEnumerationTypes(Application app) {
        List<EnumerationType> list = new ArrayList<>();

        List<ClassType> classes = app.getDataElements()
                .stream()
                .filter(d -> d instanceof ClassType)
                .map(d -> (ClassType) d)
                .collect(Collectors.toList());

        classes.forEach(c -> c.getAttributes().stream().filter(a -> a.getDataType() instanceof EnumerationType).forEach(a -> list.add((EnumerationType) a.getDataType())));

        return list;
    }

    private static void collectMenuItems(EList<NavigationItem> items, List<KeyValue<String, String>> collector) {
        for (NavigationItem item: items) {
            collector.add(new KeyValue<>("menuTree." + item.getLabel(), item.getLabel()));

            collectMenuItems(item.getItems(), collector);
        }
    }

    public static String getDefaultLanguage(String defaultLang) {
        return defaultLang != null ? defaultLang : LANGUAGE_DEFAULT;
    }

    public static Map<String, String> getApplicationTranslations(Application application) {
        Map<String, String> translations = new HashMap<>();

        translations.put("application.model.name", modelName(application.getName()));

        for (KeyValue<String, String> it: i18nMenuTreeLabels(application)) {
            translations.put(it.getKey(), it.getValue());
        }

        for (EnumerationType it: i18nEnumerationTypes(application)) {
            for (EnumerationMember em: it.getMembers()) {
                translations.put("enumerations."+ restParamName(it) + "." + em.getName(), em.getName());
            }
        }

        // iterate over routed pages
        for (PageDefinition page: getPagesForRouting(application)) {
            if (!titleComesFromAttribute(page)) {
                translations.put(magicTranslatePage(page), page.getLabel());
            }

            for (Action action: getUniquePageActions(page)) {
                translations.put(magicTranslateAction(action), action.getLabel());

                if (hasConfirmation(action)) {
                    translations.put(magicTranslateAction(action) + ".confirmation", action.getConfirmationMessage());
                }
            }

            if (page.getIsPageTypeTable()) {
                // Table pages are special because getting the actual Table reference is different compared to relations
                Table table = getTableForTablePage(page);

                addTranslationsForTable(table, translations);
            } else {
                // Create / View / OperationOutput screens are quite similar, we can process them the same way
                for (Table table: getPageTables(page)) {
                    translations.put(magicTranslate(table), table.getLabel());

                    addTranslationsForTable(table, translations);
                }

                for (Link link: getPageLinks(page)) {
                    addTranslationsForLink(link, translations);
                }

                if (page.getIsPageTypeOperationOutput()) {
                    // OperationOutput pages ar special, because page containers behave differently
                    VisualElement root = getDataContainerForPage(page);

                    if (root != null) {
                        addTranslationForVisualElement(root, translations);
                    }
                } else {
                    List<VisualElement> elements = page.getOriginalPageContainer().getChildren();

                    if (elements.size() > 0) {
                        addTranslationForVisualElement(elements.get(0), translations);
                    }
                }

            }
        }

        // Create Forms for modals and Operation Input Forms for modals
        for (Action action: getActionFormsForPages(application)) {
            PageDefinition page = action instanceof CallOperationAction ? ((CallOperationAction) action).getInputParameterPage() : ((CreateAction) action).getTarget();

            if (action.getIsCreateAction() || action.getIsCallOperationAction()) {
                if (!titleComesFromAttribute(page)) {
                    translations.put(magicTranslatePage(page), page.getLabel());
                }

                for (Table table: getPageTables(page)) {
                    translations.put(magicTranslate(table), table.getLabel());

                    addTranslationsForTable(table, translations);
                }

                for (Link link: getPageLinks(page)) {
                    addTranslationsForLink(link, translations);
                }

                List<VisualElement> elements = page.getOriginalPageContainer().getChildren();

                if (elements.size() > 0) {
                    addTranslationForVisualElement(elements.get(0), translations);
                }
            }
        }

        // Unmapped Operation Output Views for modals
        for (PageDefinition page: getUnmappedOutputViewsForPages(application)) {

            if (!titleComesFromAttribute(page)) {
                translations.put(magicTranslatePage(page), page.getLabel());
            }

            for (Table table: getPageTables(page)) {
                translations.put(magicTranslate(table), table.getLabel());

                addTranslationsForTable(table, translations);
            }

            for (Link link: getPageLinks(page)) {
                addTranslationsForLink(link, translations);
            }

            // OperationOutput pages ar special, because page containers behave differently
            VisualElement root = getDataContainerForPage(page);

            if (root != null) {
                addTranslationForVisualElement(root, translations);
            }
        }

        // filter out translation entries which are coming from system-level, generalized translations or just not being used
        Map<String, String> filtered = translations.entrySet()
                .stream()
                .filter(map -> keepTranslationKey(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, map -> map.getValue() == null ? "" : map.getValue() ));

        Map<String, String> sorted = filtered.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

        return new TreeMap<>(sorted);
    }

    public static boolean languageIsNotDefault(String defaultLanguage) {
        return !getDefaultLanguage(defaultLanguage).equals(LANGUAGE_DEFAULT);
    }

    public static String muiTranslationToken(String language, String suffix) {
        return language.replace("-", "") + (suffix != null ? suffix : "");
    }

    public static String shortLocale(String defaultLanguage) {
        return getDefaultLanguage(defaultLanguage).split("-")[0];
    }

    private static void addTranslationsForTable(Table table, Map<String, String> translations) {
        for (Action action: table.getRowActions()) {
            translations.put(magicTranslateAction(action), action.getLabel());

            if (hasConfirmation(action)) {
                translations.put(magicTranslateAction(action) + ".confirmation", action.getConfirmationMessage());
            }
        }

        for (Column column: table.getColumns()) {
            translations.put(magicTranslate(column), column.getLabel());
        }

        for (Filter filter: table.getFilters()) {
            translations.put(magicTranslate(filter), filter.getLabel());
        }
    }

    private static void addTranslationsForLink(Link link, Map<String, String> translations) {
        translations.put(magicTranslate(link), link.getLabel());

        for (Column column: ((Collection<Column>) link.getColumns())) {
            translations.put(magicTranslate(column), column.getLabel());
        }

        for (Filter filter: link.getFilters()) {
            translations.put(magicTranslate(filter), filter.getLabel());
        }
    }

    private static void addTranslationForVisualElement(VisualElement visualElement, Map<String, String> translations) {
        if (visualElement instanceof ActionGroup) {
            ActionGroup actionGroup = (ActionGroup) visualElement;
            for (Button button: featuredActionsForActionGroup(actionGroup)) {
                translations.put(magicTranslateButton(button, ""), button.getLabel());
            }
            if (displayDropdownForActionGroup(actionGroup)) {
                // button for the group itself
                translations.put(magicTranslate(visualElement), visualElement.getLabel());
                // dropdown elements
                for (Button button: nonFeaturedActionsForActionGroup(actionGroup)) {
                    translations.put(magicTranslateButton(button, "grouped"), button.getLabel());
                }
            }
        } else if (visualElement instanceof TabController) {
            TabController controller = (TabController) visualElement;

            for (Tab tab: controller.getTabs()) {
                Flex nested = (Flex) tab.getElement();
                translations.put(magicTranslateFlex(nested), nested.getLabel());

                for (VisualElement tabElementChild: nested.getChildren()) {
                    addTranslationForVisualElement(tabElementChild, translations);
                }
            }
        } else if (!(visualElement instanceof Flex)) {
            // Skip Flex, we only need the label if it's present for the Card version
            translations.put(magicTranslate(visualElement), visualElement.getLabel());
        }

        if (visualElement instanceof Container) {
            for (VisualElement element: ((Container) visualElement).getChildren()) {
                addTranslationForVisualElement(element, translations);
            }
        }

        if (visualElement instanceof TabController) {
            for (Tab tab: ((TabController) visualElement).getTabs()) {
                addTranslationForVisualElement(tab.getElement(), translations);
            }
        }
    }

    private static Boolean keepTranslationKey(String key) {
        Matcher m = TRANSLATION_KEYS_TO_SKIP.matcher(key);
        return !m.matches();
    }

    public static String magicTranslateFlex(Flex flex) {
        // currently only used to translate TabController Tabs, because the actual label is inside the first Flex of a Tab
        PageDefinition page = flex.getPageDefinition();
        String result = magicTranslatePage(page);
        result += ".".concat(flex.getName());
        return transformTranslationKey(result);
    }

    public static String magicTranslate(VisualElement element) {
        PageDefinition page = element.getPageDefinition();
        String result = magicTranslatePage(page);

        if (element instanceof Column) {
            if (element.eContainer() instanceof ReferenceTypedVisualElement) {
                ReferenceTypedVisualElement ref = (ReferenceTypedVisualElement) element.eContainer();
                result += ".".concat(ref.getDataElement().getName());
            }
            result += ".".concat(((Column) element).getAttributeType().getName());
        } else if (element instanceof Filter) {
            if (element.eContainer() instanceof ReferenceTypedVisualElement) {
                ReferenceTypedVisualElement ref = (ReferenceTypedVisualElement) element.eContainer();
                result += ".".concat(ref.getDataElement().getName());
            }
            Filter filter = (Filter) element;
            result += ".".concat(filter.getAttributeType().getName());
        } else if (element instanceof Table) {
            Table table = (Table) element;
            result += ".".concat(table.getDataElement().getName());
        } else if (element instanceof Link) {
            Link link = (Link) element;
            result += ".".concat(link.getDataElement().getName());
        } else if (element instanceof Button) {
            Button button = (Button) element;
            result += ".".concat(button.getName());
        } else if (element instanceof ActionGroup) {
            ActionGroup actionGroup = (ActionGroup) element;
            result += ".".concat(actionGroup.getName());
        } else if (element instanceof Input) {
            Input input = (Input) element;
            result += ".".concat(input.getName());
        } else if (element instanceof Formatted) {
            Formatted formatted = (Formatted) element;
            result += ".".concat(formatted.getName());
        } else if (element instanceof Label) {
            Label label = (Label) element;
            result += ".".concat(label.getName());
        } else {
            throw new RuntimeException("Unsupported Visual Element for translation: " + element.getFQName());
        }
        return transformTranslationKey(result);
    }

    public static String magicTranslateButton(Button button, String suffix) {
        PageDefinition page = button.getPageDefinition();
        String result = magicTranslatePage(page);

        if (button.eContainer() instanceof ActionGroup) {
            result += ".".concat(((ActionGroup) button.eContainer()).getName());
        }

        if (suffix != null) {
            result += ".".concat(suffix);
        }

        return transformTranslationKey(result.concat(".").concat(button.getName()));
    }

    public static String magicTranslateAction(Action action) {
        PageDefinition page = (PageDefinition) action.eContainer();
        String result = magicTranslatePage(page);
        String[] nameSplit = action.getName().split("#");

        if (action.getDefinedOn() instanceof Table || action.getDefinedOn() instanceof Link) {
            result += ".".concat(((ReferenceTypedVisualElement) action.getDefinedOn()).getDataElement().getName());
        }

        return transformTranslationKey(result
                .concat(".").concat(action.getDataElement().getName())
                .concat(".").concat(nameSplit[nameSplit.length - 1])
        );
    }

    public static String magicTranslatePage(PageDefinition page) {
        if (page.getIsPageTypeDashboard()) {
            return transformTranslationKey(page.getName());
        }
        String prefix = String.join(".", page.getOriginalPageContainer().getTransferPackageNameTokens());
        String transferPageName = page.getOriginalPageContainer().getTransferPageName();

        return prefix.length() > 0 ? prefix.concat(".").concat(transferPageName) : transferPageName;
    }

    public static String transformTranslationKey(String source) {
        return stream(source.replaceAll("#", "::")
                .replaceAll("\\.", "::")
                .replaceAll("/", "::")
                .replaceAll("_", "::")
                .split("::"))
                .filter(f -> f.trim().length() > 0)
                .collect(Collectors.joining("."));
    }
}
