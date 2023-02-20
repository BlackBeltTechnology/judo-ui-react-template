package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.Column;
import hu.blackbelt.judo.meta.ui.Filter;
import hu.blackbelt.judo.meta.ui.Link;
import hu.blackbelt.judo.meta.ui.Table;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.eclipse.emf.common.util.EList;
import org.w3c.dom.Attr;

import java.util.Collection;

@Log
@TemplateHelper
public class UiTableHelper extends Helper {

    private static String getFilterTypeForAttribute(AttributeType attributeType) {
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
            return "string";
        } else if (dataType instanceof NumericType) {
            return "number";
        } else if (dataType instanceof BooleanType) {
            return "boolean";
        }

        return "string";
    }

    public static boolean isColumnBoolean(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof BooleanType;
    }

    public static boolean isColumnBinary(Column column) {
        DataType dataType = column.getAttributeType().getDataType();

        return dataType instanceof BinaryType;
    }

    private static boolean isSortableDataType (DataType dataType) {
        return !(dataType instanceof EnumerationType) && !(dataType instanceof BinaryType);
    }

    private static boolean isSortableAttributeType (AttributeType attributeType) {
        return isSortableDataType(attributeType.getDataType()) && !attributeType.getIsMemberTypeTransient();
    }

    public static boolean isColumnSortable (Column column) {
        return isSortableAttributeType(column.getAttributeType());
    }

    public static String getDefaultSortParams(Column defaultSortColumn, Collection<Column> columns) {
        if (defaultSortColumn != null) {
            return "[{ field: '" + defaultSortColumn.getAttributeType().getName() + "', sort: 'asc' }]";
        }

        for (Column column : columns) {
            AttributeType type = column.getAttributeType();
            if (isSortableAttributeType(type)) {
                return "[{ field: '" + type.getName() + "', sort: 'asc' }]";
            }
        }

        return "[]";
    }

    public static String getDefaultSortParamsForTable (Table table) {
        return getDefaultSortParams(table.getDefaultSortColumn(), table.getColumns());
    }

    public static String getDefaultSortParamsForLink (Link link) {
        return getDefaultSortParams(link.getDefaultSortColumn(), link.getColumns());
    }

    public static Integer calculateTablePageLimit(String defaultValue) {
        // defaultValue should come from pom.xml parameter
        if (defaultValue != null) {
            return Integer.parseInt(defaultValue);
        }

        return 10;
    }

    public static Boolean tableHasActions(Table table) {
        return table.getActions().size() > 0;
    }

    public static boolean isAttributeTypeEnumeration(AttributeType attributeType) {
        return attributeType.getDataType() instanceof EnumerationType;
    }
}
