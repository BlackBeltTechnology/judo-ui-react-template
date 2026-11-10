package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.ThreadLocalContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static hu.blackbelt.judo.ui.generator.react.ReactStoredVariableHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers MUI license plan resolution from the {@code muiLicensePlan} template variable.
 * See change: JNG-6433 (fix-pro-table-stale-column-state).
 */
class ReactStoredVariableHelperTest {

    private static void bind(String muiLicensePlan, String useInlineColumnFilters) {
        Map<String, Object> context = new HashMap<>();
        if (muiLicensePlan != null) {
            context.put("muiLicensePlan", muiLicensePlan);
        }
        if (useInlineColumnFilters != null) {
            context.put("useInlineColumnFilters", useInlineColumnFilters);
        }
        ThreadLocalContextHolder.bindContext(context);
    }

    @AfterEach
    void unbind() {
        ThreadLocalContextHolder.bindContext(null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pro", "PRO", "Pro"})
    void proPlan(String plan) {
        bind(plan, "true");

        assertEquals(MUI_PLAN_PRO, getMUILicensePlan());
        assertTrue(isMUILicensed());
        assertTrue(isMUILicensePlanPro());
        assertEquals("DataGridPro", muiDataGridComponent());
        assertEquals("DataGridProProps", muiDataGridPropsType());
        assertEquals("-pro", getMUIDataGridPlanSuffix());
        assertEquals("-pro", getMUIPickersPlanSuffix());
        assertTrue(isUseInlineColumnFilters());
    }

    @Test
    void premiumPlan() {
        bind("premium", null);

        assertEquals(MUI_PLAN_PREMIUM, getMUILicensePlan());
        assertTrue(isMUILicensed());
        assertFalse(isMUILicensePlanPro());
        assertEquals("DataGridPremium", muiDataGridComponent());
        assertEquals("-premium", getMUIDataGridPlanSuffix());
        assertEquals("-pro", getMUIPickersPlanSuffix());
        assertFalse(isUseInlineColumnFilters());
    }

    @ParameterizedTest
    @ValueSource(strings = {"community", "", "enterprise"})
    void communityOrUnknownPlan(String plan) {
        bind(plan, "true");
        assertCommunity();
    }

    @Test
    void unsetPlan() {
        bind(null, "true");
        assertCommunity();
    }

    private static void assertCommunity() {
        assertEquals("", getMUILicensePlan());
        assertFalse(isMUILicensed());
        assertFalse(isMUILicensePlanPro());
        assertEquals("DataGrid", muiDataGridComponent());
        assertEquals("DataGridProps", muiDataGridPropsType());
        assertEquals("", getMUIDataGridPlanSuffix());
        assertEquals("", getMUIPickersPlanSuffix());
        assertFalse(isUseInlineColumnFilters(), "inline column filters require a licensed plan");
    }
}
