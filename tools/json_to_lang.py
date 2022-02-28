import mod_tools
import sys
import json
import collections
from os import path

lang_directory = ""
json_file_path = ""
lang_file_path = ""
json_lang = None
lang_values = {}

def load_options():
    global lang_directory
    options = mod_tools.get_tool_options(__file__)
    
    lang_directory = options["lang_directory"]
    if not path.exists(lang_directory):
        print("Path to \'lang\' directory (%s) does not exist" % (lang_directory))

def load_file_paths():
    global json_file_path
    global lang_file_path

    json_file_name = ""
    try:
        json_file_name = sys.argv[1]
    except IndexError:
        print("usage: <json_file>")
        exit()

    json_file_path = path.join(lang_directory, json_file_name)

    if not path.exists(json_file_path):
        print("File \"%s\" does not exist" % (json_file_name))
        exit()

    lang_file_name = json_file_name.split(".")[0] + ".lang"
    lang_file_path = path.join(lang_directory, lang_file_name)

def load_json_lang():
    global json_lang
    global lang_values

    try:
        _json_file = open(json_file_path, "r")
        json_lang = json.load(_json_file)
        _json_file.close()
    except json.JSONDecodeError or IOError:
        print("Failed to load JSON file \"%s\" " % (json_file_path))
        exit()

    for key in json_lang:
        lang_values[key] = json_lang[key]

    lang_values = collections.OrderedDict(sorted(lang_values.items()))

def write_lang():
    global lang_values

    try:
        _lang_file = open(lang_file_path, "w")

        for key in lang_values:
            _lang_file.write(key + "=" + lang_values[key] + "\n")

        _lang_file.close()
    except IOError:
        print("Failed to write to LANG file \"%s\"" % (lang_file_path))
        exit()
    
    print("Successfully wrote to lang file \"%s\"" % (lang_file_path))

def main():
    if mod_tools.need_help(sys.argv):
        print(mod_tools.get_tool_help(__file__))
        return

    load_options()
    load_file_paths()
    load_json_lang()
    write_lang()

if __name__ == "__main__":
    main()