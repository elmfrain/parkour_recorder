import sys
import os
import time

if len(sys.argv) < 3:
    print("Usage: <mod_version> <mc_version>")
    time.sleep(1)
    exit(0)

main_dir = os.path.dirname(os.path.realpath(__file__))

files_to_change = ["build.gradle", ".java", "mcmod.info"]


gradle_file = None
java_file = None
info_file = None

def process_info():
    prev_text = info_file.read()
    prev_lines = prev_text.split('\n')
    lines = []

    for i, line in enumerate(prev_lines):
        new_line = line
        if i < len(prev_lines) - 1:
            new_line += '\n'
        if line.find("\"version\"") > -1:
            i = line.find("\"version\"")
            new_line = line[:i] + "\"version\": \"" + sys.argv[1] + '\",\n'
        elif line.find("\"mcversion\"") > -1:
            i = line.find("\"mcversion\"")
            new_line = line[:i] + "\"mcversion\": \"" + sys.argv[2] + '\",\n'
        lines.extend(new_line)

    info_file.seek(0)
    info_file.truncate()
    info_file.writelines(lines)
    info_file.close()
    print("Successfully changed version number in: " + info_file.name)

def process_java():
    prev_text = java_file.read()
    prev_lines = prev_text.split('\n')
    lines = []

    field = "public static final String MOD_VERSION = "
    for i, line in enumerate(prev_lines):
        new_line = line
        if i < len(prev_lines) - 1:
            new_line += '\n'
        if line.find(field) > -1:
            i = line.find(field)
            new_line = line[:i] + field + '\"' + sys.argv[1] + '-' + sys.argv[2] + "\";\n"
        lines.extend(new_line)

    java_file.seek(0)
    java_file.truncate()
    java_file.writelines(lines)
    java_file.close()
    print("Successfully changed version number in: " + java_file.name)

def process_gradle():
    prev_text = gradle_file.read()
    prev_lines = prev_text.split('\n')
    lines = []

    field = "version = "
    for i, line in enumerate(prev_lines):
        new_line = line
        if i < len(prev_lines) - 1:
            new_line += '\n'
        if line[:10] == field:
            new_line = field + '\"' + sys.argv[1] + '-' + sys.argv[2] + "\"\n"
        lines.extend(new_line)

    gradle_file.seek(0)
    gradle_file.truncate()
    gradle_file.writelines(lines)
    gradle_file.close()
    print("Successfully changed version number in: " + gradle_file.name)

def all_files_found():
    return gradle_file != None and java_file != None and info_file != None

def is_mod_entry(path):
    a_java_file = open(path, 'r')
    lines = a_java_file.read().split('\n')

    for line in lines:
        if line.find("@Mod") > -1:
            a_java_file.close()
            return True

    a_java_file.close()
    return False

for (dirpath, dirnames, filenames) in os.walk(main_dir):
    if main_dir + "\\bin" in dirpath or main_dir + "\\build" in dirpath:
        continue
    for f in filenames:
        if f in files_to_change or f[-5:] == files_to_change[1]:
            if f == files_to_change[0] and gradle_file == None:
                gradle_file = open(dirpath + '\\' + f, 'r+')
            elif java_file == None and f[-5:] == files_to_change[1]:
                if is_mod_entry(dirpath + '\\' + f):
                    java_file = open(dirpath + '\\' + f, 'r+')
            elif f == files_to_change[2] and info_file == None:
                info_file = open(dirpath + '\\' + f, 'r+')
    if all_files_found():
        break

process_info()
process_java()
process_gradle()
input("Press enter to exit")