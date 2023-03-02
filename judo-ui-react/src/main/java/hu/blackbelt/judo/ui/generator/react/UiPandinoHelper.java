package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import lombok.extern.java.Log;

@Log
@TemplateHelper
public class UiPandinoHelper {
    public static String camelCaseNameToInterfaceKey(String name) {
        return name.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }
}
