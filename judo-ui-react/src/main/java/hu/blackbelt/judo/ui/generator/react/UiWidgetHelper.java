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

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.isActionDefinitionCRUDCommand;
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
        if (element.eContainer() instanceof Flex parent) {
            double parentSize = parent.getCol();
            if (parentSize != 12.0) {
                return (12.0 / parentSize) * element.getCol();
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

    public static Boolean isParentStretchVertical(VisualElement element) {
        return element.eContainer() instanceof Flex parent && (parent.getStretch().equals(Stretch.BOTH) || parent.getDirection().equals(Axis.HORIZONTAL) && alignItems(parent).equals("stretch"));
    }

    public static String alignItems(Flex flex) {
        CrossAxisAlignment alignment = flex.getCrossAxisAlignment();
        Stretch stretch = flex.getStretch();

        if (stretch.equals(Stretch.BOTH) || alignment.equals(CrossAxisAlignment.STRETCH)) {
            return "stretch";
        } else if (flex.getDirection().equals(Axis.VERTICAL) && stretch.equals(Stretch.HORIZONTAL)) {
            return "stretch";
        } else if (flex.getDirection().equals(Axis.HORIZONTAL) && stretch.equals(Stretch.VERTICAL)) {
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
        if (!element.getPageContainer().isForm()) {
            return false;
        }
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

    public static String tagComponentName(Table table) {
        return tableComponentName(table);
    }

    public static Column getFirstAutocompleteColumnForLink(Link link) {
        Optional<Column> column = link.getParts().stream()
                .filter(c -> c.getAttributeType().getDataType() instanceof StringType && !c.getAttributeType().getIsMemberTypeTransient())
                .findFirst();
        return column.orElse(null);
    }

    public static Column getFirstAutocompleteColumnForTable(Table table) {
        Optional<Column> column = table.getColumns().stream()
                .filter(c -> c.getAttributeType().getDataType() instanceof StringType && !c.getAttributeType().getIsMemberTypeTransient())
                .findFirst();
        return column.orElse(null);
    }

    public static boolean isAutocompleteAvailable(Link link) {
        if (link.getParts().isEmpty()) {
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

        if (root instanceof Container container) {
            for (VisualElement child: container.getChildren()) {
                collectVisualElementsMatchingCondition(child, condition, matches);
            }
            ButtonGroup abg = container.getActionButtonGroup();
            if (abg != null) {
                collectVisualElementsMatchingCondition(abg, condition, matches);
            }
        }

        if (root instanceof TabController tabController) {
            for (Tab tab: tabController.getTabs()) {
                collectVisualElementsMatchingCondition(tab.getElement(), condition, matches);
            }
        }

        if (root instanceof ButtonGroup buttonGroup) {
            for (Button button: buttonGroup.getButtons()) {
                collectVisualElementsMatchingCondition(button, condition, matches);
            }
        }
    }

    public static <T extends VisualElement> List<T> collectElementsOfType(VisualElement visualElement, List<T> acc, Class<T> elementType) {
        if (elementType.isInstance(visualElement)) {
            acc.add(elementType.cast(visualElement));
        }
        if (visualElement instanceof Container container) {
            for (VisualElement element : container.getChildren()) {
                collectElementsOfType(element, acc, elementType);
            }
        }
        if (visualElement instanceof TabController tabController) {
            for (Tab tab : tabController.getTabs()) {
                collectElementsOfType(tab.getElement(), acc, elementType);
            }
        }
        if (visualElement instanceof ButtonGroup buttonGroup) {
            for (Button button: buttonGroup.getButtons()) {
                collectElementsOfType(button, acc, elementType);
            }
        }
        return acc;
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
        return !nonFeaturedButtonsForButtonGroup(actionGroup).isEmpty();
    }

    public static String tableButtonVisibilityConditions(Button button, Table table, PageContainer container) {
        String result = "";

        if (table.getEnabledBy() != null) {
            if (isActionDefinitionCRUDCommand(button.getActionDefinition()) && table.getTableActionButtonGroup().getButtons().contains(button)) {
                result += "(ownerData ? ownerData." + table.getEnabledBy().getName() + " : true)";
                if (button.getActionDefinition().getIsClearAction()) {
                    result += " && data.length > 0";
                }
                if (button.getActionDefinition().isIsBulk()) {
                    result += " && selectionModel.length > 0";
                }
                if (button.getActionDefinition().getIsInlineCreateRowAction()) {
                    result += " && (rowModesModel ? Object.keys(rowModesModel).every(k => rowModesModel[k].mode !== GridRowModes.Edit) : true)";
                }
                return result;
            }
        }

        if (button.getEnabledBy() != null) {
            result += "(ownerData ? ownerData." + button.getEnabledBy().getName() + " : true) && ";
        }
        if (button.getHiddenBy() != null) {
            result += "(ownerData ? !ownerData." + button.getHiddenBy().getName() + " : true) && ";
        }
        if (button.getActionDefinition().getIsInlineCreateRowAction()) {
            result += "(rowModesModel ? Object.keys(rowModesModel).every(k => rowModesModel[k].mode !== GridRowModes.Edit) : true) && ";
        }
        if (button.getActionDefinition().getIsOpenCreateFormAction() && !table.isIsEager() && container.isView()) {
            return result += "!editMode && (isFormUpdateable ? isFormUpdateable() : false)";
        }
        if (container.isView()) {
            if (button.getActionDefinition().getIsOpenSelectorAction() || button.getActionDefinition().getIsRemoveAction()) {
                return result += "(isFormUpdateable ? (isFormUpdateable()" + (!table.isIsEager() ? "&& !editMode" : "") + ") : false)";
            }
            if (button.getActionDefinition().getIsBulkRemoveAction()) {
                return result += "(isFormUpdateable ? (isFormUpdateable()" + (!table.isIsEager() ? "&& !editMode" : "") + " && selectionModel.length > 0) : false)";
            }
        }
        if (button.getActionDefinition().getIsClearAction()) {
            result += "data.length > 0";
            if (table.getEnabledBy() != null) {
                result += " && (ownerData ? ownerData." + table.getEnabledBy().getName() +" : false)";
            }
            if (container.isView()) {
                result += " && (isFormUpdateable ? isFormUpdateable() : false)";
            }
            return result;
        }
        if (button.getActionDefinition().isIsBulk()) {
            result += "selectionModel.length > 0";
            if (container.isView() && button.getActionDefinition().getIsCallOperationAction()) {
                return result + " && !editMode";
            } else if (button.getActionDefinition().getIsBulkCallOperationAction() && button.getHiddenBy() != null) {
                if (container.isView()) {
                    result += " && !editMode";
                }
                return result + " && selectedRows.every(s => !s." + button.getHiddenBy().getName() + ")";
            }
            return result;
        }
        return result += "true";
    }

    public static String tableRowButtonHiddenConditions(Button button, Table table, PageContainer container) {
        if (button.getHiddenBy() != null) {
            return "!!row." + button.getHiddenBy().getName();
        }
        return "false";
    }

    public static String tableRowButtonDisabledConditions(Button button, Table table, PageContainer container) {
        if (table.getIsRelationType() && table.getRelationType().isIsInlineCreatable() && (button.getActionDefinition().getIsRemoveAction()) || button.getActionDefinition().getIsBulkRemoveAction()) {
            if (button.getActionDefinition().getIsRemoveAction()) {
                return "isLoading || isRowInEditMode";
            } else if (button.getActionDefinition().getIsBulkRemoveAction()) {
                return "(getSelectedRows && getSelectedRows().length > 0) || isLoading || isRowInEditMode";
            }
        }
        String result = "getSelectedRows && getSelectedRows().length > 0 ||";

        if (button.getActionDefinition().getIsRemoveAction()) {
            if (!container.isTable() && table.getEnabledBy() != null) {
                result += "(ownerData ? !ownerData." + table.getEnabledBy().getName() + " : false) || ";
            }
            if (container.isView()) {
                result += "(isFormUpdateable ? !isFormUpdateable() : false) || ";
                if (!table.isIsEager()) {
                    result += "editMode || ";
                }
            }
        } else if (button.getActionDefinition().getIsRowDeleteAction()) {
            if (!container.isTable()) {
                if (!table.isIsEager()) {
                    result += "editMode || ";
                }

                if (table.getEnabledBy() != null) {
                    result += "(ownerData ? !ownerData." + table.getEnabledBy().getName() + " : false) || ";
                }
            }
            result += "(typeof row.__deleteable === 'boolean' && !row.__deleteable) || ";

            if (container.isView()) {
                result += "(isFormUpdateable ? !isFormUpdateable() : false) || ";
            }
        } else if (!container.isTable()) {
            result += "editMode || ";
        }

        if (button.getEnabledBy() != null) {
            result += "!row." + button.getEnabledBy().getName() + " || ";
        }

        return result + "isLoading || isRowInEditMode";
    }

    public static String checkboxLabelPlacement(Checkbox checkbox) {
        if (checkbox.getValueLabelPlacement() == null || checkbox.getValueLabelPlacement().equals(Placement.DEFAULT)) {
            return null;
        }
        return checkbox.getValueLabelPlacement().getName().toLowerCase();
    }

    public static Integer calculateLinkAutocompleteRows(Link link) {
        Integer defaultValue = link.getAutoCompleteRows() != null ? link.getAutoCompleteRows() : link.getSelectorRowsPerPage();

        return defaultValue != null ? defaultValue : 10;
    }

    public static Integer calculateTableAutocompleteRows(Table table) {
        return 10;
    }

    public static boolean flexParentIsNotTab(Flex flex) {
        return !(flex.eContainer() instanceof Tab);
    }

    public static boolean elementHasIconOrLabel(VisualElement element) {
        return elementHasIcon(element) || elementHasLabel(element);
    }

    public static boolean elementHasIcon(VisualElement element) {
        return element.getIcon() != null && element.getIcon().getIconName() != null && !element.getIcon().getIconName().trim().isBlank();
    }

    public static boolean elementHasLabel(VisualElement element) {
        return element.getLabel() != null && !element.getLabel().trim().isBlank();
    }

    public static Column getSortColumnForLink(Link link) {
        if (link.getDefaultSortColumn() != null) {
            return link.getDefaultSortColumn();
        }
        return getFirstAutocompleteColumnForLink(link);
    }

    public static Column getSortColumnForTable(Table table) {
        if (table.getDefaultSortColumn() != null) {
            return table.getDefaultSortColumn();
        }
        return getFirstAutocompleteColumnForTable(table);
    }

    public static boolean isLinkAssociation(Link link) {
        return link.getRelationType().getIsRelationKindAssociation();
    }

    public static boolean displayTableHeading(Table table, PageContainer container) {
        return elementHasIconOrLabel(table) && !container.isIsSelector() && !container.isTable();
    }

    public static boolean shouldRenderConfirmationCondition(Button button) {
        return button.getConfirmation() != null && button.getConfirmation().getConfirmationCondition() != null;
    }

    public static boolean isParameterOpenerButton(Button button) {
        ActionDefinition actionDefinition = button.getActionDefinition();
        return actionDefinition.getIsOpenOperationInputFormAction() || actionDefinition.getIsOpenOperationInputSelectorAction();
    }

    public static String getCellEditType(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        if (dataType instanceof DateType || dataType instanceof TimestampType) {
            return "date";
        } else if (dataType instanceof EnumerationType) {
            return "text";
        } else if (dataType instanceof BooleanType) {
            if (!column.getAttributeType().isIsRequired()) {
                return "optionalBoolean";
            }
            return "boolean";
        }

        return "text";
    }
}
