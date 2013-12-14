#!/usr/bin/python
from bottle import run,route,app,request,response,template,default_app,Bottle,debug,abort
import sys
import os
import subprocess
import re
#from flup.server.fcgi import WSGIServer
#from cStringIO import StringIO
#import memcache

VERSION = "2.1"

app = Bottle()
default_app.push(app)

platform = os.uname()[0]
if platform == 'Linux':
    PLATDIR = '/usr/google/appinventor/'
elif platform == 'Darwin':               # MacOS
    PLATDIR = '/Applications/AppInventor/'
else:                                   # Need to add Windows
    sys.exit(1)

@route('/ping/')
def ping():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    return '{ "status" : "OK", "version" : "%s" }' % VERSION

@route('/utest/')
def utest():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return '{ "status" : "OK", "device" : "%s", "version" : "%s" }' % (device, VERSION)
    else:
        return '{ "status" : "NO", "version" : "%s" }' % VERSION

@route('/start/')
def start():
    subprocess.call(PLATDIR + "commands-for-Appinventor/run-emulator ", shell=True, close_fds=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

@route('/echeck/')
def echeck():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(True)
    if device:
        return '{ "status" : "OK", "device" : "%s", "version" : "%s"}' % (device, VERSION)
    else:
        return '{ "status" : "NO", "version" : "%s" }' % VERSION

@route('/ucheck/')
def ucheck():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return '{ "status" : "OK", "device" : "%s", "version" : "%s"}' % (device, VERSION)
    else:
        return '{ "status" : "NO", "version" : "%s" }' % VERSION

@route('/reset/')
def reset():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    killadb()
    return '{ "status" : "OK", "version" : "%s" }' % VERSION

@route('/replstart/:device')
def replstart(device=None):
    print "Device = %s" % device
    subprocess.check_output((PLATDIR + "commands-for-Appinventor/adb -s %s forward tcp:8001 tcp:8001") % device, shell=True, close_fds=True)
    if re.match('.*emulat.*', device): #  Only fake the menu key for the emulator
        subprocess.check_output((PLATDIR + "commands-for-Appinventor/adb -s %s shell input keyevent 82") % device, shell=True, close_fds=True)
    subprocess.check_output((PLATDIR + "commands-for-Appinventor/adb -s %s shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true") % device, shell=True, close_fds=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

def checkrunning(emulator):
    result = subprocess.check_output(PLATDIR + 'commands-for-Appinventor/adb devices', shell=True, close_fds=True)
    lines = result.split('\n')
    for line in lines[1:]:
        if emulator:
            m = re.search('^(.*emulator-[1-9]+)\t+device.*', line)
        else:
            if re.search('^(.*emulator-[1-9]+)\t+device.*', line): # We are an emulator
                continue                                           # Skip it
            m = re.search('^([A-z0-9.:]+.*?)\t+device.*', line)
        if m:
            break
    if m:
        return m.group(1)
    return False

def killadb():
    """Time to nuke adb!"""
    subprocess.check_output(PLATDIR + "commands-for-Appinventor/adb kill-server", shell=True, close_fds=True)
    sys.stdout.write("Killed adb\n")
    subprocess.check_output(PLATDIR + "commands-for-Appinventor/kill-emulator", shell=True, close_fds=True)
    sys.stdout.write("Killed emulator\n")

def shutdown():
    killadb()

if __name__ == '__main__':
    import atexit
    atexit.register(shutdown)
    run(host='127.0.0.1', port=8004)
    ##WSGIServer(app).run()

