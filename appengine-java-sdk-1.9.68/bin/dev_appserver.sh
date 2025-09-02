#!/bin/bash
# Copyright 2009 Google Inc. All Rights Reserved.
#
# Launches the Development AppServer.  This utility allows developers
# to test a Google App Engine application on their local workstation.

[[ -z "${DEBUG}" ]] || set -x  # trace if $DEBUG env. var. is non-zero

# Construct the absolute name of the SDK bin directory.
# Use -P so pwd will see the real name, independent of symbolic links.
readonly SDK_BIN="$(cd -P "$(dirname "$0")" && pwd)"
readonly SDK_LIB="$(dirname "${SDK_BIN}")/lib"
readonly JAR_FILE="${SDK_LIB}/appengine-tools-api.jar"

if [[ ! -e "${JAR_FILE}" ]]; then
    echo "${JAR_FILE} not found" >&2
    exit 1
fi

readonly SCRIPT_NAME=$(basename "$0")
readonly RUN_JAVA=$(dirname "$0")/run_java.sh
exec "${RUN_JAVA}" "${SCRIPT_NAME}" \
    -ea -cp "${JAR_FILE}" \
    com.google.appengine.tools.KickStart \
    com.google.appengine.tools.development.DevAppServerMain "$@"
