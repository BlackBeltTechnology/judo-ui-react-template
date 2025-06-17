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
import hu.blackbelt.judo.meta.ui.Action;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.NavigationItem;
import hu.blackbelt.judo.meta.ui.PageDefinition;
import hu.blackbelt.judo.meta.ui.data.*;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.serviceClassName;
import static hu.blackbelt.judo.ui.generator.typescript.rest.commons.UiCommonsHelper.serviceRelationName;

@Log
@TemplateHelper
public class MenuHelper {

    public static Set<DataElement> getMenuOperationOwnerTypes(Application application) {
        return application.getNavigationController()
                .getActions()
                .stream()
                .map(Action::getOwnerDataElement)
                .collect(Collectors.toSet());
    }

    public static List<PageDefinition> getAllTargetPage(Application application) {
        return application.getNavigationController()
                .getActions()
                .stream()
                .filter(action -> action.getTargetPageDefinition() != null &&
                        action.getTargetPageDefinition().isOpenInDialog())
                .map(Action::getTargetPageDefinition)
                .toList();
    }

    public static List<String> getAllRoutesFromMenuActions(Application application) {
        return application.getNavigationController()
                .getActions()
                .stream()
                .filter(action -> action.getTargetPageDefinition() != null &&
                        action.getTargetDataElement() instanceof OperationType &&
                        ((OperationType) action.getTargetDataElement()).getOutput() != null &&
                        !action.getTargetPageDefinition().isOpenInDialog())
                .map(action -> "routeTo" + UiPageHelper.pageName(action.getTargetPageDefinition()))
                .toList();
    }
}
