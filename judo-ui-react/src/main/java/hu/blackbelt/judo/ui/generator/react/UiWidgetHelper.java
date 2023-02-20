package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.generator.commons.annotations.TemplateHelper;
import hu.blackbelt.judo.meta.ui.*;
import lombok.extern.java.Log;
import org.eclipse.emf.ecore.EObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@TemplateHelper
public class UiWidgetHelper extends Helper {
    public static String getWidgetTemplate(VisualElement visualElementType) {
        String componentsLocation = "actor/src/pages/widgets/";
        return componentsLocation + visualElementType.eClass().getInstanceClass().getSimpleName().toLowerCase() + ".hbs";
    }

    private static Boolean visualElementHasSaveInputAction(VisualElement visualElementType) {
        if (visualElementType instanceof Button) {
            return ((Button) visualElementType).getAction() instanceof SaveInputAction;
        }
        return false;
    }

    private static Boolean visualElementHasBackAction(VisualElement visualElementType) {
        if (visualElementType instanceof Button) {
            return ((Button) visualElementType).getAction() instanceof BackAction;
        }
        return false;
    }

    public static Boolean excludeWidgetFromTree(VisualElement visualElementType) {
        return visualElementHasSaveInputAction(visualElementType) || visualElementHasBackAction(visualElementType);
    }

    public static Button getSaveButtonForOperationInputPage(PageDefinition pageDefinition) {
        PageContainer root = pageDefinition.getContainers().get(0);
        VisualElement button = findSaveButton(root);

        return button != null ? (Button) button : null;
    }

    public static Button getBackButtonForOperationInputPage(PageDefinition pageDefinition) {
        PageContainer root = pageDefinition.getContainers().get(0);
        VisualElement button = findBackButton(root);

        return button != null ? (Button) button : null;
    }

    private static VisualElement findSaveButton(VisualElement visualElement) {
        if (visualElementHasSaveInputAction(visualElement)) {
            return visualElement;
        } else if (visualElement instanceof Container) {
            for (VisualElement child: ((Container) visualElement).getChildren()) {
                VisualElement nested = findSaveButton(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static VisualElement findBackButton(VisualElement visualElement) {
        if (visualElementHasBackAction(visualElement)) {
            return visualElement;
        } else if (visualElement instanceof Container) {
            for (VisualElement child: ((Container) visualElement).getChildren()) {
                VisualElement nested = findBackButton(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static String getFlexDirection(Flex flex) {
        return flex.getIsDirectionHorizontal() ? "row" : "column";
    }

    public static Double calculateSize(VisualElement element) {
        if (isParentFrame(element)) {
            return 12.0;
        }
        if ((isParentStretch(element) && isParentFrame(element)) || (isParentStretch(element.eContainer()) && isParentFrame(element.eContainer()))) {
            return 12.0;
        }
        if (element.eContainer() instanceof Flex) {
            if (isParentStretch(element) && ((Flex) element.eContainer()).getIsDirectionVertical()) {
                return 12.0;
            }

            double parentSize = ((Flex) element.eContainer()).getCol();
            double calculated = (12.0 / parentSize) * element.getCol();

            if (calculated <= 12.0) {
                return calculated;
            }
        }

        return element.getCol();
    }

    public static Boolean isParentDirectionColumn(VisualElement element) {
        if (element.eContainer() instanceof Flex) {
            return ((Flex) element.eContainer()).getIsDirectionVertical();
        }
        return false;
    }

    private static Boolean isParentFrame(EObject element) {
        if (element.eContainer() instanceof Flex) {
            return ((Flex) element.eContainer()).getFrame() != null;
        }
        return false;
    }

    private static Boolean isParentStretch(EObject element) {
        if (element.eContainer() instanceof VisualElement) {
            VisualElement parent = (VisualElement) element.eContainer();
            return parent.getStretch().equals(Stretch.HORIZONTAL) || parent.getStretch().equals(Stretch.BOTH);
        }
        return false;
    }

    public static Boolean spacingForFlex(Flex flex) {
        if (flex.getChildren().stream().anyMatch(c -> c instanceof Flex && ((Flex) c).getFrame() != null)) {
            return true;
        }
        return isParentFrame(flex);
    }

    public static String alignItems(Flex flex) {
        CrossAxisAlignment alignment = flex.getCrossAxisAlignment();
        Stretch stretch = flex.getStretch();

        if (stretch.equals(Stretch.BOTH) || stretch.equals(Stretch.HORIZONTAL)) {
            return "stretch";
        }

        switch (alignment) {
            case CENTER:
                return "center";
            case END:
                return "flex-end";
            case BASELINE:
                return "baseline";
            case STRETCH:
                return "stretch";
            default:
                return "flex-start";
        }
    }

    public static String justifyContent(Flex flex) {
        MainAxisAlignment alignment = flex.getMainAxisAlignment();

        switch (alignment) {
            case END:
                return "flex-end";
            case CENTER:
                return "center";
            case SPACEEVENLY:
                return "space-evenly";
            case SPACEAROUND:
                return "space-around";
            case SPACEBETWEEN:
                return "space-between";
            default:
                return "flex-start";
        }
    }

    public static List<Button> featuredActionsForActionGroup(ActionGroup actionGroup) {
        return actionGroup.getActions().stream().limit(actionGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static List<Button> nonFeaturedActionsForActionGroup(ActionGroup actionGroup) {
        return actionGroup.getActions().stream().skip(actionGroup.getFeaturedActions()).collect(Collectors.toList());
    }

    public static Boolean displayDropdownForActionGroup(ActionGroup actionGroup) {
        return nonFeaturedActionsForActionGroup(actionGroup).size() > 0;
    }
}
