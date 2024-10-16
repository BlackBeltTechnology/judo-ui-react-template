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
import hu.blackbelt.judo.meta.ui.data.OperationType;
import lombok.extern.java.Log;
import org.eclipse.emf.common.util.EList;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.ReactStoredVariableHelper.DEFAULT_I18N_LANGUAGE;
import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.translationElementForBulkAction;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.collectVisualElementsMatchingCondition;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.elementHasLabel;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.restParamName;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiI18NHelper {

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

    private static void collectMenuItems(EList<NavigationItem> items, Map<String, String> collector) {
        for (NavigationItem item: items) {
            collector.put("menuTree." + item.getLabel(), item.getLabel());
            collectMenuItems(item.getItems(), collector);
        }
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

    public static Map<String, String> i18nMenuTreeLabels(Application app) {
        Map<String, String> collector = new HashMap<>();

        collectMenuItems(app.getNavigationController().getItems(), collector);

        return collector;
    }

    public static List<EnumerationType> i18nEnumerationTypes(Application app) {
        List<EnumerationType> list = new ArrayList<>();

        app.getClassTypes().forEach(c -> {
            ClassType classType = (ClassType) c;
            classType.getAttributes().stream()
                    .filter(a -> a.getDataType() instanceof EnumerationType)
                    .forEach(a -> list.add((EnumerationType) a.getDataType()));
        });

        return list;
    }

    public static String muiTranslationToken(String language, String suffix) {
        return language.replace("-", "") + (suffix != null ? suffix : "");
    }

    public static String shortLocale(String locale) {
        return locale.split("-")[0];
    }

    public static String getI18NKeyForNamedElement(NamedElement namedElement) {
        return stream(namedElement.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining("."));
    }

    public static String getTranslationKeyForVisualElement(VisualElement element) {
        if (hasSystemTranslation(element)) {
            return getSystemTranslationForVisualElement(element);
        }
        if (element instanceof PageContainer) {
            return element.getName().replaceAll("::", ".");
        }

        String root = element.getPageContainer().getName();
        VisualElement target = element;

        if (element instanceof Filter filter) {
            // we do not want to have dedicated keys for filters
            target = ((Table) filter.eContainer()).getColumns().stream().filter(c -> c.getAttributeType().equals(filter.getAttributeType())).findFirst().orElse(null);
        }

        assert target != null;
        String bare = target.getName();
        if (tokenNeedsPrefix(target)) {
            if (element instanceof Column column && column.eContainer() instanceof Table table && !table.getPageContainer().isTable()) {
                bare = root + "." + table.getDataElement().getName() + "." + target.getName();
            } else {
                bare = root + "." + target.getName();
            }
        }
        return bare.replaceAll("::", ".");
    }

    private static String getSystemTranslationForVisualElement(VisualElement visualElement) {
        if (visualElement instanceof Button button) {
            ActionDefinition ad = button.getActionDefinition();
            if (ad.getIsAddAction()) {
                return "judo.action.add";
            } else if (ad.getIsClearAction()) {
                return "judo.action.clear";
            } else if (ad.getIsCreateAction()) {
                return "judo.action.create";
            } else if (ad.getIsBackAction()) {
                return "judo.action.back";
            } else if (ad.getIsCancelAction()) {
                return "judo.action.cancel";
            } else if (ad.getIsSetAction()) {
                return "judo.action.set";
            } else if (ad.getIsUpdateAction()) {
                return "judo.action.update";
            } else if (ad.getIsDeleteAction()) {
                return "judo.action.delete";
            } else if (ad.getIsRowDeleteAction()) {
                return "judo.action.row-delete";
            } else if (ad.getIsRemoveAction()) {
                return "judo.action.remove";
            } else if (ad.getIsFilterAction()) {
                return "judo.action.filter";
            } else if (ad.getIsFilterRelationAction()) {
                return "judo.action.filter";
            } else if (ad.getIsRefreshAction()) {
                return "judo.action.refresh";
            } else if (ad.getIsRefreshRelationAction()) {
                return "judo.action.refresh";
            } else if (ad.getIsSelectorRangeAction()) {
                return "judo.action.refresh";
            } else if (ad.getIsBulkDeleteAction()) {
                return "judo.action.bulk-delete";
            } else if (ad.getIsBulkRemoveAction()) {
                return "judo.action.bulk-remove";
            } else if (ad.getIsExportAction()) {
                return "judo.action.export";
            } else if (ad.getIsOpenPageAction()) {
                return "judo.action.open-page";
            } else if (ad.getIsOpenCreateFormAction()) {
                return "judo.action.open-create-form";
            } else if (ad.getIsOpenAddSelectorAction()) {
                return "judo.action.open-add-selector";
            } else if (ad.getIsOpenSetSelectorAction()) {
                return "judo.action.open-set-selector";
            } else {
                throw new RuntimeException("Unsupported system translation for Button: " + button.getFQName());
            }
        } else if (visualElement instanceof ButtonGroup buttonGroup) {
            return "judo.pages.actions";
        }
        throw new RuntimeException("Unsupported system visual element: " + visualElement.getFQName());
    }

    private static boolean tokenNeedsPrefix(VisualElement visualElement) {
        if (visualElement instanceof Table table && table.isIsSelectorTable()) {
            return true;
        }
        return !visualElement.getName().contains("::") || visualElement.getName().split("(::)").length < 3;
    }

    public static String getDefaultLanguage(Application application) {
        String language = application.getDefaultLanguage();
        if (language == null || language.isBlank()) {
            return DEFAULT_I18N_LANGUAGE;
        }
        return language;
    }

    private static Boolean keepTranslationKey(String key) {
        Matcher m = TRANSLATION_KEYS_TO_SKIP.matcher(key);
        return !m.matches();
    }

    public static Map<String, String> getApplicationTranslations(Application application) {
        Map<String, String> translations = new HashMap<>();

        translations.put("application.model.name", application.getModelName());

        translations.putAll(i18nMenuTreeLabels(application));

        for (EnumerationType it : i18nEnumerationTypes(application)) {
            translations.put("enumerations." + restParamName(it) + "._null", "None");
            for (EnumerationMember em : it.getMembers()) {
                translations.put("enumerations." + restParamName(it) + "." + em.getName(), em.getName());
            }
        }

        application.getPageContainers().forEach(container -> {
            if (container.getTitleFrom() == TitleFrom.LABEL) {
                translations.put(getTranslationKeyForVisualElement(container), container.getLabel());
            }

            List<VisualElement> visualElements = new ArrayList<>();
            collectVisualElementsMatchingCondition(container, (v) -> !(v instanceof Flex), visualElements);

            visualElements.forEach(v -> {
                if (hasSystemTranslation(v)) {
                    return;
                }
                translations.put(getTranslationKeyForVisualElement(v), v.getLabel());
                if (v instanceof Button button && button.getConfirmation() != null) {
                    translations.put(getTranslationKeyForVisualElement(v) + ".confirmation", button.getConfirmation().getConfirmationMessage());
                }
                if (v instanceof TabController tabController) {
                    tabController.getTabs().forEach(t -> {
                        translations.put(getTranslationKeyForVisualElement(t.getElement()), t.getElement().getLabel());
                    });
                }
                if (v instanceof Table table) {
                    table.getColumns().forEach(c -> {
                        translations.put(getTranslationKeyForVisualElement(c), c.getLabel());
                    });
                    if (table.getTableActionButtonGroup() != null) {
                        table.getTableActionButtonGroup().getButtons().forEach(b -> {
                            if (hasSystemTranslation(b)) {
                                return;
                            }
                            translations.put(getTranslationKeyForVisualElement(b), b.getLabel());
                            if (b.getConfirmation() != null) {
                                translations.put(getTranslationKeyForVisualElement(b) + ".confirmation", b.getConfirmation().getConfirmationMessage());
                            }
                        });
                    }
                    if (table.getRowActionButtonGroup() != null) {
                        table.getRowActionButtonGroup().getButtons().forEach(b -> {
                            if (hasSystemTranslation(b)) {
                                return;
                            }
                            translations.put(getTranslationKeyForVisualElement(b), b.getLabel());
                        });
                    }
                }
                if (v instanceof Link link) {
                    translations.put(getTranslationKeyForVisualElement(link), link.getLabel());
                }
                if (v instanceof Text text) {
                    translations.put(getTranslationKeyForVisualElement(text), text.getText());
                }
                if (v instanceof Divider divider) {
                    translations.put(getTranslationKeyForVisualElement(divider), divider.getLabel());
                }
                if (v instanceof ButtonGroup buttonGroup) {
                    translations.put(getTranslationKeyForVisualElement(buttonGroup), buttonGroup.getLabel());

                    buttonGroup.getButtons().forEach(button -> {
                        if (hasSystemTranslation(button)) {
                            return;
                        }
                        translations.put(getTranslationKeyForVisualElement(button), button.getLabel());
                    });
                }
            });

            List<VisualElement> flexElements = new ArrayList<>();
            collectVisualElementsMatchingCondition(container, (v) -> v instanceof Flex flex && elementHasLabel(flex) && !(v instanceof PageContainer), flexElements);
            flexElements.forEach(f -> {
                translations.put(getTranslationKeyForVisualElement(f), f.getLabel());
            });
        });

        List<OperationType> operationsWithFaults = application.getClassTypes().stream()
                .flatMap(c -> ((ClassType) c).getOperations().stream())
                .filter(ot -> !((OperationType) ot).getFaults().isEmpty())
                .toList();


        operationsWithFaults.forEach(o -> {
            o.getFaults().forEach(f -> {
                ClassType faultClass = f.getTarget();
                faultClass.getAttributes().forEach(a -> {
                    translations.put("faults." + application.getModelName() + "." + faultClass.getName().replaceAll("::", ".") + "." + a.getName(), a.getName());
                });
            });
        });

        Map<String, String> filtered = translations.entrySet()
                .stream()
                .filter(map -> keepTranslationKey(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, map -> map.getValue() == null ? "" : map.getValue() ));

        Map<String, String> sorted = filtered.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));

        return new TreeMap<>(sorted);
    }

    public static String defaultLabelForBulkAction(Action action) {
        return translationElementForBulkAction(action).getLabel();
    }

    private static boolean hasSystemTranslation(VisualElement visualElement) {
        if (visualElement instanceof Button button) {
            ActionDefinition ad = button.getActionDefinition();
            if (ad.getIsCustomAction()) {
                return false;
            }
            return ad.getIsAddAction()
                    || ad.getIsClearAction()
                    || ad.getIsCreateAction()
                    || ad.getIsBackAction()
                    || ad.getIsCancelAction()
                    || ad.getIsSetAction()
                    || ad.getIsUpdateAction()
                    || ad.getIsDeleteAction()
                    || ad.getIsRowDeleteAction()
                    || ad.getIsRemoveAction()
                    || ad.getIsFilterAction()
                    || ad.getIsFilterRelationAction()
                    || ad.getIsRefreshAction()
                    || ad.getIsRefreshRelationAction()
                    || ad.getIsSelectorRangeAction()
                    || ad.getIsSetAction()
                    || ad.getIsBulkRemoveAction()
                    || ad.getIsBulkDeleteAction()
                    || ad.getIsOpenPageAction()
                    || ad.getIsOpenCreateFormAction()
                    || ad.getIsOpenAddSelectorAction()
                    || ad.getIsOpenSetSelectorAction()
                    || ad.getIsExportAction();
        } else if (visualElement instanceof ButtonGroup buttonGroup) {
            return buttonGroup.getFQName().endsWith("::PageActions");
        }
        return false;
    }
}
