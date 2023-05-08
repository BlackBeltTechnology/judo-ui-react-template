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
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiImportHelper.createFlattenedSetOfVisualElements;

@Log
@TemplateHelper
public class UiWidgetHelper extends Helper {
    public static String getWidgetTemplate(VisualElement visualElementType) {
        String componentsLocation = "actor/src/pages/widgets/";
        return componentsLocation + visualElementType.eClass().getInstanceClass().getSimpleName().toLowerCase() + ".hbs";
    }

    private static Boolean visualElementHasSaveInputAction(VisualElement visualElementType) {
        if (visualElementType instanceof Button) {
            return ((Button) visualElementType).getAction() instanceof SaveInputAction;
        }
        return false;
    }

    private static Boolean visualElementHasBackAction(VisualElement visualElementType) {
        if (visualElementType instanceof Button) {
            return ((Button) visualElementType).getAction() instanceof BackAction;
        }
        return false;
    }

    public static Boolean excludeWidgetFromTree(VisualElement visualElementType) {
        return visualElementHasSaveInputAction(visualElementType) || visualElementHasBackAction(visualElementType);
    }

    public static Button getSaveButtonForOperationInputPage(PageDefinition pageDefinition) {
        PageContainer root = pageDefinition.getContainers().get(0);
        VisualElement button = findSaveButton(root);

        return button != null ? (Button) button : null;
    }

    public static Button getBackButtonForOperationInputPage(PageDefinition pageDefinition) {
        PageContainer root = pageDefinition.getContainers().get(0);
        VisualElement button = findBackButton(root);

        return button != null ? (Button) button : null;
    }

    private static VisualElement findSaveButton(VisualElement visualElement) {
        if (visualElementHasSaveInputAction(visualElement)) {
            return visualElement;
        } else if (visualElement instanceof Container) {
            for (VisualElement child: ((Container) visualElement).getChildren()) {
                VisualElement nested = findSaveButton(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static VisualElement findBackButton(VisualElement visualElement) {
        if (visualElementHasBackAction(visualElement)) {
            return visualElement;
        } else if (visualElement instanceof Container) {
            for (VisualElement child: ((Container) visualElement).getChildren()) {
                VisualElement nested = findBackButton(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static String getFlexDirection(Flex flex) {
        return flex.getIsDirectionHorizontal() ? "row" : "column";
    }

    public static Double calculateSize(VisualElement element) {
        if (isParentFrame(element)) {
            return 12.0;
        }
        if ((isParentStretch(element) && isParentFrame(element)) || (isParentStretch(element.eContainer()) && isParentFrame(element.eContainer()))) {
            return 12.0;
        }
        if (element.eContainer() instanceof Flex) {
            if (isParentStretch(element) && ((Flex) element.eContainer()).getIsDirectionVertical()) {
                return 12.0;
            }

            double parentSize = ((Flex) element.eContainer()).getCol();
            double calculated = (12.0 / parentSize) * element.getCol();

            if (calculated <= 12.0) {
                return calculated;
            }
        }

        return element.getCol();
    }

    public static Boolean isParentDirectionColumn(VisualElement element) {
        if (element.eContainer() instanceof Flex) {
            return ((Flex) element.eContainer()).getIsDirectionVertical();
        }
        return false;
    }

    private static Boolean isParentFrame(EObject element) {
        if (element.eContainer() instanceof Flex) {
            return ((Flex) element.eContainer()).getFrame() != null;
        }
        return false;
    }

    private static Boolean isParentStretch(EObject element) {
        if (element.eContainer() instanceof VisualElement) {
            VisualElement parent = (VisualElement) element.eContainer();
            return parent.getStretch().equals(Stretch.HORIZONTAL) || parent.getStretch().equals(Stretch.BOTH);
        }
        return false;
    }

    public static Boolean spacingForFlex(Flex flex) {
        if (flex.getChildren().stream().anyMatch(c -> c instanceof Flex && ((Flex) c).getFrame() != null)) {
            return true;
        }
        return isParentFrame(flex);
    }

    public static String alignItems(Flex flex) {
        CrossAxisAlignment alignment = flex.getCrossAxisAlignment();
        Stretch stretch = flex.getStretch();

        if (stretch.equals(Stretch.BOTH) || stretch.equals(Stretch.HORIZONTAL)) {
            return "stretch";
        }

        switch (alignment) {
            case CENTER:
                return "center";
            case END:
                return "flex-end";
            case BASELINE:
                return "baseline";
            case STRETCH:
                return "stretch";
            default:
                return "flex-start";
        }
    }

    public static String justifyContent(Flex flex) {
        MainAxisAlignment alignment = flex.getMainAxisAlignment();

        switch (alignment) {
            case END:
                return "flex-end";
            case CENTER:
                return "center";
            case SPACEEVENLY:
                return "space-evenly";
            case SPACEAROUND:
                return "space-around";
            case SPACEBETWEEN:
                return "space-between";
            default:
                return "flex-start";
        }
    }

    public static List<Button> featuredActionsForActionGroup(ActionGroup actionGroup) {
        return actionGroup.getActions().stream().limit(actionGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static List<Button> nonFeaturedActionsForActionGroup(ActionGroup actionGroup) {
        return actionGroup.getActions().stream().skip(actionGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static Boolean displayDropdownForActionGroup(ActionGroup actionGroup) {
        return nonFeaturedActionsForActionGroup(actionGroup).size() > 0;
    }

    public static Boolean shouldElementHaveAutoFocus(VisualElement element) {
        PageDefinition pageDefinition = element.getPageDefinition();

        if (pageDefinition.getIsPageTypeOperationInput() || pageDefinition.getIsPageTypeCreate()) {
            Input input = findFirstInput(pageDefinition.getContainers().get(0));

            return input != null && element.getFQName().equals(input.getFQName());
        }

        return false;
    }

    private static Input findFirstInput(VisualElement root) {
        if (root == null) {
            return null;
        }

        if (root instanceof Input) {
            Input input = (Input) root;
            if (!input.getAttributeType().isIsReadOnly()) {
                return input;
            }
        }

        if (root instanceof Container) {
            for (VisualElement child: ((Container) root).getChildren()) {
                Input result = findFirstInput(child);

                if (result != null) {
                    return result;
                }
            }
        }

        if (root instanceof TabController) {
            for (Tab tab: ((TabController) root).getTabs()) {
                Input result = findFirstInput(tab.getElement());

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public static PageDefinition getFormVersionOfViewPage(PageDefinition pageDefinition) {
        if (pageDefinition.getIsPageTypeView() || pageDefinition.getIsPageTypeOperationOutput()) {
            Optional<EditAction> editAction = pageDefinition.getPageActions().stream()
                    .filter(a -> a instanceof EditAction)
                    .map(a -> (EditAction) a)
                    .findFirst();

            if (editAction.isPresent()) {
                return editAction.get().getTarget();
            }
        }

        return null;
    }

    public static String variantForButton(VisualElement element) {
        if (element instanceof Button) {
            String style = ((Button) element).getButtonStyle();
            if (style == null) {
                return "undefined";
            }
            // Currently we only support Material Design variants.
            switch (style) {
                case "contained":
                    return "'contained'";
                case "text":
                    return "'text'";
                case "outlined":
                    return "'outlined'";
                default:
                    return "undefined";
            }
        }
        return "undefined";
    }

    public static List<Action> getFilteredLinkActions (Link link) {
        return link.getActions().stream()
                .filter(a -> !a.getIsEditAction() && !a.getIsSetAction() && !a.getIsRemoveAction() && !a.getIsUnsetAction())
                .collect(Collectors.toList());
    }

    public static boolean linkHasActionsToImport(Link link) {
        return  getFilteredLinkActions(link).size() > 0;
    }

    public static List<Action> getFilteredTableActions (Table table) {
        SortedSet<Action> actions = new TreeSet<>(Comparator.comparing((Action a) -> a.getFQName().trim()));
        actions.addAll(table.getRowActions());
        actions.addAll(table.getPageDefinition().getActions());
        return actions.stream()
                .filter(a -> !a.getIsBackAction() && !a.getIsEditAction())
                .collect(Collectors.toList());
    }

    public static boolean tableHasActionsToImport(Table table) {
        return  getFilteredTableActions(table).size() > 0;
    }

    public static String linkComponentName(Link link) {
        return StringUtils.capitalize(link.getName()) + "Link";
    }

    public static String tableComponentName(Table table) {
        return StringUtils.capitalize(table.getName()) + "Table";
    }
}
