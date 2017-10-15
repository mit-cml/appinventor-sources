#!/usr/bin/env python
# -*- coding: utf-8 -*-

#########################################################
###     OdeMessages.java converter to .properties     ###
###   To add compatibility with translation centers   ###
###          Recommended: OneSky, Transifex           ###
### ------------------------------------------------- ###
###     Copyright 2017 - Diego Barreiro, Makeroid     ###
#########################################################


lines = open('../../appengine/src/com/google/appinventor/client/OdeMessages.java', "r", encoding="utf-8").read().split("\n")
out = open('OdeMessages.properties', 'w', encoding="utf-8")

for line in lines:
    if line.startswith("  @DefaultMessage"):
        DefaultMessage = line.split('"')
        def_message = DefaultMessage[1]
    if line.startswith("  @Description"):
        Description = line.split('"')
        if Description[1] == "":
            def_description = ""
        else:
            def_description = "# " + Description[1] + "\n"
    if line.startswith("  String"):
        def_string = line.replace(" ", "").replace("String", "").replace("();", "")
        out.write(def_description + def_string + " = \"" + def_message + "\"\n\n")
        print(def_string + ": \"" + def_message + "\"")

out.close()
print("\nFile has been generated at misc/translator/OdeMessages.properties\n")
