import http.client
import mod_tools
import sys
from os import path

changelog_file_path = ""
online_changelog_url = ""
format = ""
online_changelog = ""

def load_options():
    global changelog_file_path
    global online_changelog_url
    global format

    options = mod_tools.get_tool_options(__file__)

    changelog_file_path = options["changelog_file_path"]
    online_changelog_url = options["online_changelog_url"]
    format = options["format"]

    while online_changelog_url.endswith("/"):
        online_changelog_url = online_changelog_url[:-1]

    if not path.exists(path.dirname(changelog_file_path)):
        print("Local changelog path \"%s\" does not exist" % (path.dirname(changelog_file_path)))
        exit()

def load_online_changelog():
    global online_changelog

    if not online_changelog_url.startswith("https://"):
        print("Changelog url must use the HTTPS protocol; URL %s is not valid" % (online_changelog_url))
        exit()

    host = online_changelog_url[8:].split("/", 1)[0]
    try:
        dir = "/" + online_changelog_url[8:].split("/", 1)[1]
    except IndexError:
        dir = "/"

    print("Connecting to %s" % (online_changelog_url))
    connection = http.client.HTTPSConnection(host, timeout=5)

    print("Requesting remote changelog")

    try:
        connection.request("GET", dir)
    except:
        print("\nUnable to make a request. Either the url is invalid, the remote server is down, or you may have no internet connection.\n")
        print("Fetching of changelog failed, aborting...")
        exit()

    response = connection.getresponse()

    if response.status != http.client.OK:
        print("Unable to recieve remote changelog; Bad status code: %d" % (response.status))
        exit()

    online_changelog = response.read().decode("utf-8")
    print("Remote changelog recieved")

def format_local_changelog():
    global online_changelog

    try:
        online_changelog = format % (online_changelog)
    except TypeError:
        print("Format (%s) failed, aborting..." % format)
        exit()

def save_local_changelog():
    try:
        _file = open(changelog_file_path, "w")
        _file.write(online_changelog)
        _file.close()
    except IOError:
        print("Unable to write to file \"%s\"" % (changelog_file_path))
        exit()

    print("\nSuccessfully updated local changelog")

def main():
    if mod_tools.need_help(sys.argv):
        print(mod_tools.get_tool_help(__file__))
        return
    load_options()
    load_online_changelog()
    format_local_changelog()
    save_local_changelog()

if __name__ == "__main__":
    main()