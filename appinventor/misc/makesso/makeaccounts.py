# Code to make UUID generted accounts

import sqlite3
import subprocess
import uuid
import requests

def pickaccounts(offset=1, count=10):
    db = sqlite3.connect('accounts.sqlite')
    db.execute("create table if not exists accounts (uuid text, name text)")
    db.commit()
    for i in range(count):
        offset += 1
        name = "tempacc%06d" % offset
        auuid = str(uuid.uuid4())
        z = subprocess.run(["java", "-jar", "tokendemo.jar", "createaccount", auuid, name], stdout=subprocess.PIPE)
        c = z.stdout.strip()
        q = requests.get(c)
        if q.status_code != 200:
            print('Request Failed')
            continue
        j = q.json()
        e = j.get('error', None)
        if e:
            print("Request failed %s" % j)
            continue
        db.execute("insert into accounts (uuid, name) values (?, ?)",
                   [auuid, name])
        db.commit()

def makeinput():
    '''Move projects to temp accounts, 10 projects to an account'''
    db = sqlite3.connect('accounts.sqlite')
    fout = open('input', 'w')
    c = db.execute("select projectid from tomove")
    projects = c.fetchall()
    projects = [x[0] for x in projects]
    c = db.execute("select uuid from accounts")
    accounts = c.fetchall()
    accounts = [x[0] for x in accounts]
    for a in accounts:
        for i in range(10):
            try:
                fout.write("%s %s\n" % (projects.pop(), a))
            except IndexError:
                fout.close()
                return
    fout.close()

