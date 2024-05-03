#!/bin/bash

set -e

cp ../../runtime/judo-tatami-tests/models/ActionGroupTest/application/frontend-react/model/ActionGroupTest-ui.model ./judo-ui-react-itest/ActionGroupTest/model/ActionGroupTest-ui.model
cp ../../runtime/judo-tatami-tests/models/ActionGroupTest/application/frontend-react/model/ActionGroupTest-ui.model ./judo-ui-react-itest/ActionGroupTestPro/model/ActionGroupTestPro-ui.model
cp ../../runtime/judo-tatami-tests/models/CRUDActionsTest/application/frontend-react/model/CRUDActionsTest-ui.model ./judo-ui-react-itest/CRUDActionsTest/model/CRUDActionsTest-ui.model
cp ../../runtime/judo-tatami-tests/models/OperationParametersTest/application/frontend-react/model/OperationParametersTest-ui.model ./judo-ui-react-itest/OperationParametersTest/model/OperationParametersTest-ui.model
