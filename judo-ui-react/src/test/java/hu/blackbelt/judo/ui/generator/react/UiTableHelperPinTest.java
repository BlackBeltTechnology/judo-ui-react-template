package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.meta.ui.Column;
import hu.blackbelt.judo.meta.ui.Pin;
import hu.blackbelt.judo.meta.ui.Table;
import hu.blackbelt.judo.meta.ui.UiFactory;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import hu.blackbelt.judo.meta.ui.data.DataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UiTableHelperPinTest {

    private static Column column(String field, Pin pin) {
        AttributeType attribute = DataFactory.eINSTANCE.createAttributeType();
        attribute.setName(field);
        Column column = UiFactory.eINSTANCE.createColumn();
        column.setName(field + "Column");
        column.setAttributeType(attribute);
        column.setPinned(pin);
        return column;
    }

    @Test
    void pinnedColumnFieldsKeepsColumnOrderPerSide() {
        Table table = UiFactory.eINSTANCE.createTable();
        table.getColumns().addAll(List.of(
                column("name", Pin.LEFT),
                column("note", Pin.NONE),
                column("code", Pin.LEFT),
                column("total", Pin.RIGHT)
        ));

        assertEquals(List.of("name", "code"), UiTableHelper.pinnedColumnFields(table, "LEFT"));
        assertEquals(List.of("total"), UiTableHelper.pinnedColumnFields(table, "RIGHT"));
    }

    @Test
    void columnDefaultsToNotPinned() {
        Table table = UiFactory.eINSTANCE.createTable();
        Column column = UiFactory.eINSTANCE.createColumn();
        column.setAttributeType(DataFactory.eINSTANCE.createAttributeType());
        table.getColumns().add(column);

        assertEquals(Pin.NONE, column.getPinned());
        assertEquals(List.of(), UiTableHelper.pinnedColumnFields(table, "LEFT"));
        assertEquals(List.of(), UiTableHelper.pinnedColumnFields(table, "RIGHT"));
    }
}
