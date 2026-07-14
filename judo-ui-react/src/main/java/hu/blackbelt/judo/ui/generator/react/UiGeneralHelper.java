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
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.ReactStoredVariableHelper.getCustomComponentAnnotationPrefix;
import static hu.blackbelt.judo.ui.generator.react.UiPandinoHelper.getCustomizationComponentInterfaceKey;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.getXMIID;
import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiGeneralHelper {

    public static void debug(Object obj) {
        System.out.print(obj);
    }

    public static String pathName(String fqName) {
        return fqName
                .replaceAll("\\.", "-")
                .replaceAll("::", "-")
                .replaceAll("#", "-")
                .replaceAll("/", "-")
                .replaceAll("([a-z])([A-Z]+)", "$1-$2")
                .toLowerCase();
    }

    public static String safeName(NamedElement namedElement) {
        return stream(namedElement.getName().split("::")).map(org.springframework.util.StringUtils::capitalize).collect(Collectors.joining(""));
    }

    public static String toUnderscore(String fqName) {
        return pathName(fqName).replaceAll("-", "_");
    }

    public static String createId(EObject element) {
        return getXMIID(element).replaceAll("@", "");
    }

    /**
     * Resolve the identity of an EObject for {@code data-testid} emission.
     *
     * <p>Mirrors the runtime's {@code getElementTestId} in
     * {@code @judo/test-ids/src/element.ts}: prefers a non-empty {@code sourceId}
     * on the underlying {@link NamedElement}, otherwise falls back to the sanitized
     * xmi:id (via {@link #createId(EObject)}). Used exclusively for {@code data-testid}
     * emission — non-testid identity uses (Pandino keys, i18n keys, container-name
     * detection) continue to call {@code getXMIID} directly.
     */
    public static String getElementId(EObject element) {
        if (element == null) return "unknown";
        String sourceId = (element instanceof NamedElement)
                ? ((NamedElement) element).getSourceId()
                : null;
        String xmiIdRaw = null;
        try {
            xmiIdRaw = getXMIID(element);
        } catch (RuntimeException ignored) {
            // getXMIID requires an XMIResource; if the element is detached, fall through.
        }
        return resolveElementId(sourceId, xmiIdRaw);
    }

    /**
     * Pure-function core of {@link #getElementId(EObject)}. Package-private for
     * unit testability without EMF setup.
     *
     * @param sourceId the element's {@code sourceId} attribute value, or {@code null}
     * @param xmiIdRaw the raw xmi:id (possibly containing {@code @}), or {@code null}
     * @return the id to emit into {@code data-testid} strings
     */
    static String resolveElementId(String sourceId, String xmiIdRaw) {
        if (sourceId != null && !sourceId.isEmpty()) {
            return sourceId;
        }
        if (xmiIdRaw == null) {
            return "unknown";
        }
        return xmiIdRaw.replaceAll("@", "");
    }

    public static Boolean isNavItemAGroup(NavigationItem navigationItem) {
        return navigationItem.getTarget() == null;
    }

    public static String toLower(String str) {
        if (str == null) {
            return "";
        }
        return str.toLowerCase();
    }

    public static boolean boolValue(Boolean original) {
        return original != null && original;
    }

    public static Boolean stringValueIsTrue(String input) {
        return input != null && input.equalsIgnoreCase("true");
    }

    public static String attributePath(AttributeType attributeType) {
        return String.join("/", attributeType.getOwnerPackageNameTokens())
                .concat("/")
                .concat(attributeType.getOwnerSimpleName())
                .concat("/").concat(attributeType.getName());
    }

    public static Collection<Application> getAlternativeApplications(Application application, Collection<Application> applications) {
        // if current actor has a principal, only return other actors with the same realm
        if (application.getAuthentication() != null) {
            return applications.stream()
                    .filter(a -> a.getAuthentication() != null && !a.getFQName().equals(application.getFQName()) && a.getAuthentication().getRealm().equals(application.getAuthentication().getRealm()))
                    .collect(Collectors.toList());
        }

        // if current actor has no principal, only return other actors without principals
        return applications.stream()
                .filter(a -> a.getAuthentication() == null && !a.getFQName().equals(application.getFQName()))
                .collect(Collectors.toList());
    }

    public static String getFullAppName(Application application) {
        return application.getModelName() + "#." + application.getName();
    }

    public static boolean otherApplicationsAvailable(Application application, Collection<Application> applications) {
        return !getAlternativeApplications(application, applications).isEmpty();
    }

    public static List<String> getWritableDateAttributesForClass(ClassType classType) {
        return classType.getAttributes().stream()
                .filter(a -> a.getDataType() instanceof DateType)
                .filter(a -> !a.isIsReadOnly())
                .map(NamedElement::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getWritableDateTimeAttributesForClass(ClassType classType) {
        return classType.getAttributes().stream()
                .filter(a -> a.getDataType() instanceof TimestampType)
                .filter(a -> !a.isIsReadOnly())
                .map(NamedElement::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<String> getWritableTimeAttributesForClass(ClassType classType) {
        return classType.getAttributes().stream()
                .filter(a -> a.getDataType() instanceof TimeType)
                .filter(a -> !a.isIsReadOnly())
                .map(NamedElement::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    public static String getApplicationLogo(Application application) {
        String logo = application.getLogo();
        return logo == null ? "judo-color-logo.png" : logo;
    }

    public static String getApplicationIcon(Application application) {
        String icon = application.getIcon();
        return icon == null ? "judo-icon.webp" : icon;
    }

    public static EObject eContainer(EObject eObject) {
        return eObject.eContainer();
    }

    public static String toSafeCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean toUpperCase = false;
        boolean lastWasDelimiter = true;

        for (char c : input.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                if (!lastWasDelimiter) {
                    toUpperCase = true;
                    lastWasDelimiter = true;
                }
            } else {
                if (toUpperCase) {
                    result.append(Character.toUpperCase(c));
                    toUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
                lastWasDelimiter = false;
            }
        }

        return result.toString();
    }

    public static String escapeString(String input) {
        return input.replaceAll("\n", "\\\\n");
    }

    public static boolean elementHasAnnotation(NamedElement element, String annotation) {
        return element != null && element.getAnnotations().stream().anyMatch(a -> a.getName().equals(annotation));
    }

    public static boolean elementHasAnnotationStartingWith(NamedElement element, String prefix) {
        return element != null && getAnnotationNameStartingWith(element, prefix) != null;
    }

    public static String getAnnotationNameStartingWith(NamedElement element, String prefix) {
        if (element == null) {
            return null;
        }
        return element.getAnnotations().stream()
                .filter(a -> a.getName().startsWith(prefix))
                .map(Annotation::getName)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public static String attributeBasedComponentProxyFilters(VisualElement child) {
        String annotationPrefix = getCustomComponentAnnotationPrefix();
        // Direct component implementations will always take precedence.
        String base = "(component=${" + getCustomizationComponentInterfaceKey(child) + "})";
        if (elementHasAnnotationStartingWith(child, annotationPrefix)) {
            // Or, if defined use a specific implementation
            String full = getAnnotationNameStartingWith(child, annotationPrefix);
            String parameterName = full.substring(annotationPrefix.length());
            return "(|" + base + "(componentImplementation=" + parameterName + "))";
        }
        return base;
    }

    public static List<String> getAnnotationNamesForElement(NamedElement element) {
        return element.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .toList();
    }
    
    public static String menuLayout(Application application) {
        if (application.getDefaultMenuLayout().equals(MenuLayout.HORIZONTAL)) {
            return "MenuOrientation.HORIZONTAL";
        }
        return "MenuOrientation.VERTICAL";
    }
}
