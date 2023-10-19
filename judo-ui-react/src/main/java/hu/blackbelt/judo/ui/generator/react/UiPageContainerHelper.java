package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import lombok.extern.java.Log;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.*;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.getXMIID;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiPageContainerHelper extends Helper {
    public static final String NAME_SPLITTER = "::";

    public static String containerPath(PageContainer container) {
        return String.join("/", container.getName().split(NAME_SPLITTER));
    }

    public static String containerComponentName(PageContainer container) {
        return stream(container.getName().split(NAME_SPLITTER))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(""));
    }

    public static boolean containerIsRefreshable(PageContainer container) {
        return container.getAllActionDefinitions().stream().anyMatch(a -> ((ActionDefinition) a).getIsRefreshAction());
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
        String[] tokens = actionDefinition.getName().split(NAME_SPLITTER);
        return StringUtils.uncapitalize(stream(tokens).map(StringUtils::capitalize).collect(Collectors.joining()));
    }

    public static String simpleActionDefinitionTypeName(ActionDefinition actionDefinition) {
        return StringUtils.capitalize(simpleActionDefinitionName(actionDefinition));
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

        imports.addAll(container.getTables().stream().map(t -> tableComponentName(((Table) t)) + "ActionDefinitions").toList());
        imports.addAll(container.getLinks().stream().map(l -> linkComponentName(((Link) l)) + "ActionDefinitions").toList());

        return imports.stream().sorted().collect(Collectors.toList());
    }

    public static boolean containerIsEmptyDashboard(PageContainer container) {
        return getXMIID(container).endsWith("EmptyDashboardPageContainer");
    }

    public static String containerButtonAvailable(Button button) {
        if (button.getPageContainer().isForm()) {
            return "editMode";
        } else if (button.getActionDefinition().getIsUpdateAction()) {
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

        mask.append(String.join(",", attributeNames));

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
}
