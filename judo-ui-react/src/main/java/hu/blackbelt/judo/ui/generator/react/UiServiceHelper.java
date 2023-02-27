package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
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
        return variable(nameWithoutModel(classType.getName()) + "Service");
    }
}
