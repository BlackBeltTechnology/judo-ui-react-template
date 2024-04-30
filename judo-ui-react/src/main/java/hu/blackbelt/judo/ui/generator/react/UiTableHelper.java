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
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@TemplateHelper
public class UiTableHelper {

    public static String getFilterTypeForAttribute(AttributeType attributeType) {
        DataType dataType = attributeType.getDataType();

        if (dataType instanceof DateType) {
            return "date";
        } else if (dataType instanceof TimestampType) {
            return "dateTime";
        } else if (dataType instanceof EnumerationType) {
            return "enumeration";
        } else if (dataType instanceof NumericType) {
            return "numeric";
        } else if (dataType instanceof BooleanType) {
            if (!attributeType.isIsRequired()) {
                return "trinaryLogic";
            }
            return "boolean";
        }

        return "string";
    }

    public static String getFilterTypeForFilter(Filter filter) {
        return getFilterTypeForAttribute(filter.getAttributeType());
    }

    public static Integer columnWidth(Column column) {
        if (column.getWidth() != null) {
            String width = column.getWidth().trim();
            try {
                if (width.endsWith("px")) {
                    return Integer.valueOf((width.substring(0, width.length() - 2)).trim());
                }
                try {
                    Integer.parseInt(width);
                    return Integer.valueOf(width);
                } catch (NumberFormatException e) {
                    // fall back to default calculation
                }
            } catch (Exception e) {
                // let it fall back
                log.warn("Could not parse column width: {}, falling back to default calculated value", width);
            }
        }

        DataType dataType = column.getAttributeType().getDataType();

        if (dataType instanceof DateType) {
            return 170;
        } else if (dataType instanceof TimestampType) {
            return 170;
        } else if (dataType instanceof EnumerationType) {
            return 170;
        } else if (dataType instanceof NumericType) {
            return 100;
        } else if (dataType instanceof BooleanType) {
            return 100;
        }

        return 230;
    }

    public static String columnType(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        if (dataType instanceof DateType) {
            return "date";
        } else if (dataType instanceof TimestampType) {
            return "dateTime";
        } else if (dataType instanceof EnumerationType) {
            return "singleSelect";
        } else if (dataType instanceof NumericType) {
            return "number";
        } else if (dataType instanceof BooleanType) {
            return "boolean";
        }

        return "string";
    }

    public static boolean isColumnString(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof StringType;
    }

    public static boolean isColumnBoolean(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof BooleanType;
    }

    public static boolean isColumnNumeric(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof NumericType;
    }

    public static boolean isColumnEnumeration(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof EnumerationType;
    }

    public static boolean isColumnTimestamp(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof TimestampType;
    }

    public static boolean isColumnTime(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof TimeType;
    }

    public static boolean isColumnDate(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof DateType;
    }

    public static boolean isColumnDatetime(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof TimestampType;
    }

    public static boolean isColumnBinary(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof BinaryType;
    }

    private static boolean isSortableDataType (DataType dataType) {
        return !(dataType instanceof BinaryType);
    }

    private static boolean isSortableAttributeType (AttributeType attributeType) {
        return isSortableDataType(attributeType.getDataType()) && !attributeType.getIsMemberTypeTransient();
    }

    public static boolean isColumnSortable (Column column) {
        return isSortableAttributeType(column.getAttributeType());
    }

    private static boolean isFilterableDataType (DataType dataType) {
        return !(dataType instanceof BinaryType);
    }

    private static boolean isFilterableAttributeType (AttributeType attributeType) {
        return isFilterableDataType(attributeType.getDataType()) && !attributeType.getIsMemberTypeTransient();
    }

    public static boolean isColumnFilterable (Column column) {
        return isFilterableAttributeType(column.getAttributeType());
    }

    public static String getDefaultSortParams(Column defaultSortColumn, Collection<Column> columns) {
        if (defaultSortColumn != null && isSortableAttributeType(defaultSortColumn.getAttributeType())) {
            return "[{ field: '" + defaultSortColumn.getAttributeType().getName() + "', sort: " + getSortDirection(defaultSortColumn) + " }]";
        }

        for (Column column : columns) {
            AttributeType type = column.getAttributeType();
            if (isSortableAttributeType(type)) {
                return "[{ field: '" + type.getName() + "', sort: " + getSortDirection(column) + " }]";
            }
        }

        return "[]";
    }

    public static String getDefaultSortParamsForTable (Table table) {
        return getDefaultSortParams(table.getDefaultSortColumn(), table.getColumns());
    }

    public static Integer calculateTablePageLimit(Table table) {
        Integer defaultValue = table.isIsSelectorTable() ? table.getSelectorRowsPerPage() : table.getRowsPerPage();

        return defaultValue != null ? defaultValue : 10;
    }

    public static boolean isAttributeTypeEnumeration(AttributeType attributeType) {
        return attributeType.getDataType() instanceof EnumerationType;
    }

    public static String getSortDirection(Column column) {
        if (column.getSort() == null) {
            return "null";
        }
        if (column.getSort().equals(Sort.ASC)) {
            return "'asc'";
        } else if (column.getSort().equals(Sort.DESC)) {
            return "'desc'";
        } else {
            return "null";
        }
    }

    public static Boolean isSortDirectionDescending(Column column) {
        return column.getSort() != null && column.getSort().equals(Sort.DESC);
    }

    public static java.util.List<ActionDefinition> getBulkOperationActionDefinitionsForTable(Table table) {
        return table.getTableActionDefinitions().stream()
                .filter(a -> ((ActionDefinition) a).isIsBulk())
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .toList();
    }

    public static boolean tableHasBulkOperations(Table table) {
        return !getBulkOperationActionDefinitionsForTable(table).isEmpty();
    }

    public static boolean tableHasSelectorColumn(Table table) {
        return table.isIsSelectorTable()
                || table.getRowActionDefinitions().stream().anyMatch(a -> ((ActionDefinition) a).isIsBulkCapable())
                || table.getRowActionDefinitions().stream().anyMatch(a -> ((ActionDefinition) a).getIsDeleteAction())
                || table.getRowActionDefinitions().stream().anyMatch(a -> ((ActionDefinition) a).getIsRemoveAction());
    }

    public static Column getFirstTitleColumnForTable(Table table) {
        Optional<Column> column = table.getColumns().stream()
                .filter(c -> c.getAttributeType().getDataType() instanceof StringType && !c.getAttributeType().getIsMemberTypeTransient())
                .findFirst();
        return column.orElse(null);
    }

    public static boolean tableHasBooleanColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnBoolean);
    }

    public static boolean tableHasDateColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnDate);
    }

    public static boolean tableHasTimeColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnTime);
    }

    public static boolean tableHasDateTimeColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnDatetime);
    }

    public static boolean tableHasNumericColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnNumeric);
    }

    public static boolean tableHasEnumerationColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnEnumeration);
    }

    public static boolean tableHasStringColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnString);
    }

    public static boolean tableHasBinaryColumn(Table table) {
        return table.getColumns().stream().anyMatch(UiTableHelper::isColumnBinary);
    }

    public static List<Column> customizableColumns(Table table) {
        return table.getColumns().stream()
                .filter(VisualElement::isCustomImplementation)
                .toList();
    }

    public static boolean checkboxSelectionEnabled(Table table) {
        return table.getCheckboxSelection() == null || table.getCheckboxSelection() != CheckboxSelection.DISABLED;
    }
}
