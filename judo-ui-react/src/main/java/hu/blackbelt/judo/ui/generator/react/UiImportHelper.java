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
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import lombok.extern.java.Log;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiWidgetHelper.getReferenceClassType;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;

@Log
@TemplateHelper
public class UiImportHelper {
    private static final Map<String, Set<String>> muiMaterialWidgetImportPairs = Map.ofEntries(
            Map.entry("actiongroup", Set.of("Button")),
            Map.entry("binarytypeinput", Set.of("TextField", "InputAdornment", "Button", "ButtonGroup")),
            Map.entry("button", Set.of("Button")),
            Map.entry("dateinput", Set.of("InputAdornment")),
            Map.entry("datetimeinput", Set.of("InputAdornment")),
            Map.entry("divider", Set.of("Divider")),
            Map.entry("enumerationcombo", Set.of("TextField", "MenuItem", "InputAdornment")),
            Map.entry("enumerationradio", Set.of("RadioGroup", "FormControlLabel", "Radio", "FormControl", "FormHelperText", "InputLabel")),
            Map.entry("formatted", Set.of("Typography")),
            Map.entry("label", Set.of("Typography")),
            Map.entry("link", Set.of()),
            Map.entry("numericinput", Set.of("TextField", "InputAdornment")),
            Map.entry("spacer", Set.of()),
            Map.entry("switch", Set.of("TextField", "MenuItem", "InputAdornment", "FormGroup", "FormControlLabel", "Checkbox", "FormControl", "FormHelperText")),
            Map.entry("tabcontroller", Set.of()),
            Map.entry("table", Set.of("Button")),
            Map.entry("text", Set.of("Typography")),
            Map.entry("textarea", Set.of("TextField", "InputAdornment")),
            Map.entry("textinput", Set.of("TextField", "InputAdornment")),
            Map.entry("timeinput", Set.of("TextField", "InputAdornment")),
            Map.entry("trinarylogiccombo", Set.of("TextField", "MenuItem", "InputAdornment"))
    );

    public static SortedSet<String> getMaterialImportsForPageContainer(PageContainer container) {
        Set<String> uniqueVisualElementNames = getUniqueVisualElementNamesForPageContainer(container).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        SortedSet<String> imports = new TreeSet<>(Set.of("Button"));

        muiMaterialWidgetImportPairs.forEach((key, value) -> {
            if (uniqueVisualElementNames.contains(key)) {
                imports.addAll(value);
            }
        });

        return imports;
    }

    public static SortedSet<String>getUniqueVisualElementNamesForPageContainer(PageContainer container) {
        SortedSet<VisualElement> flattenedVisualElements = createFlattenedSetOfVisualElementsInContainer(container);
        SortedSet<String> uniqueVisualElementNames = new TreeSet<>();

        flattenedVisualElements.forEach(v -> uniqueVisualElementNames.add(getVisualElementWidgetName(v)));

        return uniqueVisualElementNames;
    }

    public static SortedSet<VisualElement> createFlattenedSetOfVisualElementsInContainer(PageContainer container) {
        SortedSet<VisualElement> flattenedVisualElements = new TreeSet<>(Comparator.comparing((VisualElement v) -> v.getFQName().trim()));

        fillFlattenedVisualElements(container, flattenedVisualElements);

        return flattenedVisualElements;
    }

    public static String getVisualElementWidgetName(VisualElement visualElement) {
        return visualElement.eClass().getInstanceClass().getSimpleName();
    }

    public static void fillFlattenedVisualElements(Container container, SortedSet<VisualElement> elements) {
        List<VisualElement> contents = container.getChildren();

        elements.addAll(contents);

        contents.stream()
                .filter(c -> c instanceof Container)
                .forEach(c -> fillFlattenedVisualElements(((Container) c), elements));

        List<TabController> tabControllers = contents.stream()
                .filter(c -> c instanceof TabController)
                .map(t -> ((TabController) t))
                .toList();

        elements.addAll(tabControllers);

        List<Tab> tabs = tabControllers.stream()
                .flatMap(t -> t.getTabs().stream())
                .toList();

        tabs.forEach(t -> {
            VisualElement element = t.getElement();
            elements.add(element);
            if (element instanceof Container) {
                fillFlattenedVisualElements(((Container) element), elements);
            }
        });
    }

    public static String getTableAPIImports(Table table, PageContainer container) {
        Set<String> res = new HashSet<>();

        if (!container.isTable() && container.getDataElement() instanceof ClassType dataElement) {
            res.add(classDataName(dataElement, ""));
            res.add(classDataName(dataElement, "Stored"));
        }

        ClassType classType = getReferenceClassType(table);

        if (classType != null) {
            res.add(classDataName(classType, ""));
            res.add(classDataName(classType, "Stored"));
            res.add(classDataName(classType, "QueryCustomizer"));
        }

        return res.stream().sorted().collect(Collectors.joining(", "));
    }

    public static String getLinkAPIImports(Link link, PageContainer container) {
        Set<String> res = new HashSet<>();

        if (!container.isTable() && container.getDataElement() instanceof ClassType dataElement) {
            res.add(classDataName(dataElement, ""));
            res.add(classDataName(dataElement, "Stored"));
        }

        if (link.getDataElement() instanceof RelationType relationType) {
            ClassType classType = relationType.getTarget();

            if (classType != null) {
                res.add(classDataName(classType, ""));
                res.add(classDataName(classType, "Stored"));
                res.add(classDataName(classType, "QueryCustomizer"));
            }
        }

        return res.stream().sorted().collect(Collectors.joining(", "));
    }
}
