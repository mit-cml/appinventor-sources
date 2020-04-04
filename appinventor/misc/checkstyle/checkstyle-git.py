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
    return StringIO(subprocess.check_output(['java', '-jar', 'lib/checkstyle/checkstyle.jar', '-c',
                                             'lib/checkstyle/appinventor_checks.xml', current_file],
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


def main(parent):
    current_file, chunk_start, chunk_length = '', 0, 0
    checkstyle_output = None
    chunks = []
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
                check_chunks(checkstyle_output, chunks)
            # Handle new file
            current_file = line[6:].strip()
            chunk_start, chunk_length = 0, 0
            current_file = os.path.join('..', current_file)
            checkstyle_output = checkstyle(current_file)
            chunks = []
        elif line.startswith('@@'):
            if chunk_length > 0:
                chunks.append((chunk_start, chunk_start + chunk_length))
            # Handle chunk
            parts = line.split(' ')
            chunk_info = parts[2][1:]
            if ',' in chunk_info:
                x, y = chunk_info.split(',')
                chunk_start = int(x)
                chunk_length = int(y)
            else:
                chunk_start = int(chunk_info)
                chunk_length = 1
    if chunk_length > 0:
        chunks.append((chunk_start, chunk_start + chunk_length))
    if len(chunks) > 0:
        return check_chunks(checkstyle_output, chunks)
    return True


if __name__ == '__main__':
    if main(parent_commit):
        sys.exit(0)
    else:
        sys.exit(-1 if 'BYPASS_CHECKSTYLE' not in os.environ else 0)
