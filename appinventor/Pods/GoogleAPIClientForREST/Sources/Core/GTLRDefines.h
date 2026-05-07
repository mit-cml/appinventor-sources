/* Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// GTLRDefines.h
//

#import <Foundation/Foundation.h>

#ifndef GTLR_DEBUG_ASSERT
  #if DEBUG && !defined(NS_BLOCK_ASSERTIONS)
    // NSCAssert to avoid capturing self if used in a block.
    #define GTLR_DEBUG_ASSERT(condition, ...) NSCAssert(condition, __VA_ARGS__)
  #elif DEBUG
    // In DEBUG builds with assertions blocked, log to avoid unused variable warnings.
    #define GTLR_DEBUG_ASSERT(condition, ...) if (!(condition)) { NSLog(__VA_ARGS__); }
  #else
    #define GTLR_DEBUG_ASSERT(condition, ...) do { } while (0)
  #endif
#endif

#ifndef GTLR_DEBUG_LOG
  #if DEBUG
    #define GTLR_DEBUG_LOG(...) NSLog(__VA_ARGS__)
  #else
    #define GTLR_DEBUG_LOG(...) do { } while (0)
  #endif
#endif
