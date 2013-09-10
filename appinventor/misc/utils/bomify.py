#!/usr/bin/env python
#
# This script takes its input argument and adds the Byte Order Marker (BOM)
# To indicate that the file contains UTF-8
#
# Does nothing if the BOM is already in place

import os
import sys
import mmap
from getopt import getopt, GetoptError

BOM = '\xef\xbb\xbf'                    # BOM for UTF-8

def main():
    filename = sys.argv[1]
    fd = os.open(filename, os.O_RDWR)
    header = os.read(fd, 3)
    if header == BOM:
        return                          # Nothing to do, BOM already present
    l = os.lseek(fd, 0, 2)
    os.write(fd, '\x00\x00\x00')
    v = mmap.mmap(fd, l + 3, access=mmap.ACCESS_WRITE)
    v[3:] = v[0:-3]                     # Shift
    v[0:3] = BOM
    v.close()
    os.close(fd)

if __name__ == '__main__':
    main()

