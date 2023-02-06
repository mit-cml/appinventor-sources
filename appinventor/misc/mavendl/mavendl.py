#!/usr/bin/env python3

import argparse
from dataclasses import dataclass
import os
import urllib.request
from io import BytesIO
from typing import Dict, List, Set, Tuple
from xml.dom import minidom
from zipfile import ZipFile

parser = argparse.ArgumentParser(
    description="Download AAR libs with transitive deps")
parser.add_argument("-t", "--dry-run", action="store_true",
                    help="dry run to only print dependency graph")
parser.add_argument("-d", "--depth", type=int, default=-1,
                    help="depth of transitive dependencies to traverse")
parser.add_argument("-o", "--output-dir", required=True,
                    help="path to output directory")
parser.add_argument("coord", metavar="G:A:V",
                    help="maven coordinate (group:artifact:version)")

args = parser.parse_args()


MAVEN_REPOS = [
    # google
    'https://maven.google.com',
    # maven central
    'https://repo1.maven.org/maven2',
]


@dataclass(frozen=True)
class MavenCoordinate:
    group: str
    artifact: str
    version: str
    type: str

    def maven_url(self, repo: str, type: str = None) -> str:
        if not type:
            type = self.type
        return '{r}/{gp}/{a}/{v}/{a}-{v}.{t}'.format(r=repo, gp=self.group.replace('.', '/'), a=self.artifact, v=self.version, t=type)

    def __repr__(self):
        return '{g}:{a}:{v}@{t}'.format(g=self.group, a=self.artifact, v=self.version, t=self.type)


def read_file(url):
    try:
        with urllib.request.urlopen(url) as response:
            data = response.read()
            return data
    except Exception as e:
        return None


# parse the maven coordinate
group, artifact, version = args.coord.split(':')

queue: List[Tuple[MavenCoordinate, int]] = [
    (MavenCoordinate(group, artifact, version, 'aar'), 0)]
visited: Set[MavenCoordinate] = set()
source_map: Dict[MavenCoordinate, str] = {}

while queue:
    coord, depth = queue.pop(0)

    if coord in visited or (args.depth != -1 and depth > args.depth):
        continue

    visited.add(coord)

    print('  ' * depth + '+', coord)

    for repo in MAVEN_REPOS:
        pom_file = read_file(coord.maven_url(repo, type='pom'))

        if not pom_file:
            continue

        source_map[coord] = repo

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

            queue.append(
                (MavenCoordinate(d_group, d_artifact, d_version, d_type), depth+1))

        # exit the loop if the dep is resolved
        break
    else:
        print('!', "Couldn't resolve", coord)
        exit(1)

print('*', 'Found', len(visited), 'dependencies')

if args.dry_run:
    exit(0)

os.makedirs(args.output_dir, exist_ok=True)

for dep in visited:
    print('.', dep, source_map[dep])

    repo = source_map[dep]

    aar_or_jar_file = read_file(dep.maven_url(repo))

    if not aar_or_jar_file:
        continue

    if dep.type == 'aar':
        aar_path = '{o}/{a}-{v}.aar'.format(o=args.output_dir,
                                            a=dep.artifact, v=dep.version)
        jar_path = '{o}/{a}-{v}.jar'.format(o=args.output_dir,
                                            a=dep.artifact, v=dep.version)

        with ZipFile(BytesIO(aar_or_jar_file)) as zi, ZipFile(aar_path, 'w') as zo:
            for item in zi.infolist():
                if item.filename == 'classes.jar':
                    with open(jar_path, 'wb') as f:
                        f.write(zi.read(item))
                else:
                    zo.writestr(item, zi.read(item))
    else:
        jar_path = '{o}/{a}-{v}.{t}'.format(o=args.output_dir,
                                            a=dep.artifact, v=dep.version, t=dep.type)

        with open(jar_path, 'wb') as f:
            f.write(aar_or_jar_file)
