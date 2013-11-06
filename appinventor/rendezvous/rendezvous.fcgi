#!/usr/bin/python
from bottle import run,route,app,request,response,template,default_app,Bottle,debug,abort
from flup.server.fcgi import WSGIServer
from cStringIO import StringIO
import memcache

app = Bottle()
default_app.push(app)

@route('/', method='POST')
def store():
    c = memcache.Client(['127.0.0.1:11211',])
    key = request.POST.get('key')
    if not key:
        abort(404, 'No Key Specified')
    d = {}
    for k,v in request.POST.items():
        d[k] = v
    c.set('rr-%s' % key, d, 1800)
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return d

@route('/', method='OPTIONS')
def options():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

@route('/<key>')
def fetch(key):
    c = memcache.Client(['127.0.0.1:11211',])
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return c.get('rr-%s' % key)

debug(True)

##run(host='127.0.0.1', port=8080)
WSGIServer(app).run()

