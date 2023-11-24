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
import hu.blackbelt.judo.meta.ui.NamedElement;
import hu.blackbelt.judo.meta.ui.NavigationItem;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.getXMIID;

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

    public static String createId(EObject element) {
        return getXMIID(element).replaceAll("@", "");
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

    public static EObject eContainer(EObject eObject) {
        return eObject.eContainer();
    }
}
