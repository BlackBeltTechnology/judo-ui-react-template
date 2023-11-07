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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiPageContainerHelper.containerComponentName;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiWidgetHelper {
    public static final String NAME_SPLITTER = "::";

    public static String getWidgetTemplate(VisualElement visualElementType) {
        String componentsLocation = "actor/src/containers/widget-fragments/";
        return componentsLocation + visualElementType.eClass().getInstanceClass().getSimpleName().toLowerCase() + ".hbs";
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

    public static Boolean shouldElementHaveAutoFocus(VisualElement element) {
        Input input = findFirstInput(element.getPageContainer());

        return input != null && element.getFQName().equals(input.getFQName());
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

    public static String componentName(VisualElement element) {
        String containerName = containerComponentName(element.getPageContainer());
        String[] splitted = element.getName().split(NAME_SPLITTER);
        return containerName + stream(splitted)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining()) + "Component";
    }

    public static String linkComponentName(Link link) {
        String containerName = containerComponentName(link.getPageContainer());
        String[] splitted = link.getName().split(NAME_SPLITTER);
        return containerName + stream(splitted)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining()) + "Component";
    }

    public static String tableComponentName(Table table) {
        String containerName = containerComponentName(table.getPageContainer());
        String[] splitted = table.getName().split(NAME_SPLITTER);
        return containerName + stream(splitted)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining()) + "Component";
    }

    public static Column getFirstAutocompleteColumnForLink(Link link) {
        Optional<Column> column = link.getParts().stream()
                .filter(c -> c.getAttributeType().getDataType() instanceof StringType && !c.getAttributeType().getIsMemberTypeTransient())
                .findFirst();
        return column.orElse(null);
    }

    public static boolean isAutocompleteAvailable(Link link) {
        if (link.getParts().size() == 0) {
            return false;
        }
        Column column = getFirstAutocompleteColumnForLink(link);

        return column != null;
    }

    public static ClassType getReferenceClassType(ReferenceTypedVisualElement ref) {
        DataElement dataElement = ref.getDataElement();
        if (dataElement instanceof OperationType) {
            OperationType operationType = (OperationType) ref.getDataElement();
            if (operationType.getOutput() != null) {
                return operationType.getOutput().getTarget();
            } else {
                return operationType.getInput().getTarget();
            }
        } else if (dataElement instanceof RelationType) {
            return ((RelationType) dataElement).getTarget();
        } else if (dataElement instanceof OperationParameterType) {
            return ((OperationParameterType) dataElement).getTarget();
        } else if (dataElement instanceof ClassType) {
            return (ClassType) dataElement;
        }

        throw new RuntimeException("Parameter's ClassType cannot be discovered: " + ref.getClass().getName());
    }

    public static void collectVisualElementsMatchingCondition(VisualElement root, Predicate<VisualElement> condition, Collection<VisualElement> matches) {
        if (root == null || condition == null) {
            return;
        }

        if (condition.test(root)) {
            matches.add(root);
        }

        if (root instanceof Container) {
            for (VisualElement child : ((Container) root).getChildren()) {
                collectVisualElementsMatchingCondition(child, condition, matches);
            }
        }

        if (root instanceof TabController) {
            for (Tab tab : ((TabController) root).getTabs()) {
                collectVisualElementsMatchingCondition(tab.getElement(), condition, matches);
            }
        }
    }

    public static List<String> getNestedDataKeysForElement(VisualElement visualElement) {
        Set<VisualElement> elements = new HashSet<>();
        collectVisualElementsMatchingCondition(visualElement, (element) -> element instanceof Input || element instanceof Table || element instanceof Link, elements);

        return elements.stream()
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .map(e -> {
                    if (e instanceof Input) {
                        return ((Input) e).getAttributeType().getName();
                    } else if (e instanceof Table) {
                        return ((Table) e).getRelationType().getName();
                    } else if (e instanceof Link) {
                        return ((Link) e).getRelationType().getName();
                    } else {
                        throw new RuntimeException("Unsupported data key type: " + e.getClass().getName());
                    }
                })
                .collect(Collectors.toList());
    }

    public static String getMenuType(NavigationItem item, Boolean isRoot) {
        if (item.getItems() != null && !item.getItems().isEmpty()) {
            if (isRoot) {
                return "group";
            }
            return "collapse";
        }
        return "item";
    }

    public static List<Button> featuredButtonsForButtonGroup(ButtonGroup buttonGroup) {
        return buttonGroup.getButtons().stream().limit(buttonGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static List<Button> nonFeaturedButtonsForButtonGroup(ButtonGroup buttonGroup) {
        return buttonGroup.getButtons().stream().skip(buttonGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static Boolean displayDropdownForButtonGroup(ButtonGroup actionGroup) {
        return nonFeaturedButtonsForButtonGroup(actionGroup).size() > 0;
    }

    public static String tableButtonVisibilityConditions(Button button, Table table, PageContainer container) {
        if (button.getActionDefinition().getIsClearAction()) {
            return "data.length";
        }
        if (button.getActionDefinition().isIsBulk()) {
            return "selectionModel.length > 0";
        }
        return "true";
    }

    public static String tableToolbarButtonDisabledConditions(Button button, Table table, PageContainer container) {
        String result = "";

        if (!container.isTable()) {
            if (button.getActionDefinition().getIsOpenFormAction() || button.getActionDefinition().getIsBulkDeleteAction()) {
                result += "editMode || ";
            } else if (button.getActionDefinition().getIsOpenSelectorAction() || button.getActionDefinition().getIsClearAction()) {
                if (container.isView()) {
                    result += "editMode || !isFormUpdateable() || ";
                }
            }
        }
        if (button.getActionDefinition().isIsBulk() && button.getHiddenBy() != null) {
            result += "!selectedRows.current.every(s => !s." + button.getHiddenBy().getName() + ") || ";
        }
        if (button.getActionDefinition().getIsBulkDeleteAction()) {
            result += "selectedRows.current.some(s => !s.__deleteable) || ";
        }

        return result + "isLoading";
    }

    public static String tableRowButtonDisabledConditions(Button button, Table table, PageContainer container) {
        String result = "";

        if (button.getActionDefinition().getIsRemoveAction()) {
            if (!container.isTable() && table.getEnabledBy() != null) {
                result += "!ownerData.{{ table.enabledBy.name }} || ";
            }
        } else if (button.getActionDefinition().getIsDeleteAction()) {
            if (!container.isTable()) {
                result += "editMode || ";

                if (table.getEnabledBy() != null) {
                    result += "!ownerData.{{ table.enabledBy.name }} || ";
                }
            }
            result += "!row.__deleteable || ";
        } else if (!container.isTable()) {
            result += "editMode || ";
        }

        if (button.getEnabledBy() != null) {
            result += "!row." + button.getEnabledBy().getName() + " || ";
        }

        return result + "isLoading";
    }
}
