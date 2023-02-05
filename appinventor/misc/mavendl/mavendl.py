# Create mavendl to download AAR libs with transitive deps 
# This stores the files in a "libs" directory relative to this script
# Usage: mavendl.py <group>:<artifact>:<version>

import argparse
import os
import urllib.request
from io import BytesIO
from typing import Dict, List, Set, Tuple
from xml.dom import minidom
from zipfile import ZipFile

parser = argparse.ArgumentParser()
parser.add_argument("coord")

args = parser.parse_args()


maven_repos = [
    # google
    'https://maven.google.com',
    # maven central
    'https://repo1.maven.org/maven2',
]


def maven_url(repo: str, group: str, artifact: str, version: str, type: str) -> str:
    return '{r}/{gp}/{a}/{v}/{a}-{v}.{t}'.format(r=repo, gp=group.replace('.', '/'), a=artifact, v=version, t=type)


def format_coord(group: str, artifact: str, version: str, type: str) -> str:
    return '{g}:{a}:{v}@{t}'.format(g=group, a=artifact, v=version, t=type)


def read_file(url):
    try:
        with urllib.request.urlopen(url) as response:
            data = response.read()
            return data
    except Exception as e:
        return None


# parse the maven coordinate
group, artifact, version = args.coord.split(':')

COORD = Tuple[str, str, str, str]

q: List[COORD] = [(group, artifact, version, 'aar')]

v: Set[COORD] = set()

sm: Dict[COORD, str] = {}

while q:
    coord = q.pop(0)

    if coord in v:
        continue

    v.add(coord)

    print('+', format_coord(*coord))

    for repo in maven_repos:
        pom = maven_url(repo, *coord[:-1], type='pom')

        pom_file = read_file(pom)

        if not pom_file:
            continue

        sm[coord] = repo

        pom_xml = minidom.parseString(pom_file)

        for dep in pom_xml.getElementsByTagName('dependency'):
            if dep.getElementsByTagName('scope')[0].firstChild.nodeValue != 'compile':
                continue

            d_group = dep.getElementsByTagName(
                'groupId')[0].firstChild.nodeValue
            d_artifact = dep.getElementsByTagName(
                'artifactId')[0].firstChild.nodeValue
            d_version = dep.getElementsByTagName(
                'version')[0].firstChild.nodeValue
            d_type = dep.getElementsByTagName(
                'type')[0].firstChild.nodeValue if dep.getElementsByTagName('type') else 'jar'

            q.append((d_group, d_artifact, d_version, d_type))

        break


os.makedirs('libs', exist_ok=True)

for d in sorted(v):
    print('.', format_coord(*d), sm[d])

    repo = sm[d]
    aar_or_jar = maven_url(repo, *d)

    aar_or_jar_file = read_file(aar_or_jar)

    if not aar_or_jar_file:
        continue

    group, artifact, version, ftype = d

    if ftype == 'aar':
        aar_path = 'libs/{a}-{v}.{t}'.format(
            a=artifact, v=version, t=ftype)
        jar_path = 'libs/{a}-{v}.{t}'.format(
            a=artifact, v=version, t='jar')

        with ZipFile(BytesIO(aar_or_jar_file)) as zi, ZipFile(aar_path, 'w') as zo:
            for item in zi.infolist():
                if item.filename == 'classes.jar':
                    with open(jar_path, 'wb') as f:
                        f.write(zi.read(item))
                else:
                    zo.writestr(item, zi.read(item))
    else:
        jar_path = 'libs/{a}-{v}.{t}'.format(
            a=artifact, v=version, t=ftype)

        with open(jar_path, 'wb') as f:
            f.write(aar_or_jar_file)
