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
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;

@Log
@TemplateHelper
public class UiServiceHelper extends Helper {

    public static List<DataElement> getDataServices(Application application) {
        List<DataElement> elements = new ArrayList<>();
        elements.addAll(application.getDataElements());
        elements.addAll(application.getRelationTypes());
        return elements;
    }

    public static String serviceName(DataElement dataElement) {
        if (dataElement instanceof ClassType) {
            return classDataName((ClassType) dataElement, "");
        } else if (dataElement instanceof OperationParameterType) {
            return ((OperationParameterType) dataElement).getName();
        } else if (dataElement instanceof RelationType) {
            return servicePackagedRelationName((RelationType) dataElement);
        } else {
            throw new RuntimeException("Unsupported DataElement: " + dataElement.getName());
        }
    }

    public static String servicePackagedRelationName(RelationType relation) {
        return classDataName((ClassType) relation.getOwner(), "")
                .concat("Relation")
                .concat(StringUtils.capitalize(relation.getName()));
    }

    public static boolean isDataElementRelationType(DataElement dataElement) {
        return dataElement instanceof RelationType;
    }

    public static boolean isDataElementClassType(DataElement dataElement) {
        return dataElement instanceof ClassType;
    }

    public static boolean isDataElementOperationParameterType(DataElement dataElement) {
        return dataElement instanceof OperationParameterType;
    }

    public static List<String> getImportsForRelationService(RelationType relationType) {
        Set<String> collector = new HashSet<>();
        ClassType target = relationType.getTarget();

        if (!relationType.isIsAccess()) {
            ClassType owner = (ClassType) relationType.getOwner();
            collector.add(classDataName(owner, ""));
            collector.add(classDataName(owner, "Stored"));
        }

        collector.add(classDataName(target, ""));
        collector.add(classDataName(target, "Stored"));
        collector.add(classDataName(target, "QueryCustomizer"));

        for (OperationType operation: target.getOperations()) {
            if (operation.getInput() != null) {
                collector.add(classDataName(operation.getInput().getTarget(), ""));
                collector.add(classDataName(operation.getInput().getTarget(), "QueryCustomizer"));
                collector.add(classDataName(operation.getInput().getTarget(), "Stored"));
            }

            if (operation.getOutput() != null) {
                collector.add(classDataName(operation.getOutput().getTarget(), "Stored"));
            }
        }

        return collector.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getImportsForClassService(ClassType classType) {
        Set<String> collector = new HashSet<>();

        if (!classType.isIsActor()) {
            collector.add(classDataName(classType, ""));
            collector.add(classDataName(classType, "Stored"));
            collector.add(classDataName(classType, "QueryCustomizer"));
        }

        for (OperationType operation: classType.getOperations()) {
            if (operation.getInput() != null) {
                collector.add(classDataName(operation.getInput().getTarget(), ""));
                collector.add(classDataName(operation.getInput().getTarget(), "QueryCustomizer"));
                collector.add(classDataName(operation.getInput().getTarget(), "Stored"));
            }

            if (operation.getOutput() != null) {
                collector.add(classDataName(operation.getOutput().getTarget(), "Stored"));
            }
        }

        return collector.stream().sorted().collect(Collectors.toList());
    }

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
        return variable(nameWithoutModel(classType.getName()) + "Service");
    }
}
