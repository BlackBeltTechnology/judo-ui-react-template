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

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.getActionOperationOutputClassType;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.collectVisualElementsMatchingCondition;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.getReferenceClassType;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.*;
import static java.util.Arrays.stream;


@Log
@TemplateHelper
public class UiPageHelper {

    public static boolean isPageRefreshable(PageDefinition pageDefinition) {
        return pageDefinition.getActions().stream().anyMatch(a -> a.getIsRefreshAction() || a.getIsRefreshRelationAction());
    }

    public static List<PageDefinition> getPagesForRouting(Application application) {
        return application.getPages().stream()
                .filter(p -> !p.isOpenInDialog())
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static List<PageDefinition> getPagesForDialogs(Application application) {
        return application.getPages().stream()
                .filter(PageDefinition::isOpenInDialog)
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static boolean hasDashboard(Application application) {
      return  application.getPages().stream().anyMatch(PageDefinition::isDashboard);
    }

    public static boolean pageHasSignedId(PageDefinition page) {
        if (isSingleAccessPage(page) || page.isOpenInDialog()) {
            return false;
        }
        if (page.getDataElement() != null) {
            if (page.getDataElement() instanceof RelationType dataElement) {
                if (dataElement.getIsMemberTypeAccess()) {
                    return page.getName().endsWith("ViewPage") && !(dataElement.getIsCreatable() && !dataElement.isIsCollection());
                }
                return true;
            } else if (page.getName().endsWith("Output::View")) {
                return isPageRefreshable(page);
            }
        }

        return false;
    }

    public static String pagePath(PageDefinition page) {
        return stream(page.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining("/"));
    }

    public static String pageName(PageDefinition page) {
        return stream(page.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining(""));
    }

    public static String getPageRoute(PageDefinition page) {
        if (page.isDashboard()) {
            return "";
        }
        String suffix = pageHasSignedId(page) ? "/:signedIdentifier" : "";
        return pagePath(page) + suffix;
    }

    public static List<AttributeType> getEnumAttributesForPage(PageDefinition pageDefinition) {
        if (pageDefinition.getDataElement() == null) {
            return List.of();
        }
        ClassType target;

        DataElement dataElement = pageDefinition.getDataElement();
        if (dataElement instanceof RelationType) {
            target = ((RelationType) dataElement).getTarget();
        } else if (dataElement instanceof OperationParameterType) {
            target = ((OperationParameterType) dataElement).getTarget();
        } else if (dataElement instanceof ClassType) {
            target = (ClassType) dataElement;
        } else {
            return List.of();
        }

        return target.getAttributes()
                .stream()
                .filter(a -> a.getDataType() instanceof EnumerationType)
                .collect(Collectors.toList());
    }

    public static Boolean titleComesFromAttribute(PageContainer pageContainer) {
        return pageContainer.getTitleFrom() != null && pageContainer.getTitleFrom() == TitleFrom.ATTRIBUTE;
    }

    public static Boolean isSingleAccessPage(PageDefinition page) {
        if (page.getRelationType() != null) {
            return !page.getRelationType().isIsCollection() && page.getRelationType().isIsAccess();
        }

        return false;
    }

    public static List<String> getApiImportsForReferenceType(ReferenceType reference) {
        Set<String> res = new HashSet<>();

        if (reference.getTarget() != null) {
            res.add(classDataName(reference.getTarget(), ""));
            res.add(classDataName(reference.getTarget(), "Stored"));
            res.add(classDataName(reference.getTarget(), "QueryCustomizer"));
        }

        if (reference instanceof RelationType && !((RelationType) reference).isIsAccess()) {
            res.add(classDataName((ClassType) reference.getOwner(), ""));
            res.add(classDataName((ClassType) reference.getOwner(), "Stored"));
        }

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForPage(PageDefinition pageDefinition) {
        Set<String> res = new HashSet<>();

        if (pageDefinition.getDataElement() instanceof ReferenceType referenceType) {
            res.addAll(getApiImportsForReferenceType(referenceType));
        }

        for (Object table: pageDefinition.getContainer().getTables()) {
            Table theTable = (Table) table;
            // for some reason table's dataElement can be ClassType, not ReferenceType
            if (theTable.getDataElement() instanceof ClassType) {
                res.add(classDataName((ClassType) theTable.getDataElement(), ""));
                res.add(classDataName((ClassType) theTable.getDataElement(), "Stored"));
                res.add(classDataName((ClassType) theTable.getDataElement(), "QueryCustomizer"));
            }
            if (theTable.getDataElement() instanceof RelationType) {
                res.addAll(getApiImportsForReferenceType((RelationType) theTable.getDataElement()));
            }
        }

        for (Object link: pageDefinition.getContainer().getLinks()) {
            Link theLink = (Link) link;
            res.addAll(getApiImportsForReferenceType((RelationType) theLink.getDataElement()));
        }

        for (ActionDefinition actionDefinition: (List<ActionDefinition>) pageDefinition.getContainer().getAllActionDefinitions()) {
            if (actionDefinition.getTargetType() != null) {
                res.add(classDataName(actionDefinition.getTargetType(), ""));
                res.add(classDataName(actionDefinition.getTargetType(), "Stored"));
                res.add(classDataName(actionDefinition.getTargetType(), "QueryCustomizer"));
            }
            if (actionDefinition instanceof CallOperationActionDefinition callOperationActionDefinition && callOperationActionDefinition.getOperation().getOutput() != null) {
                res.add(classDataName(callOperationActionDefinition.getOperation().getOutput().getTarget(), ""));
                res.add(classDataName(callOperationActionDefinition.getOperation().getOutput().getTarget(), "Stored"));
            }
        }

        if (pageDefinition.getContainer().isIsSelector()) {
            if (pageDefinition.getDataElement() instanceof OperationType operationType) {
                if (operationType.getInput() != null) {
                    res.add(classDataName(operationType.getInput().getTarget(), "Stored"));
                }
                if (operationType.getOutput() != null) {
                    res.add(classDataName(operationType.getOutput().getTarget(), "Stored"));
                }
            } else if (pageDefinition.getDataElement() instanceof RelationType relationType) {
                res.add(classDataName(relationType.getTarget(), "Stored"));
            }
        }
        if (pageDefinition.getDataElement() instanceof OperationParameterType operationParameterType) {
            res.add(classDataName(operationParameterType.getTarget(), "Stored"));

            if (operationParameterType.eContainer() instanceof OperationType operationType) {
                if (operationType.getOutput() != null) {
                    res.add(classDataName(operationType.getOutput().getTarget(), "Stored"));
                }
            }
        }
        if (pageDefinition.getDataElement() instanceof RelationType relationType) {
            res.add(classDataName(relationType.getTarget(), "Stored"));
        }

        getEnumAttributesForPage(pageDefinition).forEach(a -> {
            res.add(restParamName(a.getDataType()));
        });

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<PageDefinition> getRelatedPages(PageDefinition pageDefinition) {
        Set<PageDefinition> res = new HashSet<>();
        try {
            // a.getTargetPageDefinition() != null check is for the case where the target view is not present because it was most likely empty
            List<Action> actions = pageDefinition.getActions()
                    .stream()
                    .filter(a -> a.getIsOpenPageAction() && a.getTargetPageDefinition() != null && !a.getTargetPageDefinition().isOpenInDialog())
                    .toList();
            for (Action action: actions) {
                res.add(action.getTargetPageDefinition());
            }
            List<Action> actionsForMappedNavigation = pageDefinition.getActions()
                    .stream()
                    .filter(a -> a.getIsCallOperationAction()
                            && getActionOperationOutputClassType(a) != null
                            && getActionOperationOutputClassType(a).isIsMapped()
                            && a.getTargetPageDefinition() != null
                            && !a.getTargetPageDefinition().isOpenInDialog()
                    )
                    .toList();
            for (Action action: actionsForMappedNavigation) {
                res.add(action.getTargetPageDefinition());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res.stream()
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .toList();
    }

    public static List<PageDefinition> getRelatedDialogs(PageDefinition pageDefinition, Boolean skipSelf) {
        Set<PageDefinition> res = new HashSet<>();
        try {
            for (Action action : pageDefinition.getActions().stream().filter(a -> a.getTargetPageDefinition() != null && a.getTargetPageDefinition().isOpenInDialog() && (!skipSelf || !a.getTargetPageDefinition().equals(pageDefinition))).toList()) {
                res.add(action.getTargetPageDefinition());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static String getServiceClassForPage(PageDefinition pageDefinition) {
        DataElement dataElement = pageDefinition.getDataElement();

        if (dataElement instanceof RelationType relationType) {
//            if (pageDefinition.getContainer().isView() && !isSingleAccessPage(pageDefinition)) {
//                return serviceClassName(relationType.getTarget()) + "Impl";
//            }
            return serviceRelationName(relationType) + "Impl";
        } else if (dataElement instanceof OperationParameterType operationParameterType) {
            if (operationParameterType.eContainer() instanceof OperationType operationType) {
                if (operationType.getOutput() != null && pageDefinition.getContainer().isView()) {
                    return serviceClassName(operationType.getOutput().getTarget()) + "Impl";
                }
                if (operationType.eContainer() instanceof ClassType classType) {
                    return serviceClassName(classType) + "Impl";
                }
                if (operationParameterType.eContainer() instanceof RelationType relationType) {
                    return serviceRelationName(relationType) + "Impl";
                }
            }
        } else if (dataElement instanceof OperationType operationType) {
            if (operationType.getOutput() != null && pageDefinition.getContainer().isView()) {
                return serviceClassName(operationType.getOutput().getTarget()) + "Impl";
            }
            if (operationType.eContainer() instanceof ClassType classType) {
                return serviceClassName(classType) + "Impl";
            }
            if (operationType.getInput() != null) {
                return serviceClassName(operationType.getInput().getTarget()) + "Impl";
            }
        }

        return null;
    }

    public static String getServiceImplForPage(PageDefinition pageDefinition) {
        String res = getServiceClassForPage(pageDefinition);
        return res != null ? firstToLower(res) : null;
    }

    public static boolean isPageUpdateable(PageDefinition pageDefinition) {
        return getReferenceClassType(pageDefinition).getIsUpdatable();
    }

    public static boolean isPageDeleteable(PageDefinition pageDefinition) {
        return getReferenceClassType(pageDefinition).getIsDeletable();
    }

    public static boolean payloadDiffHasItems(ClassType classType) {
        return classType.getAttributes().size() + classType.getRelations().size() > 0;
    }

    public static boolean pageShouldInitialize(PageDefinition pageDefinition) {
        // tables initialize themselves
        if (!pageDefinition.getContainer().isTable() && pageDefinition.getContainer().getOnInit() != null) {
            return pageDefinition.getActions().stream().anyMatch(a -> a.getActionDefinition().equals(pageDefinition.getContainer().getOnInit()));
        }
        return false;
    }

    public static boolean hasPageRequiredBy(PageDefinition pageDefinition) {
        return !(getRequiredByWidgetsForPage(pageDefinition).isEmpty());
    }

    public static List<VisualElement> getRequiredByWidgetsForPage(PageDefinition pageDefinition) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(pageDefinition.getContainer(), (element) -> element.getRequiredBy() != null, elements);

        return elements.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static List<PageContainer> getPageContainersToGenerate(Application application) {
        return application.getPageContainers().stream().filter(c -> !c.isForm() && !c.isIsSelector()).toList();
    }

    public static boolean isPageForOperationParameterType(PageDefinition page) {
        return page.getDataElement() instanceof OperationParameterType;
    }

    public static boolean pageHasOutputTarget(PageDefinition page) {
        if (page.getDataElement() instanceof OperationType operationType) {
            return operationType.getOutput() != null;
        }
        if (page.getDataElement() instanceof OperationParameterType operationParameterType) {
            if (operationParameterType.eContainer() instanceof OperationType operationType) {
                return operationType.getOutput() != null;
            }
        }
        return false;
    }

    public static ClassType getPageOutputTarget(PageDefinition page) {
        if (page.getDataElement() instanceof OperationType operationType) {
            return operationType.getOutput().getTarget();
        }
        if (page.getDataElement() instanceof OperationParameterType operationParameterType) {
            if (operationParameterType.eContainer() instanceof OperationType operationType) {
                return operationType.getOutput().getTarget();
            }
        }
        return null;
    }

    public static boolean dialogHasResult(PageDefinition page) {
        return !isPageForOperationParameterType(page) || pageHasOutputTarget(page);
    }

    public static String dialogBareDataType(PageDefinition page) {
        if (page.getContainer().isIsSelector()) {
            if (page.getDataElement() instanceof OperationType operationType) {
                return classDataName(operationType.getInput().getTarget(), "");
            } else if (page.getDataElement() instanceof RelationType relationType) {
                return classDataName(relationType.getTarget(), "");
            }
        }
        if (page.getDataElement() instanceof OperationParameterType operationParameterType) {
            return classDataName(operationParameterType.getTarget(), "");
        }
        if (page.getDataElement() instanceof RelationType relationType) {
            return classDataName(relationType.getTarget(), "");
        }
        return "void";
    }

    public static String dialogDataType(PageDefinition page) {
        String bareType = dialogBareDataType(page);
        if (bareType != "void") {
            return bareType + "Stored";
        }
        return "void";
    }

    public static Action getCreateActionForPage(PageDefinition page) {
        ActionDefinition def = page.getContainer().getActionButtonGroup().getButtons().stream().map(Button::getActionDefinition).filter(a -> a instanceof CreateActionDefinition).findFirst().orElse(null);
        if (def != null) {
            return page.getActions().stream().filter(a -> a.getActionDefinition().equals(def)).findFirst().orElse(null);
        }
        return null;
    }

    public static Action getUpdateActionForPage(PageDefinition page) {
        ActionDefinition def =  page.getContainer().getActionButtonGroup().getButtons().stream().map(Button::getActionDefinition).filter(a -> a instanceof UpdateActionDefinition).findFirst().orElse(null);
        if (def != null) {
            return page.getActions().stream().filter(a -> a.getActionDefinition().equals(def)).findFirst().orElse(null);
        }
        return null;
    }

    public static Action getCallOperationActionForPage(PageDefinition page) {
        ActionDefinition def =  page.getContainer().getActionButtonGroup().getButtons().stream().map(Button::getActionDefinition).filter(a -> a instanceof CallOperationActionDefinition).findFirst().orElse(null);
        if (def != null) {
            return page.getActions().stream().filter(a -> a.getActionDefinition().equals(def)).findFirst().orElse(null);
        }
        return null;
    }
}
