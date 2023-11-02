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
        if (page.getDataElement() != null) {
            if (page.getDataElement() instanceof RelationType dataElement) {
                if (dataElement.getIsMemberTypeAccess()) {
                    return page.getName().endsWith("::View::Page") && !(dataElement.getIsCreatable() && !dataElement.isIsCollection());
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

        if (pageDefinition.getDataElement() instanceof ReferenceType) {
            res.addAll(getApiImportsForReferenceType((ReferenceType) pageDefinition.getDataElement()));
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
        }

        if (isPageForOperationParameterType(pageDefinition) && pageHasOutputTarget(pageDefinition)) {
            res.add(classDataName(getPageOutputTarget(pageDefinition), ""));
            res.add(classDataName(getPageOutputTarget(pageDefinition), "Stored"));
        }

        getEnumAttributesForPage(pageDefinition).forEach(a -> {
            res.add(restParamName(a.getDataType()));
        });

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<PageDefinition> getRelatedPages(PageDefinition pageDefinition) {
        Set<PageDefinition> res = new HashSet<>();
        try {
            for (Action action : pageDefinition.getActions().stream().filter(a -> a.getIsOpenPageAction() && !a.getTargetPageDefinition().isOpenInDialog()).toList()) {
                res.add(action.getTargetPageDefinition());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static List<PageDefinition> getRelatedDialogs(PageDefinition pageDefinition) {
        Set<PageDefinition> res = new HashSet<>();
        try {
            for (Action action : pageDefinition.getActions().stream().filter(a -> a.getTargetPageDefinition() != null && a.getTargetPageDefinition().isOpenInDialog() && !a.getTargetPageDefinition().equals(pageDefinition)).toList()) {
                res.add(action.getTargetPageDefinition());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static String getServiceImplForPage(PageDefinition pageDefinition) {
        Application application = (Application) pageDefinition.eContainer();
        Set<String> res = new HashSet<>();
        DataElement dataElement = pageDefinition.getDataElement();

        if (dataElement instanceof RelationType) {
            return firstToLower(serviceRelationName((RelationType) dataElement)) + "Impl";
        } else if (dataElement instanceof OperationParameterType) {
            ClassType cls = ((OperationParameterType) dataElement).getTarget();
            RelationType targetRelationType = (RelationType) application.getRelationTypes().stream()
                    .filter(r -> ((RelationType) r).getTarget().equals(cls))
                    .findFirst()
                    .orElse(null);

            if (targetRelationType != null) {
                return firstToLower(serviceRelationName(targetRelationType)) + "Impl";
            }

            RelationType operationInputRelationType = (RelationType) application.getRelationTypes().stream()
                    .filter(r -> ((RelationType) r).getTarget().getOperations().stream().anyMatch(o -> o.getInput() != null && o.getInput().getTarget().equals(cls)))
                    .findFirst()
                    .orElse(null);

            if (operationInputRelationType != null) {
                return firstToLower(serviceRelationName(operationInputRelationType)) + "Impl";
            }
        } else if (dataElement instanceof OperationType) {
            OperationType operationType = (OperationType) dataElement;
            if (operationType.getInput() != null) {
                ClassType cls = operationType.getInput().getTarget();
                RelationType operationInputRelationType = (RelationType) application.getRelationTypes().stream()
                        .filter(r -> ((RelationType) r).getTarget().getOperations().stream().anyMatch(o -> o.getInput() != null && o.getInput().getTarget().equals(cls)))
                        .findFirst()
                        .orElse(null);

                if (operationInputRelationType != null) {
                    return firstToLower(serviceRelationName(operationInputRelationType)) + "Impl";
                }
            }
        }

        return null;
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
        OperationParameterType type = (OperationParameterType) page.getDataElement();
        if (type.eContainer() instanceof OperationType) {
            return ((OperationType) type.eContainer()).getOutput() != null;
        }
        return false;
    }

    public static ClassType getPageOutputTarget(PageDefinition page) {
        OperationParameterType type = (OperationParameterType) page.getDataElement();
        return ((OperationType) type.eContainer()).getOutput().getTarget();
    }

    public static boolean dialogHasResult(PageDefinition page) {
        return !isPageForOperationParameterType(page) || pageHasOutputTarget(page);
    }
}
