#!/bin/bash
# Copyright 2012 Google Inc. All Rights Reserved.
#
# Tool for Google Cloud Endpoints.

[[ -z "${DEBUG}" ]] || set -x  # trace if $DEBUG env. var. is non-zero

# Construct the absolute name of the SDK bin directory.
# Use -P so pwd will see the real name, independent of symbolic links.
readonly SDK_BIN="$(cd -P "$(dirname "$0")" && pwd)"
readonly SDK_LIB="$(dirname "${SDK_BIN}")/lib"

readonly JAR_FILE1="${SDK_LIB}/opt/tools/appengine-local-endpoints/v1/\
appengine-local-endpoints.jar"
if [[ ! -e "${JAR_FILE1}" ]]; then
  echo "${JAR_FILE1} not found"
  exit 1
fi

readonly JAR_FILE2="${SDK_LIB}/opt/user/appengine-endpoints/v1/\
appengine-endpoints.jar"
if [[ ! -e "${JAR_FILE2}" ]]; then
  echo "${JAR_FILE2} not found"
  exit 1
fi

CLASSPATH="${JAR_FILE1}:${JAR_FILE2}:${SDK_LIB}/shared/servlet-api.jar:\
${SDK_LIB}/appengine-tools-api.jar:\
${SDK_LIB}/opt/user/datanucleus/v1/jdo2-api-2.3-eb.jar"
for jar in "${SDK_LIB}"/user/*.jar; do
  CLASSPATH="${CLASSPATH}:$jar"
done

readonly SCRIPT_NAME=$(basename "$0")
readonly RUN_JAVA=$(dirname "$0")/run_java.sh
exec "${RUN_JAVA}" "${SCRIPT_NAME}" \
    -cp "${CLASSPATH}" com.google.api.server.spi.tools.EndpointsTool "$@"
