package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.EnumerationType;
import hu.blackbelt.judo.meta.ui.data.OperationType;
import hu.blackbelt.judo.ui.generator.react.mask.MaskEntry;
import hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper;
import lombok.extern.java.Log;

import org.eclipse.emf.ecore.EObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.*;
import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.safeName;
import static hu.blackbelt.judo.ui.generator.react.UiPandinoHelper.getVisualElementsWithCustomImplementation;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.*;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.*;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiPageContainerHelper {
    public static final String NAME_SPLITTER = "::";

    public static String containerPath(PageContainer container) {
        return String.join("/", stream(container.getName().split(NAME_SPLITTER)).map(UiCommonsHelper::firstToUpper).toList());
    }

    public static String containerComponentName(PageContainer container) {
        return stream(container.getName().split(NAME_SPLITTER))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(""));
    }

    public static String containerName(PageContainer container) {
        return String.join("", stream(container.getName().split(NAME_SPLITTER)).map(UiCommonsHelper::firstToUpper).toList());
    }

    public static boolean containerIsRefreshable(PageContainer container) {
        return container.getPageActionDefinitions().stream().anyMatch(a -> ((ActionDefinition) a).getIsRefreshAction());
    }

    public static List<Link> getLinksForPageContainers(Application application) {
        return application.getPageContainers().stream().flatMap(c -> ((List<Link>) c.getLinks()).stream())
                .collect(Collectors.toList());
    }

    public static List<Table> getTablesForPageContainers(Application application) {
        return application.getPageContainers().stream().flatMap(c -> ((List<Table>) c.getTables()).stream())
                .collect(Collectors.toList());
    }

    public static String pageContainerActionDefinitionsName(PageContainer pageContainer) {
        return containerComponentName(pageContainer) + "ActionDefinitions";
    }

    public static String pageContainerActionDefinitionTypeName(PageContainer pageContainer) {
        return StringUtils.capitalize(pageContainerActionDefinitionsName(pageContainer));
    }

    public static String simpleActionDefinitionName(ActionDefinition actionDefinition) {
        String relationName = "";

        if (!getPageContainerForActionDefinition(actionDefinition).isTable()) {
            Link link = getLinkParentForActionDefinition(actionDefinition);
            Table table = getTableParentForActionDefinition(actionDefinition);
            EObject container = actionDefinition.eContainer();

            if (link != null && link.getRelationName() != null) {
                relationName += link.getRelationName();
            } else if (table != null && table.getRelationName() != null) {
                relationName += table.getRelationName();
            } else if (container instanceof Button button && button.getRelationName() != null) {
                // single association button case...
                relationName += button.getRelationName();
            }
        }

        String definitionName = actionDefinition.eClass().getInstanceClass().getSimpleName();
        String suffix = firstToLower(definitionName.substring(0, definitionName.indexOf("ActionDefinition")));

        if (actionDefinition instanceof CallOperationActionDefinition callOperationActionDefinition) {
            // We need to append a discriminator for actions because for unmapped operation input forms, caller
            // CallOperation actions are rolled on the form container, which could lead to method name collisions.
            suffix = firstToLower(getCallOperationName(callOperationActionDefinition)) + "For" + firstToUpper(callOperationActionDefinition.getOperation().getOwnerSimpleName());
        } else if (actionDefinition instanceof BulkCallOperationActionDefinition bulkCallOperationActionDefinition) {
            suffix = "bulk" + firstToUpper(getCallOperationName(bulkCallOperationActionDefinition));
        } else if (actionDefinition instanceof OpenOperationInputSelectorActionDefinition openOperationInputSelectorActionDefinition) {
            if (openOperationInputSelectorActionDefinition.getSelectorFor() != null) {
                if (openOperationInputSelectorActionDefinition.getSelectorFor() instanceof CallOperationActionDefinition callOperationActionDefinition) {
                    suffix = firstToLower(getCallOperationName(callOperationActionDefinition));
                }
            }
        } else if (actionDefinition instanceof OpenFormActionDefinition openFormActionDefinition) {
            if (openFormActionDefinition.getFormFor() != null) {
                if (openFormActionDefinition.getFormFor() instanceof CallOperationActionDefinition callOperationActionDefinition) {
                    suffix = firstToLower(getCallOperationName(callOperationActionDefinition));
                }
            }
        } else if (actionDefinition instanceof CustomActionDefinition) {
            Button button = (Button) actionDefinition.eContainer();
            suffix = button.getName();
        }

        return relationName + (!relationName.isEmpty() ? firstToUpper(suffix) : suffix) + "Action";
    }

    private static String getCallOperationName(ActionDefinition actionDefinition) {
        OperationType operationType = null;
        int index = 2;
        if (actionDefinition instanceof CallOperationActionDefinition callOperationActionDefinition) {
            operationType = callOperationActionDefinition.getOperation();
        } else if (actionDefinition instanceof BulkCallOperationActionDefinition bulkCallOperationActionDefinition) {
            operationType = bulkCallOperationActionDefinition.getOperation();
        }
        String actionDefinitionName = actionDefinition.getName();
        String fallbackName = operationType.getName();
        String[] nameParts = actionDefinitionName.split(NAME_SPLITTER);
        if (nameParts.length >= index + 1) {
            return nameParts[nameParts.length - index];
        } else {
            return fallbackName;
        }
    }
    public static List<ClassType> getContainerApiImports(PageContainer container) {
        Set<ClassType> imports = new HashSet<>();

        try {
            imports.add((ClassType) container.getDataElement());
            imports.addAll(container.getTables().stream().map(t -> getReferenceClassType(((Table) t))).toList());

            imports.addAll(container.getLinks().stream().map(l -> getReferenceClassType(((Link) l))).toList());

            for (ActionDefinition actionDefinition : (List<ActionDefinition>) container.getAllActionDefinitions()) {
                if (actionDefinition.getTargetType() != null) {
                    imports.add(actionDefinition.getTargetType());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return imports.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static boolean containerHasRelationComponents(PageContainer container) {
        return !container.getTables().isEmpty() || !container.getLinks().isEmpty();
    }

    public static List<String> getContainerActionsExtends(PageContainer container) {
        Set<String> imports = new HashSet<>();

        imports.addAll(container.getTables().stream().map(t -> componentName(((Table) t)) + "ActionDefinitions").toList());
        imports.addAll(container.getLinks().stream().map(l -> componentName(((Link) l)) + "ActionDefinitions").toList());

        return imports.stream().sorted().collect(Collectors.toList());
    }

    public static boolean containerIsEmptyDashboard(PageContainer container) {
        return getXMIID(container).endsWith("EmptyDashboardPageContainer");
    }

    public static String containerButtonAvailable(Button button) {
        List<String> tokens = new ArrayList<>();
        if (button.getHiddenBy() != null) {
            tokens.add("(actions?.is" + safeName(button) + "Hidden ? !actions?.is" + safeName(button) + "Hidden(data, editMode) : !data." + button.getHiddenBy().getName() + ")");
        } else if (button.getPageContainer().isForm()) {
            tokens.add("editMode");
        } else if (button.getActionDefinition().getIsCancelAction() || button.getActionDefinition().getIsUpdateAction()) {
            tokens.add("editMode");
        } else if (button.getActionDefinition().getIsRefreshAction()) {
            // In case of dialogs that are open in draft mode, we should consider checking actions
            // because it could be possible that there are no valid use cases where these actions
            // should be generated in the first place.
            tokens.add("!isDraft");
        } else if (button.getActionDefinition().getIsDeleteAction()) {
            tokens.add("!editMode && (isFormDeleteable() || isDraft)");
        } else {
            tokens.add("!editMode");
        }
        return String.join(" && ", tokens);
    }

    public static MaskEntry getMaskForTable(Table table, PageDefinition pageDefinition, Integer counter) {
        MaskEntry mask = new MaskEntry(Set.of(), null);
        Set<String> columnAttributeNames = table.getColumns().stream()
                .map(c -> c.getAttributeType().getName())
                .collect(Collectors.toSet());
        columnAttributeNames.addAll(table.getAdditionalMaskAttributes().stream().map(NamedElement::getName).collect(Collectors.toSet()));
        mask.addPrimitives(columnAttributeNames);
        if (table.isIsEager() && counter < 5) {
            // table items can be potentially opened, therefore we need the target's attributes as well
            Button openPageButton = table.getRowActionButtonGroup().getButtons().stream()
                    .filter(b -> b.getActionDefinition().getIsOpenPageAction())
                    .findFirst().orElse(null);
            if (openPageButton != null) {
                OpenPageActionDefinition def = (OpenPageActionDefinition) openPageButton.getActionDefinition();
                Action openAction = pageDefinition.getActions().stream().filter(a -> a.getActionDefinition().equals(def)).findFirst().orElse(null);
                if (openAction != null && openAction.getTargetPageDefinition() != null) {
                    MaskEntry viewMask = getMaskForView(openAction.getTargetPageDefinition(), counter + 1);
                    mask.addPrimitives(viewMask.getPrimitives());
                    mask.addRelations(viewMask.getRelations());
                }
            }
        }

        return mask;
    }

    public static String serializeMaskForTable(Table table, PageDefinition pageDefinition) {
        return "{" + getMaskForTable(table, pageDefinition, 0).serialize() + "}";
    }

    public static MaskEntry getMaskForLink(Link link, PageDefinition pageDefinition, Integer counter) {
        MaskEntry mask = new MaskEntry(Set.of(), null);
        Set<String> columnAttributeNames = ((List<Column>) link.getColumns()).stream().map(c -> c.getAttributeType().getName()).collect(Collectors.toSet());
        columnAttributeNames.addAll(link.getAdditionalMaskAttributes().stream().map(NamedElement::getName).collect(Collectors.toSet()));
        mask.addPrimitives(columnAttributeNames);

        if (link.isIsEager() && counter < 5) {
            // link items can be potentially opened, therefore we need the target's attributes as well
            Button openPageButton = link.getActionButtonGroup().getButtons().stream()
                    .filter(b -> b.getActionDefinition().getIsOpenPageAction())
                    .findFirst().orElse(null);
            if (openPageButton != null) {
                OpenPageActionDefinition def = (OpenPageActionDefinition) openPageButton.getActionDefinition();
                Action openAction = pageDefinition.getActions().stream().filter(a -> a.getActionDefinition().equals(def)).findFirst().orElse(null);
                if (openAction != null && openAction.getTargetPageDefinition() != null) {
                    MaskEntry viewMask = getMaskForView(openAction.getTargetPageDefinition(), counter + 1);
                    mask.addPrimitives(viewMask.getPrimitives());
                    mask.addRelations(viewMask.getRelations());
                }
            }
        }

        return mask;
    }

    public static String serializeMaskForLink(Link link, PageDefinition pageDefinition) {
        return "{" + getMaskForLink(link, pageDefinition, 0).serialize() + "}";
    }

    public static MaskEntry getMaskForView(PageDefinition pageDefinition, Integer counter) {
        MaskEntry mask = new MaskEntry(Set.of(), null);
        PageContainer container = pageDefinition.getContainer();
        Set<String> tokens = new LinkedHashSet<>();

        Set<VisualElement> inputs = new HashSet<>();
        collectVisualElementsMatchingCondition(container, (VisualElement element) -> element instanceof AttributeBased, inputs);

        Set<String> attributeNames = inputs.stream().map(i -> ((AttributeBased) i)).map(i -> i.getAttributeType().getName()).collect(Collectors.toSet());
        attributeNames.addAll(container.getAdditionalMaskAttributes().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Set<VisualElement> AllVisualElements = new HashSet<>();
        collectVisualElementsMatchingCondition(container, (VisualElement element) -> true, AllVisualElements);

        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getHiddenBy() != null).map(i -> i.getHiddenBy().getName()).collect(Collectors.toSet()));
        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getEnabledBy() != null).map(i -> i.getEnabledBy().getName()).collect(Collectors.toSet()));
        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getRequiredBy() != null).map(i -> i.getRequiredBy().getName()).collect(Collectors.toSet()));

        if (container.getTitleFrom() != null && container.getTitleFrom().equals(TitleFrom.ATTRIBUTE)) {
            attributeNames.add(container.getTitleAttribute().getName());
        }

        mask.addPrimitives(attributeNames);

        for (Table table: ((List<Table>) container.getTables()).stream().filter(t -> t.getRelationType().getIsRelationKindComposition() || t.getRelationType().getIsRelationKindAggregation()).toList()) {
            MaskEntry tableMask = getMaskForTable(table, pageDefinition, counter + 1);
            tableMask.setRelationName(table.getDataElement().getName());
            mask.addRelations(tableMask);
        }

        for (Link link: ((List<Link>) container.getLinks()).stream().filter(t -> t.getRelationType().getIsRelationKindComposition() || t.getRelationType().getIsRelationKindAggregation()).toList()) {
            MaskEntry linkMask = getMaskForLink(link, pageDefinition, counter + 1);
            linkMask.setRelationName(link.getDataElement().getName());
            mask.addRelations(linkMask);
        }

        return mask;
    }

    public static String serializeMaskForView(PageDefinition pageDefinition) {
        try {
            return "{" + getMaskForView(pageDefinition, 0).serialize() + "}";
        } catch (StackOverflowError e) {
            // keeping this for debugging purposes
            throw e;
        }
    }

    public static boolean containerHasDateInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), DateInput.class).isEmpty();
    }

    public static boolean containerHasTimeInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), TimeInput.class).isEmpty();
    }

    public static boolean containerHasDateTimeInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), DateTimeInput.class).isEmpty();
    }

    public static boolean containerHasTable(PageContainer container) {
        return !container.getTables().isEmpty();
    }

    public static Table getFirstTableForContainer(PageContainer container) {
        return (Table) container.getTables().stream().findFirst().orElse(null);
    }

    public static boolean containerHasNumericInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), NumericInput.class).isEmpty();
    }

    public static boolean containerHasTrinaryLogicCombo(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), TrinaryLogicCombo.class).isEmpty();
    }

    public static boolean containerHasTypeAhead(PageContainer container) {
        var elements = collectElementsOfType(container, new ArrayList<>(), TextInput.class);
        return elements.stream().anyMatch(TextInput::isIsTypeAheadField);
    }

    public static boolean containerHasDivider(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), Divider.class).isEmpty();
    }

    public static boolean containerHasSpacer(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), Spacer.class).isEmpty();
    }

    public static boolean containerHasBinaryInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), BinaryTypeInput.class).isEmpty();
    }

    public static boolean containerHasTabs(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), TabController.class).isEmpty();
    }

    public static boolean containerHasCustomComponent(PageContainer container) {
        List<VisualElement> acc = collectElementsOfType(container, new ArrayList<>(), VisualElement.class);
        return acc.stream().anyMatch(VisualElement::isCustomImplementation);
    }

    public static boolean containerHasAssociationButton(PageContainer container) {
        List<Button> acc = collectElementsOfType(container, new ArrayList<>(), Button.class);
        return acc.stream().anyMatch(b -> b.getActionDefinition().getIsOpenPageAction());
    }

    public static boolean containerHasCards(PageContainer container) {
        List<Flex> acc = collectElementsOfType(container, new ArrayList<>(), Flex.class);
        return acc.stream().anyMatch(Flex::isCard);
    }

    public static boolean containerHasEnums(PageContainer container) {
        return !getEnumsForContainer(container).isEmpty();
    }

    public static List<Input> getInputsForContainer(PageContainer container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e instanceof Input, elements);
        return elements.stream().map(e -> ((Input) e)).sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static List<Input> getEnumsForContainer(PageContainer container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e instanceof EnumerationCombo || e instanceof EnumerationRadio, elements);
        return elements.stream()
                .map(e -> ((Input) e))
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static List<Input> getTextInputsWithTypeAhead(PageContainer container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e instanceof TextInput, elements);
        return elements.stream()
                .filter(e -> ((TextInput) e).isIsTypeAheadField())
                .map(e -> ((Input) e))
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static List<EnumerationType> getEnumDataTypesForContainer(PageContainer container) {
        return getEnumsForContainer(container).stream()
                .map(e -> (EnumerationType) e.getAttributeType().getDataType())
                .collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static List<Link> getLinksForContainer(PageContainer container) {
        return container.getLinks().stream().map(e -> ((Link) e)).sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static boolean containerHasCreateAction(PageContainer container) {
        return container.getActionButtonGroup() != null && container.getActionButtonGroup().getButtons().stream()
                .anyMatch(b -> b.getActionDefinition() instanceof CreateActionDefinition);
    }

    public static ActionDefinition getCreateActionDefinitionForCreateContainer(PageContainer container) {
        if (container.getActionButtonGroup() != null) {
            container.getActionButtonGroup().getButtons().stream()
                    .map(Button::getActionDefinition)
                    .filter(actionDefinition -> actionDefinition instanceof CreateActionDefinition)
                    .findFirst().orElse(null);
        }
        return null;
    }

    public static boolean containerHasTableWithTotalCount(PageContainer container) {
        return container.getTables().stream()
                .filter(e -> e instanceof Table)
                .anyMatch(t -> ((Table) t).isShowTotalCount());
    }

    public static boolean containerButtonHasDisabledConditions(Button button, PageContainer container) {
        return !containerButtonGroupButtonDisabledConditions(button, container).isEmpty();
    }

    public static String containerButtonGroupButtonDisabledConditions(Button button, PageContainer container) {
        Set<String> segments = new HashSet<>();
        if (button.getEnabledBy() != null) {
            segments.add("!data." + button.getEnabledBy().getName());
        }
        if (container.isView()) {
            if (!button.getActionDefinition().getIsCancelAction() && !button.getActionDefinition().getIsUpdateAction()) {
                segments.add("editMode");
            }
        }
        if (button.getActionDefinition().getIsSetAction() || button.getActionDefinition().getIsAddAction()) {
            return "!selectionDiff.length";
        }
        segments.add("isLoading");

        if (container.isIsSelector() && button.getActionDefinition() instanceof CallOperationActionDefinition callOperationActionDefinition) {
            if (!callOperationActionDefinition.getOperation().getInput().isIsOptional()) {
                segments.add("!selectionDiff.length");
            }
        }

        return String.join(" || ", segments);
    }

    public static boolean cardHasHeaderContent(Flex flex) {
        return (elementHasIconOrLabel(flex) || flex.getActionButtonGroup() != null) && !(flex.eContainer() instanceof Tab);
    }

    public static boolean containerHasDateOrDateTimeInput(PageContainer container) {
        return !getDateOrDateTimeInputs(container).isEmpty();
    }

    public static List<VisualElement> getDateOrDateTimeInputs(PageContainer container) {
        List<Input> inputs = new ArrayList<>();
        inputs.addAll(collectElementsOfType(container, new ArrayList<>(), DateInput.class));
        inputs.addAll(collectElementsOfType(container, new ArrayList<>(), DateTimeInput.class));
        return inputs.stream()
                .filter(i -> !i.isIsReadOnly())
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static Set<PageContainer> getPageContainersWithCustomImplementations(Application app) {
        Set<PageContainer> containers = new HashSet<>();
        for (PageContainer container : app.getPageContainers()) {
            Set<VisualElement> elements = getVisualElementsWithCustomImplementation(container);
            if (!elements.isEmpty()) {
                containers.add(container);
            }
        }
        return containers;
    }

    public static String getProxyPropsForCustomImplementation(VisualElement element) {
        PageContainer container = element.getPageContainer();
        String actionType = pageContainerActionDefinitionTypeName(container);
        String targetType = container.isTable() ? "any" : classDataName((ClassType) container.getDataElement(), "Stored");
        if (element instanceof Table) {
            return "TableProxyProps<" + targetType + ", " + actionType + ">";
        } else if (element instanceof Link) {
            return "LinkProxyProps<" + targetType + ", " + actionType + ">";
        }
        return "GenericProxyProps<" + targetType + ", " + actionType + ">";
    }

    public static String getCustomImplementationProps(VisualElement element) {
        if (element instanceof Table) {
            return "ownerData, isOwnerLoading, actions, editMode, storeDiff, isFormUpdateable";
        } else if (element instanceof Link) {
            return "ownerData, data, disabled, readOnly, editMode, validation, actions, isLoading, isDraft";
        }
        return "data, validation, editMode, storeDiff, isLoading, actions";
    }

    public static List<PageDefinition> getOperationFormCallerPages(PageContainer container, Application application) {
        List<PageDefinition> pages = new ArrayList<>();
        if (!container.isForm()) {
            return pages;
        }
        boolean hasOp = getContainerOwnActionDefinitions(container).stream().anyMatch(ActionDefinition::getIsInputFormCallOperationAction);

        if (hasOp) {
            pages = application.getPages().stream().filter(p -> p.getContainer().equals(container)).toList();
        }
        return pages;
    }

    public static boolean hasOperationFormCallerPages(PageContainer container, Application application) {
        return !getOperationFormCallerPages(container, application).isEmpty();
    }

    public static PageDefinition getCallerPageForOperationFormCallButton(Button button, Application application) {
        PageContainer container = button.getPageContainer();
        List<PageDefinition> pages = getOperationFormCallerPages(container, application);
        return pages.stream()
                .filter(p -> p.getActions().stream().anyMatch(a -> a.getActionDefinition().equals(button.getActionDefinition())))
                .findFirst().orElse(null);
    }

    public static boolean isVisualElementContainerButton(VisualElement visualElement) {
        if (visualElement instanceof Button button) {
            if (button.eContainer() instanceof ButtonGroup buttonGroup) {
                return buttonGroup.eContainer() instanceof PageContainer;
            }
        }
        return false;
    }
}
