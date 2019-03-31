/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.nio.charset;

import javaemul.internal.EmulatedCharset;

/**
 * Constant definitions for the standard Charsets.
 */
public final class StandardCharsets {
  public static final Charset ISO_8859_1 = EmulatedCharset.ISO_8859_1;
  public static final Charset UTF_8 = EmulatedCharset.UTF_8;

  private StandardCharsets() {
    // Hides the constructor.
  }
}
