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

import hu.blackbelt.judo.generator.commons.StaticMethodValueResolver;
import hu.blackbelt.judo.generator.commons.ThreadLocalContextHolder;
import hu.blackbelt.judo.generator.commons.annotations.ContextAccessor;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.Application;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * The handlebars context inaccessible in helpers / value resolvers
 * because there is no state for them. The ThreadLocal is used
 * to init variable values from template execution.
 */
@TemplateHelper
@ContextAccessor
public class StoredVariableHelper extends StaticMethodValueResolver {
    public static final String DEFAULT_I18N_LANGUAGE = "en-US";
    public static final String MUI_PLAN_COMMUNITY = "community";
    public static final String MUI_PLAN_PRO = "pro";
    public static final String MUI_PLAN_PREMIUM = "premium";


    public static void bindContext(Map<String, ?> context) {
        ThreadLocalContextHolder.bindContext(context);
    }

    public static synchronized Boolean isDebugPrint() {
        return Boolean.parseBoolean((String) ThreadLocalContextHolder.getVariable("debugPrint"));
    }

    public static synchronized Boolean isMUILicensed() {
        return !getMUILicensePlan().isBlank();
    }

    public static synchronized Boolean isMUILicensePlanPro() {
        return getMUILicensePlan().equals(MUI_PLAN_PRO);
    }

    public static synchronized String getMUILicensePlan() {
        String muiPlan = (String) ThreadLocalContextHolder.getVariable("muiLicensePlan");

        if (MUI_PLAN_PREMIUM.equalsIgnoreCase(muiPlan)) {
            return MUI_PLAN_PREMIUM;
        } else if (MUI_PLAN_PRO.equalsIgnoreCase(muiPlan)) {
            return MUI_PLAN_PRO;
        } else {
            return ""; // community also means no suffix
        }
    }

    public static synchronized String getMUIDataGridPlanSuffix() {
        String muiPlan = getMUILicensePlan();

        return  muiPlan.length() > 0 ? "-" + muiPlan : muiPlan;
    }

    public static synchronized String muiDataGridComponent() {
        return "DataGrid" + StringUtils.capitalize(getMUILicensePlan());
    }

    public static synchronized String muiDataGridPropsType() {
        return muiDataGridComponent() + "Props";
    }
}
