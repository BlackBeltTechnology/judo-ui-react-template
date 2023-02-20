package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.Application;
import lombok.extern.java.Log;

import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.pathName;

@Log
@TemplateHelper
public class UiNPMHelper extends Helper {
    public static String npmPackageScopedName(String scope, String name) {
        String result = scope != null ? scope + "/" : "";
        return result + name;
    }

    public static String npmPackageRootName(String scope, Application app) {
        return npmPackageScopedName(scope, pathName(app.getActor().getName()));
    }

    public static String npmPackageName(String scope, Application app, String name) {
        return npmPackageRootName(scope, app) + "-" + name;
    }
}
