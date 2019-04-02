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
/**
 * Pure PHP implementation of glob(), to support Google Cloud Storage.
 *
 * This implemenation replaces the built-in glob function, and uses opendir/
 * readdir/closedir to retrieve the list of files for a given path.
 */

namespace google\appengine\runtime;

final class Glob {
  // Regular expression used to find braces in the path that need expanding.
  const BRACE_PATTERN = '/(.*){(.*)}(.*)/';

  // Regular expression used to find and extract the first path element that
  // contains shell wildcards such as ? and *.
  const WILDCARD_PATTERN = '#(.*?)([^%s]*[\*\?]+[^%s]*)(.*)#';

  // The list of file names that can be returned from readdir() that should not
  // be returned by glob().
  static private $excluded_file_names = ['.', '..'];

  /**
   * Find pathnames matching a pattern.
   *
   * The glob() function searches for all the pathnames matching pattern
   * according to the rules used by the libc glob() function, which is similar
   * to the rules used by common shells.
   *
   * @param string $pattern The pattern to match.
   * @param int $options optional Valid flags. For a list of flags refer to
   *     http://php.net/manual/en/function.glob.php
   * @return Returns an array containing the matched files/directories, an
   *     empty array if no file matched or FALSE on error.
   */
  public static function doGlob($pattern, $options = 0) {
    $results = [];
    if ($options & GLOB_BRACE) {
      $patterns = static::expandFilenameBraces($pattern);
    } else {
      $patterns = [$pattern];
    }
    foreach ($patterns as $pat) {
      $out = static::doGlobForPath($pat, $options);
      if ($out === false) {
        if ($options & GLOB_ERR) {
          break;
        }
      } else {
        if (($options & GLOB_NOSORT) === 0) {
          sort($out);
        }
        $results = array_merge($results, $out);
      }
    }
    if (($options & GLOB_NOCHECK) && empty($results)) {
      $results[] = $pattern;
    }
    return $results;
  }

  /**
   * Glob a given path, after braces have been expanded but before wildcards
   * have been expanded.
   *
   * @param string $path The path to glob, can contain wildcards.
   * @param int $options Valid Glob flags.
   * @return Returns an array containing the matched files/directories, an
   *     empty array if no file matched or FALSE on error.
   */
  private static function doGlobForPath($path, $options) {
    $dirname = static::getDirNameForPath($path);
    $basename = static::getBaseNameForPath($path);

    $expanded_path = static::splitPathOnWildcard($dirname);
    if ($expanded_path === false) {
      return static::doGlobForExpandedPath($path, $options);
    }

    // Use glob to get the directory expansion for the wildcard path.
    $path = $expanded_path[0] . $expanded_path[1];
    $dirs = static::doGlobForPath($path, GLOB_ONLYDIR);
    $results = [];
    foreach ($dirs as $dir) {
      // Stitch the full path back together and then recurse.
      $dirname = $expanded_path[0] . $dir . $expanded_path[2];
      $path = $dirname . DIRECTORY_SEPARATOR . $basename;
      $subdir = static::doGlobForPath($path, $options);
      $results = array_merge($results, $subdir);
    }
    return $results;
  }

  /**
   * Glob a given path.
   *
   * At this point any braces and path wildcards have been expaneded.
   * @param string $path The path to glob, can contain wildcards.
   * @param int $options Valid Glob flags.
   * @return Returns an array containing the matched files/directories, an
   *     empty array if no file matched or FALSE on error.
   */
  private static function doGlobForExpandedPath($filename, $options) {
    $openpath = static::getDirNameForPath($filename);
    $dirname = pathinfo($filename, PATHINFO_DIRNAME);
    $basename = static::getBaseNameForPath($filename);

    $results = [];
    $handle = @opendir($openpath);

    if ($handle === false) {
      return false;
    }
    while (($name = readdir($handle)) !== false) {
      if (static::isFileNamePatternMatch($name, $basename, $options)) {
        if (strpos($filename, DIRECTORY_SEPARATOR) !== false) {
          $name = $dirname . DIRECTORY_SEPARATOR . $name;
        }
        $results[] = $name;
      }
    }
    closedir($handle);

    if ($options & (GLOB_MARK | GLOB_ONLYDIR)) {
      $results = static::doDirectoryOptions($dirname, $results, $options);
    }

    return $results;
  }

  /**
   * Get the dirname for a path ready for calling opendir.
   *
   * If there is no directory (i.e. we are globbing the current directory) then
   * return '.' so opendir() will work on the current directory.
   *
   * @param string $path The file path that is being globbed.
   * @return string The directory to be opened for the path.
   */
  private static function getDirNameForPath($path) {
    $result = pathinfo($path, PATHINFO_DIRNAME);
    if (empty($result)) {
      return '.';
    }
    return $result;
  }

  /**
   * Get the base name for a given path. This is used to match against files
   * read from a directory.
   *
   * @param string $path The path being for glob()
   * @return string The file part for the glob, or '*' if no file was specified.
   */
  private static function getBaseNameForPath($path) {
    $basename = pathinfo($path, PATHINFO_BASENAME);
    // An empty basename will match everything.
    if (empty($basename)) {
      return '*';
    } else {
      return $basename;
    }
  }

  /**
   * Check if a file name matches the specified glob pattern.
   *
   * @param string $name The file name
   * @param string $pattern The pattern to match against.
   * @param int $options The flags passed to glob().
   * @return boolean True if there is a match, false otherwise.
   */
  private static function isFileNamePatternMatch($name, $pattern, $options) {
    if (in_array($name, static::$excluded_file_names)) {
      return false;
    }

    $fnmatch_flags = 0;
    if ($options & GLOB_NOESCAPE) {
      $fnmatch_flags |= FNM_NOESCAPE;
    }

    return fnmatch($pattern, $name, $fnmatch_flags);
  }

  /**
   * Expand out any braces in the incomming pattern, acording to shell brace
   * rules.
   *
   * @param $pattern The pattern to expand.
   * @return An array where any braces have been expanded.
   */
  private static function expandFilenameBraces($pattern) {
    $result = [];
    if (preg_match(self::BRACE_PATTERN, $pattern, $matches) === 1) {
      $items = explode(",", $matches[2]);
      foreach($items as $match) {
        $str = $matches[1] . $match . $matches[3];
        $exp = static::expandFilenameBraces($str);
        $result = array_merge($result, $exp);
      }
    } else {
      $result[] = $pattern;
    }
    return $result;
  }

  /**
   * Perform glob directory actions on a list of files.
   *
   * @param string $basename The directory name for the glob results.
   * @param array $filenames An array of file name strings.
   * @param int $options The options passed to glob().
   * @return array An array of results that match the glob() options.
   */
  private static function doDirectoryOptions($basename, $filenames, $options) {
    $name_list = [];
    $results = [];
    // We need to know the path to the file so we can check if it's a directory.
    foreach($filenames as $file) {
      $name_list[] = ['name' => $file,
                      'path' => implode(DIRECTORY_SEPARATOR, [
                                        rtrim($basename, DIRECTORY_SEPARATOR),
                                        ltrim($file, DIRECTORY_SEPARATOR)]),
      ];
    }
    foreach($name_list as $name_and_path) {
      $name = $name_and_path['name'];
      $isdir = is_dir($name_and_path['path']);
      if (($options & GLOB_MARK) && $isdir) {
        $name = rtrim($name, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR;
      }
      if ($options & GLOB_ONLYDIR) {
        if ($isdir) {
          $results[] = $name;
        }
      } else {
        $results[] = $name;
      }
    }
    return $results;
  }

  /**
   * Look for shell wildcards in a path, if found split the path around the
   * first wilcard segment.
   *
   * If there is a wilcard in the path then this method will return an array, as
   * follows (note that the directory separator is not included in the matching
   * wildcard segment):
   *
   * Input: '/foo/abc*xyz/bar/1?2/zoo'
   * Output: [ '/foo/, 'abc*xyz', '/bar/1?2/zoo']
   *
   * @param string $path The path that may contain wildcards.
   * @return array|boolean Returns an array of the split path, or false if there
   *     is no wildcard in the path.
   */
  private static function splitPathOnWildcard($path) {
    // On windows DIRECTORY_SEPARATOR is a '\' which we need to escape.
    $sep = addslashes(DIRECTORY_SEPARATOR);
    $pattern = sprintf(static::WILDCARD_PATTERN, $sep, $sep);
    if (preg_match($pattern, $path, $matches)) {
      return array_slice($matches, 1);
    }
    return false;
  }
}
