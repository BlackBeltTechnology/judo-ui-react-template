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
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.EnumerationMember;
import hu.blackbelt.judo.meta.ui.data.EnumerationType;
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

        classes.stream().forEach(c -> {
            c.getAttributes().stream().filter(a -> a.getDataType() instanceof EnumerationType).forEach(a -> list.add((EnumerationType) a.getDataType()));
        });

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

    public static String idToTranslationKey(String id, Application application) {
        String result = id;

        if (result.contains("@")) {
            result = result.split("@")[1];
        }

        result = result.replaceAll("LabelWrapper::", "")
                .replaceAll("Create::default::", "")
                .replaceAll("Input::default::", "")
                .replaceAll("Output::default::", "")
                .replaceAll("View::default::", "")
                .replaceAll("Edit::default::", "")
                .replaceAll("Table::default::", "")
                .replaceAll(application.getName() + "::", "")
                .replaceAll(modelName(application.getName()) + "::", "")
                .replaceAll("(#ButtonCallOperation)$", "")
                .replaceAll("(#ButtonNavigate)$", "")
                .replaceAll("(#TabularReferenceButton)$", "");


        return stream(result.replaceAll("#", "::")
                .replaceAll("\\.", "::")
                .replaceAll("/", "::")
                .replaceAll("_", "::")
                .split("::"))
                .filter(f -> f.trim().length() > 0)
                .collect(Collectors.joining("."));
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
            translations.put(idToTranslationKey(page.getFQName(), application), page.getLabel());

            for (Action action: getUniquePageActions(page)) {
                translations.put(idToTranslationKey(action.getFQName(), application), action.getLabel());

                if (hasConfirmation(action)) {
                    translations.put(idToTranslationKey(action.getFQName(), application) + ".confirmation", action.getConfirmationMessage());
                }
            }

            if (page.getIsPageTypeTable()) {
                // Table pages are special because getting the actual Table reference is different compared to relations
                Table table = getTableForTablePage(page);

                addTranslationsForTable(table, translations, application);
            } else {
                // Create / View / OperationOutput screens are quite similar, we can process them the same way
                for (Table table: getPageTables(page)) {
                    translations.put(idToTranslationKey(table.getFQName(), application), table.getLabel());

                    addTranslationsForTable(table, translations, application);
                }

                for (Link link: getPageLinks(page)) {
                    addTranslationsForLink(link, translations, application);
                }

                if (page.getIsPageTypeOperationOutput()) {
                    // OperationOutput pages ar special, because page containers behave differently
                    VisualElement root = getDataContainerForPage(page);

                    if (root != null) {
                        addTranslationForVisualElement(root, translations, application);
                    }
                } else {
                    List<VisualElement> elements = page.getContainers().get(0).getChildren();

                    if (elements.size() > 0) {
                        addTranslationForVisualElement(elements.get(0), translations, application);
                    }
                }

            }
        }

        // Create Forms for modals and Operation Input Forms for modals
        for (KeyValue<Action, PageDefinition> def: getActionFormsForPages(application)) {
            Action action = def.getKey();
            PageDefinition page = action instanceof CallOperationAction ? ((CallOperationAction) action).getInputParameterPage() : ((CreateAction) action).getTarget();

            if (action.getIsCreateAction() || action.getIsCallOperationAction()) {
                translations.put(idToTranslationKey(page.getFQName(), application), page.getLabel());

                for (Table table: getPageTables(page)) {
                    translations.put(idToTranslationKey(table.getFQName(), application), table.getLabel());

                    addTranslationsForTable(table, translations, application);
                }

                for (Link link: getPageLinks(page)) {
                    addTranslationsForLink(link, translations, application);
                }

                List<VisualElement> elements = page.getContainers().get(0).getChildren();

                if (elements.size() > 0) {
                    addTranslationForVisualElement(elements.get(0), translations, application);
                }
            }
        }

        // Unmapped Operation Output Views for modals
        for (KeyValue<Action, PageDefinition> def: getUnmappedOutputViewsForPages(application)) {
            CallOperationAction action = (CallOperationAction) def.getKey();
            PageDefinition page = action.getOutputParameterPage();

            translations.put(idToTranslationKey(page.getFQName(), application), page.getLabel());

            for (Table table: getPageTables(page)) {
                translations.put(idToTranslationKey(table.getFQName(), application), table.getLabel());

                addTranslationsForTable(table, translations, application);
            }

            for (Link link: getPageLinks(page)) {
                addTranslationsForLink(link, translations, application);
            }

            // OperationOutput pages ar special, because page containers behave differently
            VisualElement root = getDataContainerForPage(page);

            if (root != null) {
                addTranslationForVisualElement(root, translations, application);
            }
        }

        // filter out translation entries which are coming from system-level, generalized translations or just not being used
        Map<String, String> filtered = translations.entrySet()
                .stream()
                .filter(map -> keepTranslationKey(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, map -> map.getValue() == null ? "" : map.getValue() ));

        return new TreeMap<>(filtered);
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

    private static void addTranslationsForTable(Table table, Map<String, String> translations, Application application) {
        for (Action action: table.getRowActions()) {
            translations.put(idToTranslationKey(action.getFQName(), application), action.getLabel());

            if (hasConfirmation(action)) {
                translations.put(idToTranslationKey(action.getFQName(), application) + ".confirmation", action.getConfirmationMessage());
            }
        }

        for (Column column: table.getColumns()) {
            translations.put(idToTranslationKey(column.getFQName(), application), column.getLabel());
        }

        for (Filter filter: table.getFilters()) {
            translations.put(idToTranslationKey(filter.getFQName(), application), filter.getLabel());
        }
    }

    private static void addTranslationsForLink(Link link, Map<String, String> translations, Application application) {
        translations.put(idToTranslationKey(link.getFQName(), application), link.getLabel());

        for (Column column: ((Collection<Column>) link.getColumns())) {
            translations.put(idToTranslationKey(column.getFQName(), application), column.getLabel());
        }

        for (Filter filter: link.getFilters()) {
            translations.put(idToTranslationKey(filter.getFQName(), application), filter.getLabel());
        }
    }

    private static void addTranslationForVisualElement(VisualElement visualElement, Map<String, String> translations, Application application) {
        translations.put(idToTranslationKey(visualElement.getFQName(), application), visualElement.getLabel());

        if (visualElement instanceof Container) {
            for (VisualElement element: ((Container) visualElement).getChildren()) {
                addTranslationForVisualElement(element, translations, application);
            }
        }

        if (visualElement instanceof TabController) {
            for (Tab tab: ((TabController) visualElement).getTabs()) {
                addTranslationForVisualElement(tab.getElement(), translations, application);
            }
        }
    }

    private static Boolean keepTranslationKey(String key) {
        Matcher m = TRANSLATION_KEYS_TO_SKIP.matcher(key);
        return !m.matches();
    }
}
