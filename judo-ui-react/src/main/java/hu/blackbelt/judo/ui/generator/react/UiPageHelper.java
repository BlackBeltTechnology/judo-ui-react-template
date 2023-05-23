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

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.*;
import hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper;
import lombok.extern.java.Log;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.react.UiGeneralHelper.hasDataElementOwner;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.classDataName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.restParamName;
import static java.util.Arrays.stream;


@Log
@TemplateHelper
public class UiPageHelper extends Helper {
    public static String getRelationClassName(RelationType relation) {
        return relation.getOwnerPackageNameTokens().stream().map(Helper::getCamelCaseVersion).collect(Collectors.joining())
                .concat(getCamelCaseVersion(relation.getOwnerSimpleName())).concat(getCamelCaseVersion(relation.getName()));
    }

    public static String getPageTypePath(PageDefinition page) {
        String pageType = page.getPageType().toString().toLowerCase();
        String suffix = page.getIsPageTypeDashboard() ? "" : pageType.concat("/");
        if (page.getDataElement() instanceof RelationType) {
            RelationType dataElement = (RelationType) page.getDataElement();
            return getFeaturePath(dataElement).concat(suffix);
        } else if (page.getDataElement() instanceof OperationParameterType) {
            return String.join("/", getPagePathTokens(page)).concat("/");
        }
        ClassType actor = ((Application)page.eContainer()).getActor();
        return getTypeNamePath(actor).concat(suffix);
    }

    public static List<String> getPagePathTokens(PageDefinition page) {
        return stream(page.getName().replaceAll("#", "::")
                .replaceAll("\\.", "::")
                .replaceAll("/", "::")
                .split("::"))
                .skip(1)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private static Boolean keepPageType(PageDefinition page) {
        PageType pageType = page.getPageType();

        if (pageType.equals(PageType.DASHBOARD)) {
            return true;
        }

        if (isPageSingletonAccessCreate(page)) {
            return true;
        }

        if (page.getDataElement() != null) {
            if (page.getDataElement() instanceof ReferenceType) {
                if (pageType.equals(PageType.CREATE)) {
                    return false;
                }
                ReferenceType referenceType = (ReferenceType) page.getDataElement();
                if (referenceType.getIsRefreshable()) {
                    if (referenceType.isIsCollection()) {
                        return pageType.equals(PageType.VIEW) || pageType.equals(PageType.TABLE);
                    }
                    // Currently there is a shady behaviour where in for single relations we would generate TABLE screens
                    // as well. We think this is a transformation bug, therefore for now we exclude these.
                    return pageType.equals(PageType.VIEW) || pageType.equals(PageType.OPERATION_OUTPUT);
                }
            }

            if (page.getIsPageTypeOperationOutput()) {
                return isPageRefreshable(page);
            }
        }

        return false;
    }

    public static boolean isPageRefreshable(PageDefinition pageDefinition) {
        return pageDefinition.getPageActions().stream().anyMatch(a -> a instanceof RefreshAction);
    }

    public static boolean isPageSingletonAccessCreate(PageDefinition page) {
        if (page.getDataElement() != null && page.getDataElement() instanceof RelationType) {
            RelationType relationType = (RelationType) page.getDataElement();

            return relationType.isIsAccess() && page.getPageType().equals(PageType.CREATE) && !relationType.isIsCollection();
        }

        return false;
    }

    public static List<PageDefinition> getPagesForRouting(Application application) {
        return application.getPages().stream()
                .filter(UiPageHelper::keepPageType)
                .sorted(Comparator.comparing(NamedElement::getFQName))
                .collect(Collectors.toList());
    }

    public static List<Link> getLinksForPages(Application application) {
        List<PageDefinition> pages = getPagesForRouting(application);

        return pages.stream()
                .flatMap(p -> ((List<Link>) p.getOriginalPageContainer().getLinks()).stream())
                .collect(Collectors.toList());
    }

    public static List<Table> getTablesForPages(Application application) {
        List<PageDefinition> pages = getPagesForRouting(application);

        return pages.stream()
                .flatMap(p -> ((List<Table>) p.getOriginalPageContainer().getTables()).stream())
                .collect(Collectors.toList());
    }

    public static boolean hasDashboard(Application application) {
      return  application.getPages().stream().filter(UiPageHelper::keepPageType).filter(page -> page.getIsPageTypeDashboard()).findFirst().isPresent();
    }

    public static String pageIndexRelativeImportPath(PageDefinition page) {
        return getPageTypePath(page).concat("index");
    }

    public static String pageIndexRelativePath(PageDefinition page) {
        return pageIndexRelativeImportPath(page).concat(".tsx");
    }

    public static String pagesFolderPath(ClassType actor) {
        return "/src/pages/";
    }

    public static String pageIndexPath(PageDefinition page) {
        return pagesFolderPath(((Application)page.eContainer()).getActor()).concat(pageIndexRelativePath(page));
    }

    public static String pagePath(PageDefinition page) {
        return pagesFolderPath(((Application)page.eContainer()).getActor()).concat(getPageTypePath(page));
    }

    public static String pagePathForTilde(PageDefinition page) {
        String path = "pages/".concat(getPageTypePath(page));

        return path.endsWith("/") ? StringUtils.substring(path, 0, -1) : path;
    }

    public static String pageName(PageDefinition page) {
        String pageType = page.getPageType().toString().toLowerCase();
        if (page.getDataElement() != null && keepPageType(page)) {
            if (page.getDataElement() instanceof RelationType) {
                RelationType dataElement = (RelationType) page.getDataElement();
                return getRelationClassName(dataElement).concat(getCamelCaseVersion(pageType));
            } else if (page.getDataElement() instanceof OperationParameterType) {
                return getPagePathTokens(page).stream().map(StringUtils::capitalize).collect(Collectors.joining(""));
            }
        }
        ClassType actor = ((Application)page.eContainer()).getActor();
        return getClassName(actor).concat(getCamelCaseVersion(pageType));
    }

    public static String getPageRoute(PageDefinition page) {
        String route = "" + getPageTypePath(page);

        if (route.endsWith("/")) {
            route = route.substring(0, route.length() - 1);
        }

        if (page.getDataElement() != null) {
            if (page.getDataElement() instanceof RelationType) {
                RelationType dataElement = (RelationType) page.getDataElement();

                if (dataElement.getIsMemberTypeAccess()) {
                    if (page.getIsPageTypeView() && !(dataElement.getIsCreatable() && !dataElement.isIsCollection())) {
                        route = route + "/:signedIdentifier";
                    }
                } else {
                    route = route + "/:signedIdentifier";
                }
            } else if (page.getIsPageTypeOperationOutput()) {
                if (isPageRefreshable(page)) {
                    route = route + "/:signedIdentifier";
                }
            } else {
                throw new IllegalArgumentException("Cannot process route for PageDefinition: " + page.getName());
            }
        }

        return route;
    }

    public static List<Action> getButtonActions(PageDefinition page) {
        return page.getButtons().stream()
                .map(b -> ((Button) b).getAction())
                .filter(a -> !(a instanceof BackAction))
                .collect(Collectors.toList());
    }

    public static Collection<Action> getUniquePageActions(PageDefinition page) {
        Collection<Action> actions = new ArrayList<>(page.getActions());
        return actions.stream()
                .filter(a -> !a.getIsBackAction() && !a.getIsSaveCreateAction())
                .collect(Collectors.toMap(UiCommonsHelper::getXMIID, p -> p, (p, q) -> p)).values();
    }

    public static Collection<Action> getOnlyPageActions(PageDefinition page) {
        Collection<Action> actions = getUniquePageActions(page);
        return actions.stream().filter(a -> a.getType().equals(ActionType.PAGE)).collect(Collectors.toList());
    }

    public static String getNavigationForPage(PageDefinition page, String signedId) {
        String route = getPageRoute(page);

        return signedId != null && route.contains(":signedIdentifier") ? route.replace(":signedIdentifier", "${" + signedId + "}") : route;
    }

    public static Table getTableForTablePage(PageDefinition page) {
        PageContainer container = page.getOriginalPageContainer();
        VisualElement visualElement = container.getChildren().get(0);

        if (visualElement instanceof Flex) {
            return (Table) ((Flex) visualElement).getChildren().stream().filter(c -> c instanceof Table).findFirst().get();
        } else if (visualElement instanceof Table) {
            return (Table) visualElement;
        } else {
            throw new RuntimeException("Error processing node, visual element type not supported: " + visualElement.getClass().getName());
        }
    }

    public static PageDefinition getViewPageForPage(PageDefinition pageDefinition) {
        Optional<ViewAction> pageAction = pageDefinition.getActions().stream().filter(a -> a instanceof ViewAction).map(a -> (ViewAction) a).findFirst();

        return pageAction.map(PageAction::getTarget).orElse(null);
    }

    public static List<Link> getPageLinks(PageDefinition pageDefinition) {
        return pageDefinition.getOriginalPageContainer().getLinks().stream().map(l -> (Link) l).collect(Collectors.toList());
    }

    public static List<Table> getPageTables(PageDefinition pageDefinition) {
        return (List<Table>) pageDefinition.getOriginalPageContainer().getTables().stream().map(t -> (Table) t).collect(Collectors.toList());
    }

    public static List<AttributeType> getEnumAttributesForPage(PageDefinition pageDefinition) {
        if (pageDefinition.getDataElement() == null) {
            return List.of();
        }

        ReferenceType dataElement = (ReferenceType) pageDefinition.getDataElement();

        return dataElement.getTarget().getAttributes()
                .stream()
                .filter(a -> a.getDataType() instanceof EnumerationType)
                .collect(Collectors.toList());
    }

    public static Boolean titleComesFromAttribute(PageDefinition page) {
        return page.getTitleFrom() != null && page.getTitleFrom() == TitleFrom.ATTRIBUTE;
    }

    public static Boolean isSingleAccessPage(PageDefinition page) {
        if (page.getRelationType() != null) {
            return !page.getRelationType().isIsCollection() && page.getRelationType().isIsAccess();
        }

        return false;
    }

    public static PageDefinition getViewPageForCreatePage(PageDefinition page, Application application) {
        String viewPageFQName = page.getFQName().replace("#Create", "#View");
        return application.getPages().stream()
                .filter(p -> p.getFQName().equals(viewPageFQName))
                .findFirst()
                .get();
    }

    private static void addReferenceTypesToCollection(PageDefinition pageDefinition, Set<String> res) {
        getPageLinks(pageDefinition).forEach(l -> {
            ReferenceType rel = (ReferenceType) l.getDataElement();
            res.addAll(getApiImportsForReferenceType(rel));
        });

        getPageTables(pageDefinition).forEach(t -> {
            ReferenceType rel = (ReferenceType) t.getDataElement();
            res.addAll(getApiImportsForReferenceType(rel));
        });
    }

    public static List<String> getApiImportsForReferenceType(ReferenceType reference) {
        Set<String> res = new HashSet<>();

        res.add(classDataName(reference.getTarget(), ""));
        res.add(classDataName(reference.getTarget(), "Stored"));
        res.add(classDataName(reference.getTarget(), "QueryCustomizer"));

        if (reference instanceof RelationType && !((RelationType) reference).isIsAccess()) {
            res.add(classDataName((ClassType) reference.getOwner(), ""));
            res.add(classDataName((ClassType) reference.getOwner(), "Stored"));
        }

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getOwnerApiImportsForDataElement(DataElement dataElement) {
        Set<String> res = new HashSet<>();

        if (hasDataElementOwner(dataElement)) {
            res.add(classDataName(((ClassType) dataElement.getOwner()), "Stored"));
            res.add(classDataName(((ClassType) dataElement.getOwner()), ""));
        }

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForTablePage(PageDefinition pageDefinition) {
        return getApiImportsForReferenceType((ReferenceType) pageDefinition.getDataElement());
    }

    public static List<String> getApiImportsForViewPage(PageDefinition pageDefinition) {
        Set<String> res = new HashSet<>();

        if (pageDefinition.getDataElement() instanceof ReferenceType) {
            res.addAll(getApiImportsForReferenceType((ReferenceType) pageDefinition.getDataElement()));
        }

        getEnumAttributesForPage(pageDefinition).forEach(a -> {
            res.add(restParamName(a.getDataType()));
        });

        addReferenceTypesToCollection(pageDefinition, res);

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForCreatePage(PageDefinition pageDefinition) {
        Set<String> res = new HashSet<>();

        if (pageDefinition.getDataElement() instanceof ReferenceType) {
            res.addAll(getApiImportsForReferenceType((ReferenceType) pageDefinition.getDataElement()));
        }

        addReferenceTypesToCollection(pageDefinition, res);

        getEnumAttributesForPage(pageDefinition).forEach(a -> {
            res.add(restParamName(a.getDataType()));
        });

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForCreateAction(CreateAction action) {
        Set<String> res = new HashSet<>(getApiImportsForViewPage(action.getTarget()));

        res.addAll(getOwnerApiImportsForDataElement(action.getDataElement()));

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForCallOperationAction(CallOperationAction action) {
        PageDefinition ownerPage = ((PageDefinition) action.eContainer());
        PageDefinition outputParameterPage = action.getOutputParameterPage();
        Set<String> res = action.getInputParameterPage() != null ? new HashSet<>(getApiImportsForViewPage(action.getInputParameterPage())) : new HashSet<>();

        res.addAll(getOwnerApiImportsForDataElement(action.getDataElement()));
        if (ownerPage != null) {
            if (ownerPage.getDataElement() instanceof ReferenceType) {
                res.addAll(getApiImportsForReferenceType((ReferenceType) ownerPage.getDataElement()));
            }
        }

        if (action.getOperation().getIsMapped()) {
            res.add(classDataName((ClassType) action.getDataElement().getOwner(), ""));
            res.add(classDataName((ClassType) action.getDataElement().getOwner(), "Stored"));
        }

        if (outputParameterPage != null) {
            res.addAll(getApiImportsForReferenceType((ReferenceType) outputParameterPage.getDataElement()));
        }

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForFormAction(Action action) {
        if (action.getIsCreateAction()) {
            return getApiImportsForCreateAction((CreateAction) action);
        } else {
            return getApiImportsForCallOperationAction((CallOperationAction) action);
        }
    }

    public static List<String> getApiImportsForUnmappedOperationOutputAction(PageDefinition page) {
        Set<String> res = new HashSet<>(getApiImportsForViewPage(page));
        res.addAll(getApiImportsForReferenceType((ReferenceType) page.getDataElement()));
        return res.stream().sorted().collect(Collectors.toList());
    }

    public static List<String> getApiImportsForRowAction(Action action) {
        Set<String> res = new HashSet<>();

        if (action.getDataElement() instanceof ReferenceType) {
            res.addAll(getApiImportsForReferenceType((ReferenceType) action.getDataElement()));
        }

        res.addAll(getOwnerApiImportsForDataElement(action.getDataElement()));

        return res.stream().sorted().collect(Collectors.toList());
    }

    public static VisualElement firstViewChildForContainer(PageContainer container) {
        Optional<VisualElement> element = container.getChildren()
                .stream()
                .filter(c -> {
                    if (c instanceof Flex) {
                        return ((Flex) c).getDataElement() != null;
                    }
                    return false;
                })
                .findFirst();
        return element.orElse(null);
    }

    public static VisualElement getDataContainerForPage(PageDefinition pageDefinition) {
        PageContainer defaultContainer = pageDefinition.getOriginalPageContainer();

        if (defaultContainer != null) {
            return firstViewChildForContainer(defaultContainer);
        }

        return null;
    }

    public static PageDefinition getPageForFormAction(Action action) {
        if (action.getIsCreateAction()) {
            return ((CreateAction) action).getTarget();
        } else if (action.getIsCallOperationAction()) {
            return ((CallOperationAction) action).getInputParameterPage();
        }
        throw new RuntimeException("Unsupported action type: " + action.getType());
    }
}
