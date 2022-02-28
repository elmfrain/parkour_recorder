import mod_tools
import sys
import collections
import json
from os import path

lang_file_path = ""
json_file_path = ""
lang_directory = ""
lang_keys = {}

def load_options():
    global lang_directory

    options = mod_tools.get_tool_options(__file__)

    lang_directory = options["lang_directory"]

def read_params():
    global lang_file_path
    global json_file_path

    try:
        lang_file_name = sys.argv[1]
    except IndexError:
        print("usage: <lang_file>")
        exit()

    lang_file_path = path.join(lang_directory, lang_file_name)

    if not path.exists(lang_file_path):
        print("File \"%s\" does not exist" % (lang_file_name))
        exit()

    json_file_name = lang_file_name.split(".")[0] + ".json"
    json_file_path = path.join(lang_directory, json_file_name)

def read_lang_keys():
    global lang_keys

    try:
        _file = open(lang_file_path, "r")
        lang_file_content = _file.read()
        _file.close()
    except IOError:
        print("Unable to read file \"%s\"" % (path.basename(lang_file_path)))
        exit()

    for line in lang_file_content.splitlines():
        entry = line.split("=")
        if len(entry) != 2:
            continue

        lang_keys[entry[0]] = entry[1]

    lang_keys = collections.OrderedDict(sorted(lang_keys.items()))

def save_json():
    json_file_content = json.dumps(lang_keys, indent=4)

    try:
        _file = open(json_file_path, "w")
        _file.write(json_file_content)
        _file.close()
    except IOError:
        print("Unable to write to file \"%s\"" % (path.basename(json_file_path)))
        exit()

    print("Successfully converted lang file to \"%s\"" % (path.basename(json_file_path)))

def main():
    if mod_tools.need_help(sys.argv):
        print(mod_tools.get_tool_help(__file__))
        return
    
    load_options()
    read_params()
    read_lang_keys()
    save_json()

if __name__ == "__main__":
    main()