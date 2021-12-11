import sys
import os
import time
import json

if len(sys.argv) < 2:
    print("Usage: <lang_file>")
    time.sleep(1)
    exit(0)

lang_name = sys.argv[1].split('/')
lang_name = lang_name[len(lang_name) - 1][:-5]

if not os.path.exists(sys.argv[1]) or os.path.isdir(sys.argv[1]) or sys.argv[1][-5:] != ".lang":
    print("Cannot find file!: " + sys.argv[1])
    time.sleep(1)
    exit(0)

lang_file = open(sys.argv[1], "r")
json_file = open(os.path.dirname(sys.argv[1]) + '/' + lang_name + ".json", "w")

lines = lang_file.read().split("\n")

lang_keys = {}

for line in lines:
    if(line.find('=') > -1):
        index = line.find('=')
        lang_keys[line[:index]] = line[-(len(line) - index - 1):]

json_file.write(json.dumps(lang_keys, indent=4))
