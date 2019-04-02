# Nose program for testing (some) grizzled.file classes/functions

# ---------------------------------------------------------------------------
# Imports
# ---------------------------------------------------------------------------

from grizzled.file import *
import os
import tempfile
import atexit

# ---------------------------------------------------------------------------
# Classes
# ---------------------------------------------------------------------------

class TestFilePackage(object):

    def testUnlinkQuietly(self):
        fd, path = tempfile.mkstemp()
        os.unlink(path)

        try:
            os.unlink(path)
            assert False, 'Expected an exception'
        except OSError:
            pass

        unlink_quietly(path)

    def testRecursivelyRemove(self):
        path = tempfile.mkdtemp()
        print('Created directory "{0}"'.format(path))

        # Create some files underneath

        touch([os.path.join(path, 'foo'),
               os.path.join(path, 'bar')])

        try:
            os.unlink(path)
            assert False, 'Expected an exception'
        except OSError:
            pass

        recursively_remove(path)

    def testTouch(self):
        path = tempfile.mkdtemp()
        atexit.register(recursively_remove, path)
        f = os.path.join(path, 'foo')
        assert not os.path.exists(f)
        touch(f)
        assert os.path.exists(f)
