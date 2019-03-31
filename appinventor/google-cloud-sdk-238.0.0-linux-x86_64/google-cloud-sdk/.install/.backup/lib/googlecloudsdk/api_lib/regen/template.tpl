# -*- coding: utf-8 -*- #
## Copyright 2015 Google Inc. All Rights Reserved.
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##    http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
${api_def_source}

MAP = {
% for api_name, api_versions in sorted(apis_map.items()):
    '${api_name}': {
      % for api_version, api_def in sorted(api_versions.items()):
        '${api_version}': APIDef(
            class_path='${api_def.class_path}',
            client_classpath='${api_def.client_classpath}',
            messages_modulepath='${api_def.messages_modulepath}',
            default_version=${api_def.default_version}
        ),
      % endfor
    },
% endfor
}
