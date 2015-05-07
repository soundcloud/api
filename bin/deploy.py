#!/usr/bin/env python

from subprocess import check_call, Popen, PIPE
from urllib2 import urlopen, URLError
import sys, re, os, time

# bazooka ps output columns
PS_PROC     = 'PROC'
PS_REV      = 'REV'
PS_ENV      = 'ENV'
PS_HOST     = 'HOSTNAME'
PS_PORT     = 'PORT'
PS_TELEPORT = 'TELE_PORT'
PS_STATUS   = 'STATUS'

# bazooka revs output columns
REVS_REVISION    = 'REVISION'
REVS_ARCHIVE_URL = 'ARCHIVE-URL'

def console(*args):
    print ' '.join(str(x) for x in args)
    sys.stdout.flush()

def getenv(name):
    value = os.getenv(name)
    if not value:
        raise Exception('Missing environment variable', name)
    return value

def git_rev():
    return Popen(['git', 'rev-parse', '--short', 'HEAD'], stdout=PIPE).communicate()[0].rstrip()

def find_revs(name, bazooka_zone):
    whitespace = re.compile(r'\s+')
    bazooka_revs = Popen(['bazooka', '-srv.zone', bazooka_zone , '-a', name, 'revs'], stdout=PIPE).communicate()[0]
    lines = [whitespace.split(line) for line in bazooka_revs.splitlines()]
    if len(lines) == 0:
        return []

    header = lines[0]
    columns = {
        REVS_REVISION    : header.index(REVS_REVISION),
        REVS_ARCHIVE_URL : header.index(REVS_ARCHIVE_URL)
    }
    revs = {}
    for line in lines:
        rev = {}
        for key, index in columns.iteritems():
            rev[key] = line[index]
        revs[rev[REVS_REVISION]] = rev

    return revs

def find_running(name, proc, bazooka_zone):
    whitespace = re.compile(r'\s+')
    bazooka_ps = Popen(['bazooka', '-srv.zone', bazooka_zone, '-a', name, 'ps', '-l'], stdout=PIPE).communicate()[0]
    lines = [whitespace.split(line) for line in bazooka_ps.splitlines()]
    if len(lines) == 0:
        return []
    header = lines[0]
    columns = {
        PS_PROC:     header.index(PS_PROC),
        PS_REV:      header.index(PS_REV),
        PS_ENV:      header.index(PS_ENV),
        PS_HOST:     header.index(PS_HOST),
        PS_PORT:     header.index(PS_PORT),
        PS_TELEPORT: header.index(PS_TELEPORT),
        PS_STATUS:   header.index(PS_STATUS)
    }
    apps = []
    for line in lines:
        app = {}
        for key, index in columns.iteritems():
            app[key] = line[index]
        apps.append(app)
    return [app for app in apps if app[PS_STATUS] == 'running' and app[PS_PROC] == proc]

def sanity_check(revisions, environments):
    if len(revisions) != 1:
        raise Exception('Cannot find a single revision!', revisions)
    if len(environments) != 1:
        raise Exception('Cannot find a single environment!', environments)

def scale(name, proc, revision, environment, count, bazooka_zone):
    check_call(['bazooka', '-srv.zone', bazooka_zone, '-a', name, 'scale', '-n', str(count), '-r', revision, '-e', environment, proc])

def find_deployed(name, proc, revision, environment, bazooka_zone):
    return [app for app in find_running(name, proc, bazooka_zone) if app[PS_REV] == revision and app[PS_ENV] == environment]

def healthy(instances, path):
    healthy = True
    time.sleep(1)
    for instance in instances:
      if healthy == False:
        return False

      healthy = False
      for attempt in range(10):
          try:
              url = 'http://{0}:{1}{2}'.format(instance[PS_HOST], instance[PS_PORT], path)
              console('Checking', url)
              response = urlopen(url)
              if response.getcode() == 200:
                console('got response: ', response.read())
                healthy = True
                break
          except URLError:
              time.sleep(3)

    return healthy

def main():
    app_name = getenv('BAZOOKA_APP')
    new_instances = int(getenv('INSTANCES'))
    process_type = getenv('PROCESS_TYPE')
    scale_step = int(os.getenv('SCALE_STEP', '4'))
    health_path = getenv('HEALTH_PATH')
    bazooka_zone = getenv('BAZOOKA_ZONE')

    new_revision = git_rev()
    # TODO: Automate the creation of this environment
    new_environment = "empty"

    revs = find_revs(app_name, bazooka_zone)
    #if not new_revision in revs or revs['release'][REVS_ARCHIVE_URL] != revs[new_revision][REVS_ARCHIVE_URL]:
    #    console('Revision ', new_revision, ' is not the release revision -- chickening out')
    #    return 1

    current_instances = 0
    current_revision = None
    current_environment = None

    running = find_running(app_name, process_type, bazooka_zone)

    if len(running) > 0:
        revisions = set()
        environments = set()

        for app in running:
            environments.add(app[PS_ENV])
            revisions.add(app[PS_REV])

        sanity_check(revisions, environments)

        current_instances = len(running)
        current_revision = revisions.pop()
        current_environment = environments.pop()

        console('Currently running', current_instances,
                'instances of proc', process_type,
                'with revision', current_revision,
                'and', current_environment, 'environment.')

        if current_revision == new_revision:
            console('Revision', new_revision, 'is already deployed!')
            return 1

    console('Scaling up to one new instance ...')
    scale(app_name, process_type, new_revision, new_environment, 1, bazooka_zone)

    if not healthy(find_deployed(app_name, process_type, new_revision, new_environment, bazooka_zone)[:1], health_path):
        console('The application is not healthy. The health page did not return a successful response!')
        scale(app_name, process_type, new_revision, new_environment, 0, bazooka_zone)
        return 1

    console('Application is healthy.')

    if scale_step > 0:
        for scale_up in range(scale_step, new_instances, scale_step):
            scale(app_name, process_type, new_revision, new_environment, scale_up, bazooka_zone)
            if not healthy(find_deployed(app_name, process_type, new_revision, new_environment, bazooka_zone), health_path):
               return 1

            scale_down = current_instances - scale_up
            if scale_down > 0:
                scale(app_name, process_type, current_revision, current_environment, scale_down, bazooka_zone)

    scale(app_name, process_type, new_revision, new_environment, new_instances, bazooka_zone)

    if not healthy(find_deployed(app_name, process_type, new_revision, new_environment, bazooka_zone), health_path):
      return 1

    if current_instances > 0:
        scale(app_name, process_type, current_revision, current_environment, 0, bazooka_zone)

    return 0

if __name__ == '__main__':
    sys.exit(main())
