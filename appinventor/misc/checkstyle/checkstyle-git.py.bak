#!/usr/bin/env python3
import subprocess
from io import StringIO
from re import compile
import os
import sys


parent_commit = 'HEAD' if len(sys.argv) < 2 else sys.argv[1]
checkstyle_pattern = compile(r'^\[(?P<severity>[^\]]*)\] (?P<filename>[^:]*):(?P<line>[0-9]*):(?P<message>.*)'
                             r' \[(?P<rule>[^\]]*)\]$')
compile(r'^\[(?P<severity>[^\]]*)\]')
basedir = os.getcwd() if os.path.exists('build.xml') else os.path.join(os.getcwd(), 'appinventor')


def checkstyle(current_file):
    return StringIO(subprocess.check_output(['java', '-cp',
                                             'lib/checkstyle/checkstyle.jar:lib/checkstyle/appinventor-checks.jar',
                                             'com.puppycrawl.tools.checkstyle.Main', '-c',
                                             'lib/checkstyle/appinventor-checks.xml', current_file],
                                            encoding='utf-8', cwd=basedir))


def check_chunks(checkstyle_output, chunks):
    """

    :param checkstyle_output:
    :param chunks:
    :type chunks: list[(int, int)]
    :return:
    """
    success = True
    i = 0
    for line in checkstyle_output:
        match = checkstyle_pattern.match(line.strip())
        if match:
            linenum = int(match.group('line'))
            while i < len(chunks):
                if chunks[i][0] <= linenum < chunks[i][1]:
                    print(line.strip())
                    success = False
                    break
                elif chunks[i][0] <= linenum:
                    i += 1
                    if i >= len(chunks):
                        return success
                else:
                    break
    return success


def process_chunk_info(line):
    """
    Processes a unified diff chunk header and returns a tuple indication the start and length of the deletion and
    addition hunks.

    :param line: Unified diff chunk marker, beginning with '@@'
    :type line: str
    :return: a 4-tuple of deletion start, deletion length, addition start, addition length
    :rtype: tuple[int]
    """
    parts = line.split(' ')
    del_info = parts[1][1:]
    add_info = parts[2][1:]
    del_start, del_length = map(int, del_info.split(',')) if ',' in del_info else (int(del_info), 1)
    add_start, add_length = map(int, add_info.split(',')) if ',' in add_info else (int(add_info), 1)
    return del_start, del_length, add_start, add_length


def main(parent):
    current_file, chunk_start, chunk_length = '', 0, 0
    checkstyle_output = None
    chunks = []
    passed = True
    for line in StringIO(subprocess.check_output(['git', 'diff', '-U0', parent], encoding='utf-8')):
        if line.startswith('diff --git'):
            pass
        elif line.startswith('index '):
            pass
        elif line.startswith('---'):
            if '/dev/null' in line:
                new_file = True
            else:
                new_file = False
            pass
        elif line.startswith('+++'):
            if line.startswith('+++ /dev/null'):
                continue  # Deleted file
            if len(chunks) > 0 and current_file != '':
                passed = check_chunks(checkstyle_output, chunks) and passed
            # Handle new file
            current_file = line[6:].strip()
            current_file = os.path.join('..', current_file)
            checkstyle_output = checkstyle(current_file)
            chunks = []
        elif line.startswith('@@'):
            # Handle chunk
            del_start, del_length, add_start, add_length = process_chunk_info(line)
            if add_length > 0:
                # Addition or replacement
                chunks.append((add_start, add_start + add_length))
            else:
                # Code removal. Check next line to ensure it didn't introduce an error
                chunks.append((del_start, del_start + 1))

    if len(chunks) > 0:
        passed = check_chunks(checkstyle_output, chunks) and passed

    return passed


if __name__ == '__main__':
    if 'BYPASS_CHECKSTYLE' in os.environ:
        sys.exit(0)
    if main(parent_commit):
        sys.exit(0)
    else:
        sys.exit(1)
