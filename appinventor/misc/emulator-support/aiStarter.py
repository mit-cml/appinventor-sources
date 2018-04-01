#!/usr/bin/python
import os
import platform
import re
import subprocess
import sys

from bottle import run, route, response

VERSION = "2.2"

OS = platform.system()

if OS == 'Linux':       # Linux
    PLATDIR = '/usr/google/appinventor/commands-for-Appinventor'
elif OS == 'Darwin':    # MacOS
    PLATDIR = '/Applications/AppInventor/commands-for-Appinventor'
elif OS == 'Windows':   # Windows
    PLATDIR = os.path.join(os.environ["ProgramFiles"], 'AppInventor', 'commands-for-Appinventor')
else:                   # Unknown OS
    sys.exit(1)

PLATDIR += os.path.sep  # Append path separator at the end


@route('/ping/')
def ping():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    response.headers['Content-Type'] = 'application/json'
    # return {
    # "status" : "OK", "version" : "{VERSION}" }}'
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
    subprocess.call(f'"{PLATDIR}run-emulator"', shell=True)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''


@route('/emulatorreset/')
def emulatorreset():
    subprocess.call(f'"{PLATDIR}reset-emulator"', shell=True)
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
            "device": device, "version": VERSION
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
    print(f"Device = {device}")
    try:
        subprocess.check_output(f'"{PLATDIR}adb" -s {device} forward tcp:8001 tcp:8001', shell=True)
        if re.match('.*emulat.*', device):  # Only fake the menu key for the emulator
            subprocess.check_output(f'"{PLATDIR}adb" -s {device} shell input keyevent 82', shell=True,)
        subprocess.check_output(
            f'"{PLATDIR}adb" -s {device} shell am start -a android.intent.action.VIEW -n edu.mit.appinventor.aicompanion3/.Screen1 --ez rundirect true',
            shell=True)
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
        return ''
    except subprocess.CalledProcessError as e:
        print(f"Problem starting companion app : status {e.returncode}\n")
        return ''


def checkrunning(emulator):
    try:
        result = subprocess.check_output(f'"{PLATDIR}adb" devices', shell=True)
        lines = result.splitlines()
        for line in lines[1:]:
            if line:
                l = str(line, 'utf-8')  # convert byte to string
                if emulator:
                    m = re.search(r'^(emulator-\d+)\s+device.*', l)
                else:
                    if re.search(r'^(emulator-\d+)\s+device.*', l):  # We are an emulator
                        continue  # Skip it
                    m = re.search(r'^([\w\d]+)\s+device.*', l)
                if m:
                    break
        if m:
            return m.group(1)
        return False
    except subprocess.CalledProcessError as e:
        print(f"Problem checking for devices : status {e.returncode}\n")
        return False


def killadb():
    """Time to nuke adb!"""
    try:
        subprocess.check_output(f'"{PLATDIR}adb" kill-server', shell=True)
        print("Killed adb\n")
    except subprocess.CalledProcessError as e:
        print(f"Problem stopping adb : status {e.returncode}\n")
        return ''


def killemulator():
    try:
        subprocess.check_output(f'"{PLATDIR}kill-emulator"', shell=True)
        print("Killed emulator\n")
    except subprocess.CalledProcessError as e:
        print(f"Problem stopping emulator : status {e.returncode}\n")
        return ''


def shutdown():
    killadb()
    killemulator()


if __name__ == '__main__':
    print(f'AppInventor tools located here: "{PLATDIR}"')

    import atexit

    atexit.register(shutdown)
    run(host='127.0.0.1', port=8004)
