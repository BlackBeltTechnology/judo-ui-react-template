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
import hu.blackbelt.judo.meta.ui.data.OperationType;
import hu.blackbelt.judo.meta.ui.data.ReferenceType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import jdk.dynalink.Operation;
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.getReferenceClassType;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.firstToUpper;

@Log
@TemplateHelper
public class UiActionsHelper {
    public static List<ActionDefinition> getContainerOwnActionDefinitions(PageContainer container) {
        List<ButtonGroup> groups = new ArrayList<>();
        collectElementsOfType(container, groups, ButtonGroup.class);

        List<ActionDefinition> actionDefinitions = groups.stream()
                .flatMap(g -> g.getButtons().stream())
                .map(Button::getActionDefinition)
                .filter(a -> a instanceof CallOperationActionDefinition || a instanceof OpenPageActionDefinition || a instanceof OpenFormActionDefinition || a instanceof OpenSelectorActionDefinition/* || actionIsSetOpenAction(a) || actionIsAddOpenAction(a)*/)
                .collect(Collectors.toList());

        List<Button> buttons = new ArrayList<>();
        collectElementsOfType(container, buttons, Button.class);

        actionDefinitions.addAll(buttons.stream().map(Button::getActionDefinition).toList());
        actionDefinitions.addAll(buttons.stream().map(Button::getPreFetchActionDefinition).filter(Objects::nonNull).toList());

        actionDefinitions.sort(Comparator.comparing(NamedElement::getFQName));

        return actionDefinitions;
    }

    public static String getContainerOwnActionParameters(ActionDefinition actionDefinition, PageContainer container) {
        String res = "";
        // queryCustomizer: {{ classDataName container.dataElement 'QueryCustomizer' }}
        if (actionDefinition.getIsRefreshAction()) {
            res += "queryCustomizer: " + classDataName((ClassType) container.getDataElement(), "QueryCustomizer");
        } else if (actionDefinition.getTargetType() != null) {
            String targetName = classDataName(actionDefinition.getTargetType(), "Stored");
            if (container.isIsRelationSelector()) {
                res += "selected: " + targetName + "[]";
            } else if (!actionDefinition.getIsGetTemplateAction()) {
                res += "target?: " + targetName;
            }
        }

        return res;
    }

    public static String getContainerOwnActionReturnType(ActionDefinition actionDefinition, PageContainer container) {
        if (actionDefinition.getIsRefreshAction()) {
            // {{ classDataName container.dataElement 'Stored' }}{{# if container.table }}[]{{/ if }}
            return classDataName((ClassType) container.getDataElement(), "Stored") + (container.isTable() ? "[]" : "");
        } else if (actionDefinition.getIsPreFetchAction()) {
            return classDataName(actionDefinition.getTargetType(), "Stored");
        } else if (actionDefinition.getIsGetTemplateAction()) {
            return classDataName(actionDefinition.getTargetType(), "");
        }
        return "void";
    }

    public static <T extends VisualElement> void collectElementsOfType(VisualElement visualElement, List<T> acc, Class<T> elementType) {
        if (elementType.isInstance(visualElement)) {
            acc.add(elementType.cast(visualElement));
        } else if (visualElement instanceof Container) {
            for (VisualElement element : ((Container) visualElement).getChildren()) {
                collectElementsOfType(element, acc, elementType);
            }
        } else if (visualElement instanceof TabController) {
            for (Tab tab : ((TabController) visualElement).getTabs()) {
                collectElementsOfType(tab.getElement(), acc, elementType);
            }
        }
    }

    public static ActionDefinition getRefreshActionDefinitionForTable(Table table) {
        if (table.getRelationType() != null && !table.getRelationType().getIsRelationKindAssociation()) {
            return null;
        }
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsRefreshAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getRangeActionDefinitionForTable(Table table) {
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsSelectorRangeAction()).findFirst().orElse(null);
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
            if (actionDefinition.getIsAutocompleteRangeAction()) {
                return "queryCustomizer: " + classDataName(target, "QueryCustomizer");
            } else if (actionDefinition.getTargetType() != null) {
                return "target: " + classDataName(target, target.isIsMapped() ? "Stored" : "");
            }
        }
        return "";
    }

    public static String linkActionDefinitionResponseType(Link link, ActionDefinition actionDefinition) {
        if (actionDefinition.getIsAutocompleteRangeAction()) {
            return "Array<" + classDataName(((ReferenceType) link.getDataElement()).getTarget(), "Stored") + ">";
        }
        return "void";
    }

    public static String getServiceMethodSuffix(Action action) {
        String suffix = "";
        if (action.getOwnerDataElement() instanceof OperationType) {
            suffix += "For" + firstToUpper(action.getOwnerDataElement().getName());
        } else if (action.getOwnerDataElement() instanceof RelationType) {
            suffix += "For" + firstToUpper(action.getOwnerDataElement().getName());
        }
        return suffix;
    }

    public static String getDialogOpenParameters(PageDefinition pageDefinition) {
        List<String> result = new ArrayList<>();
        result.add("ownerData: any");
        if (pageDefinition.getContainer().isIsRelationSelector()) {
            result.add("alreadySelected: " + classDataName(getReferenceClassType(pageDefinition), "Stored") + "[]");
        }
        return String.join(", ", result);
    }

    public static String getSelectorOpenActionParameters(Action action, PageContainer container) {
        List<String> tokens = new ArrayList<>();
        if (container.isTable()) {
            if (action.getTargetPageDefinition().getContainer().isIsRelationSelector()) {
                tokens.add("{ __signedIdentifier: signedIdentifier }");
            } else {
                tokens.add("[]");
            }
        } else {
            tokens.add("data");
        }

        if (action.getTargetPageDefinition().getContainer().isIsRelationSelector()) {
            if (action.getTargetDataElement() instanceof RelationType check) {
                if (container.isTable()) {
                    tokens.add("[]");
                } else {
                    String result = "data." + check.getName();
                    boolean isCollection = check.isIsCollection();
                    if (isCollection) {
                        tokens.add(result + " ?? []");
                    } else {
                        tokens.add(result + "? [" + result + "] : []");
                    }
                }
            }
        }
        return String.join(", ", tokens);
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
}
