from subprocess import Popen,PIPE
import time
import signal
import os
import pwd
from logging.handlers import SysLogHandler
import logging

server = None
stopping = False

def main():
    '''Start the buildserver and keep track of it, i.e., restart if it fails'''
    global server, log
    signal.signal(signal.SIGTERM, sighandler)
    signal.signal(signal.SIGQUIT, sighandler)
    signal.signal(signal.SIGINT, sighandler)
    count = 0
    buildpwd = pwd.getpwnam('buildserver')
    os.setgid(buildpwd.pw_gid)
    os.setuid(buildpwd.pw_uid)
    os.chdir('/home/buildserver')
    log = logging.getLogger()
    log.addHandler(SysLogHandler('/dev/log'))
    log.handlers[0].setFormatter(logging.Formatter('BUILDSERVER: %(levelname)s: %(message)s'))
    log.info('Buildserver Monitor Starting')
    while True:
        ts = time.time()
        server = Popen(['./launch-buildserver',], close_fds=True)
        while not server.poll():     # While we are still running
            time.sleep(10)      # Sleep for ten seconds
        nts = time.time()
        code = server.wait()
        server = None
        log.warn('Build Server Terminated with code = %d' % code)
        if (nts - ts) < 60:     # It was running less then a minute?
            log.error('Build Server Terminated within 1 minute of start!')
            count += 1          # Bump count
            if count > 4:
                log.critical('Count of looping buildserver > 4, just hanging...')
                while True:
                    signal.pause()
                    
def sighandler(*args):
    if server:
        server.terminate()
    log.warn('Build Server Shutdown')
    logging.shutdown()
    raise SystemExit            # bye bye

if __name__ == '__main__':
    main()

                
