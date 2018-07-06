#!/usr/bin/python
import os
import platform
import re
import subprocess
import sys

from bottle import run, route, response

VERSION = '2.2'

OS = platform.system()

if OS == 'Linux':     # Linux
    PLATDIR = '/usr/google/appinventor/commands-for-Appinventor'
elif OS == 'Darwin':  # MacOS
    PLATDIR = '/Applications/AppInventor/commands-for-Appinventor'
elif OS == 'Windows': # Windows
    PLATDIR = os.path.join(os.environ["ProgramFiles"], 'AppInventor', 'commands-for-Appinventor')
else:                 # Unknown OS
    sys.exit(1)

# Path to executables
ADB = PLATDIR + os.path.sep + 'adb'
RUN_EMULATOR = PLATDIR + os.path.sep + 'run-emulator'
RESET_EMULATOR = PLATDIR + os.path.sep + 'reset-emulator'
KILL_EMULATOR = PLATDIR + os.path.sep + 'kill-emulator'


@route('/ping/')
def ping():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    return {
        "status": "OK",
        "version": VERSION
    }


@route('/utest/')
def utest():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return {
            "status": "OK",
            "device": device,
            "version": VERSION
        }
    else:
        return {
            "status": "NO",
            "version": VERSION
        }


@route('/start/')
def start():
    subprocess.call(f'"{RUN_EMULATOR}"', shell=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''


@route('/emulatorreset/')
def emulatorreset():
    subprocess.call(f'"{RESET_EMULATOR}"', shell=True)
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
        return {
            "status": "OK",
            "device": device,
            "version": VERSION
        }
    else:
        return {
            "status": "NO",
            "version": VERSION
        }


@route('/ucheck/')
def ucheck():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    device = checkrunning(False)
    if device:
        return {
            "status": "OK",
            "device": device,
            "version": VERSION
        }
    else:
        return {
            "status": "NO",
            "version": VERSION
        }


@route('/reset/')
def reset():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    shutdown()
    return {
        "status": "OK",
        "version": VERSION
    }


@route('/replstart/:device')
def replstart(device=None):
    print('Device =', device)
    try:
        subprocess.check_output(f'"{ADB}" -s {device} forward tcp:8001 tcp:8001', shell=True)
        if re.match('emulator.*', device):  # Only fake the menu key for the emulator
            subprocess.check_output(f'"{PLATDIR}adb" -s {device} shell input keyevent 82', shell=True)
        subprocess.check_output(
            f'"{ADB}" -s {device} shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true',
            shell=True)
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
        return ''
    except subprocess.CalledProcessError as e:
        print('Problem starting companion app : status', e.returncode)
        return ''


def checkrunning(emulator):
    try:
        match = None
        result = subprocess.check_output(f'"{ADB}" devices', shell=True)
        lines = result.splitlines()
        for line in lines[1:]:
            line = str(line, 'utf-8')
            if line:
                if emulator:
                    match = re.search(r'^(emulator-\d+)\s+device$', line)
                else:
                    if re.search(r'^(emulator-\d+)\s+device$', line): # We are emulator
                        continue                                      # Skip it
                    match = re.search(r'^([\w\d]+)\s+device$', line)
                if match:
                    break
        if match:
            return match.group(1)
        return False
    except subprocess.CalledProcessError as e:
        print('Problem checking for devices : status', e.returncode)
        return False


def killadb():
    try:
        subprocess.check_output(f'"{ADB}" kill-server', shell=True)
        print('Killed adb')
    except subprocess.CalledProcessError as e:
        print('Problem stopping adb : status', e.returncode)


def killemulator():
    try:
        subprocess.check_output(f'"{KILL_EMULATOR}"', shell=True)
        print('Killed emulator')
    except subprocess.CalledProcessError as e:
        print('Problem stopping emulator : status', e.returncode)


def shutdown():
    try:
        killemulator()
        killadb()
    except:
        pass


if __name__ == '__main__':
    print('AppInventor tools located here:', PLATDIR, '\n')

    import atexit
    atexit.register(shutdown)

    run(host='127.0.0.1', port=8004)
