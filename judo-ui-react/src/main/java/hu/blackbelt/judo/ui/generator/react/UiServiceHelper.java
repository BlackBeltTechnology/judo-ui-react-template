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
import hu.blackbelt.judo.meta.ui.Table;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.DataElement;
import hu.blackbelt.judo.meta.ui.data.OperationParameterType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import lombok.extern.java.Log;

import java.util.stream.Collectors;

@Log
@TemplateHelper
public class UiServiceHelper extends Helper {
    public static String dataElementRelationName(DataElement dataElement) {
        if (dataElement instanceof RelationType) {
            RelationType relation = (RelationType) dataElement;

            return variable(
                    relation.getOwnerPackageNameTokens().stream()
                            .map(Helper::getCamelCaseVersion)
                            .collect(Collectors.joining())
                            .concat(getCamelCaseVersion(relation.getOwnerSimpleName()))
                            .concat("ServiceFor")
                            .concat(getCamelCaseVersion(relation.getName()))
            );
        } else if (dataElement instanceof OperationParameterType) {
            OperationParameterType operationParameterType = (OperationParameterType) dataElement;

            return classServiceName(operationParameterType.getTarget());
        }

        throw new RuntimeException("Unable to process DataElement for dataElementRelationName: " + dataElement.getName());
    }

    public static String classServiceName(ClassType classType) {
        return variable(nameWithoutModel(classType.getName()) + "ServiceForClass");
    }

    public static String classServiceTypeName(ClassType classType) {
        return classType.getName().replaceAll("::", ".");
    }
}
