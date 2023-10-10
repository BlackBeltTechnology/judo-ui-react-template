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
import hu.blackbelt.judo.meta.ui.VisualElement;
import lombok.extern.java.Log;
import org.springframework.util.StringUtils;

@Log
@TemplateHelper
public class UiPandinoHelper {
    public static String camelCaseNameToInterfaceKey(String name) {
        return name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

//    public static SortedSet<VisualElement> getVisualElementsWithCustomImplementation(PageDefinition pageDefinition) {
//        Set<VisualElement> visualElements = createFlattenedSetOfVisualElements(pageDefinition).stream()
//                .filter(VisualElement::isCustomImplementation).collect(Collectors.toSet());
//
//        SortedSet<VisualElement> result = new TreeSet<>(Comparator.comparing((VisualElement v) -> v.getFQName().trim()));
//
//        result.addAll(visualElements);
//
//        return result;
//    }

    public static String getCustomizationComponentInterface(VisualElement element) {
        return /*pageName(element.getPageDefinition()) + */StringUtils.capitalize(element.getName());
    }

    public static String getCustomizationComponentInterfaceKey(VisualElement element) {
        return camelCaseNameToInterfaceKey(getCustomizationComponentInterface(element));
    }

//    public static String getCustomizationActionFunctionInterfaceKey(Action action) {
//        return camelCaseNameToInterfaceKey(actionFunctionName(action)) + "_INTERFACE_KEY";
//    }
//
//    public static String getCustomizationActionFunctionHandlerInterfaceKey(Action action, String handlerType) {
//        return camelCaseNameToInterfaceKey(actionFunctionHandlerTypeName(action, handlerType)) + "_INTERFACE_KEY";
//    }
//
//    public static List<VisualElement> getOnBlurWidgetsForPage(PageDefinition pageDefinition) {
//        Set<VisualElement> elements = new LinkedHashSet<>();
//        collectVisualElementsMatchingCondition(pageDefinition.getOriginalPageContainer(), e -> e.getOnBlur() != null && e.getOnBlur(), elements);
//
//        return elements.stream().sorted(Comparator.comparing(NamedElement::getFQName)).collect(Collectors.toList());
//    }
}
