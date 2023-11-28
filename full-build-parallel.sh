#!/bin/bash
set -e

source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

MVND_OPTS=$(cat << EOF
-Dmvnd.threads=4
-V
-Dhttp.keepAlive=false
-Dmaven.wagon.http.pool=false
-Dmaven.wagon.httpconnectionManager.ttlSeconds=120
--no-transfer-progress
--color always
--fail-fast
-e
EOF
)

echo "Stop mvnd..."
mvnd --stop

echo "Clean project..."
mvnd $MVND_OPTS clean install