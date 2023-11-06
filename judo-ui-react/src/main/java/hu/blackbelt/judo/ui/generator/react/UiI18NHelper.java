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
import hu.blackbelt.judo.meta.ui.*;
import lombok.extern.java.Log;

import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.ReactStoredVariableHelper.DEFAULT_I18N_LANGUAGE;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiI18NHelper {

    // Translations ending with these tokens are excluded because they are handled in system-level, generalized translations
    private static final String[] EXCLUDED_TRANSLATION_SUFFIXES = {
            ".LabelWrapper",
            ".Left",
            ".Right",
            ".TableCreate",
            ".LinkCreate",
            ".LinkDelete",
            ".LinkEdit",
            ".LinkView",
            ".RowView",
            ".RowRemove",
            ".RowDelete",
            ".RowEdit",
            ".PageCreate",
            ".PageEdit",
            ".PageBack",
            ".PageAdd",
            ".PageDelete",
            ".PageClear",
            ".PageFilter",
            ".PageRefresh",
            ".PageSet"
    };
    private static final Pattern TRANSLATION_KEYS_TO_SKIP = Pattern.compile(".*(" + String.join("|", EXCLUDED_TRANSLATION_SUFFIXES) + ")$");

    public static String muiTranslationToken(String language, String suffix) {
        return language.replace("-", "") + (suffix != null ? suffix : "");
    }

    public static String shortLocale(String locale) {
        return locale.split("-")[0];
    }

    public static String getI18NKeyForNamedElement(NamedElement namedElement) {
        return stream(namedElement.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining("."));
    }

    public static String getTranslationKeyForVisualElement(VisualElement element) {
        if (element instanceof PageContainer) {
            return transformTranslationKey(element.getName());
        }
        String root = transformTranslationKey(element.getPageContainer().getName());
        return root + "." + String.join(".", collectUp(element, new ArrayList<>()));
    }

    public static String transformTranslationKey(String source) {
        return stream(source.replaceAll("[#./_]", "::")
                .split("::"))
                .filter(f -> !f.trim().isEmpty())
                .collect(Collectors.joining("."));
    }

    private static List<String> collectUp(VisualElement element, List<String> acc) {
        List<String> accReal = acc != null ? acc : new ArrayList<>();

        accReal.add(element.getName());

        if (element.eContainer() instanceof Container) {
            collectUp((VisualElement) element.eContainer(), accReal);
        }

        if (element.eContainer() instanceof TabController) {
            for (Tab tab: ((TabController) element.eContainer()).getTabs()) {
                collectUp(tab.getElement(), accReal);
            }
        }

        return accReal;
    }


    public static String getDefaultLanguage(Application application) {
        String language = application.getDefaultLanguage();
        if (language == null || language.isBlank()) {
            return DEFAULT_I18N_LANGUAGE;
        }
        return language;
    }
}
