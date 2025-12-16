import pika
import sys
from email.utils import formatdate
from email.parser import Parser
import smtplib
from getpass import getpass
from email.charset import add_charset

from message_pb2 import Message

parser = Parser()
password = getpass()

def on_message(channel, method_frame, header_frame, body):
    sys.stderr.write("%s..." % method_frame.delivery_tag)
    m = Message.FromString(body)
    print 'New Message: email = %s, url = %s, locale = %s' % (m.email, m.url, m.locale)
    sendmail(m.email, m.url, m.locale)
    channel.basic_ack(delivery_tag=method_frame.delivery_tag)

class _setupconsume(object):

    def __init__(self, queue, handler):
        self.queue = queue
        self.handler = handler

    def start(self, arg):
        print 'Queue %s Declared and Ready' % self.queue
        channel.basic_consume(self.handler, self.queue)

def sendmail(email, url, locale):
    t = templates.get(locale, None)
    if t == None:
        t = templates.get('en')
    if locale != 'en':
        url += '?locale=' + locale
    m = parser.parsestr((t % (email, url)).encode('utf8'))
    m['Date'] = formatdate(localtime=True)
    s = smtplib.SMTP('osiris.mit.edu', 587)
    s.starttls()
    s.login('jis', password)
    try:
        retval = s.sendmail('appinventor@osiris.mit.edu', [email,], str(m))
    except:
        import traceback
        traceback.print_exc()
    s.quit()

templates = { 'en' : '''From: MIT App Inventor System <appinventor@mit.edu>
To: %s
Subject: Password Reset for you MIT App Inventor Account

You have requested a new password for your MIT App Inventor Account.
Use the link below to set (or reset) your password. After you click on
this link you will be asked to provide a new password. Once you do that
you will be logged in to App Inventor.

    Your Link is: %s

Happy Inventing!

The MIT App Inventor Team
''',
              'zh_TW' :  u'To: %s\nContent-Type: text/plain; charset=utf-8; format=flowed\nContent-Transfer-Encoding: 8bit\nSubject: =?UTF-8?B?6K+35qOA5p+l5oKo55qE55S15a2Q6YKu5Lu277yM5oiR5Lus5bey57uP5Y+R6YCB6ZO+5o6l57uZ5L2g6K6+572u5L2g55qE5Yid6K6+5a+G56CBL+aUueWPmOS9oOeahOaXp+WvhueggeOAgg==?=\n\n\u4f60\u5411\u7cfb\u7edf\u8981\u6c42\u8bbe\u7f6e\u4f60 MIT App Inventor \u8d26\u6237\u7684\u521d\u8bbe\u5bc6\u7801/\u66f4\u6539\u65e7\u7684\u5bc6\u7801\u3002\n\u4f7f\u7528\u4e0b\u9762\u7684\u94fe\u63a5\u8bbe\u7f6e\u521d\u8bbe\u6216\u91cd\u7f6e\u60a8\u7684\u5bc6\u7801\u3002\u4f60\u70b9\u51fb\n\u8fd9\u4e2a\u94fe\u63a5\u540e\uff0c\u4f60\u5c06\u88ab\u8981\u6c42\u63d0\u4f9b\u4e00\u4e2a\u65b0\u5bc6\u7801\u3002\u4e00\u65e6\u4f60\u8f93\u5165\u4e86\u5bc6\u7801\n\uff0c\u4f60\u4f1a\u6210\u529f\u767b\u5f55\u5230 Apple Inventor \u3002\n\u3000\u3000\n\u3000\u3000\u4f60\u7684\u94fe\u63a5\u662f: %s\n\u3000\u3000\n\u3000\u3000\u5feb\u4e50\u521b\u9020!\nMIT App Inventor \u56e2\u961f',
              'zh_CN' : u'To: %s\nContent-Type: text/plain; charset=utf-8; format=flowed\nContent-Transfer-Encoding: 8bit\nSubject: =?UTF-8?B?6K+35qOA5p+l5oKo55qE55S15a2Q6YKu5Lu277yM5oiR5Lus5bey57uP5Y+R6YCB6ZO+5o6l57uZ5L2g6K6+572u5L2g55qE5Yid6K6+5a+G56CBL+aUueWPmOS9oOeahOaXp+WvhueggeOAgg==?=\n\n\u4f60\u5411\u7cfb\u7edf\u8981\u6c42\u8bbe\u7f6e\u4f60 MIT App Inventor \u8d26\u6237\u7684\u521d\u8bbe\u5bc6\u7801/\u66f4\u6539\u65e7\u7684\u5bc6\u7801\u3002\n\u4f7f\u7528\u4e0b\u9762\u7684\u94fe\u63a5\u8bbe\u7f6e\u521d\u8bbe\u6216\u91cd\u7f6e\u60a8\u7684\u5bc6\u7801\u3002\u4f60\u70b9\u51fb\n\u8fd9\u4e2a\u94fe\u63a5\u540e\uff0c\u4f60\u5c06\u88ab\u8981\u6c42\u63d0\u4f9b\u4e00\u4e2a\u65b0\u5bc6\u7801\u3002\u4e00\u65e6\u4f60\u8f93\u5165\u4e86\u5bc6\u7801\n\uff0c\u4f60\u4f1a\u6210\u529f\u767b\u5f55\u5230 Apple Inventor \u3002\n\u3000\u3000\n\u3000\u3000\u4f60\u7684\u94fe\u63a5\u662f: %s\n\u3000\u3000\n\u3000\u3000\u5feb\u4e50\u521b\u9020!\nMIT App Inventor \u56e2\u961f',
              }

connection = pika.BlockingConnection()
channel = connection.channel()
channel.queue_declare(queue='passmail', durable=True, callback=_setupconsume('passmail', on_message).start)
channel.basic_consume(on_message, 'passmail')

try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()

connection.close()
