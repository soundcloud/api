#!/usr/bin/env python

import re
import subprocess
import sys

def production_properties():
    with open('production.properties', 'r') as f:
        return f.readlines()

def zookeeper_host():
    for line in production_properties():
        result = re.match( r'ZOOKEEPER_SERVERS="(.*?),.*"', line)
        if result:
            return result.group(1) 

def app_name():
    for line in production_properties():
        result = re.match( r'APP_NAME="(.*)"', line)
        if result:
            return result.group(1)
    
whitelist_path = "/" + app_name() + "/ratelimits/whitelist"
zookeeper_server = zookeeper_host()
   
def create_command(node):
    return "create " + node + " '' false false true"  

def delete_command(node):
    return "rmr " + node
  
def create_node_recursively(node, zookeeper_host):
    subprocess.call(["zk-shell", "--run-once", create_command(node), zookeeper_host])

def delete_node_recursively(node, zookeeper_host):
    subprocess.call(["zk-shell", "--run-once", delete_command(node), zookeeper_host])

def delete_whitelist(zookeeper_host):
    delete_node_recursively(whitelist_path, zookeeper_host)

def create_whitelist(zookeeper_host):
    create_node_recursively(whitelist_path, zookeeper_host)

def client_id_and_comment(line):
  result = re.match( r'(.*?)#(.*)', line)
  if result:
      return (result.group(1).strip(), result.group(2).strip())

def whitelisted_clients():
    with open('ratelimiting_whitelist.txt', 'r') as f:
        return [client_id_and_comment(line.rstrip()) for line in f.readlines()]

def whitelisted_client_path(id):
    return whitelist_path + '/' + id

def populate_whitelist(zookeeper_host):
    for (id, comment) in whitelisted_clients():
        create_node_recursively(whitelisted_client_path(id), zookeeper_host)
        print "added to whitelist: " + id + " (" + comment + ")"

def update():
    print "Updating whitelist on ZooKeeper server " + zookeeper_server + "..." 
    print "Removing previous whitelist..."  
    delete_whitelist(zookeeper_server)
    print "Creating new empty whitelist..."    
    create_whitelist(zookeeper_server)
    populate_whitelist(zookeeper_server)
    print "Done!"
 
def parse_client_id():
    if len(sys.argv) != 3:
        print "Wrong number of arguments"
        sys.exit(1)
    else:
        return sys.argv[2].strip()
    
def run():
    if len(sys.argv) > 1:
        command = sys.argv[1]
        if command == 'add':
            client = parse_client_id()
            create_node_recursively(whitelisted_client_path(client), zookeeper_server)
            print "Added " + client + " to the whitelist"
        elif command == 'remove':
            client = parse_client_id()
            delete_node_recursively(whitelisted_client_path(client), zookeeper_server)
            print "Remove " + client + " to the whitelist"
        elif command == 'update':
            update()
        else:
            print "Unsupported command."
    else:
        print "No command specified."
    
run()
