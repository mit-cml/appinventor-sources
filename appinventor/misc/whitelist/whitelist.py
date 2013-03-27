#!/usr/bin/env python
#
# Tool for managing the App Inventor whitelist. The whitelist
# is stored in the App Engine Data Store. However it is only
# consulted if "user.whitelist" is set to true in appengine-web.xml
#

# WhiteListData, an element of the whitelist. This class definition
# *MUST* be congruent to the Java class WhiteListData in StoredData.java
import os
import sys
import getpass
from getopt import getopt, GetoptError

os.environ['SERVER_SOFTWARE'] = 'MIT Whitelist Generator 1.0' # Googlism

def auth_func():
  return (raw_input('Email: '), getpass.getpass('Password: '))

def main():
    getlibdir()
    from google.appengine.ext import db
    from google.appengine.ext.remote_api import remote_api_stub
    from google.appengine.tools import appengine_rpc

    class WhiteListData(db.Model):
        emailLower = db.StringProperty();

    whitelistname = 'whitelist'
    host = 'localhost'
    getonly = False
    try:
        opts = getopt(sys.argv[1:], 'n:h:d', ['name=','getonly'])
    except GetoptError:
        sys.stderr.write('Usage: whitelist.py [-n whitelistfile] [-h host] [-d] [--getonly]\n')
        sys.exit(1)
    for opt in opts[0]:
        if opt == []:
            continue
        if len(opt) < 2:
            sys.stderr.write('Usage: whitelist.py [-n whitelistfile] [-h host] [-d] [--getonly]\n')
            sys.exit(1)
        if opt[0] in ('-n', '--name'):
            whitelistname = opt[1]
        elif opt[0] == '-h':
            host = opt[1]
        elif opt[0] == '-d':
            host = 'localhost'
        elif opt[0] == '--getonly':
            getonly = True

    print 'Using %s for input' % whitelistname
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

    input_people = open(whitelistname).readlines()
    input_people = [x.strip() for x in input_people]

    installed_people = []
    c = None
    while True:
        q = WhiteListData.gql("")
        if c:
            q = q.with_cursor(c)
        z = q.fetch(500)
        c = q.cursor()
        installed_people += z
        if len(z) < 500:
            break

    if getonly:
        print 'Getonly set, returning existing whitelist with no changes'
        for person in installed_people:
            print person.emailLower
        return

    WHITE = {}
    for email in input_people:
        WHITE[unicode(email)] = [0, None]
    for person in installed_people:
        email = person.emailLower
        if WHITE.has_key(email):
            WHITE[email] = [2, person]
        else:
            WHITE[email] = [1, person]

    # Now we go through the dictionary. Remove people in state 1
    # and add people in state 0

    for (email, z) in WHITE.items():
        state, person = z
        if state == 0:
            v = WhiteListData()
            v.emailLower = email
            v.put()
            print 'Added %s' % email
        elif state == 1:
            person.delete()
            print 'Removed %s' % email


def getlibdir():
    '''Find the googl_appengine library directory'''
    from os.path import expanduser
    import ConfigParser
    doupdate = False
    config = ConfigParser.RawConfigParser()
    configfile = expanduser('~/.appinv_whitelist')
    config.read(configfile)
    libdir = '/usr/local/google_appengine' # Default
    if config.has_section('whitelist'):
        try:
            libdir = config.get('whitelist', 'googlelibdir')
        except ConfigParser.NoOptionError:
            config.set('whitelist', 'googlelibdir', libdir)
            doupdate = True
    else:
        config.add_section('whitelist')
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
        config.set('whitelist', 'googlelibdir', libdir)
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

