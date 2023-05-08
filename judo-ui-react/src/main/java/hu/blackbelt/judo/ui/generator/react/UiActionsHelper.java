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

    public static String actionFunctionName(Action action) {
        String tmp = pageActionPathSuffix(action);

        return (tmp.contains("/") ? tmp.substring(tmp.lastIndexOf("/") + 1) : tmp) + "Action";
    }

    public static String actionFunctionTypeName(Action action) {
        return StringUtils.capitalize(actionFunctionName(action));
    }

    public static String actionFunctionHookName(Action action) {
        return "use".concat(actionFunctionTypeName(action));
    }

    public static String actionFunctionHandlerTypeName(Action action, String handlerType) {
        return StringUtils.capitalize(actionFunctionName(action)) + handlerType;
    }

    public static String pageActionPathSuffix(Action action) {
        PageDefinition page = (PageDefinition) action.eContainer();
        String result = "";
        String[] segments = action.getName().split("::");
        String end = segments[segments.length - 1];
        String first = end.split("#")[0];
        String last = end.split("#")[1];

        if (action.getType().equals(ActionType.PAGE)) {
            last = last.replaceFirst("^Page", "");
        } else if (action.getType().equals(ActionType.ROW)) {
            last = last.replaceFirst("^Row", "");
        } else if (action.getType().equals(ActionType.LINK)) {
            last = last.replaceFirst("^Link", "");
        } else if (action.getType().equals(ActionType.TABLE)) {
            last = last.replaceFirst("^Table", "");
        } else if (action.getType().equals(ActionType.BUTTON)) {
            last = last.replaceFirst("^Button", "");
        }

        if (action instanceof CallOperationAction) {
            if (!action.getDataElement().getOwner().getName().equals(page.getDataElement().getOwner().getName())) {
                String targetClassName = getClassName((ClassType) action.getDataElement().getOwner());
                first = targetClassName + StringUtils.capitalize(first);
                return StringUtils.uncapitalize(targetClassName) + "/" + StringUtils.uncapitalize(first);
            }
            return first;
        } else if (first.equals(page.getDataElement().getName())) {
            result += StringUtils.uncapitalize(last);
        } else {
            result += first + "/" + StringUtils.uncapitalize(last);
        }

        String suffix = action.getDataElement().getName();

        return result.length() > 0 ? StringUtils.uncapitalize(result) + StringUtils.capitalize(suffix) : suffix;
    }

    public static String pageActionFilePathSuffix(Action action) {
        String actionPath = pageActionPathSuffix(action);
        String converted = actionPath;

        if (actionPath.contains("/")) {
            int lastSlashPosition = actionPath.lastIndexOf("/");
            String lead = actionPath.substring(0, lastSlashPosition);
            String trail = actionPath.substring(lastSlashPosition + 1);
            converted = lead + "/" + StringUtils.capitalize(trail);
        } else {
            converted = StringUtils.capitalize(converted);
        }

        return converted;
    }

    public static String pageActionFormPathSuffix(Action action) {
        return pageActionFilePathSuffix(action) + "Form";
    }

    public static String pageActionFormComponentName(Action action) {
        String full = pageActionFormPathSuffix(action);

        return cutAtLastSlash(full);
    }

    public static String relativePathToPageActionOutputViewComponent(Action action) {
        String pageTypePath = getPageTypePath(((CallOperationAction) action).getOutputParameterPage());
        String full = removeTrailingSlash(pageTypePath);
        String root = relativePathFromAction(((PageDefinition) action.eContainer()), action, full);

        // Both input forms and output modals are placed under `pages` therefore we can skip the first jump up
        // in the directory tree.
        return root.replaceFirst("\\.\\./", "");
    }

    public static List<Action> getActionsForPages(Application application) {
        return getPagesForRouting(application).stream().flatMap(p -> p.getActions().stream()).collect(Collectors.toList());
    }

    public static List<Action> getActionsForOutputPages(Application application) {
        return getUnmappedOutputViewsForPages(application).stream().flatMap(p -> p.getActions().stream()).collect(Collectors.toList());
    }

    public static List<Action> getActionFormsForPages(Application application) {
        List<Action> actions = getActionsForPages(application);
        return actions.stream().filter(UiActionsHelper::actionHasInputForm).collect(Collectors.toList());
    }

    public static List<KeyValue<Link, Action>> getLinksForActionFormPages(Application application) {
        List<Action> actions = getActionFormsForPages(application);
        List<KeyValue<Link, Action>> kvs = new ArrayList<>();
        actions.forEach(a -> {
            PageDefinition page = getTargetFormForAction(a);
            ((List<Link>) page.getLinks()).forEach(l -> {
                kvs.add(new KeyValue<>(l, a));
            });
        });
        return kvs;
    }

    public static List<KeyValue<Table, Action>> getTablesForActionFormPages(Application application) {
        List<Action> actions = getActionFormsForPages(application);
        List<KeyValue<Table, Action>> kvs = new ArrayList<>();
        actions.forEach(a -> {
            PageDefinition page = getTargetFormForAction(a);
            ((List<Table>) page.getTables()).forEach(l -> {
                kvs.add(new KeyValue<>(l, a));
            });
        });
        return kvs;
    }

    public static PageDefinition getTargetFormForAction(Action action) {
        PageDefinition page;
        if (action.getIsCallOperationAction()) {
            page = ((CallOperationAction) action).getInputParameterPage();
        } else if (action.getIsCreateAction()) {
            page = ((CreateAction) action).getTarget();
        } else {
            throw new RuntimeException("Unsupported page type for action: " + action.toString());
        }
        return page;
    }

    public static List<PageDefinition> getUnmappedOutputViewsForPages(Application application) {
        Set<PageDefinition> unmappedOutputForms = getActionsForPages(application)
                .stream()
                .filter(a -> a instanceof CallOperationAction && actionHasUnmappedOutputForm(a))
                .map(a -> ((CallOperationAction) a).getOutputParameterPage()).collect(Collectors.toSet());
        return unmappedOutputForms.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static String actionsPath(PageDefinition page) {
        String base = pagesFolderPath(((Application)page.eContainer()).getActor()).concat(getPageTypePath(page));
        return base.concat(base.endsWith("/") ? "" : "/").concat("actions");
    }

    public static String actionsIndexPath(PageDefinition page) {
        return actionsPath(page).concat("/index.tsx");
    }

    public static boolean actionHasInputForm(Action action) {
        if (action.getIsCreateAction()) {
            return true;
        }
        if (action.getIsCallOperationAction()) {
            PageDefinition input = ((CallOperationAction) action).getInputParameterPage();

            return input != null && !((ReferenceType) input.getDataElement()).getTarget().isIsMapped();
        }
        return false;
    }

    public static boolean actionHasUnmappedOutputForm(Action action) {
        if (action.getIsCallOperationAction()) {
            CallOperationAction callOperationAction = (CallOperationAction) action;

            if (callOperationAction.getOutputParameterPage() != null) {
                return !isPageRefreshable(callOperationAction.getOutputParameterPage());
            }
        }
        return false;
    }

    public static String relativePathFromAction(PageDefinition page, Action action, String suffix) {
        String actionPath = pagePath(page) + "actions/" + pageActionPathSuffix(action);
//        String actionPath = actionsPath(page);
        int srcSegment = actionPath.lastIndexOf("src");
        String srcPart = actionPath.substring(srcSegment);
        int tokens = srcPart.split("/").length;

        return "../".repeat(tokens - 2) + suffix;
    }

    public static boolean isActionAccess(Action action) {
        if (action instanceof PageAction) {
            PageDefinition target = ((PageAction) action).getTarget();
            return target != null && target.getRelationType() != null && target.getRelationType().isIsAccess();
        }
        return false;
    }

    public static boolean hasConfirmation(Action action) {
        return action.getIsConfirmationTypeConditional() || action.getIsConfirmationTypeMandatory();
    }

    public static boolean actionHasVisualElements(Action action) {
        // At the time of writing we only wanted to use this functionality for create actions.
        if (action instanceof CreateAction) {
            CreateAction createAction = (CreateAction) action;
            VisualElement dataContainer = getDataContainerForPage(createAction.getTarget());

            if (dataContainer != null) {
                return dataContainer instanceof Flex && ((Flex) dataContainer).getChildren().size() > 0;
            }
        }

        return false;
    }

    public static EObject getActionContainer(Action action) {
        return action.eContainer();
    }
}
