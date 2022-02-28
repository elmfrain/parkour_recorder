from dataclasses import dataclass
from typing import Tuple
import mod_tools
import sys
from os import path

files = []
mod_version = ""
mc_version = ""

@dataclass
class Entry:
    start: str = ""
    end: str = ""
    format: str = ""
    format_parameters: Tuple = ()

    def update_params(self):
        params = []

        for param in self.format_parameters:
            if param == "mod_version":
                params.append(mod_version)
            elif param == "mc_version":
                params.append(mc_version)

        self.format_parameters = tuple(params)
        

class FileEntry:
    file: str = ""
    entries: Tuple = ()

    def append_entry(self, entry):
        entries_list = list(self.entries)
        entries_list.append(entry)
        self.entries = tuple(entries_list)

    def __repr__(self) -> str:
        return "[%s] %s" % (self.file, self.entries)

def read_params():
    global mod_version
    global mc_version

    try:
        mod_version = sys.argv[1]
        mc_version = sys.argv[2]
    except IndexError:
        print("usage: <mod_version> <mc_version>")
        exit()

def load_options():
    global files
    options = mod_tools.get_tool_options(__file__)

    for file in options["files"]:
        file_entry = FileEntry()
        file_entry.file = file["file"]

        for entry in file["entries"]:
            entry_entry = Entry()
            entry_entry.start = entry["start"]
            entry_entry.end = entry["end"]
            entry_entry.format = entry["format"]
            entry_entry.format_parameters = tuple(entry["format_parameters"])
            entry_entry.update_params()
            file_entry.append_entry(entry_entry)

        files.append(file_entry)

def replace_entries():
    for file in files:
        file_content = ""

        try:
            _file = open(file.file, "r")
            file_content = _file.read()
            _file.close()
        except IOError:
            print("Unable to open file \"%s\", skipping" % (path.basename(file.file)))
            continue
        
        for entry in file.entries:
            start = 0
            end = 0

            try:
                start = file_content.index(entry.start)
                end = file_content[start:].index(entry.end) + start
            except ValueError:
                print("Cannot find entry (%s), skipping" % (entry.start))
                continue

            new_content = ""
            try:
                new_content = entry.format % entry.format_parameters
            except TypeError:
                print("Syntax error with format (%s) with params %s, skipping" % (entry.format, entry.format_parameters))
                continue
            file_content = file_content[:start] + new_content + file_content[end:]
        
        try:
            _file = open(file.file, "w")
            _file.write(file_content)
            _file.close()
        except IOError:
            print("Unable to modify file \"%s\", skipping" % (path.basename(file.file)))
            continue

        print("Successfully modified file \"%s\"" % (path.basename(file.file)))

def main():
    if mod_tools.need_help(sys.argv):
        print(mod_tools.get_tool_help(__file__))
        return
    
    read_params()
    load_options()
    replace_entries()

if __name__ == "__main__":
    main()