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

import hu.blackbelt.judo.generator.commons.ThreadLocalContextHolder;
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

import static hu.blackbelt.judo.ui.generator.react.StoredVariableHelper.DEFAULT_I18N_LANGUAGE;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.modelName;
import static hu.blackbelt.judo.ui.generator.react.UiPageContainerHelper.containerIsEmptyDashboard;
import static hu.blackbelt.judo.ui.generator.react.UiPageHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiServiceHelper.classServiceTypeName;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.*;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.*;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiI18NHelper extends Helper {

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

//    public static Map<String, String> getApplicationTranslations(Application application) {
//        Map<String, String> translations = new HashMap<>();
//
//        translations.put("application.model.name", modelName(application.getName()));
//
//        for (KeyValue<String, String> it: i18nMenuTreeLabels(application)) {
//            translations.put(it.getKey(), it.getValue());
//        }
//
//        for (EnumerationType it: i18nEnumerationTypes(application)) {
//            for (EnumerationMember em: it.getMembers()) {
//                translations.put("enumerations."+ restParamName(it) + "." + em.getName(), em.getName());
//            }
//        }
//
//        // iterate over routed pages
//        for (PageDefinition page: getPagesForRouting(application)) {
//            if (!titleComesFromAttribute(page)) {
//                translations.put(getTranslationKeyForPage(page), page.getLabel());
//            }
//
//            for (Action action: getUniquePageActions(page)) {
//                translations.put(getTranslationKeyForAction(action), action.getLabel());
//
//                if (hasConfirmation(action)) {
//                    translations.put(getTranslationKeyForAction(action) + ".confirmation", action.getConfirmationMessage());
//                }
//            }
//
//            if (page.getIsPageTypeTable()) {
//                // Table pages are special because getting the actual Table reference is different compared to relations
//                Table table = getTableForTablePage(page);
//
//                addTranslationsForTable(table, translations);
//            } else {
//                // Create / View / OperationOutput screens are quite similar, we can process them the same way
//                for (Table table: getPageTables(page)) {
//                    translations.put(getTranslationKeyForVisualElement(table), table.getLabel());
//
//                    addTranslationsForTable(table, translations);
//                }
//
//                for (Link link: getPageLinks(page)) {
//                    addTranslationsForLink(link, translations);
//                }
//
//                if (page.getIsPageTypeOperationOutput()) {
//                    // OperationOutput pages ar special, because page containers behave differently
//                    VisualElement root = getDataContainerForPage(page);
//
//                    if (root != null) {
//                        addTranslationsForVisualElement(root, translations);
//                    }
//                } else {
//                    List<VisualElement> elements = page.getOriginalPageContainer().getChildren();
//
//                    if (elements.size() > 0) {
//                        addTranslationsForVisualElement(elements.get(0), translations);
//                    }
//                }
//
//            }
//        }
//
//        // view dialogs
//        for (PageDefinition page: getViewDialogs(application)) {
//            if (!titleComesFromAttribute(page)) {
//                translations.put(getTranslationKeyForPage(page), page.getLabel());
//            }
//
//            for (Action action: getUniquePageActions(page)) {
//                translations.put(getTranslationKeyForAction(action), action.getLabel());
//
//                if (hasConfirmation(action)) {
//                    translations.put(getTranslationKeyForAction(action) + ".confirmation", action.getConfirmationMessage());
//                }
//            }
//
//            if (page.getIsPageTypeTable()) {
//                // Table pages are special because getting the actual Table reference is different compared to relations
//                Table table = getTableForTablePage(page);
//
//                addTranslationsForTable(table, translations);
//            } else {
//                // Create / View / OperationOutput screens are quite similar, we can process them the same way
//                for (Table table: getPageTables(page)) {
//                    translations.put(getTranslationKeyForVisualElement(table), table.getLabel());
//
//                    addTranslationsForTable(table, translations);
//                }
//
//                for (Link link: getPageLinks(page)) {
//                    addTranslationsForLink(link, translations);
//                }
//
//                if (page.getIsPageTypeOperationOutput()) {
//                    // OperationOutput pages ar special, because page containers behave differently
//                    VisualElement root = getDataContainerForPage(page);
//
//                    if (root != null) {
//                        addTranslationsForVisualElement(root, translations);
//                    }
//                } else {
//                    List<VisualElement> elements = page.getOriginalPageContainer().getChildren();
//
//                    if (elements.size() > 0) {
//                        addTranslationsForVisualElement(elements.get(0), translations);
//                    }
//                }
//
//            }
//        }
//
//        // Create Forms for modals and Operation Input Forms for modals
//        for (Action action: getActionFormsForPages(application)) {
//            PageDefinition page = action instanceof CallOperationAction ? ((CallOperationAction) action).getInputParameterPage() : ((CreateAction) action).getTarget();
//
//            if (action.getIsCreateAction() || action.getIsCallOperationAction()) {
//                if (!titleComesFromAttribute(page)) {
//                    translations.put(getTranslationKeyForPage(page), page.getLabel());
//                }
//
//                for (Table table: getPageTables(page)) {
//                    translations.put(getTranslationKeyForVisualElement(table), table.getLabel());
//
//                    addTranslationsForTable(table, translations);
//                }
//
//                for (Link link: getPageLinks(page)) {
//                    addTranslationsForLink(link, translations);
//                }
//
//                List<VisualElement> elements = page.getOriginalPageContainer().getChildren();
//
//                if (elements.size() > 0) {
//                    addTranslationsForVisualElement(elements.get(0), translations);
//                }
//
//                if (action.getIsCallOperationAction()) {
//                    for (OperationParameterType fault: ((CallOperationAction) action).getOperation().getFaults()) {
//                        for (AttributeType attributeType: fault.getTarget().getAttributes()) {
//                            translations.put("faults." + classServiceTypeName(fault.getTarget()) + "." + attributeType.getName(), attributeType.getName());
//                        }
//                    }
//                }
//            }
//        }
//
//        // Unmapped Operation Output Views for modals
//        for (PageDefinition page: getUnmappedOutputViewsForPages(application)) {
//
//            if (!titleComesFromAttribute(page)) {
//                translations.put(getTranslationKeyForPage(page), page.getLabel());
//            }
//
//            for (Table table: getPageTables(page)) {
//                translations.put(getTranslationKeyForVisualElement(table), table.getLabel());
//
//                addTranslationsForTable(table, translations);
//            }
//
//            for (Link link: getPageLinks(page)) {
//                addTranslationsForLink(link, translations);
//            }
//
//            // OperationOutput pages ar special, because page containers behave differently
//            VisualElement root = getDataContainerForPage(page);
//
//            if (root != null) {
//                addTranslationsForVisualElement(root, translations);
//            }
//        }
//
//        // filter out translation entries which are coming from system-level, generalized translations or just not being used
//        Map<String, String> filtered = translations.entrySet()
//                .stream()
//                .filter(map -> keepTranslationKey(map.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, map -> map.getValue() == null ? "" : map.getValue() ));
//
//        Map<String, String> sorted = filtered.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
//
//        return new TreeMap<>(sorted);
//    }

    public static String muiTranslationToken(String language, String suffix) {
        return language.replace("-", "") + (suffix != null ? suffix : "");
    }

    public static String shortLocale(String locale) {
        return locale.split("-")[0];
    }

//    private static void addTranslationsForTable(Table table, Map<String, String> translations) {
//        for (Action action: table.getRowActions()) {
//            translations.put(getTranslationKeyForAction(action), action.getLabel());
//
//            if (hasConfirmation(action)) {
//                translations.put(getTranslationKeyForAction(action) + ".confirmation", action.getConfirmationMessage());
//            }
//        }
//
//        for (Column column: table.getColumns()) {
//            translations.put(getTranslationKeyForVisualElement(column), column.getLabel());
//        }
//
//        for (Filter filter: table.getFilters()) {
//            translations.put(getTranslationKeyForVisualElement(filter), filter.getLabel());
//        }
//    }
//
//    private static void addTranslationsForLink(Link link, Map<String, String> translations) {
//        translations.put(getTranslationKeyForVisualElement(link), link.getLabel());
//
//        for (Column column: ((Collection<Column>) link.getColumns())) {
//            translations.put(getTranslationKeyForVisualElement(column), column.getLabel());
//        }
//
//        for (Filter filter: link.getFilters()) {
//            translations.put(getTranslationKeyForVisualElement(filter), filter.getLabel());
//        }
//    }
//
//    private static void addTranslationsForVisualElement(VisualElement visualElement, Map<String, String> translations) {
//        if (visualElement instanceof ActionGroup) {
//            ActionGroup actionGroup = (ActionGroup) visualElement;
//            for (Button button: featuredActionsForActionGroup(actionGroup)) {
//                translations.put(getTranslationKeyForButton(button, ""), button.getLabel());
//            }
//            if (displayDropdownForActionGroup(actionGroup)) {
//                // button for the group itself
//                translations.put(getTranslationKeyForVisualElement(visualElement), visualElement.getLabel());
//                // dropdown elements
//                for (Button button: nonFeaturedActionsForActionGroup(actionGroup)) {
//                    translations.put(getTranslationKeyForButton(button, "grouped"), button.getLabel());
//                }
//            }
//        } else if (visualElement instanceof TabController) {
//            TabController controller = (TabController) visualElement;
//
//            for (Tab tab: controller.getTabs()) {
//                Flex nested = (Flex) tab.getElement();
//
//                for (VisualElement tabElementChild: nested.getChildren()) {
//                    addTranslationsForVisualElement(tabElementChild, translations);
//                }
//
//                translations.put(getTranslationKeyForFlex(nested), nested.getLabel());
//            }
//        } else if (visualElement instanceof Text text) {
//            translations.put(getTranslationKeyForVisualElement(text), text.getText());
//        } else if (!(visualElement instanceof Flex) && !(visualElement instanceof Spacer) && !(visualElement instanceof Divider)) {
//            // Skip Flex, we only need the label if it's present for the Card version
//            // Skip Spacer and Divider, because these elements cannot contain any text
//            translations.put(getTranslationKeyForVisualElement(visualElement), visualElement.getLabel());
//        }
//
//        if (visualElement instanceof Container) {
//            for (VisualElement element: ((Container) visualElement).getChildren()) {
//                addTranslationsForVisualElement(element, translations);
//            }
//        }
//    }
//
//    private static Boolean keepTranslationKey(String key) {
//        Matcher m = TRANSLATION_KEYS_TO_SKIP.matcher(key);
//        return !m.matches();
//    }
//
//    public static String getTranslationKeyForFlex(Flex flex) {
//        // currently only used to translate TabController Tabs, because the actual label is inside the first Flex of a Tab
//        PageDefinition page = flex.getPageDefinition();
//        return transformTranslationKey(getTranslationKeyForPage(page) + "." + flex.getName());
//    }
//
//    public static String getTranslationKeyForVisualElement(VisualElement element) {
//        PageDefinition page = element.getPageDefinition();
//        String result = getTranslationKeyForPage(page);
//
//        if (element instanceof Column) {
//            if (element.eContainer() instanceof ReferenceTypedVisualElement) {
//                ReferenceTypedVisualElement ref = (ReferenceTypedVisualElement) element.eContainer();
//                result += "." + ref.getDataElement().getName();
//            }
//            result += "." + ((Column) element).getAttributeType().getName();
//        } else if (element instanceof Filter) {
//            if (element.eContainer() instanceof ReferenceTypedVisualElement) {
//                ReferenceTypedVisualElement ref = (ReferenceTypedVisualElement) element.eContainer();
//                result += "."+ ref.getDataElement().getName();
//            }
//            Filter filter = (Filter) element;
//            result += "." + filter.getAttributeType().getName();
//        } else if (element instanceof Table) {
//            Table table = (Table) element;
//            result += "." + table.getDataElement().getName();
//        } else if (element instanceof Link) {
//            Link link = (Link) element;
//            result += "." + link.getDataElement().getName();
//        } else if (element instanceof Button) {
//            Button button = (Button) element;
//            result += "." + button.getName();
//        } else if (element instanceof ActionGroup) {
//            ActionGroup actionGroup = (ActionGroup) element;
//            result += "." + actionGroup.getName();
//        } else if (element instanceof Input) {
//            Input input = (Input) element;
//            result += "." + input.getName();
//        } else if (element instanceof Formatted) {
//            Formatted formatted = (Formatted) element;
//            result += "." + formatted.getName();
//        } else if (element instanceof Label) {
//            Label label = (Label) element;
//            result += "." + label.getName();
//        } else if (element instanceof Text text) {
//            result += "." + text.getName();
//        } else {
//            throw new RuntimeException("Unsupported Visual Element for translation: " + getXMIID(element));
//        }
//        return transformTranslationKey(result);
//    }
//
//    public static String getTranslationKeyForButton(Button button, String suffix) {
//        PageDefinition page = button.getPageDefinition();
//        String result = getTranslationKeyForPage(page);
//
//        if (button.eContainer() instanceof ActionGroup) {
//            result += "." + ((ActionGroup) button.eContainer()).getName();
//        }
//
//        if (suffix != null) {
//            result += "." + suffix;
//        }
//
//        return transformTranslationKey(result + "." + button.getName());
//    }
//
//    public static String getTranslationKeyForAction(Action action) {
//        PageDefinition page = (PageDefinition) action.eContainer();
//        String result = getTranslationKeyForPage(page);
//        String[] nameSplit = action.getName().split("#");
//
//        if (action.getDefinedOn() instanceof Table || action.getDefinedOn() instanceof Link) {
//            result += "." + ((ReferenceTypedVisualElement) action.getDefinedOn()).getDataElement().getName();
//        }
//
//        return transformTranslationKey(result
//                + "." + action.getDataElement().getName()
//                + "." + nameSplit[nameSplit.length - 1]
//        );
//    }

    public static String getTranslationKeyForPage(PageDefinition page) {
        return stream(page.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining("."));
    }

//    public static String transformTranslationKey(String source) {
//        return stream(source.replaceAll("[#./_]", "::")
//                .split("::"))
//                .filter(f -> f.trim().length() > 0)
//                .collect(Collectors.joining("."));
//    }

    public static String getTranslationKeyForVisualElement(VisualElement element) {
        if (element instanceof PageContainer) {
            return transformTranslationKey(element.getName());
        }
        String root = transformTranslationKey(element.getPageContainer().getName());
        return root + "." + String.join(".", collectUp(element, new ArrayList<>()));
    }

    public static String transformTranslationKey(String source) {
        return stream(source.replaceAll("[#./_]", "::")
                .split("::"))
                .filter(f -> !f.trim().isEmpty())
                .collect(Collectors.joining("."));
    }

    private static List<String> collectUp(VisualElement element, List<String> acc) {
        List<String> accReal = acc != null ? acc : new ArrayList<>();

        accReal.add(element.getName());

        if (element.eContainer() instanceof Container) {
            collectUp((VisualElement) element.eContainer(), accReal);
        }

        if (element.eContainer() instanceof TabController) {
            for (Tab tab: ((TabController) element.eContainer()).getTabs()) {
                collectUp(tab.getElement(), accReal);
            }
        }

        return accReal;
    }


    public static String getDefaultLanguage(Application application) {
        String language = application.getDefaultLanguage();
        if (language == null || language.isBlank()) {
            return DEFAULT_I18N_LANGUAGE;
        }
        return language;
    }
}
