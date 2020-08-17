import sys
import os
import time
import json

if len(sys.argv) < 2:
    print("Usage: <json_file>")
    time.sleep(1)
    exit(0)

json_name = sys.argv[1].split('\\')
json_name = json_name[len(json_name) - 1][:-5]

if not os.path.exists(sys.argv[1]) or os.path.isdir(sys.argv[1]) or sys.argv[1][-5:] != ".json":
    print("Cannot find file!: " + sys.argv[1])
    time.sleep(1)
    exit(0)

json_file = open(sys.argv[1], "r")
lang_file = open(os.path.dirname(sys.argv[1]) + '\\' + json_name + ".lang", "w")

the_json = json.load(json_file)

lang_keys = {}

for key in the_json:
    lang_file.write(key + '=' + the_json[key] + '\n')

print("Successfully Converted!")