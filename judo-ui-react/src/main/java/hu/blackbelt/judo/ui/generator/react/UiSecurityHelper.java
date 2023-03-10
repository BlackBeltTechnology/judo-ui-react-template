package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.Application;
import lombok.extern.java.Log;

@Log
@TemplateHelper
public class UiSecurityHelper {
    public static String clientId(Application application) {
        return application.getName().replace("::", "-");
    }
}
