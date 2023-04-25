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
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiPageHelper.*;

@Log
@TemplateHelper
public class UiActionsHelper {

    public static String actionFunctionName(Action action, PageDefinition page) {
        String tmp = pageActionPathSuffix(action, page);

        return (tmp.contains("/") ? tmp.substring(tmp.lastIndexOf("/") + 1) : tmp) + "Action";
    }

    public static String actionFunctionTypeName(Action action, PageDefinition page) {
        return StringUtils.capitalize(actionFunctionName(action, page));
    }

    public static String actionFunctionHookName(Action action, PageDefinition page) {
        return "use".concat(actionFunctionTypeName(action, page));
    }

    public static String actionFunctionHandlerTypeName(Action action, PageDefinition page, String handlerType) {
        return StringUtils.capitalize(actionFunctionName(action, page)) + handlerType;
    }

    public static String pageActionPathSuffix(Action action, PageDefinition page) {
        String result = "";
        String[] segments = action.getName().split("::");
        String end = segments[segments.length - 1];
        String first = end.split("#")[0];
        String last = end.split("#")[1];

        if (page.getIsPageTypeTable() && action.getType().equals(ActionType.ROW)) {
            result += "row/";
        }

        if (action instanceof CallOperationAction) {
            if (!action.getDataElement().getOwner().getName().equals(page.getDataElement().getOwner().getName())) {
                String targetClassName = getClassName((ClassType) action.getDataElement().getOwner());
                first = targetClassName + StringUtils.capitalize(first);
                return StringUtils.uncapitalize(targetClassName) + "/" + StringUtils.capitalize(first);
            }
            return first;
        } else if (first.equals(page.getDataElement().getName())) {
            result += StringUtils.uncapitalize(last);
        } else {
            result += first + "/" + StringUtils.uncapitalize(last);
        }

        return result + StringUtils.capitalize(action.getDataElement().getName());
    }

    public static String pageActionFilePathSuffix(Action action, PageDefinition page) {
        String actionPath = pageActionPathSuffix(action, page);
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

    public static String pageActionFormPathSuffix(Action action, PageDefinition page) {
        return pageActionFilePathSuffix(action, page) + "Form";
    }

    public static String pageActionFormComponentName(Action action, PageDefinition page) {
        String full = pageActionFormPathSuffix(action, page);

        return cutAtLastSlash(full);
    }

    public static String relativePathToPageActionOutputViewComponent(Action action, PageDefinition page) {
        String pageTypePath = getPageTypePath(((CallOperationAction) action).getOutputParameterPage());
        String full = removeTrailingSlash(pageTypePath);
        String root = relativePathFromAction(page, action, full);

        // Both input forms and output modals are placed under `pages` therefore we can skip the first jump up
        // in the directory tree.
        return root.replaceFirst("\\.\\./", "");
    }

    public static List<KeyValue<Action, PageDefinition>> getActionsForPages(Application application) {
        List<KeyValue<Action, PageDefinition>> actions = new ArrayList<>();
        getPagesForRouting(application)
                .forEach(p -> {
                    getUniquePageActions(p).forEach(a -> {
                        KeyValue<Action, PageDefinition> kv = new KeyValue<>();
                        kv.setKey(a);
                        kv.setValue(p);
                        actions.add(kv);
                    });
                });

        return actions;
    }

    public static List<KeyValue<Action, PageDefinition>> getActionsForOutputPages(Application application) {
        List<KeyValue<Action, PageDefinition>> actions = new ArrayList<>();
        getUnmappedOutputViewsForPages(application)
                .forEach(p -> {
                    getUniquePageActions(p).forEach(a -> {
                        KeyValue<Action, PageDefinition> kv = new KeyValue<>();
                        kv.setKey(a);
                        kv.setValue(p);
                        actions.add(kv);
                    });
                });

        return actions;
    }

    public static List<KeyValue<Action, PageDefinition>> getActionFormsForPages(Application application) {
        List<KeyValue<Action, PageDefinition>> actions = getActionsForPages(application);
        return actions.stream().filter(kv -> actionHasInputForm(kv.getKey())).collect(Collectors.toList());
    }

    public static List<PageDefinition> getUnmappedOutputViewsForPages(Application application) {
        Set<PageDefinition> unmappedOutputForms = getActionsForPages(application)
                .stream()
                .filter(kv -> kv.getKey() instanceof CallOperationAction &&
                        actionHasUnmappedOutputForm(kv.getKey()))
                .map(kv -> ((CallOperationAction) kv.getKey()).getOutputParameterPage()).collect(Collectors.toSet());
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
        String actionPath = pagePath(page) + "actions/" + pageActionPathSuffix(action, page);
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
}
