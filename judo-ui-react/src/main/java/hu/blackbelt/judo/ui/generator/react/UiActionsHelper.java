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
import org.eclipse.emf.ecore.EObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiPageHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.*;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.firstToUpper;

@Log
@TemplateHelper
public class UiActionsHelper {
    public static Set<ActionDefinition> getContainerOwnActionDefinitions(PageContainer container) {
        List<ButtonGroup> groups = new ArrayList<>();
        collectElementsOfType(container, groups, ButtonGroup.class);

        Set<ActionDefinition> actionDefinitions = groups.stream()
                .flatMap(g -> g.getButtons().stream())
                .map(Button::getActionDefinition)
                .filter(a -> a instanceof CallOperationActionDefinition || a instanceof OpenPageActionDefinition || a instanceof OpenFormActionDefinition || a instanceof OpenSelectorActionDefinition)
                .collect(Collectors.toSet());

        List<Flex> allFlex = new ArrayList<>();
        collectElementsOfType(container, allFlex, Flex.class);

        Set<ActionDefinition> flexActionDefinitions = allFlex.stream()
                .filter(f -> f.getActionButtonGroup() != null)
                .flatMap(g -> g.getActionButtonGroup().getButtons().stream())
                .map(Button::getActionDefinition)
                .filter(a -> a instanceof CallOperationActionDefinition || a instanceof OpenPageActionDefinition || a instanceof OpenFormActionDefinition || a instanceof OpenSelectorActionDefinition)
                .collect(Collectors.toSet());

        List<Button> buttons = new ArrayList<>();
        collectElementsOfType(container, buttons, Button.class);

        actionDefinitions.addAll(buttons.stream().map(Button::getActionDefinition).toList());
        actionDefinitions.addAll(buttons.stream().map(Button::getPreFetchActionDefinition).filter(Objects::nonNull).toList());

        SortedSet<ActionDefinition> sorted = new TreeSet<>(Comparator.comparing(NamedElement::getFQName));

        sorted.addAll(actionDefinitions);
        sorted.addAll(flexActionDefinitions);

        return sorted;
    }

    public static String getContainerOwnActionParameters(ActionDefinition actionDefinition, PageContainer container) {
        String res = "";
        // queryCustomizer: {{ classDataName container.dataElement 'QueryCustomizer' }}
        if (actionDefinition.getIsRefreshAction()) {
            res += "queryCustomizer: " + classDataName((ClassType) container.getDataElement(), "QueryCustomizer");
        } else if (actionDefinition.getIsCreateAction()) {
            res += "openCreated?: boolean";
        } else if (actionDefinition.getTargetType() != null) {
            String targetName = classDataName(actionDefinition.getTargetType(), "Stored");
            if (container.isIsRelationSelector()) {
                res += "selected: " + targetName + "[]";
            } else if (actionDefinition.getIsOpenPageAction()) {
                res += "target: " + targetName + ", isDraft?: boolean";
            } else if (!actionDefinition.getIsGetTemplateAction()) {
                res += "target?: " + targetName;
            }
        }

        return res;
    }

    public static String getContainerOwnActionReturnType(ActionDefinition actionDefinition, PageContainer container) {
        if (actionDefinition.getIsRefreshAction()) {
            // {{ classDataName container.dataElement 'Stored' }}{{# if container.table }}[]{{/ if }}
            return "JudoRestResponse<" + classDataName((ClassType) container.getDataElement(), "Stored") + (container.isTable() ? "[]" : "") + ">";
        } else if (actionDefinition.getIsPreFetchAction()) {
            return "JudoRestResponse<" + classDataName(actionDefinition.getTargetType(), "Stored") + " | null>";
        } else if (actionDefinition.getIsGetTemplateAction()) {
            return "JudoRestResponse<" + classDataName(actionDefinition.getTargetType(), "") + ">";
        }
        return "void";
    }

    public static ActionDefinition getRefreshActionDefinitionForTable(Table table) {
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsRefreshAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getRangeActionDefinitionForTable(Table table) {
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsSelectorRangeAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getGetTemplateActionDefinitionForContainer(PageContainer container) {
        return (ActionDefinition) container.getPageActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsGetTemplateAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getCreateDefinitionForContainer(PageContainer container) {
        return (ActionDefinition) container.getPageActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsCreateAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getRefreshActionDefinitionForContainer(PageContainer container) {
        if (container.isTable()) {
            return getRefreshActionDefinitionForTable((Table) container.getTables().get(0));
        }
        return (ActionDefinition) container.getPageActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsRefreshAction()).findFirst().orElse(null);
    }

    public static String getActionTemplate(Action action) {
        String componentsLocation = "actor/src/pages/actions/";
        String actionDefinitionBareName = action.getActionDefinition().eClass().getInstanceClass().getSimpleName();
        String suffixToCut = "Definition";
        String actionName = actionDefinitionBareName.substring(0, actionDefinitionBareName.length() - suffixToCut.length());
        return componentsLocation + actionName + ".fragment.hbs";
    }

    public static PageContainer getPageContainerForActionDefinition(ActionDefinition actionDefinition) {
        PageContainer pageContainer = null;
        EObject parent = actionDefinition.eContainer();

        while (parent.eContainer() != null) {
            if (parent instanceof PageContainer) {
                pageContainer = (PageContainer) parent;
                break;
            } else {
                parent = parent.eContainer();
            }
        }

        return pageContainer;
    }

    public static Link getLinkParentForActionDefinition(ActionDefinition actionDefinition) {
        Link link = null;
        EObject parent = actionDefinition.eContainer();

        while (parent.eContainer() != null) {
            if (parent instanceof Link) {
                link = (Link) parent;
                break;
            } else {
                parent = parent.eContainer();
            }
        }

        return link;
    }

    public static Table getTableParentForActionDefinition(ActionDefinition actionDefinition) {
        Table table = null;
        EObject parent = actionDefinition.eContainer();

        while (parent.eContainer() != null) {
            if (parent instanceof Table) {
                table = (Table) parent;
                break;
            } else {
                parent = parent.eContainer();
            }
        }

        return table;
    }

    public static String linkActionDefinitionParameters(Link link, ActionDefinition actionDefinition) {
        if (link.getDataElement() instanceof ReferenceType referenceType) {
            ClassType target = referenceType.getTarget();
            if (actionDefinition.getIsAutocompleteRangeAction() || actionDefinition.getIsRefreshAction()) {
                return "queryCustomizer: " + classDataName(target, "QueryCustomizer");
            } else if (actionDefinition.getTargetType() != null) {
                String base = "target: " + classDataName(target, target.isIsMapped() ? "Stored" : "");
                if (actionDefinition.getIsOpenPageAction()) {
                    base += ", isDraft?: boolean";
                }
                return base;
            }
        }
        return "";
    }

    public static String linkActionDefinitionResponseType(Link link, ActionDefinition actionDefinition) {
        if (actionDefinition.getIsAutocompleteRangeAction()) {
            return "Array<" + classDataName(((ReferenceType) link.getDataElement()).getTarget(), "Stored") + ">";
        } else if (actionDefinition.getIsOpenSelectorAction()) {
            return classDataName(((ReferenceType) link.getDataElement()).getTarget(), "Stored") + " | undefined";
        } else if (actionDefinition.getIsRefreshAction()) {
            return "JudoRestResponse<" + classDataName(((ReferenceType) link.getDataElement()).getTarget(), "Stored") + " | null>";
        }
        return "void";
    }

    public static String getServiceMethodSuffix(Action action) {
        String suffix = "";
        if (action.getOwnerDataElement() instanceof OperationType) {
            suffix += "On" + firstToUpper(action.getOwnerDataElement().getName());
        } else if (action.getOwnerDataElement() instanceof RelationType) {
            suffix += "For" + firstToUpper(action.getOwnerDataElement().getName());
        }
        return suffix;
    }

    public static boolean isPageDataElementUnmappedSingle(PageDefinition pageDefinition) {
        return pageHasOutputTarget(pageDefinition)
                && !getPageOutputTarget(pageDefinition).isIsMapped()
                && pageDefinition.getDataElement() instanceof OperationParameterType parameterType &&
                !parameterType.isIsCollection();
    }

    public static RelationType getRelationTypeForActionTarget(Action action) {
        if (action.getTargetDataElement() instanceof RelationType check) {
            return check;
        }
        return null;
    }

    public static boolean isActionAddOrSet(ActionDefinition actionDefinition) {
        return actionDefinition.getIsAddAction() || actionDefinition.getIsSetAction();
    }

    public static String operationCallSuffix(Action action) {
        if (action.getActionDefinition() instanceof CallOperationActionDefinition actionDefinition) {
            boolean isOnViewPage = ((PageDefinition)action.eContainer()).getContainer().isView();
            Table table = getTableParentForActionDefinition(actionDefinition);
            Link link = getLinkParentForActionDefinition(actionDefinition);
            if (isOnViewPage && (table != null || link != null)) {
                return "For" + StringUtils.capitalize(action.getOwnerDataElement().getName());
            }
        }
        return "";
    }

    public static VisualElement translationElementForBulkAction(Action action) {
        // .eContainer is not working in templates...
        return (VisualElement) action.getActionDefinition().eContainer();
    }

    public static boolean isActionOnOperationInput(Action action) {
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();
        // exclude output views...
        return pageDefinition.getDataElement() instanceof OperationParameterType && !pageDefinition.getContainer().isView();
    }

    public static String getOperationNameForActionOnInput(Action action) {
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();
        return ((OperationType) pageDefinition.getDataElement().eContainer()).getName();
    }

    public static String refreshActionDataParameter(Action action) {
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();
        if (pageDefinition.isOpenInDialog()) {
            if (isRefreshPageInitializer(action, pageDefinition)) {
                return "ownerData";
            }
            return "data";
        }
        if (pageHasSignedId(pageDefinition)) {
            return "{ __signedIdentifier: signedIdentifier } as any";
        }
        if (isSingleAccessPage(pageDefinition)) {
            return "owner.current";
        }
        return "undefined";
    }

    public static String postCreateActionParams(PageDefinition page, ActionDefinition actionDefinition) {
        List<String> tokens = new ArrayList<>();
        String type = classDataName(getReferenceClassType(page), "Stored");
        tokens.add("data: " + classDataName(getReferenceClassType(page), ""));
        tokens.add("res: " + type);
        tokens.add("onSubmit: (result?: " + type + ") => Promise<void>");
        tokens.add("onClose: () => Promise<void>");
        tokens.add("openCreated?: boolean");
        return String.join(", ", tokens);
    }

    public static boolean isRelationOpenCreateActionOnForm(PageDefinition pageDefinition, Action action) {
        return pageDefinition.getContainer().isForm()
                && action.getIsOpenFormAction();
    }

    public static boolean isRelationOpenCreateActionOnEagerView(PageDefinition pageDefinition, Action action) {
        return pageDefinition.getContainer().isView()
                && action.getIsOpenFormAction()
                && action.getTargetDataElement() instanceof RelationType relationType
                && relationType.isIsInlineCreatable();
    }

    public static String postCallOperationActionParams(PageDefinition page, ActionDefinition actionDefinition) {
        List<String> tokens = new ArrayList<>();
        if (actionDefinition.getTargetType() != null) {
            tokens.add("data: " + classDataName(actionDefinition.getTargetType(), "Stored"));
        }
        if (actionDefinition instanceof CallOperationActionDefinition call && call.getOperation().getOutput() != null) {
            tokens.add("output: " + classDataName(call.getOperation().getOutput().getTarget(), "Stored"));
        }
        if (page.getContainer().isForm()) {
            String result = (pageHasOutputTarget(page) ? classDataName(getPageOutputTarget(page), "Stored") : dialogDataType(page)) + (page.getContainer().isTable() ? "[]" : "");
            tokens.add("onSubmit: (result?: " + result + ") => Promise<void>");
        }
        if (page.isOpenInDialog()) {
            tokens.add("onClose: () => Promise<void>");
        }
        return String.join(", ", tokens);
    }

    public static String postRefreshActionParams(PageDefinition page, ActionDefinition actionDefinition) {
        String res = "";
        res += "data: " + classDataName(getReferenceClassType(page), "Stored") + (page.getContainer().isTable() ? "[]" : "");
        if (!page.getContainer().isTable()) {
            res += ", ";
            res += "storeDiff: (attributeName: keyof " + classDataName(getReferenceClassType(page), "") + ", value: any) => void, ";
            res += "setValidation: Dispatch<SetStateAction<Map<keyof " + classDataName(getReferenceClassType(page), "") + ", string>>>";
        }
        return res;
    }

    public static String postGetTemplateActionParams(PageDefinition page, ActionDefinition actionDefinition) {
        String res = "";
        res += "ownerData: any, ";
        res += "data: " + classDataName(getReferenceClassType(page), "") + ", ";
        res += "storeDiff: (attributeName: keyof " + classDataName(getReferenceClassType(page), "") + ", value: any) => void, ";
        return res;
    }

    public static String onBlurActionParams(PageContainer container) {
        List<String> tokens = new ArrayList<>();
        if (!container.isTable()) {
            tokens.add("data: " + classDataName((ClassType) container.getDataElement(), "Stored"));
            tokens.add("storeDiff: (attributeName: keyof " + classDataName((ClassType) container.getDataElement(), "") + ", value: any) => void");
            tokens.add("editMode: boolean");
//            tokens.add("setValidation: Dispatch<SetStateAction<Map<keyof " + classDataName((ClassType) container.getDataElement(), "") + ", string>>>");
        }
        tokens.add("submit: () => Promise<void>");

        return String.join(", ", tokens);
    }

    public static String inputModifierParams(PageContainer container, Boolean checkIsLoading) {
        List<String> tokens = new ArrayList<>();
        if (!container.isTable()) {
            tokens.add("data: " + classDataName((ClassType) container.getDataElement(), "") + " | " + classDataName((ClassType) container.getDataElement(), "Stored"));
            tokens.add("editMode?: boolean");
            if (checkIsLoading != null && checkIsLoading) {
                tokens.add("isLoading?: boolean");
            }
        }

        return String.join(", ", tokens);
    }

    public static ClassType getActionOperationOutputClassType(Action action) {
        if (action.getActionDefinition() instanceof CallOperationActionDefinition callOperationActionDefinition) {
            if (callOperationActionDefinition.getOperation().getOutput() != null) {
                return callOperationActionDefinition.getOperation().getOutput().getTarget();
            }
        }
        return null;
    }

    public static boolean isActionOutputMapped(Action action) {
        return getActionOperationOutputClassType(action) != null && getActionOperationOutputClassType(action).isIsMapped();
    }

    public static Action getOpenFormActionPairForOpenPageAction(PageDefinition pageDefinition, Action action) {
        if (action.getActionDefinition().getIsOpenPageAction()) {
            return pageDefinition.getActions().stream()
                    .filter(a -> a.getActionDefinition().getIsOpenFormAction() && a.getTargetDataElement() != null && action.getTargetDataElement() != null && a.getTargetDataElement().equals(action.getTargetDataElement()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static String actionTargetPageName(Action action) {
        return pageName(action.getTargetPageDefinition());
    }

    public static boolean createNestedValidation(RelationType relationType, PageDefinition pageDefinition) {
        return relationType != null && !pageDefinition.getContainer().isTable() && (relationType.getIsCreateValidatable() || relationType.getIsUpdateValidatable());
    }

    public static boolean skipNestedValidationBody(PageDefinition pageDefinition) {
        return pageDefinition.getDataElement() instanceof OperationParameterType operationParameterType && !operationParameterType.getTarget().isIsMapped();
    }

    public static boolean isRowActionCRUD(ActionDefinition actionDefinition) {
        return actionDefinition.getIsRemoveAction() || actionDefinition.getIsRowDeleteAction();
    }

    public static boolean allowRefreshAfterOperationCall(Action action) {
        if (action.getActionDefinition() instanceof CallOperationActionDefinition callOperationActionDefinition) {
            if (callOperationActionDefinition.getOperation().getOutput() == null) {
                return true;
            } else if (action.getTargetPageDefinition() == null || action.getTargetPageDefinition().isOpenInDialog()) {
                return true;
            } else {
                return false;
            }
        }
        throw new RuntimeException("The allowRefreshAfterOperationCall helper received Action type which is not a CallOperationAction type: " + action.getFQName());
    }

    public static Action getRowViewActionForCreateOpenAction(Action action) {
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();
        if (pageDefinition.getContainer().isTable() && ((RelationType) pageDefinition.getDataElement()).isIsAccess()) {
            return ((PageDefinition) action.eContainer()).getActions().stream().filter(a -> a.getActionDefinition().getIsOpenPageAction()).findFirst().orElse(null);
        }
        if (action.getActionDefinition().getIsOpenCreateFormAction()) {
            return pageDefinition.getActions().stream()
                    .filter(t -> t.getActionDefinition().getIsOpenPageAction() && t.getTargetDataElement() != null && t.getTargetDataElement().equals(action.getTargetDataElement()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static boolean isActionParentEagerElement(Action action) {
        Table table = getTableParentForActionDefinition(action.getActionDefinition());
        Link link = getLinkParentForActionDefinition(action.getActionDefinition());
        if (table != null) {
            return table.isIsEager();
        }
        if (link != null) {
            return link.isIsEager();
        }
        return  false;
    }

    public static boolean isActionParentEagerTable(Action action) {
        Table table = getTableParentForActionDefinition(action.getActionDefinition());
        return table != null && table.isIsEager();
    }

    public static boolean isRefreshPageInitializer(Action action, PageDefinition page) {
        if (action.getIsRefreshAction()) {
            return page.getContainer().getOnInit() != null && page.getContainer().getOnInit().equals(action.getActionDefinition());
        }
        return false;
    }
}
