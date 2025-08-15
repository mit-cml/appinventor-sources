#!/usr/bin/python
# -*- coding: utf-8; fill-column: 120 -*-
import os
import platform
import re
import subprocess
import sys
import config

from bottle import run, route, response

VERSION = '%d.%d.%d%s' % (config.ANDROID_PLATFORM, config.COMPANION_VERSION, config.MINOR_VERSION, config.BUILD_EXTRAS)

PLATDIR = os.path.abspath(os.path.dirname(sys.argv[0]))

# Path to executables
ADB = os.path.join(PLATDIR, 'from-Android-SDK', 'platform-tools', 'adb')
RUN_EMULATOR = os.path.join(PLATDIR, 'run-emulator')
RESET_EMULATOR = os.path.join(PLATDIR, 'reset-emulator')
KILL_EMULATOR = os.path.join(PLATDIR, 'kill-emulator')


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
    subprocess.call(RUN_EMULATOR, shell=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''


@route('/emulatorreset/')
def emulatorreset():
    subprocess.call(RESET_EMULATOR, shell=True)
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
        subprocess.check_output('"%s" -s %s forward tcp:8001 tcp:8001' % (ADB, device), shell=True)
        if re.match('emulator.*', device):  # Only fake the menu key for the emulator
            subprocess.check_output('"%s" -s %s shell input keyevent 82' % (ADB, device), shell=True)
        subprocess.check_output(
            '"%s" -s %s shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true' % (ADB, device),
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
        result = subprocess.check_output('"%s" devices' % ADB, shell=True)
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
        subprocess.check_output('"%s" kill-server' % ADB, shell=True)
        print('Killed adb')
    except subprocess.CalledProcessError as e:
        print('Problem stopping adb : status', e.returncode)


def killemulator():
    try:
        subprocess.check_output('"%s"' % KILL_EMULATOR, shell=True)
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
    print('App Inventor version:', VERSION, '\n')
    print('Architecture:', platform.machine(), '\n')
    print('AppInventor tools located here:', PLATDIR, '\n')
    print('ADB path:', ADB)

    import atexit
    atexit.register(shutdown)

    run(host='127.0.0.1', port=8004)
