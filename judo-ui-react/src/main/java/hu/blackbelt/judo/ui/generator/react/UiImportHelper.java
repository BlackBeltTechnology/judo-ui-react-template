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

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Log
@TemplateHelper
public class UiImportHelper {
    private static final Map<String, Set<String>> muiMaterialWidgetImportPairs = Map.ofEntries(
            Map.entry("actiongroup", Set.of("Button")),
            Map.entry("binarytypeinput", Set.of("TextField", "InputAdornment", "Button")),
            Map.entry("button", Set.of("Button")),
            Map.entry("dateinput", Set.of("TextField", "InputAdornment")),
            Map.entry("datetimeinput", Set.of("TextField", "InputAdornment")),
            Map.entry("divider", Set.of("Divider")),
            Map.entry("enumerationcombo", Set.of("TextField", "MenuItem", "InputAdornment")),
            Map.entry("enumerationradio", Set.of("RadioGroup", "FormControlLabel", "Radio")),
            Map.entry("flex", Set.of("Card", "CardContent")),
            Map.entry("formatted", Set.of("Typography")),
            Map.entry("label", Set.of("Typography")),
            Map.entry("link", Set.of()),
            Map.entry("numericinput", Set.of("TextField", "InputAdornment")),
            Map.entry("spacer", Set.of()),
            Map.entry("switch", Set.of("TextField", "MenuItem", "InputAdornment")),
            Map.entry("tabcontroller", Set.of()),
            Map.entry("table", Set.of("Button")),
            Map.entry("text", Set.of("Typography")),
            Map.entry("textarea", Set.of("TextField", "InputAdornment")),
            Map.entry("textinput", Set.of("TextField", "InputAdornment")),
            Map.entry("timeinput", Set.of("TextField", "InputAdornment")),
            Map.entry("trinarylogiccombo", Set.of("TextField", "MenuItem", "InputAdornment"))
    );

    private static final Map<String, Set<String>> muiDataGridWidgetImportPairs = Map.ofEntries(
            Map.entry("table", Set.of("DataGrid", "GridToolbarContainer"))
    );

    private static final Map<String, Set<String>> muiDatePickerWidgetImportPairs = Map.ofEntries(
            Map.entry("dateinput", Set.of("DatePicker")),
            Map.entry("datetimeinput", Set.of("DateTimePicker")),
            Map.entry("timeinput", Set.of("TimePicker"))
    );

    public static boolean hasPageDateTimePickers(PageDefinition pageDefinition) {
        Set<String> uniqueVisualElementNames = getUniqueVisualElementNamesForPage(pageDefinition).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return uniqueVisualElementNames.stream()
                .anyMatch(muiDatePickerWidgetImportPairs::containsKey);
    }

    public static String getMuiDateTimePickerImportsForPage(PageDefinition pageDefinition) {
        Set<String> uniqueVisualElementNames = getUniqueVisualElementNamesForPage(pageDefinition).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> imports = new HashSet<>();

        muiDatePickerWidgetImportPairs.forEach((key, value) -> {
            if (uniqueVisualElementNames.contains(key)) {
                imports.addAll(value);
            }
        });

        return String.join(", ", imports).concat(imports.size() > 0 ? "," : "");
    }

    public static Set<String> getMuiDataGridImports(PageDefinition pageDefinition) {
        Set<String> uniqueVisualElementNames = getUniqueVisualElementNamesForPage(pageDefinition).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> imports = new HashSet<>();

        muiDataGridWidgetImportPairs.forEach((key, value) -> {
            if (uniqueVisualElementNames.contains(key)) {
                imports.addAll(value);
            }
        });

        imports.addAll(Set.of("GridRowId", "GridSortModel", "GridSortItem", "GridRowParams"));

        return imports;
    }

    public static String getMuiDataGridImportsForPage(PageDefinition pageDefinition) {
        Set<String> imports = getMuiDataGridImports(pageDefinition);

        return String.join(", ", imports).concat(imports.size() > 0 ? "," : "");
    }

    public static String getMuiDataGridImportsForActionForm(PageDefinition pageDefinition) {
        Set<String> imports = getMuiDataGridImports(pageDefinition);

        imports.addAll(Set.of("GridSelectionModel", "GridRenderCellParams", "GridColDef"));

        return String.join(", ", imports).concat(imports.size() > 0 ? "," : "");
    }

    public static Set<String> getMaterialImportsForPage(PageDefinition pageDefinition) {
        Set<String> uniqueVisualElementNames = getUniqueVisualElementNamesForPage(pageDefinition).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> imports = new HashSet<>();

        imports.addAll(Set.of("Button"));

        muiMaterialWidgetImportPairs.forEach((key, value) -> {
            if (uniqueVisualElementNames.contains(key)) {
                imports.addAll(value);
            }
        });

        return imports;
    }

    public static String getMuiMaterialImportsForPage(PageDefinition pageDefinition) {
        Set<String> imports = getMaterialImportsForPage(pageDefinition);

        return String.join(", ", imports).concat(imports.size() > 0 ? "," : "");
    }

    public static String getMuiMaterialImportsForActionForm(PageDefinition pageDefinition) {
        Set<String> imports = getMaterialImportsForPage(pageDefinition);

        imports.addAll(Set.of("Button", "IconButton", "DialogActions", "DialogContent", "DialogContentText", "DialogTitle"));

        return String.join(", ", imports).concat(imports.size() > 0 ? "," : "");
    }

    public static Set<String> getUniqueVisualElementNamesForPage(PageDefinition pageDefinition) {
        Set<VisualElement> flattenedVisualElements = createFlattenedSetOfVisualElements(pageDefinition);
        Set<String> uniqueVisualElementNames = new HashSet<>();

        flattenedVisualElements.forEach(v -> uniqueVisualElementNames.add(getVisualElementWidgetName(v)));

        return uniqueVisualElementNames;
    }

    public static boolean isVisualElementName(VisualElement visualElement, String name) {
        return getVisualElementWidgetName(visualElement).equalsIgnoreCase(name);
    }

    public static String getVisualElementWidgetName(VisualElement visualElement) {
        return visualElement.eClass().getInstanceClass().getSimpleName();
    }

    public static Set<VisualElement> createFlattenedSetOfVisualElements(PageDefinition pageDefinition) {
        Set<VisualElement> flattenedVisualElements = new HashSet<>();
        Container container = pageDefinition.getContainers().get(0);

        fillFlattenedVisualElements(container, flattenedVisualElements);

        return flattenedVisualElements;
    }

    public static void fillFlattenedVisualElements(Container container, Set<VisualElement> elements) {
        List<VisualElement> contents = container.getChildren();

        elements.addAll(contents);

        contents.stream()
                .filter(c -> c instanceof Container)
                .forEach(c -> fillFlattenedVisualElements(((Container) c), elements));

        List<TabController> tabControllers = contents.stream()
                .filter(c -> c instanceof TabController)
                .map(t -> ((TabController) t))
                .collect(Collectors.toList());

        elements.addAll(tabControllers);

        List<Tab> tabs = tabControllers.stream()
                .flatMap(t -> t.getTabs().stream())
                .collect(Collectors.toList());

        tabs.forEach(t -> {
            VisualElement element = t.getElement();
            elements.add(element);
            if (element instanceof Container) {
                fillFlattenedVisualElements(((Container) element), elements);
            }
        });
    }
}
