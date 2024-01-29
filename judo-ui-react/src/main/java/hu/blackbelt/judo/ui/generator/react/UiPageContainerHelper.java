package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper;
import lombok.extern.java.Log;

import org.eclipse.emf.ecore.EObject;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiActionsHelper.*;
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
            suffix = firstToLower(callOperationActionDefinition.getOperation().getName()) + "For" + firstToUpper(callOperationActionDefinition.getOperation().getOwnerSimpleName());
        } else if (actionDefinition instanceof BulkCallOperationActionDefinition bulkCallOperationActionDefinition) {
            suffix = "bulk" + firstToUpper(bulkCallOperationActionDefinition.getBulkOf().getOperation().getName());
        } else if (actionDefinition instanceof OpenOperationInputSelectorActionDefinition openOperationInputSelectorActionDefinition) {
            if (openOperationInputSelectorActionDefinition.getSelectorFor() != null) {
                if (openOperationInputSelectorActionDefinition.getSelectorFor() instanceof CallOperationActionDefinition callOperationActionDefinition) {
                    suffix = firstToLower(callOperationActionDefinition.getOperation().getName());
                }
            }
        } else if (actionDefinition instanceof OpenFormActionDefinition openFormActionDefinition) {
            if (openFormActionDefinition.getFormFor() != null) {
                if (openFormActionDefinition.getFormFor() instanceof CallOperationActionDefinition callOperationActionDefinition) {
                    suffix = firstToLower(callOperationActionDefinition.getOperation().getName());
                }
            }
        } else if (actionDefinition instanceof CustomActionDefinition) {
            Button button = (Button) actionDefinition.eContainer();
            suffix = button.getName();
        }

        return relationName + (!relationName.isEmpty() ? firstToUpper(suffix) : suffix) + "Action";
    }

    public static List<String> getContainerApiImports(PageContainer container) {
        Set<String> imports = new HashSet<>();

        try {
            imports.add(classDataName((ClassType) container.getDataElement(), ""));
            imports.add(classDataName((ClassType) container.getDataElement(), "Stored"));
            imports.add(classDataName((ClassType) container.getDataElement(), "QueryCustomizer"));
            imports.addAll(container.getTables().stream().map(t -> classDataName(getReferenceClassType(((Table) t)), "")).toList());
            imports.addAll(container.getTables().stream().map(t -> classDataName(getReferenceClassType(((Table) t)), "Stored")).toList());
            imports.addAll(container.getTables().stream().map(t -> classDataName(getReferenceClassType(((Table) t)), "QueryCustomizer")).toList());

            imports.addAll(container.getLinks().stream().map(l -> classDataName(getReferenceClassType(((Link) l)), "")).toList());
            imports.addAll(container.getLinks().stream().map(l -> classDataName(getReferenceClassType(((Link) l)), "Stored")).toList());
            imports.addAll(container.getLinks().stream().map(l -> classDataName(getReferenceClassType(((Link) l)), "QueryCustomizer")).toList());

            for (ActionDefinition actionDefinition : (List<ActionDefinition>) container.getAllActionDefinitions()) {
                if (actionDefinition.getTargetType() != null) {
                    imports.add(classDataName(actionDefinition.getTargetType(), ""));
                    imports.add(classDataName(actionDefinition.getTargetType(), "Stored"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return imports.stream().sorted().collect(Collectors.toList());
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
        if (button.getPageContainer().isForm()) {
            return "editMode";
        } else if (button.getActionDefinition().getIsCancelAction() || button.getActionDefinition().getIsUpdateAction()) {
            return "editMode";
        }
        return "!editMode";
    }

    public static String getMaskForTable(Table table) {
        String tableColumns = table.getColumns().stream().map(c -> c.getAttributeType().getName()).collect(Collectors.joining(","));

        return "{" + tableColumns + "}";
    }

    public static String getMaskForLink(Link link) {
        String linkColumns = ((List<Column>) link.getColumns()).stream().map(c -> c.getAttributeType().getName()).collect(Collectors.joining(","));

        return "{" + linkColumns + "}";
    }

    public static String getMaskForView(PageContainer container) {
        StringBuilder mask = new StringBuilder();

        Set<VisualElement> inputs = new HashSet<>();
        collectVisualElementsMatchingCondition(container, (VisualElement element) -> element instanceof Input, inputs);

        Set<String> attributeNames = inputs.stream().map(i -> ((Input) i)).map(i -> i.getAttributeType().getName()).collect(Collectors.toSet());

        Set<VisualElement> AllVisualElements = new HashSet<>();
        collectVisualElementsMatchingCondition(container, (VisualElement element) -> true, AllVisualElements);

        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getHiddenBy() != null).map(i -> i.getHiddenBy().getName()).collect(Collectors.toSet()));
        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getEnabledBy() != null).map(i -> i.getEnabledBy().getName()).collect(Collectors.toSet()));
        attributeNames.addAll(AllVisualElements.stream().filter(e -> e.getRequiredBy() != null).map(i -> i.getRequiredBy().getName()).collect(Collectors.toSet()));

        if (container.getTitleFrom() != null && container.getTitleFrom().equals(TitleFrom.ATTRIBUTE)) {
            attributeNames.add(container.getTitleAttribute().getName());
        }

        mask.append(String.join(",", attributeNames.stream().sorted().toList()));

        for (Table table: ((List<Table>) container.getTables()).stream().filter(t -> t.getRelationType().getIsRelationKindComposition() || t.getRelationType().getIsRelationKindAggregation()).toList()) {
            if (!mask.toString().endsWith(",")) {
                mask.append(",");
            }
            mask.append(table.getDataElement().getName());
            mask.append(getMaskForTable(table));
        }

        for (Link link: ((List<Link>) container.getLinks()).stream().filter(t -> t.getRelationType().getIsRelationKindComposition() || t.getRelationType().getIsRelationKindAggregation()).toList()) {
            if (!mask.toString().endsWith(",")) {
                mask.append(",");
            }
            mask.append(link.getDataElement().getName());
            mask.append(getMaskForLink(link));
        }

        return "{" + mask + "}";
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

    public static boolean containerHasNumericInput(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), NumericInput.class).isEmpty();
    }

    public static boolean containerHasTrinaryLogicCombo(PageContainer container) {
        return !collectElementsOfType(container, new ArrayList<>(), TrinaryLogicCombo.class).isEmpty();
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

    public static List<Input> getInputsForContainer(PageContainer container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e instanceof Input, elements);
        return elements.stream().map(e -> ((Input) e)).sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
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
}
