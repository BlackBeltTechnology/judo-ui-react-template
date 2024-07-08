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
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiPageContainerHelper.containerComponentName;
import static hu.blackbelt.judo.ui.generator.react.UiPageContainerHelper.simpleActionDefinitionName;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.collectVisualElementsMatchingCondition;
import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.componentName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.firstToLower;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.firstToUpper;

@Log
@TemplateHelper
public class UiPandinoHelper {
    public static String camelCaseNameToInterfaceKey(String name) {
        return name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static String getCustomizationComponentInterface(VisualElement element) {
        return /*pageName(element.getPageDefinition()) + */componentName(element);
    }

    public static String getCustomizationComponentInterfaceKey(VisualElement element) {
        return camelCaseNameToInterfaceKey(getCustomizationComponentInterface(element));
    }

    public static SortedSet<VisualElement> getVisualElementsWithCustomImplementation(PageContainer container) {
        SortedSet<VisualElement> result = new TreeSet<>(Comparator.comparing((VisualElement v) -> v.getFQName().trim()));

        collectVisualElementsMatchingCondition(container, VisualElement::isCustomImplementation, result);

        return result;
    }

    public static SortedSet<VisualElement> getContainerActionsVisualElementsWithCustomImplementation(PageContainer container) {
        SortedSet<VisualElement> result = new TreeSet<>(Comparator.comparing((VisualElement v) -> v.getFQName().trim()));

        collectVisualElementsMatchingCondition(container.getActionButtonGroup(), VisualElement::isCustomImplementation, result);

        return result;
    }

    public static boolean containerHasActionsWithCustomImplementation(PageContainer container) {
        return !getContainerActionsVisualElementsWithCustomImplementation(container).isEmpty();
    }

    public static String pageActionFQName(Action action) {
        String adn = simpleActionDefinitionName(action.getActionDefinition());
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();

        return firstToLower(containerComponentName(pageDefinition.getContainer())) + firstToUpper(adn);
    }

    public static String pageActionTypeName(Action action) {
        String adn = simpleActionDefinitionName(action.getActionDefinition());
        PageDefinition pageDefinition = (PageDefinition) action.eContainer();

        return containerComponentName(pageDefinition.getContainer()) + firstToUpper(adn);
    }

    public static String pageActionHookInterfaceKey(Action action) {
        String asd = pageActionFQName(action);
        return camelCaseNameToInterfaceKey(firstToUpper(asd)) + "_HOOK_INTERFACE_KEY";
    }

    public static String pageActionInterfaceKey(Action action) {
        String asd = pageActionFQName(action);
        return camelCaseNameToInterfaceKey(firstToUpper(asd)) + "_INTERFACE_KEY";
    }

    public static List<Action> getAllCallOperationActions(PageDefinition pageDefinition) {
        return pageDefinition.getActions().stream()
                .filter(Action::getIsCallOperationAction)
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .toList();
    }

    public static List<AttributeType> getOnBlurAttributesForContainer(PageContainer container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e.getOnBlur() != null && e.getOnBlur(), elements);

        Set<AttributeType> filtered  = new LinkedHashSet<>();

        for (VisualElement e: elements) {
            if (e instanceof Input input) {
                AttributeType attributeType = input.getAttributeType();
                if (filtered.stream().noneMatch(a -> a.getName().equals(attributeType.getName()))) {
                    filtered.add(attributeType);
                }
            }
        }

        return filtered.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }

    public static List<VisualElement> getElementsWithHiddenBy(Container container) {
        Set<VisualElement> elements = new LinkedHashSet<>();
        collectVisualElementsMatchingCondition(container, e -> e.getHiddenBy() != null, elements);
        return elements.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
    }
}
