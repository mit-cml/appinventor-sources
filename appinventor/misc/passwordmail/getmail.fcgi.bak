#!/usr/bin/python
from bottle import run,route,app,request,response,template,default_app,Bottle,debug,abort
from flup.server.fcgi import WSGIServer
from email.utils import formatdate
from email.parser import Parser
import smtplib
from email.charset import add_charset
import pika

from message_pb2 import Message

app = Bottle()
default_app.push(app)

def init():
    global channel
    conn = pika.BlockingConnection()
    channel = conn.channel()

init()

@route('/', method='POST')
def store():
    d = {}
    for k,v in request.POST.items():
        d[k] = v.decode('utf-8')
    message = Message()
    message.email = d['email']
    message.url = d['url']
    message.locale = d.get('locale', 'en')
    if d.get('pass') != 'changeme':
        return ''
    channel.basic_publish('', 'passmail', message.SerializeToString(),
                          pika.BasicProperties(content_type='text/plain',
                                               delivery_mode=1))
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

@route('/', method='OPTIONS')
def options():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Headers'] = 'origin, content-type'
    return ''

debug(True)



##run(host='127.0.0.1', port=8080)
WSGIServer(app).run()

