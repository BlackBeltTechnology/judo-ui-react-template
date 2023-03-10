package hu.blackbelt.judo.ui.generator.react;

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.NavigationItem;
import hu.blackbelt.judo.meta.ui.Sort;
import hu.blackbelt.judo.meta.ui.Stretch;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Log
@TemplateHelper
public class UiGeneralHelper extends Helper {

    public static void debug(Object obj) {
        System.out.print(obj);
    }

    public static String plainName(String input) {
        return input.replaceAll("[^\\.A-Za-z0-9_]", "_").toLowerCase();
    }

    public static String projectPathName(String fqName) {
        return fqName
                .replaceAll("\\.", "__")
                .replaceAll("::", "__")
                .replaceAll("#", "__")
                .replaceAll("/", "__")
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase();
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

    public static String path(String fqName) {
        String fq = pathName(fqName);
        if (fq.lastIndexOf("-") > -1) {
            return fq.substring(fq.lastIndexOf("-") + 2);
        } else {
            return fq;
        }
    }

    public static String modelName(String fqName) {
        String[] splitted = fqName.split("::");
        return fqClass(stream(splitted)
                .map(StringUtils::capitalize)
                .findFirst().get());
    }

    @Deprecated
    public static String fqClass(String fqName) {
        return stream(fqName.replaceAll("#", "::")
                .replaceAll("\\.", "::")
                .replaceAll("/", "::")
                .replaceAll("_", "::")
                .split("::"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    public static Boolean isNavItemAGroup(NavigationItem navigationItem) {
        return navigationItem.getTarget() == null;
    }

    public static String variable(String fqName) {
        return StringUtils.uncapitalize(className(fqName));
    }

    public static String ucFirst(String name) {
        return StringUtils.capitalize(name);
    }

    public static String safeSort(Sort input) {
        if (input == null) {
            return Sort.ASC.toString().toLowerCase();
        }
        return input.equals(Sort.NONE) ? Sort.ASC.toString().toLowerCase() : input.toString().toLowerCase();
    }

    public static boolean boolValue(Boolean original) {
        return original != null && original;
    }

    public static boolean hasElements(Collection<Object> items) {
        return items.size() > 0;
    }

    public static String emptyStringFallback(String input) {
        return input == null ? "" : input;
    }

    public static String attributePath(AttributeType attributeType) {
        return "/".concat(String.join("/", attributeType.getOwnerPackageNameTokens())
                .concat("/")
                .concat(attributeType.getOwnerSimpleName())
                .concat("/")).concat(attributeType.getName());
    }

    public static boolean hasDataElementOwner(DataElement dataElement) {
        if (dataElement instanceof RelationType) {
            return !((RelationType) dataElement).isIsAccess();
        }
        return false;
    }

    public static String appBaseHref(Application application) {
        return String.join("/", modelName(application.getName()) + "_react", projectPathName(application.getName()));
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
        return getAlternativeApplications(application, applications).size() > 0;
    }
}
