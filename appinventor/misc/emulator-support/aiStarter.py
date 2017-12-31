#!/usr/bin/python
import os
import platform
import re
import subprocess
import sys

from bottle import run, route, response, default_app, Bottle

# from flup.server.fcgi import WSGIServer
# from cStringIO import StringIO
# import memcache

VERSION = "2.2"

# app = Bottle()
# default_app.push(app)

_OS = platform.system()
if _OS == 'Linux':      # Linux
    PLATDIR = '/usr/google/appinventor/commands-for-Appinventor'
elif _OS == 'Darwin':   # MacOS
    PLATDIR = '/Applications/AppInventor/commands-for-Appinventor'
elif _OS == 'Windows':  # Windows
    PLATDIR = os.path.join(os.environ["ProgramFiles"], 'AppInventor', 'commands-for-Appinventor')
else:                   # Unknown OS
    sys.exit(1)
print('AppInventor tools located here: "{}"'.format(PLATDIR))
PLATDIR += os.sep  # Append path separator at the end


@route('/ping/')
def ping():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    return '{{ "status" : "OK", "version" : "{}" }}'.format(VERSION)


@route('/utest/')
def utest():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return '{{ "status" : "OK", "device" : "{}", "version" : "{}" }}'.format(device, VERSION)
    else:
        return '{{ "status" : "NO", "version" : "{}" }}'.format(VERSION)


@route('/start/')
def start():
    subprocess.call(PLATDIR + "run-emulator ", shell=True, close_fds=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''


@route('/emulatorreset/')
def emulatorreset():
    subprocess.call(PLATDIR + "reset-emulator ", shell=True, close_fds=True)
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
        return '{{ "status" : "OK", "device" : "{}", "version" : "{}" }}'.format(device, VERSION)
    else:
        return '{{ "status" : "NO", "version" : "{}" }}'.format(VERSION)


@route('/ucheck/')
def ucheck():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return '{{ "status" : "OK", "device" : "{}", "version" : "{}" }}'.format(device, VERSION)
    else:
        return '{{ "status" : "NO", "version" : "{}" }}'.format(VERSION)


@route('/reset/')
def reset():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    killadb()
    killemulator()
    return '{{ "status" : "OK", "version" : "{}" }}'.format(VERSION)


@route('/replstart/:device')
def replstart(device=None):
    print("Device = {}".format(device))
    try:
        subprocess.check_output(PLATDIR + "adb -s {} forward tcp:8001 tcp:8001".format(device),
                                shell=True, close_fds=True)
        if re.match('.*emulat.*', device):  # Only fake the menu key for the emulator
            subprocess.check_output(PLATDIR + "adb -s {} shell input keyevent 82".format(device),
                                    shell=True, close_fds=True)
        subprocess.check_output(
            PLATDIR + "adb -s {} shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true".format(device),
            shell=True, close_fds=True)
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
        return ''
    except subprocess.CalledProcessError as e:
        print("Problem starting companion app : status {}\n".format(e.returncode))
        return ''


def checkrunning(emulator):
    try:
        result = subprocess.check_output(PLATDIR + 'adb devices', shell=True, close_fds=True)
        lines = result.splitlines()
        for line in lines[1:]:
            if emulator:
                m = re.search('^(.*emulator-[1-9]+)\\t+device.*', line)
            else:
                if re.search('^(.*emulator-[1-9]+)\\t+device.*', line):  # We are an emulator
                    continue  # Skip it
                m = re.search('^([A-z0-9.:]+.*?)\\t+device.*', line)
            if m:
                break
        if m:
            return m.group(1)
        return False
    except subprocess.CalledProcessError as e:
        print("Problem checking for devices : status {}\n".format(e.returncode))
        return False


def killadb():
    """Time to nuke adb!"""
    try:
        subprocess.check_output(PLATDIR + "adb kill-server", shell=True, close_fds=True)
        print("Killed adb\n")
    except subprocess.CalledProcessError as e:
        print("Problem stopping adb : status {}\n".format(e.returncode))
        return ''


def killemulator():
    try:
        subprocess.check_output(PLATDIR + "kill-emulator", shell=True)
        print("Killed emulator\n")
    except subprocess.CalledProcessError as e:
        print("Problem stopping emulator : status {}\n".format(e.returncode))
        return ''


def shutdown():
    try:  # Be quiet...
        killadb()
        killemulator()
    except:
        pass


if __name__ == '__main__':
    import atexit

    atexit.register(shutdown)
    run(host='127.0.0.1', port=8004)
    # WSGIServer(app).run()
