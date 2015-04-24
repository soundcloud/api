#!/usr/bin/env python

import re
import subprocess
import sys
import argparse

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
zookeeper_server = 'localhost'
   
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

def whitelisted_clients(whitelist_file):
    with open(whitelist_file, 'r') as f:
        return [client_id_and_comment(line.rstrip()) for line in f.readlines()]

def whitelisted_client_path(id):
    return whitelist_path + '/' + id

def populate_whitelist(whitelist_file, zookeeper_host):
    for (id, comment) in whitelisted_clients(whitelist_file):
        create_node_recursively(whitelisted_client_path(id), zookeeper_host)
        print "added to whitelist: " + id + " (" + comment + ")"

def update(whitelist_file):
    print "Updating whitelist on ZooKeeper server " + zookeeper_server + "..." 
    print "Removing previous whitelist..."  
    delete_whitelist(zookeeper_server)
    print "Creating new empty whitelist..."    
    create_whitelist(zookeeper_server)
    populate_whitelist(whitelist_file, zookeeper_server)
    print "Done!"
 
def parse_client_id():
    if len(sys.argv) != 3:
        print "Wrong number of arguments"
        sys.exit(1)
    else:
        return sys.argv[2].strip()
    
def parse_args():
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group()
    group.add_argument("-a", "--add", nargs = 1, metavar='CLIENT_ID', help="Add the client to the whitelist")
    group.add_argument("-r", "--remove", nargs = 1, metavar='CLIENT_ID', help="Remove the client from the whitelist")
    group.add_argument("-u", "--update", nargs = 1, metavar='FILE', help="Update the whitelist setting it to the content of the given file")
    return (parser, parser.parse_args())

def run():
    (parser, parsed) = parse_args()
    if parsed.add:
        create_node_recursively(whitelisted_client_path(parsed.add[0]), zookeeper_server)
        print "Added " + parsed.add[0] + " to the whitelist" 
    elif parsed.remove:
        delete_node_recursively(whitelisted_client_path(parsed.remove[0]), zookeeper_server)
        print "Remove " + parsed.remove[0] + " to the whitelist"  
    elif parsed.update:
        update(parsed.update[0])
    else:
        parser.print_help()
        
run()