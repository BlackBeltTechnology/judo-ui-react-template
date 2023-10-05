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

@Log
@TemplateHelper
public class UiActionsHelper {

//    public static String actionFunctionName(Action action) {
//        String tmp = pageActionPathSuffix(action);
//
//        return (tmp.contains("/") ? tmp.substring(tmp.lastIndexOf("/") + 1) : tmp) + "Action";
//    }

//    public static String actionFunctionTypeName(Action action) {
//        return StringUtils.capitalize(actionFunctionName(action));
//    }
//
//    public static String actionFunctionHookName(Action action) {
//        return "use".concat(actionFunctionTypeName(action));
//    }
//
//    public static String actionFunctionHandlerTypeName(Action action, String handlerType) {
//        return StringUtils.capitalize(actionFunctionName(action)) + handlerType;
//    }

//    public static String pageActionPathSuffix(Action action) {
//        PageDefinition page = (PageDefinition) action.eContainer();
//        String result = "";
//        String[] segments = action.getName().split("::");
//        String end = segments[segments.length - 1];
//        String first = end.split("#")[0];
//        String last = end.split("#")[1];
//
//        if (action instanceof CallOperationAction) {
//            if (!action.getDataElement().getOwner().getName().equals(page.getDataElement().getOwner().getName())) {
//                String targetClassName = getClassName((ClassType) action.getDataElement().getOwner());
//                first = targetClassName + StringUtils.capitalize(first);
//                return StringUtils.uncapitalize(targetClassName) + "/" + StringUtils.uncapitalize(first);
//            }
//            return first;
//        } else if (first.equals(page.getDataElement().getName())) {
//            result += StringUtils.uncapitalize(last);
//        } else {
//            result += first + "/" + StringUtils.uncapitalize(last);
//        }
//
//        String suffix = action.getDataElement().getName();
//
//        return result.length() > 0 ? StringUtils.uncapitalize(result) + StringUtils.capitalize(suffix) : suffix;
//    }
//
//    public static String pageActionFilePathSuffix(Action action) {
//        String actionPath = pageActionPathSuffix(action);
//        String converted = actionPath;
//
//        if (actionPath.contains("/")) {
//            int lastSlashPosition = actionPath.lastIndexOf("/");
//            String lead = actionPath.substring(0, lastSlashPosition);
//            String trail = actionPath.substring(lastSlashPosition + 1);
//            converted = lead + "/" + StringUtils.capitalize(trail);
//        } else {
//            converted = StringUtils.capitalize(converted);
//        }
//
//        return converted;
//    }
//
//    public static String pageActionFormPathSuffix(Action action) {
//        return pageActionFilePathSuffix(action) + "Form";
//    }
//
//    public static String pageActionFormComponentName(Action action) {
//        String full = pageActionFormPathSuffix(action);
//
//        return cutAtLastSlash(full);
//    }
//
//    public static List<Action> getActionsForPages(Application application) {
//        return getPagesForRouting(application).stream()
//                .flatMap(p -> getUniquePageActions(p).stream())
//                .collect(Collectors.toList());
//    }
//
//    public static List<Action> getActionsForViewDialogs(Application application) {
//        return getViewDialogs(application).stream()
//                .flatMap(p -> getUniquePageActions(p).stream())
//                .collect(Collectors.toList());
//    }
//
//    public static List<Action> getActionsForOutputPages(Application application) {
//        return getUnmappedOutputViewsForPages(application).stream().flatMap(p -> p.getActions().stream()).collect(Collectors.toList());
//    }
//
//    public static List<Action> getActionFormsForPages(Application application) {
//        List<Action> actions = getActionsForPages(application);
//        return actions.stream().filter(UiActionsHelper::actionHasInputForm).collect(Collectors.toList());
//    }
//
//    public static List<Action> getActionFormsForViewDialogs(Application application) {
//        List<Action> actions = getActionsForViewDialogs(application);
//        return actions.stream().filter(UiActionsHelper::actionHasInputForm).collect(Collectors.toList());
//    }
//
//    public static List<KeyValue<Link, Action>> getLinksForActionFormPages(Application application) {
//        List<Action> actions = getActionFormsForPages(application);
//        List<KeyValue<Link, Action>> kvs = new ArrayList<>();
//        actions.forEach(a -> {
//            PageDefinition page = getTargetFormForAction(a);
//            ((List<Link>) page.getOriginalPageContainer().getLinks()).forEach(l -> {
//                kvs.add(new KeyValue<>(l, a));
//            });
//        });
//        return kvs;
//    }
//
//    public static List<KeyValue<Table, Action>> getTablesForActionFormPages(Application application) {
//        List<Action> actions = getActionFormsForPages(application);
//        List<KeyValue<Table, Action>> kvs = new ArrayList<>();
//        actions.forEach(a -> {
//            PageDefinition page = getTargetFormForAction(a);
//            ((List<Table>) page.getOriginalPageContainer().getTables()).forEach(l -> {
//                kvs.add(new KeyValue<>(l, a));
//            });
//        });
//        return kvs;
//    }
//
//    public static List<KeyValue<Link, Action>> getLinksForActionFormViewDialogs(Application application) {
//        List<Action> actions = getActionFormsForViewDialogs(application);
//        List<KeyValue<Link, Action>> kvs = new ArrayList<>();
//        actions.forEach(a -> {
//            PageDefinition page = getTargetFormForAction(a);
//            ((List<Link>) page.getOriginalPageContainer().getLinks()).forEach(l -> {
//                kvs.add(new KeyValue<>(l, a));
//            });
//        });
//        return kvs;
//    }
//
//    public static List<KeyValue<Table, Action>> getTablesForActionFormViewDialogs(Application application) {
//        List<Action> actions = getActionFormsForViewDialogs(application);
//        List<KeyValue<Table, Action>> kvs = new ArrayList<>();
//        actions.forEach(a -> {
//            PageDefinition page = getTargetFormForAction(a);
//            ((List<Table>) page.getOriginalPageContainer().getTables()).forEach(l -> {
//                kvs.add(new KeyValue<>(l, a));
//            });
//        });
//        return kvs;
//    }
//
//    public static PageDefinition getTargetFormForAction(Action action) {
//        PageDefinition page;
//        if (action.getIsCallOperationAction()) {
//            page = ((CallOperationAction) action).getInputParameterPage();
//        } else if (action.getIsCreateAction()) {
//            page = ((CreateAction) action).getTarget();
//        } else {
//            throw new RuntimeException("Unsupported page type for action: " + action.toString());
//        }
//        return page;
//    }
//
//    public static List<PageDefinition> getUnmappedOutputViewsForPages(Application application) {
//        Set<PageDefinition> unmappedOutputForms = getActionsForPages(application)
//                .stream()
//                .filter(a -> a instanceof CallOperationAction && actionHasUnmappedOutputForm(a))
//                .map(a -> ((CallOperationAction) a).getOutputParameterPage()).collect(Collectors.toSet());
//        return unmappedOutputForms.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
//    }
//
//    public static List<Link> getLinksForUnmappedOutputViewPages(Application application) {
//        List<PageDefinition> pages = getUnmappedOutputViewsForPages(application);
//
//        return pages.stream()
//                .flatMap(p -> ((List<Link>) p.getOriginalPageContainer().getLinks()).stream())
//                .collect(Collectors.toList());
//    }
//
//    public static List<Table> getTablesForUnmappedOutputViewPages(Application application) {
//        List<PageDefinition> pages = getUnmappedOutputViewsForPages(application);
//
//        return pages.stream()
//                .flatMap(p -> ((List<Table>) p.getOriginalPageContainer().getTables()).stream())
//                .collect(Collectors.toList());
//    }
//
//    public static String actionsPath(PageDefinition page) {
//        String base = pagesFolderPath(((Application)page.eContainer()).getActor()).concat(getPageTypePath(page));
//        return base.concat(base.endsWith("/") ? "" : "/").concat("actions");
//    }
//
//    public static String actionsIndexPath(PageDefinition page) {
//        return actionsPath(page).concat("/index.tsx");
//    }
//
//    public static boolean actionHasInputForm(Action action) {
//        if (action.getIsCreateAction()) {
//            return true;
//        }
//        if (action.getIsCallOperationAction()) {
//            PageDefinition input = ((CallOperationAction) action).getInputParameterPage();
//
//            return input != null && !((ReferenceType) input.getDataElement()).getTarget().isIsMapped();
//        }
//        return false;
//    }
//
//    public static boolean actionHasUnmappedOutputForm(Action action) {
//        if (action.getIsCallOperationAction()) {
//            CallOperationAction callOperationAction = (CallOperationAction) action;
//
//            if (callOperationAction.getOutputParameterPage() != null) {
//                return !isPageRefreshable(callOperationAction.getOutputParameterPage());
//            }
//        }
//        return false;
//    }
//
//    public static boolean isActionAccess(Action action) {
//        if (action instanceof PageAction) {
//            PageDefinition target = ((PageAction) action).getTarget();
//            return target != null && target.getRelationType() != null && target.getRelationType().isIsAccess();
//        }
//        return false;
//    }
//
//    public static boolean hasConfirmation(Action action) {
//        return action.getIsConfirmationTypeConditional() || action.getIsConfirmationTypeMandatory();
//    }
//
//    public static boolean actionHasVisualElements(Action action) {
//        // At the time of writing we only wanted to use this functionality for create actions.
//        if (action instanceof CreateAction) {
//            CreateAction createAction = (CreateAction) action;
//            VisualElement dataContainer = getDataContainerForPage(createAction.getTarget());
//
//            if (dataContainer != null) {
//                return dataContainer instanceof Flex && ((Flex) dataContainer).getChildren().size() > 0;
//            }
//        }
//
//        return false;
//    }
//
//    public static EObject getActionContainer(Action action) {
//        return action.eContainer();
//    }
//
//    public static boolean isLinkAction (Action action) {
//        return action.getType().equals(ActionType.LINK);
//    }
//
//    public static boolean hasCallOperationActionFaults(CallOperationAction action) {
//        return action.getOperation().getFaults().size() > 0;
//    }

    public static boolean actionIsSetOpenAction(ActionDefinition actionDefinition) {
        return actionDefinition.getName().endsWith("::Set::Open");
    }

    public static boolean actionIsAddOpenAction(ActionDefinition actionDefinition) {
        return actionDefinition.getName().endsWith("::Add::Open");
    }

    public static List<ActionDefinition> getButtonGroupActionDefinitions(PageContainer container) {
        List<ButtonGroup> groups = new ArrayList<>();
        collectElementsOfType(container, groups, ButtonGroup.class);

        List<ActionDefinition> actionDefinitions = groups.stream()
                .flatMap(g -> g.getButtons().stream())
                .map(b -> b.getActionDefinition())
                .filter(a -> a instanceof CallOperationActionDefinition || a instanceof OpenPageActionDefinition || actionIsSetOpenAction(a) || actionIsAddOpenAction(a))
                .collect(Collectors.toList());

        List<Button> buttons = new ArrayList<>();
        collectElementsOfType(container, buttons, Button.class);

        actionDefinitions.addAll(buttons.stream().map(Button::getActionDefinition).toList());

        actionDefinitions.sort(Comparator.comparing(NamedElement::getFQName));

        return actionDefinitions;
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

    public static ActionDefinition getFilterActionDefinitionForTable(Table table) {
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsFilterAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getRefreshActionDefinitionForTable(Table table) {
        return (ActionDefinition) table.getTableActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsRefreshAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getRefreshActionDefinitionForContainer(PageContainer container) {
        return (ActionDefinition) container.getPageActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsRefreshAction()).findFirst().orElse(null);
    }

    public static ActionDefinition getUpdateActionDefinitionForContainer(PageContainer container) {
        return (ActionDefinition) container.getPageActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getIsUpdateAction()).findFirst().orElse(null);
    }

    public static String getActionTemplate(Action action) {
        String componentsLocation = "actor/src/pages/v2/actions/";
        String actionDefinitionBareName = action.getActionDefinition().eClass().getInstanceClass().getSimpleName();
        String suffixToCut = "Definition";
        String actionName = actionDefinitionBareName.substring(0, actionDefinitionBareName.length() - suffixToCut.length());
        return componentsLocation + actionName + ".fragment.hbs";
    }
}
