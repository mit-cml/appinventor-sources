#!/usr/bin/python
from bottle import run,route,app,request,response,template,default_app,Bottle,debug,abort
import sys
import os
#from flup.server.fcgi import WSGIServer
#from cStringIO import StringIO
#import memcache

app = Bottle()
default_app.push(app)

platform = os.uname()[0]
if platform == 'Linux':
    PLATDIR = '/usr/google/appinventor/'
elif platform == 'Darwin':               # MacOS
    PLATDIR = '/Applications/AppInventor/'
else:                                   # Need to add Windows
    sys.exit(1)

@route('/start/')
def start():
    os.system(PLATDIR + "commands-for-Appinventor/run-emulator ")
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

@route('/check/')
def check():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    if checkrunning():
        return '"OK"'
    else:
        return '"NO"'

@route('/replstart/')
def replstart():
    os.system(PLATDIR + "commands-for-Appinventor/adb forward tcp:8001 tcp:8001")
    os.system(PLATDIR + "commands-for-Appinventor/adb shell input keyevent 82")
    os.system(PLATDIR + "commands-for-Appinventor/adb shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1")
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

def checkrunning():
    import subprocess
    import re
    result = subprocess.check_output(PLATDIR + 'commands-for-Appinventor/adb devices', shell=True)
    m = re.search('^.*emula.*device.*', result, re.MULTILINE)
    if m:
        return True
    return False

if __name__ == '__main__':
    run(host='127.0.0.1', port=8004)
    ##WSGIServer(app).run()

