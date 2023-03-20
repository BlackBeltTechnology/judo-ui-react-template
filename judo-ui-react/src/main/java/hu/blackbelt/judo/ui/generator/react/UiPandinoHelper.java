package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.PageDefinition;
import hu.blackbelt.judo.meta.ui.VisualElement;
import lombok.extern.java.Log;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiImportHelper.createFlattenedSetOfVisualElements;
import static hu.blackbelt.judo.ui.generator.react.UiPageHelper.pageName;

@Log
@TemplateHelper
public class UiPandinoHelper {
    public static String camelCaseNameToInterfaceKey(String name) {
        return name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static Set<VisualElement> getVisualElementsWithCustomImplementation(PageDefinition pageDefinition) {
        return createFlattenedSetOfVisualElements(pageDefinition).stream()
                .filter(VisualElement::isCustomImplementation).collect(Collectors.toSet());
    }

    public static String getCustomizationComponentInterface(VisualElement element) {
        return pageName(element.getPageDefinition()) + StringUtils.capitalize(element.getName());
    }

    public static String getCustomizationComponentInterfaceKey(VisualElement element) {
        return camelCaseNameToInterfaceKey(getCustomizationComponentInterface(element));
    }
}
