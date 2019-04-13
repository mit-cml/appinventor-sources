<?php
/**
 * Copyright 2007 Google Inc.
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
namespace google\appengine\util;

use google\appengine\util\ArrayUtil;

class ArrayUtilTest extends \PHPUnit_Framework_TestCase {

  private $merge_fn = "google\appengine\util\ArrayUtil::arrayMergeIgnoreCase";
  /**
   * @dataProvider arrayMergeDataProvider
   */
  public function testArrayMerge($array_args, $expected) {
    $result = call_user_func_array ($this->merge_fn, $array_args);
    // Accoring to S.O, best way is to assert same diff both ways on the arrays.
    $this->assertSame(array_diff($expected, $result),
                      array_diff($result, $expected));
  }

  public function arrayMergeDataProvider() {
    $input1 = ["A" => "B"];
    $input2 = ["C" => "D"];

    yield [[$input1, $input2], ["A" => "B", "C" => "D"]];

    $input3 = ["a" => "b"];
    yield [[$input1, $input3], ["A" => "b"]];

    yield [[$input1, $input2, $input3], ["A" => "b", "C" => "D"]];

    $input4 = ["A" => "z"];
    yield [[$input1, $input3, $input4], ["A" => "z"]];

    // Flip the input order of the arrays
    yield [[$input3, $input4, $input1], ["a" => "B"]];
  }
}
