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
namespace google\appengine\runtime;

use google\appengine\testing\TestUtils;

class GlobTest extends \PHPUnit_Framework_TestCase {

  protected function tearDown() {
    if (!empty($GLOBALS['opendir'])) {
      $this->fail('Expected opendir call not made.');
    }
    if (!empty($GLOBALS['readdir'])) {
      $this->fail('Expected readdir call not made.');
    }
    if (!empty($GLOBALS['closedir'])) {
      $this->fail('Expected listdir call not made.');
    }
  }

  protected function setupBasicRead($files, $path = '.') {
    $handle = uniqid();
    $this->expectOpenDir($path, $handle);
    $this->expectReadDir($handle, $files);
    $this->expectCloseDir($handle);
  }

  public function testBasicGlob() {
    $this->setupBasicRead(['foo.txt', 'bar.txt']);
    $result = Glob::doGlob('*', GLOB_NOSORT);
    $this->assertEquals(['foo.txt', 'bar.txt'], $result);
  }

  public function testBasicGlobWithPath() {
    $this->setupBasicRead(['foo.txt', 'bar.txt'], 'bar/baz/zoo');
    $result = Glob::doGlob('bar/baz/zoo/*', GLOB_NOSORT);
    $this->assertEquals(
        ['bar/baz/zoo/foo.txt', 'bar/baz/zoo/bar.txt'], $result);
  }

  public function testPatternMatchGlob() {
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('*.txt', GLOB_NOSORT);
    $this->assertEquals(['foo.txt'], $result);
  }

  public function testBraceExpansionGlob() {
    // Expanding the braces will result in muliple reads
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('{*.txt,*.jpg}', GLOB_BRACE);
    $expected = ['foo.txt', 'bar.jpg'];
    $this->assertEquals($expected, $result);
    // We want to ensure that the items in the result array are also in the
    // expected order.
    while (!empty($expected)) {
      $val1 = array_shift($expected);
      $val2 = array_shift($result);
      $this->assertEquals($val1, $val2);
    }
  }

  public function testGlobSortOrder() {
    // Make sure sorting does not maintain the original index
    $this->setupBasicRead(['foo.txt', 'zoo.txt', 'bar.txt']);
    $result = Glob::doGlob('*');
    // The order is important.
    $expected = ['bar.txt', 'foo.txt', 'zoo.txt'];
    $this->assertEquals($expected, $result);
    // We want to ensure that the items in the result array are also in the
    // expected order.
    while (!empty($expected)) {
      $val1 = array_shift($expected);
      $val2 = array_shift($result);
      $this->assertEquals($val1, $val2);
    }
  }

  public function testNoBraceExpansionGlob() {
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('{*.txt,*.jpg}');
    $this->assertEquals([], $result);
  }

  public function testEscapeGlob() {
    $this->setupBasicRead(['[house].txt']);
    $result = Glob::doGlob('\[house\].txt');
    $this->assertEquals(['[house].txt'], $result);
  }

  public function testNoEscapeSetGlob() {
    $this->setupBasicRead(['[house].txt']);
    $result = Glob::doGlob('\[house\].txt', GLOB_NOESCAPE);
    $this->assertEquals([], $result);
  }

  public function testNoCheckGlob() {
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('*.png', GLOB_NOCHECK);
    $this->assertEquals(['*.png'], $result);
  }

  public function testNoCheckGlobNotEmpty() {
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('*.txt', GLOB_NOCHECK);
    $this->assertEquals(['foo.txt'], $result);
  }

  public function testErrorDoesNotTerminateGlob() {
    $this->expectFailedOpenDir('.');
    $this->setupBasicRead(['foo.txt', 'bar.jpg']);
    $result = Glob::doGlob('{foo.*,*.txt}', GLOB_BRACE);
    $this->assertEquals(['foo.txt'], $result);
  }

  public function testDotDirectoryNamesExcluded() {
    $this->setupBasicRead(['.', '..', 'foo.txt', 'bar.txt']);
    $result = Glob::doGlob('*', GLOB_NOSORT);
    $this->assertEquals(['foo.txt', 'bar.txt'], $result);
  }

  public function testWildcardPath() {
    $this->setupBasicRead(['foo']);
    $this->setFileIsDir('./foo');
    $this->setupBasicRead(['1.txt', '2.txt'], 'foo');
    $result = Glob::doGlob('*/*.txt');
    $this->assertEquals(['foo/1.txt', 'foo/2.txt'], $result);
  }

  public function testErrors() {
    // Match the semantics of glob as observed in
    // https://github.com/php/php-src/blob/master/ext/standard/tests/file/glob_error.phpt
    $this->setExpectedException('PHPUnit_Framework_Error_Warning');
    Glob::doGlob();  // Not enough arguments
    $this->setExpectedException('PHPUnit_Framework_Error_Warning');
    Glob::doGlob("*", GLOB_ERR, 2);  // Too many arguments
    $this->setExpectedException('PHPUnit_Framework_Error_Warning');
    Glob::doGlob('*', '');
    $this->setExpectedException('PHPUnit_Framework_Error_Warning');
    Glob::doGlob('*', 'str');
  }

  /**
   * @dataProvider braceExpansionDataProvider
   */
  public function testBraceExpansion($input, $output) {
    $glob = new Glob();
    $result = TestUtils::invokeMethod($glob, "expandFilenameBraces", [$input]);
    $this->assertEquals($output, $result);
  }

  /**
   * DataProvider for testBraceExpansion
   */
  public function braceExpansionDataProvider() {
    // Format is $input, $output
    return [
        ["/foo/bar", ["/foo/bar"]],
        ["/foo/*/bar", ["/foo/*/bar"]],
        ["{*.jpg}", ["*.jpg"]],
        ["{*.jpg,*.png}", ["*.jpg", "*.png"]],
        ["{*.jpg, *.png}", ["*.jpg", " *.png"]],
        ["/foo/{a,b}/bar", ["/foo/a/bar", "/foo/b/bar"]],
        ["/a/{1,2}/b/{3,4}", ["/a/1/b/3", "/a/2/b/3", "/a/1/b/4", "/a/2/b/4"]],
    ];
  }

  /**
   * @dataProvider dirNameForPathProvider
   */
  public function testDirNameForPath($input, $output) {
    $glob = new Glob();
    $this->assertEquals(
        $output, TestUtils::invokeMethod($glob, "getDirNameForPath", [$input]));
  }

  /**
   * DataProvider for testDirNameForPath.
   */
  public function dirNameForPathProvider() {
    // Format is [$input, $output]
    return [
      ['/foo/bar.txt', '/foo'],
      ['', '.'],
      ['gs://foo/bar/zoo.txt', 'gs://foo/bar'],
      ['./foo/bar/*', './foo/bar'],
    ];
  }

  /**
   * @dataProvider fileNamePatternMatchProvider
   */
  public function testFileNamePatternMatch($input, $pattern, $output, $opts) {
    $glob = new Glob();
    $result = TestUtils::invokeMethod($glob,
                                      "isFileNamePatternMatch",
                                      [$input, $pattern, $opts]);
    $this->assertEquals($output, $result);
  }

  /**
   * DataProvider for testFileNamePatternMatch.
   */
  public function fileNamePatternMatchProvider() {
    // Format input, pattern, output, options
    return [
      ['foo.txt', '*.txt', true, 0],
      ['foo.txt', '*', true, 0],
      ['foo.txt', '*.jpg', false, 0],
      ['foo.txt', 'f??.txt', true, 0],
      ['foo.txt', 'f?.txt', false, 0],
      ['f9.txt', 'f[0-9].txt', true, 0],
      ['f[].txt', 'f\[\].txt', true, 0],
      ['f[].txt]', 'f\[\].txt', false, GLOB_NOESCAPE],
      ['\".txt', '\".txt', true, GLOB_NOESCAPE],
      ['\".txt', '\".txt', false, 0],
    ];
  }

  /**
   * @dataProvider directoryOptionsDataProvider
   */
  public function testDirectoryOptions($base, $files, $dirs, $opts, $expected) {
    foreach($dirs as $dir) {
      $this->setFileIsDir(
          implode(DIRECTORY_SEPARATOR, [
              rtrim($base, DIRECTORY_SEPARATOR),
              ltrim($dir, DIRECTORY_SEPARATOR)
          ])
      );
    }
    $glob = new Glob();
    $result = TestUtils::invokeMethod($glob,
                                      "doDirectoryOptions",
                                      [$base, $files, $opts]);
    $this->assertEquals($expected, $result);
  }

  /**
   * DataProvider for testDirectoryOptions.
   */
  public function directoryOptionsDataProvider() {
    // Format basename, array files, array dirs, options, result array
    return [
      ['/foo', ['bar.txt'], ['bar.txt'], GLOB_ONLYDIR, ['bar.txt']],
      ['/foo', ['bar.txt'], [], GLOB_ONLYDIR, []],
      ['/foo', ['a', 'b', 'c'], ['a'], GLOB_ONLYDIR, ['a']],
      ['/foo', ['a', 'b'], ['a'], GLOB_MARK, ['a/', 'b']],
      ['/foo', ['a', 'b', 'c'], ['a'], GLOB_ONLYDIR | GLOB_MARK, ['a/']],
    ];
  }

  /**
   * @dataProvider splitPathOnWilcardDataProvider
   */
  public function testSplitPathOnWildcard($input, $output) {
    $glob = new Glob();
    $result = TestUtils::invokeMethod($glob,
                                      "splitPathOnWildcard",
                                      [$input]);
    $this->assertEquals($output, $result);

  }

  /**
   * DataProvider for testSplitPathOnWildcard.
   */
  public function splitPathOnWilcardDataProvider() {
    return [
      ['foo/*/bar/', ['foo/', '*', '/bar/']],
      ['*/bar/', ['', '*', '/bar/']],
      ['/foo/*', ['/foo/', '*', '']],
      ['/foo/*/', ['/foo/', '*', '/']],
      ['foo/?/bar/', ['foo/', '?', '/bar/']],
      ['?/bar/', ['', '?', '/bar/']],
      ['/foo/?', ['/foo/', '?', '']],
      ['foo/?baz/bar/', ['foo/', '?baz', '/bar/']],
      ['?baz/bar/', ['', '?baz', '/bar/']],
      ['/foo/?baz', ['/foo/', '?baz', '']],
      ['foo/*/bar/*/baz/', ['foo/', '*', '/bar/*/baz/']],
      ['*/bar/*/baz/', ['', '*', '/bar/*/baz/']],
      ['/foo/*/bar/*', ['/foo/', '*', '/bar/*']],
      ['./foo/*/', ['./foo/', '*', '/']],
      ['*/', ['', '*', '/']],
      ['*foo', ['', '*foo', '']],
      ['*', ['', '*', '']],
    ];
  }

  private function expectOpenDir($dirname, $handle) {
    $GLOBALS['opendir'][] = ['name' => $dirname, 'handle' => $handle];
  }

  private function expectFailedOpenDir($dirname) {
    $GLOBALS['opendir'][] = ['name' => $dirname, 'handle' => false];
  }

  private function expectReadDir($handle, $result) {
    foreach ($result as $r) {
      $GLOBALS['readdir'][] = ['handle' => $handle, 'result' => $r];
    }
    $GLOBALS['readdir'][] = ['handle' => $handle, 'result' => false];
  }

  private function expectCloseDir($handle) {
    $GLOBALS['closedir'][] = ['handle' => $handle];
  }

  private function setFileIsDir($file) {
    $GLOBALS['is_dir'][] = $file;
  }
}

// Mock functions
function opendir($dirname) {
  assert(isset($GLOBALS['opendir']), 'opendir called when not expected.');

  $expected = array_shift($GLOBALS['opendir']);
  assert($expected['name'] === $dirname,
         'opendir got: ' . $dirname . ' expected: ' . $expected['name']);
  return $expected['handle'];
}

function readdir($handle) {
  assert(isset($GLOBALS['readdir']), 'readdir called when not expected.');
  $expected = array_shift($GLOBALS['readdir']);
  assert($expected['handle'] === $handle,
         'readdir got: ' . $handle . ' expected: ' . $expected['handle']);
  return $expected['result'];
}

function closedir($handle) {
  assert(isset($GLOBALS['closedir']), 'closedir called when not expected.');
  $expected = array_shift($GLOBALS['closedir']);
  assert($expected['handle'] === $handle,
         'closedir got: ' . $handle . ' expected: ' . $expected['handle']);
}

function is_dir($file) {
  if (isset($GLOBALS['is_dir'])) {
    return in_array($file, $GLOBALS['is_dir']);
  } else {
    return false;
  }
}
