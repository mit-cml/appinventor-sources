#!/usr/bin/python
from bottle import run,route,app,request,response,template,default_app,Bottle,debug,abort
import sys
import os
import platform
import subprocess
import re
#from flup.server.fcgi import WSGIServer
#from cStringIO import StringIO
#import memcache

app = Bottle()
default_app.push(app)

VERSION = "2.2"

platforms = platform.uname()[0]
print "Platform = %s" % platforms
if platforms == 'Windows':               # Windows
    PLATDIR = os.environ["ProgramFiles"]
    PLATDIR = '"' + PLATDIR + '"'
    print "AppInventor tools located here: %s" % PLATDIR
else:
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
    subprocess.call(PLATDIR + "\\AppInventor\\commands-for-Appinventor\\run-emulator ", shell=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

@route('/emulatorreset/')
def emulatorreset():
    subprocess.call(PLATDIR + "\\AppInventor\\commands-for-Appinventor\\reset-emulator ", shell=True)
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
    killemulator()
    return '{ "status" : "OK", "version" : "%s" }' % VERSION

@route('/replstart/:device')
def replstart(device=None):
    print "Device = %s" % device
    try:
        subprocess.check_output((PLATDIR + "\\AppInventor\\commands-for-Appinventor\\adb -s %s forward tcp:8001 tcp:8001") % device, shell=True)
        if re.match('.*emulat.*', device): #  Only fake the menu key for the emulator
            subprocess.check_output((PLATDIR + "\\AppInventor\\commands-for-Appinventor\\adb -s %s shell input keyevent 82") % device, shell=True)
        subprocess.check_output((PLATDIR + "\\AppInventor\\commands-for-Appinventor\\adb -s %s shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true") % device, shell=True)
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
        return ''
    except subprocess.CalledProcessError as e:
        print "Problem starting companion app : status %i\n" % e.returncode
        return ''


def checkrunning(emulator):
    try:
        result = subprocess.check_output(PLATDIR + "\\AppInventor\\commands-for-Appinventor\\adb devices", shell=True)
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
    except subprocess.CalledProcessError as e:
        print "Problem checking for devices : status %i\n" % e.returncode
        return False

def killadb():
    try:
        subprocess.check_output(PLATDIR + "\\AppInventor\\commands-for-Appinventor\\adb kill-server", shell=True)
        print "Killed adb\n"
    except subprocess.CalledProcessError as e:
        print "Problem stopping adb : status %i\n" % e.returncode
        return ''

def killemulator():
    try:
        subprocess.check_output(PLATDIR + "\\AppInventor\\commands-for-Appinventor\\kill-emulator", shell=True)
        print "Killed emulator\n"
    except subprocess.CalledProcessError as e:
        print "Problem stopping emulator : status %i\n" % e.returncode
        return ''

def shutdown():
    try:                                # Be quiet...
        killadb()
        killemulator()
    except:
        pass

if __name__ == '__main__':
    import atexit
    atexit.register(shutdown)
    run(host='127.0.0.1', port=8004)
    ##WSGIServer(app).run()

