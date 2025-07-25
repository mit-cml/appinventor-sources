#version 300 es
/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec2 a_CameraTexCoord;
// The virtual scene texture coordinate is unused in the background shader, but
// is defined in the BackgroundRenderer Mesh.
layout(location = 2) in vec2 a_VirtualSceneTexCoord;

out vec2 v_CameraTexCoord;
out vec2 v_FilamentTexCoord;

void main() {
  // Simply use a_Position directly since it's already a vec4
  gl_Position = a_Position;
  v_CameraTexCoord = a_CameraTexCoord;
  v_FilamentTexCoord = a_VirtualSceneTexCoord;
}