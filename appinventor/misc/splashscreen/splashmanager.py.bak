#!/usr/bin/env python
#
# Simple tool for managing the Splash Screen in MIT App Inventor using
# the Remote API Service
#
# @author Jeffrey I. Schiller <jis@mit.edu>

import os
import sys
import getpass
from getopt import getopt, GetoptError

os.environ['SERVER_SOFTWARE'] = 'MIT SplashScreen Manager 1.0' # Googlism


def auth_func():
  return (raw_input('Email: '), getpass.getpass('Password: '))

def main():
    getlibdir()
    from google.appengine.ext import db
    from google.appengine.ext.remote_api import remote_api_stub
    from google.appengine.tools import appengine_rpc

    class SplashData(db.Model):
        version = db.IntegerProperty()
        content = db.TextProperty()
        width = db.IntegerProperty()
        height = db.IntegerProperty()

    filename = None
    host = 'localhost'
    getversion = False
    bumpversion = False
    setversion = None
    width = None
    height = None
    try:
        opts = getopt(sys.argv[1:], 'sbgf:w:h:H:', ['set','bump','getversion','file=','width=','height=','host='])
    except GetoptError:
        sys.stderr.write('Usage: splashmanager.py [-f splashfile -w width -h height] [-H host] \n')
        sys.exit(1)
    for opt in opts[0]:
        if opt == []:
            continue
        if len(opt) < 2:
            sys.stderr.write('Usage: splashmanager.py [-f splashfile -w width -h height] [-H host] \n')
            sys.exit(1)
        if opt[0] in ('--file', '-f'):
            filename = opt[1]
            whitelistname = opt[1]
        elif opt[0] in ('-H','--host'):
            host = opt[1]
        elif opt[0] in ('-g','--getversion'):
            getversion = True
        elif opt[0] in ('--bump','-b'):
            bumpversion = True
        elif opt[0] in ('-w','--width'):
            width = opt[1]
        elif opt[0] in ('-h','--host'):
            height = opt[1]
        elif opt[0] in ('-s','--set'):
            setversion = opt[1]

    if setversion and bumpversion:
        sys.stderr.write('Error: -s and -b are incompatible, use one or the other.\n')
        sys.stderr.write('Usage: splashmanager.py [-f splashfile -w width -h height] [-H host] \n')
        sys.exit(1)

    print 'Connecting to %s' % host
    if host == 'localhost':
        host = host + ':8888'
        secure = False
    else:
        secure = True

    remote_api_stub.ConfigureRemoteApi(None, '/remote_api', auth_func,
                                       servername=host,
                                       save_cookies=True, secure=secure,
                                       rpc_server_factory=appengine_rpc.HttpRpcServer)
    remote_api_stub.MaybeInvokeAuthentication()

    update = False
    sd = SplashData.all().fetch(1)[0]
    if getversion:
        print 'Splash Version: %d' % sd.version
    if setversion:
        sd.version = setversion
        update = True
    if filename:
        if width == None or height == None:
            sys.stderr.write('Must specify width and height while you provide a file.\n')
            sys.exit(1)
        data = open(filename).read()
        sd.content = data
        sd.width = int(width)
        sd.height = int(height)
        update = True
    if bumpversion:
        sd.version = sd.version + 1
        update = True
    if update:
        sd.put()

def getlibdir():
    '''Find the googl_appengine library directory'''
    from os.path import expanduser
    import ConfigParser
    doupdate = False
    config = ConfigParser.RawConfigParser()
    configfile = expanduser('~/.appinv_splashmanager')
    config.read(configfile)
    libdir = '/usr/local/google_appengine' # Default
    if config.has_section('splashmanager'):
        try:
            libdir = config.get('splashmanager', 'googlelibdir')
        except ConfigParser.NoOptionError:
            config.set('splashmanager', 'googlelibdir', libdir)
            doupdate = True
    else:
        config.add_section('splashmanager')
        doupdate = True
    if doupdate:
        f = open(configfile, 'w')
        config.write(f)
        f.close()
    sys.path.insert(0, libdir)
    sys.path.insert(1, libdir + '/lib/fancy_urllib')
    try:
        from google.appengine.ext import db
    except ImportError:
        newpath = raw_input('Google Python App Engine SDK Path [%s]: ' % libdir)
        if newpath == '':
            newpath = libdir
        libdir = newpath
        config.set('splashmanager', 'googlelibdir', libdir)
        f = open(configfile, 'w')
        config.write(f)
        f.close()
        print 'Location of Google Library Directory Saved, exiting, try again...'
        sys.exit(0)

# The stuff below is to permit the prompt for the library dir to
# use filename completion....

class Completer(object):

    def _listdir(self, root):
        "List directory 'root' appending the path separator to subdirs."
        res = []
        for name in os.listdir(root):
            path = os.path.join(root, name)
            if os.path.isdir(path):
                name += os.sep
            res.append(name)
        return res

    def _complete_path(self, path=None):
        "Perform completion of filesystem path."
        if not path:
            return self._listdir('.')
        dirname, rest = os.path.split(path)
        tmp = dirname if dirname else '.'
        res = [os.path.join(dirname, p)
                for p in self._listdir(tmp) if p.startswith(rest)]
        # more than one match, or single match which does not exist (typo)
        if len(res) > 1 or not os.path.exists(path):
            return res
        # resolved to a single directory, so return list of files below it
        if os.path.isdir(path):
            return [os.path.join(path, p) for p in self._listdir(path)]
        # exact file match terminates this completion
        return [path + ' ']

    def complete_filename(self, args):
        "Completions for the 'extra' command."
        if not args:
            return self._complete_path('.')
        # treat the last arg as a path and complete it
        return self._complete_path(args[-1])

    def complete(self, text, state):
        "Generic readline completion entry point."
        buffer = readline.get_line_buffer()
        line = buffer.split()
        return (self.complete_filename(line) + [None])[state]

comp = Completer()

try:
    import readline
except ImportError:
    print "Module readline not available."
else:
    import rlcompleter
    readline.set_completer_delims(' \t\n;')
    readline.parse_and_bind("tab: complete")
    readline.set_completer(comp.complete)

if __name__ == '__main__':
    main()

