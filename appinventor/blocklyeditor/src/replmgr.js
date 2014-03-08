// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2013 Massachusetts Institute of Technology. All rights reserved.

/**
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to handle communicating with the repl (MIT AICompanion).
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

Blockly.ReplMgr = {};

Blockly.ReplMgr.yail = null;

goog.require('goog.ui.Dialog');
goog.require('goog.net.XmlHttp');
goog.require('goog.json');
goog.require('goog.Uri.QueryData');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('goog.crypt.Hash');
goog.require('goog.crypt.Sha1');
goog.require('goog.crypt.Hmac');
goog.require('goog.crypt.base64');

// Repl State
// Repl "state" definitions

Blockly.ReplMgr.rsState = {
    IDLE : 0,                   // Not connected nor connection requested
    RENDEZVOUS: 1,              // Waiting for the Rendezvous server to answer
    CONNECTED: 2,               // Connected to Repl
    WAITING: 3                  // Waiting for the Emulator to start
};

Blockly.ReplStateObj = function() {};

Blockly.ReplStateObj.prototype = {
    'state' : Blockly.ReplMgr.rsState.IDLE,     // Is the connection to the Repl Up
    'url' : null,                       // The url of the repl (Companion) when known
    'baseurl' : null,                  // URL used to upload assets
    'replcode' : null,                  // The six digit code used for rendezvous
    'rendezvouscode' : null,            // Code used for Rendezvous (hash of replcode)
    'dialog' : null,                    // The Dialog Box with the code and QR Code
    'count' : 0,                        // Count of number of reads from rendezvous server
    'didversioncheck' : false
};

// Blockly.mainWorkSpace --- hold the main workspace

Blockly.ReplMgr.buildYail = function() {
    var phoneState;
    var code = [];
    var blocks;
    var block;
    var needinitialize = false;
    if (!window.parent.ReplState.phoneState) { // If there is no phone state, make some!
        window.parent.ReplState.phoneState = {};
    }
    phoneState = window.parent.ReplState.phoneState;
    if (!phoneState.formJson || !phoneState.packageName)
        return;                 // Nothing we can do without these
    if (!phoneState.initialized) {
        phoneState.initialized = true;
        phoneState.blockYail = {};
        phoneState.componentYail = "";
    }

    var jsonObject = JSON.parse(phoneState.formJson);
    var formProperties;
    var formName;
    if (jsonObject.Properties) {
        formProperties = jsonObject.Properties;
        formName = formProperties.$Name;
    }
    var componentMap = Blockly.Component.buildComponentMap([], [], false, false);
    var componentNames = [];
    for (var comp in componentMap.components)
        componentNames.push(comp);
    if (formProperties) {
        if (formName != 'Screen1')
            code.push(Blockly.Yail.getComponentRenameString("Screen1", formName));
        var sourceType = jsonObject.Source;
        if (sourceType == "Form") {
            code = code.concat(Blockly.Yail.getComponentLines(formName, formProperties, null /*parent*/, componentMap, true /* forRepl */));
        } else {
            throw "Source type " + sourceType + " is invalid.";
        }

        // Fetch all of the components in the form, this may result in duplicates
        componentNames = Blockly.Yail.getDeepNames(formProperties, componentNames);
        // Remove the duplicates
        var uniqueNames = componentNames.filter(function(elem, pos) {
            return componentNames.indexOf(elem) == pos;});
        componentNames = uniqueNames;

        code = code.join('\n');

        if (phoneState.componentYail != code) {
            // We need to send all of the comonent cruft (sorry)
            needinitialize = true;
            phoneState.blockYail = {}; // Sorry, have to send the blocks again.
            this.putYail(Blockly.Yail.YAIL_CLEAR_FORM);
            this.putYail(code);
            this.putYail(Blockly.Yail.YAIL_INIT_RUNTIME);
            phoneState.componentYail = code;
        }
    }

    blocks = Blockly.mainWorkspace.getTopBlocks(true);
    var success = function() {
        if (this.block.replError)
            this.block.replError = null;
        Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors();
    };
    var failure = function(message) {
        this.block.replError = message;
        Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors();
    };

    for (var x = 0; (block = blocks[x]); x++) {
        if (!block.category || (block.hasError && !block.replError)) { // Don't send blocks with
            continue;           // Errors, unless they were errors signaled by the repl
        }
        if (block.disabled) {   // Don't send disabled blocks
            continue;
        }
        if (block.blockType != "event" &&
            block.type != "global_declaration" &&
            block.type != "procedures_defnoreturn" &&
            block.type != "procedures_defreturn")
            continue;
        var tempyail = Blockly.Yail.blockToCode(block);
        if (phoneState.blockYail[block.id] != tempyail) { // Only send changed yail
            this.putYail(tempyail, block, success, failure);
            phoneState.blockYail[block.id] = tempyail;
        }
    }

    // need to do this after the blocks have been defined
    if (needinitialize) {
        this.putYail(Blockly.Yail.getComponentInitializationString(formName, componentNames));
    }
};

Blockly.ReplMgr.sendFormData = function(formJson, packageName) {
    window.parent.ReplState.phoneState.formJson = formJson;
    window.parent.ReplState.phoneState.packageName = packageName;
    var context = this;
    var poller = function() {   // Keep track of "this"
        context.polltimer = null;
        return context.pollYail.call(context);
    };
    if (this.polltimer) {       // We have one running, punt it.
        clearTimeout(this.polltimer);
    }
    this.polltimer = setTimeout(poller, 500);
};

Blockly.ReplMgr.RefreshAssets = null;

Blockly.ReplMgr.pollYail = function() {
    try {
        if (window === undefined)    // If window is gone, then we are a zombie timer firing
            return;                  // in a destroyed frame.
    } catch (err) {                  // We get an error on FireFox when window is gone.
        return;
    }
    if (window.parent.ReplState.state == this.rsState.CONNECTED) {
        this.buildYail();
    }
    if (this.RefreshAssets === null) {
        try {
            this.RefreshAssets = window.parent.AssetManager_refreshAssets;
        } catch (err) {
        }
    }
    if (window.parent.ReplState.state == this.rsState.CONNECTED) {
        this.RefreshAssets(this.formName);
    }
};

Blockly.ReplMgr.resetYail = function(code) {
    window.parent.ReplState.phoneState.initialized = false; // so running io stops
    this.putYail.reset();
    window.parent.ReplState.phoneState = { "phoneQueue" : []};
};

// Theory of Operation
//
// This blocks of code implements communication to the phone. Yail Forms
// are queued on ReplState.phoneState.phoneQueue. Each entry in the
// queue is an object that contains the yail to run and two callbacks, one
// for success and one for failure.
//
// putYail enqueues forms for the phone and is the only function exported
// pollphone processes the queue using Ajax calls. The completion of each
// Ajax call looks to process the next entry in the queue. This continues
// until the queue is empty.

Blockly.ReplMgr.putYail = (function() {
    var rs;
    var context;
    var conn;                   // XMLHttpRequest Object sending to Phone
    var rxhr;                   // XMLHttpRequest Object listening for returns
    var phonereceiving = false;
    var engine = {
        // Enqueue form for the phone
        'putYail' : function(code, block, success, failure) {
            rs = window.parent.ReplState;
            context = this;
            if (rs === undefined || rs === null) {
                console.log('putYail: replState not set yet.');
                return;
            }
            if (rs.state != Blockly.ReplMgr.rsState.CONNECTED) {
                console.log('putYail: phone not connected');
                return;
            }
            if (!rs.phoneState.phoneQueue) {
                rs.phoneState.phoneQueue = [];
            }
            rs.phoneState.phoneQueue.push({
                'code' : Blockly.ReplMgr.quoteUnicode(code), // Deal with unicode characters and kawa
                'success' : success,
                'failure' : failure,
                'block' : block
            });
            if (!rs.phoneState.ioRunning) {
                rs.phoneState.ioRunning = true;
                engine.pollphone(); // Trigger callback side
            }
        },
        'pollphone' : function() {
            if (!rs.didversioncheck) {
                engine.doversioncheck();
                return;
            }
            if (!phonereceiving) {
                engine.receivefromphone();
            }
            var work = rs.phoneState.phoneQueue.shift();
            if (!work) {
                rs.phoneState.ioRunning = false;
                return;
            }
            var encoder = new goog.Uri.QueryData();
            conn = goog.net.XmlHttp();
            var blockid;
            if (work.block) {
                blockid = work.block.id;
            } else {
                blockid = "-1";
            }

            conn.open('POST', rs.url, true);
            conn.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    var json = goog.json.parse(this.response);
                    if (json.status != 'OK') {
                        if (work.failure)
                            work.failure("Error from Companion");
                    } else {
                        if (work.success)
                            work.success();
                    }
                    context.processRetvals(json.values);
                    rs.seq_count += 1;
                    if (rs.phoneState.initialized) // Only continue if we are still initialized
                        engine.pollphone(); // And on to the next!
                } else {
                    if (this.readyState == 4) {
                        console.log("putYail(poller): status = " + this.status);
                        if (work.failure) {
                            work.failure("Network Connection Error");
                        }
                        var dialog = new Blockly.ReplMgr.Dialog("Network Error", "Network Error Communicating with Companion.<br />Try restarting the Companion and reconnecting", "OK", null, 0,
                            function() {
                                dialog.hide();
                                context.hardreset(context.formName);
                            });
                        engine.resetcompanion();
                    }
                }

            };
            encoder.add('mac', Blockly.ReplMgr.hmac(work.code + rs.seq_count + blockid));
            encoder.add('seq', rs.seq_count);
            encoder.add('code', work.code);
            encoder.add('blockid', blockid);
            var stuff = encoder.toString();
            conn.send(stuff);
        },
        'doversioncheck' : function() {
            var conn = goog.net.XmlHttp();
            conn.open('GET', rs.versionurl, true);
            conn.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    rs.didversioncheck = true;
                    if (this.response[0] != "{") {
                        engine.checkversionupgrade(true, "", true); // Old Companion
                        engine.resetcompanion();
                        return;
                    } else {
                        var json = goog.json.parse(this.response);
                        if (!Blockly.ReplMgr.acceptableVersion(json.version)) {
                            engine.checkversionupgrade(true, json.installer, false);
                            return;
                        }
                    }
                    engine.pollphone();
                    return;
                }
                if (this.readyState == 4) { // Old Companion, doesn't do CORS so we fail to talk to it
                    var dialog = new Blockly.ReplMgr.Dialog("Network Error", "Network Error Communicating with Companion.<br />Try restarting the Companion and reconnecting", "OK", null, 0, function() {
                        dialog.hide();
                    });
                    engine.resetcompanion();
                    return;
                }
            };
            conn.send();
        },
        "receivefromphone" : function() {
            phonereceiving = true;
            console.log("receivefromphone called.");
            rxhr = goog.net.XmlHttp();
            rxhr.open('POST', rs.rurl, true); // We post to avoid caching issues
            rxhr.onreadystatechange = function() {
                if (this.readyState != 4) return;
                console.log("receivefromphone returned.");
                if (this.status == 200) {
                    var json = goog.json.parse(this.response);
                    if (json.status == 'OK') {
                        context.processRetvals(json.values);
                    }
                    engine.receivefromphone(); // Continue...
                }
            };
            rxhr.send("IGNORED=STUFF");
        },
        "reset" : function() {
            if (rxhr)
                rxhr.abort();
            rxhr = null;
//            if (conn)  // This seems to cause disconnects on project switch
//                conn.abort();
//            conn = null;
            phonereceiving = false;
        },
        "resetcompanion" : function() {
            console.log("reseting companion");
            rs.state = Blockly.ReplMgr.rsState.IDLE;
            rs.connection = null;
            context.resetYail();
//   hardreset is now done in the handler for the network error dialog OK
//   button.
//          context.hardreset(context.formName); // kill adb and emulator
            rs.didversioncheck = false;
            window.parent.BlocklyPanel_indicateDisconnect();
        },
        "checkversionupgrade" : function(fatal, installer, force) {
            var dialog;
            var cancelButton;
            if (force) {
                cancelButton = null; // Don't permit deferring the upgrade
            } else {
                cancelButton = "Not Now";
            }
            if (installer === undefined)
                installer = "com.android.vending"; // Temp kludge: Treat old Companions as un-updateable (as they are)
            if (installer != "com.android.vending" && window.parent.COMPANION_UPDATE_URL) {
                var emulator = (rs.replcode == 'emulator'); // Kludgey way to tell




                dialog = new Blockly.ReplMgr.Dialog("Companion Version Check",
                                                    'Your Companion App is out of date. Click "OK" to start the update. ' +
"Watch your " + (emulator?"emulator's":"device's") + ' screen because you will be asked to approve the update.', "OK", cancelButton, 0, function(response) {
                    dialog.hide();
                    if (response != "Not Now") {
                        context.triggerUpdate();
                    } else {
                        engine.pollphone();
                    }
                });
            } else if (fatal) {
                dialog = new Blockly.ReplMgr.Dialog("Companion Version Check", "The Companion you are using is out of date.<br/><br/>This Version of App Inventor should be used with Companion version" + window.parent.PREFERRED_COMPANION, "OK", null, 0, function() { dialog.hide();});
                engine.resetcompanion();
            } else {
                dialog = new Blockly.ReplMgr.Dialog("Companion Version Check", "You are using an out-of-date Companion. You need not update the Companion immediately but should consider updating soon.", "Dismiss", null, 1, function() { dialog.hide();});
                engine.resetcompanion();
            }
        }
    };
    engine.putYail.reset = engine.reset;
    return engine.putYail;
})();

// This function is called when we need to update the Companion, we have
// an update-able Companion and we have a path to update it from. Otherwise
// we are never called and the user is given a message that their Companion
// is out of date.
Blockly.ReplMgr.triggerUpdate = function() {
    var rs = window.parent.ReplState;
    var fetchconn = goog.net.XmlHttp();
    var encoder = new goog.Uri.QueryData();
    var context = this;

    // Setup Dialog management code

    var dialog = null;
    var okbuttonshowing = false;
    var showdialog = function(OkButton, message) {
        if (dialog) {
            if (!!OkButton != okbuttonshowing) { // The !! construct turns OkButton into a boolean
                dialog.hide();
                dialog = null;
            }
        }
        if (dialog) {
            dialog.setContent(message);
        } else {
            if (OkButton) {
                dialog = new Blockly.ReplMgr.Dialog("Software Update", message, OkButton, null, 0,
                                                    function() { dialog.hide();});
                okbuttonshowing = true;
            } else {
                dialog = new Blockly.ReplMgr.Dialog("Software Update", message, null, null, 0, undefined);
                dialog.display();
                okbuttonshowing = false;
            }
        }
    };
    var hidedialog = function() {
        if (dialog) {
            dialog.hide();
        }
    };

    // End of Dialog management code

    var reset = function() {
        // Reset companion state
        rs.state = Blockly.ReplMgr.rsState.IDLE;
        rs.connection = null;
        rs.didversioncheck = false;
        context.resetYail();
        top.BlocklyPanel_indicateDisconnect();
        // End reset companion state
    };

    var fail = function(message) {
        showdialog("Ok", message);
        reset();
    };

    encoder.add('package', 'update.apk');
    var qs = encoder.toString();
    fetchconn.open("GET", top.COMPANION_UPDATE_URL, true);
    fetchconn.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            try {
                showdialog("Got It", 'The update is now being installed on your device. Watch your device (or emulator) screen and approve the software installation when prompted.<br /><br />IMPORTANT: When the update finishes, choose "DONE" (don\'t click "open"). Then go to App Inventor in your web browser, click the "Connect" menu and choose "Reset Connection".');
                Blockly.ReplMgr.putAsset("update.apk", goog.crypt.base64.decodeStringToByteArray(this.response),
                                         function() {
                                             // Trigger Update Here
                                             console.log("Update: Downloaded");
                                             var conn = goog.net.XmlHttp();
                                             conn.open("POST", rs.baseurl + "_package", true);
                                             conn.onreadystatechange = function() {
                                                 if (this.readyState == 4 && this.status == 200) {
                                                     console.log("Update: _package success");
                                                 }
                                             };
                                             conn.send(qs);
                                         },
                                         function() {
                                             fail("Unable to send update to device/emulator");
                                         }, true);
            } catch (err) {     // Most likely a decoding error from goog.crypt.base64...
                fail("Unable to load update from App Inventor server");
            }
        } else if (this.readyState == 4) {
            fail("Unable to load update from App Inventor server (server not responding)");
        }
    };
    showdialog(false, "We are now downloading update from the App Inventor Server, please standby");
    fetchconn.send();
};

Blockly.ReplMgr.acceptableVersion = function(version) {
    for (var i = 0; i < window.parent.ACCEPTABLE_COMPANIONS.length; i++) {
        if (window.parent.ACCEPTABLE_COMPANIONS[i] == version) {
            return true;
        }
    }
    return false;
};

Blockly.ReplMgr.processRetvals = function(responses) {
    var block;
    for (var i = 0; i < responses.length; i++) {
        var r = responses[i];
        console.log("processRetVals: " + JSON.stringify(r));
        switch(r.type) {
        case "return":
            if (r.blockid != "-1") {
                block = Blockly.mainWorkspace.getBlockById(r.blockid);
                if (r.status == "OK") {
                    block.replError = null;
                    if (r.value && (r.value != '*nothing*')) {
                        this.setDoitResult(block, r.value);
                    }
                } else {
                    if (r.value) {
                        block.replError = "Error from Companion: " + r.value;
                    } else {
                        block.replError = "Error from Companion";
                    }
                }
            }
            break;
        case "pushScreen":
            var success = window.parent.BlocklyPanel_pushScreen(r.screen);
            if (!success) {
                console.log("processRetVals: Invalid Screen: " + r.screen);
            }
            break;
        case "popScreen":
            window.parent.BlocklyPanel_popScreen();
            break;
        case "error":
            if (!this.runtimeError) {
                this.runtimeError = new goog.ui.Dialog(null, true);
            }
            if (this.runtimeError.isVisible()) {
                this.runtimeError.setVisible(false);
            }
            this.runtimeError.setTitle("Runtime Error");
            this.runtimeError.setButtonSet(new goog.ui.Dialog.ButtonSet().
                                           addButton({caption:"Dismiss"}, false, true));
            this.runtimeError.setContent(r.value + "<br/><i>Note:</i>&nbsp;You will not see another error reported for 5 seconds.");
            this.runtimeError.setVisible(true);
        }
    }
    Blockly.WarningHandler.checkAllBlocksForWarningsAndErrors();
};

Blockly.ReplMgr.setDoitResult = function(block, value) {
    var patt = /Do It Result:.*?\n---\n/m;
    var comment = "";
    var result = 'Do It Result: ' + value + '\n---\n';
    if (block.comment) {
        comment = block.comment.getText();
    }
    if (!comment) {
        comment = result;
    } else {
        if (patt.test(comment)) { // Already a doit there!
            comment = comment.replace(patt, result);
        } else {
            comment = result + comment;
        }
    }
    // If we don't set visible to false, the comment
    // doesn't always change when it should...
    if (block.comment) {
        block.comment.setVisible(false);
    }
    block.setCommentText(comment);
    block.comment.setVisible(true);
};

Blockly.ReplMgr.startAdbDevice = function(rs, usb) {
    var first = true;
    var context = this;
    var counter = 0;            // Used to for counting down
    var ubercounter = 0;        // Used to keep track of how many times we
                                // have attempted to start the emulator
    var ubergiveup = 4;         // How many attempts to start the emulator
    var pc = 0;                 // Use to keep track of state
    var dialog = null;          // We have one dialog for the block
                                // so we don't create multiple ones
    var udialog = null;         // Dialog to tell the user to plug phone in
    var progdialog = null;      // Tell the end-user about our progress
    var interval;               // Our interval id, used to stop the train
    var device;
    var message;
    if (usb) {
        message = 'Connecting via USB Cable';
    } else {
        message = 'Starting the Android Emulator';
    }
    progdialog = new Blockly.ReplMgr.Dialog("Connecting...", message, "Cancel", null, 0, function() {
        progdialog.hide();
        clearInterval(interval);
        window.parent.ReplState.state = Blockly.ReplMgr.rsState.IDLE;
        window.parent.BlocklyPanel_indicateDisconnect();
        if (dialog) {
            dialog.hide();
            dialog = null;
        }
    });
    var timeout = function() {
        clearInterval(interval);    // Stop polling
        dialog = new Blockly.ReplMgr.Dialog("Connection Failure", "We could not start the MIT AI Companion within the Emulator", "OK", null, 0, function() {
            dialog.hide();
            dialog = null;
            if (progdialog) {
                progdialog.hide();
                progdialog = null;
            }
            top.ReplState.state = Blockly.ReplMgr.rsState.IDLE;
            top.ReplState.connection = null;
            top.BlocklyPanel_indicateDisconnect();
            context.resetYail();
            context.hardreset(context.formName);});
    };

    // 0 == starting emulator
    // 1 == Counting down after emulator started
    // 2 == Counting down after repl start requested
    // 3 == Done (nothing to do), interval should be cleared
    interval = setInterval(function() {
        var xhr;
        switch(pc) {
        case 0:
            xhr = goog.net.XmlHttp();
            xhr.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    var result = goog.json.parse(this.response);
                    if (result.status == "OK") { // We're running!
                        device = result.device;    // the device we are going to talk to
                        console.log("ReplMgr: set device = " + device);
                        pc = 1;                    // Next State
                        if (usb) {
                            counter = 6;               // Wait five seconds for usb
                        } else {
                            counter = 21;              // Wait twenty seconds for the emulator
                        }
                        if (udialog) {             // Get rid of dialog he/she plugged in the cable!
                            udialog.hide();
                            udialog = null;
                        }
                    } else {
                        if (first && !usb) { // Need to actually start the thing!
                            var xhr = goog.net.XmlHttp();
                            xhr.open("GET", "http://localhost:8004/start/", true); // We don't look at the response
                            xhr.send();
                            first = false;
                        } else if (first) { // USB
                            udialog = new Blockly.ReplMgr.Dialog("Plugged In?", "AI2 does not see your device, make sure the cable is plugged in and drivers are correct.", "OK", null, 0, function() { udialog.hide(); udialog = null;});
                            first = false;
                        }
                    }
                } else if (this.readyState == 4) {
                    // readyState is 4 but status isn't 200 is daemon running?
                    clearInterval(interval);
                    if (progdialog) {
                        progdialog.hide();
                        progdialog = null;
                    }
                    if (!dialog) {
                        window.parent.BlocklyPanel_indicateDisconnect();
                        dialog = new Blockly.ReplMgr.Dialog("Helper?", 'The aiStarter helper does not appear to be running<br /><a href="http://appinventor.mit.edu" target="_blank">Need Help?</a>', "OK", null, 0, function() {
                            dialog.hide();
                            dialog = null;
                            if (progdialog) {
                                progdialog.hide();
                                progdialog = null;
                            }
                            window.parent.ReplState.state = Blockly.ReplMgr.rsState.IDLE;
                        });
                    }
                }
            };
            if (usb) {
                xhr.open("GET", "http://localhost:8004/ucheck/", true);
            } else {
                xhr.open("GET", "http://localhost:8004/echeck/", true);
            }
            xhr.send();
            break;
        case 1:
            counter -= 1;
            if (usb) {
                progdialog.setContent("USB Connected, waiting " + counter + " seconds to ensure all is running.");
            } else {
                progdialog.setContent("Emulator started, waiting " + counter + " seconds to ensure all is running.");
            }
            if (counter <= 0) {
                if (usb) {
                    progdialog.setContent("Starting the Companion App on the connected phone.");
                } else {
                    progdialog.setContent("Starting the Companion App in the emulator.");
                }
                pc = 2;
                counter = 6;
                ubercounter = 0;
                xhr = goog.net.XmlHttp();
                xhr.open("GET", "http://localhost:8004/replstart/" + device, true); // Don't look at response
                xhr.send();
            }
            break;
        case 2:
            counter -= 1;
            if (counter > 0) {
                progdialog.setContent("Companion started, waiting " + counter + " seconds to ensure all is running.");
            } else {
                progdialog.setContent("Verifying that the Companion Started....");
                xhr = goog.net.XmlHttp();
                xhr.timeout = 4000; // 4 seconds
                xhr.open("GET", rs.versionurl, true);
                xhr.onreadystatechange = function() {
                    if (this.readyState == 4) {
                        if (this.status == 200) {
                            pc = 4; // We got a response!
                            return;
                        } else {
                            ubercounter += 1;
                            if (ubercounter > ubergiveup) { // It's never going to work!
                                timeout();
                            } else {
                                // We didn't work yet, add some time and go back to state 2
                                counter = 5; // Wait 5 more seconds
                                pc = 2;
                            }
                        }
                    }
                };
                xhr.send();
                pc = 3;
            }
            break;
        case 3:
            break;              // We don't do anything in this state
                                // we are waiting for the version check (noop) to finish
        case 4:
            progdialog.hide();
            rs.state = context.rsState.CONNECTED; // Indicate that we are good to go!
            clearInterval(interval);
            window.parent.BlocklyPanel_blocklyWorkspaceChanged(context.formName);
        }
    }, 1000);                   // We poll once per second
};

// Convert non-ASCII Characters to kawa unicode escape
Blockly.ReplMgr.quoteUnicode = function(input) {
    if (!input)
        return null;
    var sb = [];
    var len = input.length;
    for (var i = 0; i < len; i++) {
        var u = input.charCodeAt(i); // Unicode of the character
        if (u < ' '.charCodeAt(0) || u > '~'.charCodeAt(0)) {
          // Replace any special chars with \u1234 unicode
            var hex = "000" + u.toString(16);
            hex = hex.substring(hex.length - 4);
            sb.push("\\u" + hex);
        } else {
            sb.push(input.charAt(i));
        }
    }
    return sb.join("");
};

Blockly.ReplMgr.startRepl = function(already, emulator, usb) {
    var refreshAssets = window.parent.AssetManager_refreshAssets;
    var rs = window.parent.ReplState;
    rs.didversioncheck = false; // Re-check
    if (rs.phoneState) {
        rs.phoneState.initialized = false; // Make sure we re-send the yail to the Companion
    }
    if (!already) {
        if (window.parent.ReplState.state != this.rsState.IDLE) // If we are not idle, we don't do anything!
            return;
        if (emulator || usb) {         // If we are talking to the emulator, don't use rendezvou server
            this.startAdbDevice(rs, usb);
            rs.state = this.rsState.WAITING; // Wait for the emulator to start
            rs.replcode = "emulator";          // Must match code in Companion Source
            rs.url = 'http://127.0.0.1:8001/_newblocks';
            rs.rurl = 'http://127.0.0.1:8001/_values';
            rs.versionurl = 'http://127.0.0.1:8001/_getversion';
            rs.baseurl = 'http://127.0.0.1:8001/';
            rs.seq_count = 1;
            rs.count = 0;
            this.rendPoll();
            refreshAssets(this.formName);
            return;             // All done
        }
        rs = window.parent.ReplState;
        rs.state = this.rsState.RENDEZVOUS; // We are now rendezvousing
        rs.replcode = this.genCode();
        rs.rendezvouscode = this.sha1(rs.replcode);
        rs.seq_count = 1;          // used for the creating the hmac mac
        rs.count = 0;
        rs.dialog = new Blockly.ReplMgr.Dialog("Connect to Companion", this.makeDialogMessage(rs.replcode), "Cancel", null, 1, function() {
            rs.dialog.hide();
            rs.state = Blockly.ReplMgr.rsState.IDLE; // We're punting
            rs.connection = null;
            window.parent.BlocklyPanel_indicateDisconnect();
        });
        this.getFromRendezvous();
    } else {
        if (window.parent.ReplState.state == this.rsState.RENDEZVOUS) {
            window.parent.ReplState.dialog.hide();
        }
        this.resetYail();
        window.parent.ReplState.state = this.rsState.IDLE;
        this.hardreset(this.formName);       // Tell aiStarter to kill off adb
    }
};

Blockly.ReplMgr.genCode = function() {
    var retval = '';
    for (var i = 0; i < 6; i++) {
        retval = retval + String.fromCharCode(Math.floor(Math.random()*26) + 97);
    }
    return retval;
};

// Request ipAddress information from the Rendezvous Server
Blockly.ReplMgr.getFromRendezvous = function() {
    var xmlhttp = goog.net.XmlHttp();
    if (window.parent.ReplState === undefined || window.parent.ReplState === null) {
        console.log('getFromRendezvous: replState not set yet.');
        return;
    }
    var rs = window.parent.ReplState;
    var context = this;
    var refreshAssets = window.parent.AssetManager_refreshAssets; // This is where GWT puts this
    var poller = function() {                                     // So "this" is correct when called
        context.rendPoll.call(context);                           // from setTimeout
    };
    xmlhttp.open('GET', 'http://rendezvous.appinventor.mit.edu/rendezvous/' + rs.rendezvouscode, true);
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && this.status == 200) {
            try {
                var json = goog.json.parse(xmlhttp.response);
                rs.url = 'http://' + json.ipaddr + ':8001/_newblocks';
                rs.rurl = 'http://' + json.ipaddr + ':8001/_values';
                rs.versionurl = 'http://' + json.ipaddr + ':8001/_getversion';
                rs.baseurl = 'http://' + json.ipaddr + ':8001/';
                rs.state = Blockly.ReplMgr.rsState.CONNECTED;
                rs.dialog.hide();
                window.parent.BlocklyPanel_blocklyWorkspaceChanged(context.formName);
                  // Start the connection with the Repl itself
                refreshAssets(context.formName);    // Start assets loading
            } catch (err) {
                console.log("getFromRendezvous(): Error: " + err);
                setTimeout(poller, 2000); // Queue next attempt
            }
        }
    };
    xmlhttp.send();
};

// Called by the main poller function. Manages the state transitions for polling
// The rendezvous server
Blockly.ReplMgr.rendPoll = function() {
    var dialog;
    if (window.parent.ReplState.state == this.rsState.RENDEZVOUS) {
        window.parent.ReplState.count = window.parent.ReplState.count + 1;
        if (window.parent.ReplState.count > 40) {
            window.parent.ReplState.state = this.rsState.IDLE;
            window.parent.ReplState.dialog.hide(); // Punt the dialog
            dialog = new Blockly.ReplMgr.Dialog('Connection Failure', 'Failed to Connect to the MIT AI2 Companion, try again.', "OK", null, 0, function() {
                dialog.hide();
            });
            window.parent.ReplState.url = null;
            window.parent.BlocklyPanel_indicateDisconnect();
        }
        this.getFromRendezvous();
    }
};

// Blockly.ReplMgr.Dialog -- A way to get GWT Dialogs to appear from the top window.
// There is some hair here because we need this code to work both when the GWT code is
// compiled and optimized and when this code is compiled with the closure compiler.
// So we call up to GWT to create the actual dialog, hide the dialog and change the
// dialog's content. We pass the callback as a GWT "JavaScriptObject" which is then
// passed back to javascript for actual evaluation. The way we do this results in no
// argument being passed to the callback. If in the future we need to pass an arugment
// we can worry about adding that functionality.

Blockly.ReplMgr.Dialog = function(title, content, buttonName, cancelButtonName, size, callback) {
    this.title = title;
    this.content = content;
    this.size = size;
    this.buttonName = buttonName;
    this.cancelButtonName = cancelButtonName;
    this.callback = callback;
    if (this.buttonName) {
        this.display();
    }
};

Blockly.ReplMgr.Dialog.prototype = {
    'display' : function() {
        this._dialog = window.parent.BlocklyPanel_createDialog(this.title, this.content, this.buttonName, this.cancelButtonName, this.size, this.callback);
    },
    'hide' : function() {
        if (this._dialog) {
            top.BlocklyPanel_hideDialog(this._dialog);
            this._dialog = null;
        }
    },
    'setContent' : function(message) {
        if (this._dialog) {
            top.BlocklyPanel_setDialogContent(this._dialog, message);
        }
    }
};

Blockly.ReplMgr.makeDialogMessage = function(code) {
    var qr = this.qrcode(1, 'L');
    qr.addData(code);
    qr.make();
    var img = qr.createImgTag(6);
    retval = '<table><tr><td>' + img + '</td><td><font size="+1">Your code is:<br /><br /><font size="+1"><b>' + code + '</b></font></font></td></tr></table>';
    return retval;
};

Blockly.ReplMgr.hmac = function(input) {
    var googhash = new goog.crypt.Hmac(new goog.crypt.Sha1(), this.string_to_bytes(window.parent.ReplState.replcode), 64);
    return(this.bytes_to_hexstring(googhash.getHmac(this.string_to_bytes(input))));
};

Blockly.ReplMgr.sha1 = function(input) {
    var hasher = new goog.crypt.Sha1();
    hasher.update(this.string_to_bytes(input));
    return(this.bytes_to_hexstring(hasher.digest()));
};

Blockly.ReplMgr.string_to_bytes = function(input) {
    var z = [];
    for (var i = 0; i < input.length; i++ )
        z.push(input.charCodeAt(i));
    return z;
};

Blockly.ReplMgr.bytes_to_hexstring = function(input) {
    var z = [];
    for (var i = 0; i < input.length; i++ )
        z.push(Number(256 + input[i]).toString(16).substring(1, 3));
    return z.join("");
};

Blockly.ReplMgr.putAsset = function(filename, blob, success, fail, force) {
    if (window.parent.ReplState === undefined)
        return false;
    if (!force && (window.parent.ReplState.state != this.rsState.CONNECTED))
        return false;           // We didn't really do anything
    var conn = goog.net.XmlHttp();
    var rs = window.parent.ReplState;
    var encoder = new goog.Uri.QueryData();
    var z = filename.split('/'); // Remove any directory components
    encoder.add('filename', z[z.length-1]);
    conn.open('PUT', rs.baseurl + '?' + encoder.toString(), true);
    conn.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            if (success) {      // process callbacks
                success();
            }
        } else if (this.readyState == 4) {
            if (fail) {
                fail();
            }
        }
    };

    var arraybuf = new ArrayBuffer(blob.length);
    var arrayview = new Uint8Array(arraybuf);
    for (var i = 0; i < blob.length; i++) {
        arrayview[i] = blob[i];
    }
    conn.send(arraybuf);
    return true;
};

Blockly.ReplMgr.hardreset = function(formName, callback) {
    window.parent.AssetManager_reset(formName); // Reset the notion of what assets
                                                // are loaded.
    var xhr = goog.net.XmlHttp();
    xhr.open("GET", "http://localhost:8004/reset/", true);
    xhr.onreadystatechange = function() {
        if (this.readyState == 4) {
            if (callback) {     // Always call the callback
                callback(this.status);
            }
        }
    };
    xhr.send();
};

// ehardreset -- Reset connections and then tell aiStarter to
// run the reset-emulator script. This will reset things to their
// "factory" defaults.

Blockly.ReplMgr.ehardreset = function(formName) {
    var context = this;
    var dialog = new Blockly.ReplMgr.Dialog("Do You Really?", 'This will attempt to reset your Emulator to its "factory" state. If you had previously updated the Companion installed in the Emulator, you will likely have to do this again.', "OK", "Cancel", 0, function(response) {
        dialog.hide();
        if (response == "OK") {
            context.hardreset(formName, function() {
                var xhr = goog.net.XmlHttp();
                xhr.open("GET", "http://localhost:8004/emulatorreset/", true);
                xhr.onreadystatchange = function() {}; // Ignore errors
                xhr.send();
            });
        }
    });
};

// Make a QRCode in an image tag. This is currently used by the
// Build->Show QR Code action. Frankly this code shouldn't be in
// replmgr.js, but we don't have another convenient place for it so
// here it is. This is called from Blockly Panel because we need to
// know the current form in order to find the Blockly instance that is
// live.

Blockly.ReplMgr.makeqrcode = function(instring) {
    var q = this.qrcode(4, 'L'); // First try a type 4 code
    var retval;
    q.addData(instring);
    try {
        q.make();
    } catch (e) {
        q = this.qrcode(5, 'L'); // OK, that failed try type 5
        q.addData(instring);
        q.make();
    }
    return q.createImgTag(4);
};

//---------------------------------------------------------------------
//
// QR Code Generator for JavaScript
//
// Copyright (c) 2009 Kazuhiko Arase
//
// URL: http://www.d-project.com/
//
// Licensed under the MIT license:
//      http://www.opensource.org/licenses/mit-license.php
//
// The word 'QR Code' is registered trademark of
// DENSO WAVE INCORPORATED
//      http://www.denso-wave.com/qrcode/faqpatent-e.html
//
//---------------------------------------------------------------------

Blockly.ReplMgr.qrcode = function() {

    //---------------------------------------------------------------------
    // qrcode
    //---------------------------------------------------------------------

    /**
     * qrcode
     * @param typeNumber 1 to 10
     * @param errorCorrectLevel 'L','M','Q','H'
     */
    var qrcode = function(typeNumber, errorCorrectLevel) {

        var PAD0 = 0xEC;
        var PAD1 = 0x11;

        var _typeNumber = typeNumber;
        var _errorCorrectLevel = QRErrorCorrectLevel[errorCorrectLevel];
        var _modules = null;
        var _moduleCount = 0;
        var _dataCache = null;
        var _dataList = [];

        var _this = {};

        var makeImpl = function(test, maskPattern) {

            _moduleCount = _typeNumber * 4 + 17;
            _modules = function(moduleCount) {
                var modules = new Array(moduleCount);
                for (var row = 0; row < moduleCount; row += 1) {
                    modules[row] = new Array(moduleCount);
                    for (var col = 0; col < moduleCount; col += 1) {
                        modules[row][col] = null;
                    }
                }
                return modules;
            }(_moduleCount);

            setupPositionProbePattern(0, 0);
            setupPositionProbePattern(_moduleCount - 7, 0);
            setupPositionProbePattern(0, _moduleCount - 7);
            setupPositionAdjustPattern();
            setupTimingPattern();
            setupTypeInfo(test, maskPattern);

            if (_typeNumber >= 7) {
                setupTypeNumber(test);
            }

            if (_dataCache === null) {
                _dataCache = createData(_typeNumber, _errorCorrectLevel, _dataList);
            }

            mapData(_dataCache, maskPattern);
        };

        var setupPositionProbePattern = function(row, col) {

            for (var r = -1; r <= 7; r += 1) {

                if (row + r <= -1 || _moduleCount <= row + r) continue;

                for (var c = -1; c <= 7; c += 1) {

                    if (col + c <= -1 || _moduleCount <= col + c) continue;

                    if ( (0 <= r && r <= 6 && (c === 0 || c == 6) ) ||
                         (0 <= c && c <= 6 && (r === 0 || r == 6) ) ||
                         (2 <= r && r <= 4 && 2 <= c && c <= 4) ) {
                        _modules[row + r][col + c] = true;
                    } else {
                        _modules[row + r][col + c] = false;
                    }
                }
            }
        };

        var getBestMaskPattern = function() {

            var minLostPoint = 0;
            var pattern = 0;

            for (var i = 0; i < 8; i += 1) {

                makeImpl(true, i);

                var lostPoint = QRUtil.getLostPoint(_this);

                if (i === 0 || minLostPoint > lostPoint) {
                    minLostPoint = lostPoint;
                    pattern = i;
                }
            }

            return pattern;
        };

        var setupTimingPattern = function() {

            for (var r = 8; r < _moduleCount - 8; r += 1) {
                if (_modules[r][6] !== null) {
                    continue;
                }
                _modules[r][6] = (r % 2 === 0);
            }

            for (var c = 8; c < _moduleCount - 8; c += 1) {
                if (_modules[6][c] !== null) {
                    continue;
                }
                _modules[6][c] = (c % 2 === 0);
            }
        };

        var setupPositionAdjustPattern = function() {

            var pos = QRUtil.getPatternPosition(_typeNumber);

            for (var i = 0; i < pos.length; i += 1) {

                for (var j = 0; j < pos.length; j += 1) {

                    var row = pos[i];
                    var col = pos[j];

                    if (_modules[row][col] !== null) {
                        continue;
                    }

                    for (var r = -2; r <= 2; r += 1) {

                        for (var c = -2; c <= 2; c += 1) {

                            if (r == -2 || r == 2 || c == -2 || c == 2 ||
                               (r === 0 && c === 0) ) {
                                _modules[row + r][col + c] = true;
                            } else {
                                _modules[row + r][col + c] = false;
                            }
                        }
                    }
                }
            }
        };

        var setupTypeNumber = function(test) {

            var bits = QRUtil.getBCHTypeNumber(_typeNumber);
            var i;
            var mod;

            for (i = 0; i < 18; i += 1) {
                mod = (!test && ( (bits >> i) & 1) == 1);
                _modules[Math.floor(i / 3)][i % 3 + _moduleCount - 8 - 3] = mod;
            }

            for (i = 0; i < 18; i += 1) {
                mod = (!test && ( (bits >> i) & 1) == 1);
                _modules[i % 3 + _moduleCount - 8 - 3][Math.floor(i / 3)] = mod;
            }
        };

        var setupTypeInfo = function(test, maskPattern) {

            var data = (_errorCorrectLevel << 3) | maskPattern;
            var bits = QRUtil.getBCHTypeInfo(data);
            var i;
            var mod;

            // vertical
            for (i = 0; i < 15; i += 1) {

                mod = (!test && ( (bits >> i) & 1) == 1);

                if (i < 6) {
                    _modules[i][8] = mod;
                } else if (i < 8) {
                    _modules[i + 1][8] = mod;
                } else {
                    _modules[_moduleCount - 15 + i][8] = mod;
                }
            }

            // horizontal
            for (i = 0; i < 15; i += 1) {

                mod = (!test && ( (bits >> i) & 1) == 1);

                if (i < 8) {
                    _modules[8][_moduleCount - i - 1] = mod;
                } else if (i < 9) {
                    _modules[8][15 - i - 1 + 1] = mod;
                } else {
                    _modules[8][15 - i - 1] = mod;
                }
            }

            // fixed module
            _modules[_moduleCount - 8][8] = (!test);
        };

        var mapData = function(data, maskPattern) {

            var inc = -1;
            var row = _moduleCount - 1;
            var bitIndex = 7;
            var byteIndex = 0;
            var maskFunc = QRUtil.getMaskFunction(maskPattern);

            for (var col = _moduleCount - 1; col > 0; col -= 2) {

                if (col == 6) col -= 1;

                while (true) {

                    for (var c = 0; c < 2; c += 1) {

                        if (_modules[row][col - c] === null) {

                            var dark = false;

                            if (byteIndex < data.length) {
                                dark = ( ( (data[byteIndex] >>> bitIndex) & 1) == 1);
                            }

                            var mask = maskFunc(row, col - c);

                            if (mask) {
                                dark = !dark;
                            }

                            _modules[row][col - c] = dark;
                            bitIndex -= 1;

                            if (bitIndex == -1) {
                                byteIndex += 1;
                                bitIndex = 7;
                            }
                        }
                    }

                    row += inc;

                    if (row < 0 || _moduleCount <= row) {
                        row -= inc;
                        inc = -inc;
                        break;
                    }
                }
            }
        };

        var createBytes = function(buffer, rsBlocks) {

            var offset = 0;

            var maxDcCount = 0;
            var maxEcCount = 0;
            var i,r;

            var dcdata = new Array(rsBlocks.length);
            var ecdata = new Array(rsBlocks.length);

            for (r = 0; r < rsBlocks.length; r += 1) {

                var dcCount = rsBlocks[r].dataCount;
                var ecCount = rsBlocks[r].totalCount - dcCount;

                maxDcCount = Math.max(maxDcCount, dcCount);
                maxEcCount = Math.max(maxEcCount, ecCount);

                dcdata[r] = new Array(dcCount);

                for (i = 0; i < dcdata[r].length; i += 1) {
                    dcdata[r][i] = 0xff & buffer.getBuffer()[i + offset];
                }
                offset += dcCount;

                var rsPoly = QRUtil.getErrorCorrectPolynomial(ecCount);
                var rawPoly = qrPolynomial(dcdata[r], rsPoly.getLength() - 1);

                var modPoly = rawPoly.mod(rsPoly);
                ecdata[r] = new Array(rsPoly.getLength() - 1);
                for (i = 0; i < ecdata[r].length; i += 1) {
                    var modIndex = i + modPoly.getLength() - ecdata[r].length;
                    ecdata[r][i] = (modIndex >= 0)? modPoly.get(modIndex) : 0;
                }
            }

            var totalCodeCount = 0;
            for (i = 0; i < rsBlocks.length; i += 1) {
                totalCodeCount += rsBlocks[i].totalCount;
            }

            var data = new Array(totalCodeCount);
            var index = 0;

            for (i = 0; i < maxDcCount; i += 1) {
                for (r = 0; r < rsBlocks.length; r += 1) {
                    if (i < dcdata[r].length) {
                        data[index] = dcdata[r][i];
                        index += 1;
                    }
                }
            }

            for (i = 0; i < maxEcCount; i += 1) {
                for (r = 0; r < rsBlocks.length; r += 1) {
                    if (i < ecdata[r].length) {
                        data[index] = ecdata[r][i];
                        index += 1;
                    }
                }
            }

            return data;
        };

        var createData = function(typeNumber, errorCorrectLevel, dataList) {

            var rsBlocks = QRRSBlock.getRSBlocks(typeNumber, errorCorrectLevel);
            var i;

            var buffer = qrBitBuffer();

            for (i = 0; i < dataList.length; i += 1) {
                var data = dataList[i];
                buffer.put(data.getMode(), 4);
                buffer.put(data.getLength(), QRUtil.getLengthInBits(data.getMode(), typeNumber) );
                data.write(buffer);
            }

            // calc num max data.
            var totalDataCount = 0;
            for (i = 0; i < rsBlocks.length; i += 1) {
                totalDataCount += rsBlocks[i].dataCount;
            }

            if (buffer.getLengthInBits() > totalDataCount * 8) {
                throw new Error('code length overflow. (' +
                                buffer.getLengthInBits() +
                                '>' +
                                totalDataCount * 8 +
                                ')');
            }

            // end code
            if (buffer.getLengthInBits() + 4 <= totalDataCount * 8) {
                buffer.put(0, 4);
            }

            // padding
            while (buffer.getLengthInBits() % 8 !== 0) {
                buffer.putBit(false);
            }

            // padding
            while (true) {

                if (buffer.getLengthInBits() >= totalDataCount * 8) {
                    break;
                }
                buffer.put(PAD0, 8);

                if (buffer.getLengthInBits() >= totalDataCount * 8) {
                    break;
                }
                buffer.put(PAD1, 8);
            }

            return createBytes(buffer, rsBlocks);
        };

        _this.addData = function(data) {
            var newData = qr8BitByte(data);
            _dataList.push(newData);
            _dataCache = null;
        };

        _this.isDark = function(row, col) {
            if (row < 0 || _moduleCount <= row || col < 0 || _moduleCount <= col) {
                throw new Error(row + ',' + col);
            }
            return _modules[row][col];
        };

        _this.getModuleCount = function() {
            return _moduleCount;
        };

        _this.make = function() {
            makeImpl(false, getBestMaskPattern() );
        };

        _this.createTableTag = function(cellSize, margin) {

            cellSize = cellSize || 2;
            margin = (typeof margin == 'undefined')? cellSize * 4 : margin;

            var qrHtml = '';

            qrHtml += '<table style="';
            qrHtml += ' border-width: 0px; border-style: none;';
            qrHtml += ' border-collapse: collapse;';
            qrHtml += ' padding: 0px; margin: ' + margin + 'px;';
            qrHtml += '">';
            qrHtml += '<tbody>';

            for (var r = 0; r < _this.getModuleCount(); r += 1) {

                qrHtml += '<tr>';

                for (var c = 0; c < _this.getModuleCount(); c += 1) {
                    qrHtml += '<td style="';
                    qrHtml += ' border-width: 0px; border-style: none;';
                    qrHtml += ' border-collapse: collapse;';
                    qrHtml += ' padding: 0px; margin: 0px;';
                    qrHtml += ' width: ' + cellSize + 'px;';
                    qrHtml += ' height: ' + cellSize + 'px;';
                    qrHtml += ' background-color: ';
                    qrHtml += _this.isDark(r, c)? '#000000' : '#ffffff';
                    qrHtml += ';';
                    qrHtml += '"/>';
                }

                qrHtml += '</tr>';
            }

            qrHtml += '</tbody>';
            qrHtml += '</table>';

            return qrHtml;
        };

        _this.createImgTag = function(cellSize, margin) {

            cellSize = cellSize || 2;
            margin = (typeof margin == 'undefined')? cellSize * 4 : margin;

            var size = _this.getModuleCount() * cellSize + margin * 2;
            var min = margin;
            var max = size - margin;

            return createImgTag(size, size, function(x, y) {
                if (min <= x && x < max && min <= y && y < max) {
                    var c = Math.floor( (x - min) / cellSize);
                    var r = Math.floor( (y - min) / cellSize);
                    return _this.isDark(r, c)? 0 : 1;
                } else {
                    return 1;
                }
            } );
        };

        return _this;
    };

    //---------------------------------------------------------------------
    // qrcode.stringToBytes
    //---------------------------------------------------------------------

    qrcode.stringToBytes = function(s) {
        var bytes = [];
        for (var i = 0; i < s.length; i += 1) {
            var c = s.charCodeAt(i);
            bytes.push(c & 0xff);
        }
        return bytes;
    };

    //---------------------------------------------------------------------
    // qrcode.createStringToBytes
    //---------------------------------------------------------------------

    /**
     * @param unicodeData base64 string of byte array.
     * [16bit Unicode],[16bit Bytes], ...
     * @param numChars
     */
    qrcode.createStringToBytes = function(unicodeData, numChars) {

        // create conversion map.

        var unicodeMap = function() {

            var bin = base64DecodeInputStream(unicodeData);
            var read = function() {
                var b = bin.read();
                if (b == -1) throw new Error();
                return b;
            };

            var count = 0;
            var unicodeMap = {};
            while (true) {
                var b0 = bin.read();
                if (b0 == -1) break;
                var b1 = read();
                var b2 = read();
                var b3 = read();
                var k = String.fromCharCode( (b0 << 8) | b1);
                var v = (b2 << 8) | b3;
                unicodeMap[k] = v;
                count += 1;
            }
            if (count != numChars) {
                throw new Error(count + ' != ' + numChars);
            }

            return unicodeMap;
        }();

        var unknownChar = '?'.charCodeAt(0);

        return function(s) {
            var bytes = [];
            for (var i = 0; i < s.length; i += 1) {
                var c = s.charCodeAt(i);
                if (c < 128) {
                    bytes.push(c);
                } else {
                    var b = unicodeMap[s.charAt(i)];
                    if (typeof b == 'number') {
                        if ( (b & 0xff) == b) {
                            // 1byte
                            bytes.push(b);
                        } else {
                            // 2bytes
                            bytes.push(b >>> 8);
                            bytes.push(b & 0xff);
                        }
                    } else {
                        bytes.push(unknownChar);
                    }
                }
            }
            return bytes;
        };
    };

    //---------------------------------------------------------------------
    // QRMode
    //---------------------------------------------------------------------

    var QRMode = {
        MODE_NUMBER :           1 << 0,
        MODE_ALPHA_NUM :        1 << 1,
        MODE_8BIT_BYTE :        1 << 2,
        MODE_KANJI :            1 << 3
    };

    //---------------------------------------------------------------------
    // QRErrorCorrectLevel
    //---------------------------------------------------------------------

    var QRErrorCorrectLevel = {
        L : 1,
        M : 0,
        Q : 3,
        H : 2
    };

    //---------------------------------------------------------------------
    // QRMaskPattern
    //---------------------------------------------------------------------

    var QRMaskPattern = {
        PATTERN000 : 0,
        PATTERN001 : 1,
        PATTERN010 : 2,
        PATTERN011 : 3,
        PATTERN100 : 4,
        PATTERN101 : 5,
        PATTERN110 : 6,
        PATTERN111 : 7
    };

    //---------------------------------------------------------------------
    // QRUtil
    //---------------------------------------------------------------------

    var QRUtil = function() {

        var PATTERN_POSITION_TABLE = [
            [],
            [6, 18],
            [6, 22],
            [6, 26],
            [6, 30],
            [6, 34],
            [6, 22, 38],
            [6, 24, 42],
            [6, 26, 46],
            [6, 28, 50],
            [6, 30, 54],
            [6, 32, 58],
            [6, 34, 62],
            [6, 26, 46, 66],
            [6, 26, 48, 70],
            [6, 26, 50, 74],
            [6, 30, 54, 78],
            [6, 30, 56, 82],
            [6, 30, 58, 86],
            [6, 34, 62, 90],
            [6, 28, 50, 72, 94],
            [6, 26, 50, 74, 98],
            [6, 30, 54, 78, 102],
            [6, 28, 54, 80, 106],
            [6, 32, 58, 84, 110],
            [6, 30, 58, 86, 114],
            [6, 34, 62, 90, 118],
            [6, 26, 50, 74, 98, 122],
            [6, 30, 54, 78, 102, 126],
            [6, 26, 52, 78, 104, 130],
            [6, 30, 56, 82, 108, 134],
            [6, 34, 60, 86, 112, 138],
            [6, 30, 58, 86, 114, 142],
            [6, 34, 62, 90, 118, 146],
            [6, 30, 54, 78, 102, 126, 150],
            [6, 24, 50, 76, 102, 128, 154],
            [6, 28, 54, 80, 106, 132, 158],
            [6, 32, 58, 84, 110, 136, 162],
            [6, 26, 54, 82, 110, 138, 166],
            [6, 30, 58, 86, 114, 142, 170]
        ];
        var G15 = (1 << 10) | (1 << 8) | (1 << 5) | (1 << 4) | (1 << 2) | (1 << 1) | (1 << 0);
        var G18 = (1 << 12) | (1 << 11) | (1 << 10) | (1 << 9) | (1 << 8) | (1 << 5) | (1 << 2) | (1 << 0);
        var G15_MASK = (1 << 14) | (1 << 12) | (1 << 10) | (1 << 4) | (1 << 1);

        var _this = {};

        var getBCHDigit = function(data) {
            var digit = 0;
            while (data !== 0) {
                digit += 1;
                data >>>= 1;
            }
            return digit;
        };

        _this.getBCHTypeInfo = function(data) {
            var d = data << 10;
            while (getBCHDigit(d) - getBCHDigit(G15) >= 0) {
                d ^= (G15 << (getBCHDigit(d) - getBCHDigit(G15) ) );
            }
            return ( (data << 10) | d) ^ G15_MASK;
        };

        _this.getBCHTypeNumber = function(data) {
            var d = data << 12;
            while (getBCHDigit(d) - getBCHDigit(G18) >= 0) {
                d ^= (G18 << (getBCHDigit(d) - getBCHDigit(G18) ) );
            }
            return (data << 12) | d;
        };

        _this.getPatternPosition = function(typeNumber) {
            return PATTERN_POSITION_TABLE[typeNumber - 1];
        };

        _this.getMaskFunction = function(maskPattern) {

            switch (maskPattern) {

            case QRMaskPattern.PATTERN000 :
                return function(i, j) { return (i + j) % 2 === 0; };
            case QRMaskPattern.PATTERN001 :
                return function(i, j) { return i % 2 === 0; };
            case QRMaskPattern.PATTERN010 :
                return function(i, j) { return j % 3 === 0; };
            case QRMaskPattern.PATTERN011 :
                return function(i, j) { return (i + j) % 3 === 0; };
            case QRMaskPattern.PATTERN100 :
                return function(i, j) { return (Math.floor(i / 2) + Math.floor(j / 3) ) % 2 === 0; };
            case QRMaskPattern.PATTERN101 :
                return function(i, j) { return (i * j) % 2 + (i * j) % 3 === 0; };
            case QRMaskPattern.PATTERN110 :
                return function(i, j) { return ( (i * j) % 2 + (i * j) % 3) % 2 === 0; };
            case QRMaskPattern.PATTERN111 :
                return function(i, j) { return ( (i * j) % 3 + (i + j) % 2) % 2 === 0; };

            default :
                throw new Error('bad maskPattern:' + maskPattern);
            }
        };

        _this.getErrorCorrectPolynomial = function(errorCorrectLength) {
            var a = qrPolynomial([1], 0);
            for (var i = 0; i < errorCorrectLength; i += 1) {
                a = a.multiply(qrPolynomial([1, QRMath.gexp(i)], 0) );
            }
            return a;
        };

        _this.getLengthInBits = function(mode, type) {

            if (1 <= type && type < 10) {

                // 1 - 9

                switch(mode) {
                case QRMode.MODE_NUMBER         : return 10;
                case QRMode.MODE_ALPHA_NUM      : return 9;
                case QRMode.MODE_8BIT_BYTE      : return 8;
                case QRMode.MODE_KANJI          : return 8;
                default :
                    throw new Error('mode:' + mode);
                }

            } else if (type < 27) {

                // 10 - 26

                switch(mode) {
                case QRMode.MODE_NUMBER         : return 12;
                case QRMode.MODE_ALPHA_NUM      : return 11;
                case QRMode.MODE_8BIT_BYTE      : return 16;
                case QRMode.MODE_KANJI          : return 10;
                default :
                    throw new Error('mode:' + mode);
                }

            } else if (type < 41) {

                // 27 - 40

                switch(mode) {
                case QRMode.MODE_NUMBER         : return 14;
                case QRMode.MODE_ALPHA_NUM      : return 13;
                case QRMode.MODE_8BIT_BYTE      : return 16;
                case QRMode.MODE_KANJI          : return 12;
                default :
                    throw new Error('mode:' + mode);
                }

            } else {
                throw new Error('type:' + type);
            }
        };

        _this.getLostPoint = function(qrcode) {

            var moduleCount = qrcode.getModuleCount();
            var row, col;

            var lostPoint = 0;

            // LEVEL1

            for (row = 0; row < moduleCount; row += 1) {
                for (col = 0; col < moduleCount; col += 1) {

                    var sameCount = 0;
                    var dark = qrcode.isDark(row, col);

                    for (var r = -1; r <= 1; r += 1) {

                        if (row + r < 0 || moduleCount <= row + r) {
                            continue;
                        }

                        for (var c = -1; c <= 1; c += 1) {

                            if (col + c < 0 || moduleCount <= col + c) {
                                continue;
                            }

                            if (r === 0 && c === 0) {
                                continue;
                            }

                            if (dark == qrcode.isDark(row + r, col + c) ) {
                                sameCount += 1;
                            }
                        }
                    }

                    if (sameCount > 5) {
                        lostPoint += (3 + sameCount - 5);
                    }
                }
            }

            // LEVEL2

            for (row = 0; row < moduleCount - 1; row += 1) {
                for (col = 0; col < moduleCount - 1; col += 1) {
                    var count = 0;
                    if (qrcode.isDark(row, col) ) count += 1;
                    if (qrcode.isDark(row + 1, col) ) count += 1;
                    if (qrcode.isDark(row, col + 1) ) count += 1;
                    if (qrcode.isDark(row + 1, col + 1) ) count += 1;
                    if (count === 0 || count == 4) {
                        lostPoint += 3;
                    }
                }
            }

            // LEVEL3

            for (row = 0; row < moduleCount; row += 1) {
                for (col = 0; col < moduleCount - 6; col += 1) {
                    if (qrcode.isDark(row, col) &&
                        !qrcode.isDark(row, col + 1) &&
                        qrcode.isDark(row, col + 2) &&
                        qrcode.isDark(row, col + 3) &&
                        qrcode.isDark(row, col + 4) &&
                        !qrcode.isDark(row, col + 5) &&
                        qrcode.isDark(row, col + 6) ) {
                        lostPoint += 40;
                    }
                }
            }

            for (col = 0; col < moduleCount; col += 1) {
                for (row = 0; row < moduleCount - 6; row += 1) {
                    if (qrcode.isDark(row, col) &&
                        !qrcode.isDark(row + 1, col) &&
                        qrcode.isDark(row + 2, col) &&
                        qrcode.isDark(row + 3, col) &&
                        qrcode.isDark(row + 4, col) &&
                        !qrcode.isDark(row + 5, col) &&
                        qrcode.isDark(row + 6, col) ) {
                        lostPoint += 40;
                    }
                }
            }

            // LEVEL4

            var darkCount = 0;

            for (col = 0; col < moduleCount; col += 1) {
                for (row = 0; row < moduleCount; row += 1) {
                    if (qrcode.isDark(row, col) ) {
                        darkCount += 1;
                    }
                }
            }

            var ratio = Math.abs(100 * darkCount / moduleCount / moduleCount - 50) / 5;
            lostPoint += ratio * 10;

            return lostPoint;
        };

        return _this;
    }();

    //---------------------------------------------------------------------
    // QRMath
    //---------------------------------------------------------------------

    var QRMath = function() {

        var EXP_TABLE = new Array(256);
        var LOG_TABLE = new Array(256);
        var i;

        // initialize tables
        for (i = 0; i < 8; i += 1) {
            EXP_TABLE[i] = 1 << i;
        }
        for (i = 8; i < 256; i += 1) {
            EXP_TABLE[i] = EXP_TABLE[i - 4]
                ^ EXP_TABLE[i - 5]
                ^ EXP_TABLE[i - 6]
                ^ EXP_TABLE[i - 8];
        }
        for (i = 0; i < 255; i += 1) {
            LOG_TABLE[EXP_TABLE[i] ] = i;
        }

        var _this = {};

        _this.glog = function(n) {

            if (n < 1) {
                throw new Error('glog(' + n + ')');
            }

            return LOG_TABLE[n];
        };

        _this.gexp = function(n) {

            while (n < 0) {
                n += 255;
            }

            while (n >= 256) {
                n -= 255;
            }

            return EXP_TABLE[n];
        };

        return _this;
    }();

    //---------------------------------------------------------------------
    // qrPolynomial
    //---------------------------------------------------------------------

    function qrPolynomial(num, shift) {

        if (typeof num.length == 'undefined') {
            throw new Error(num.length + '/' + shift);
        }

        var _num = function() {
            var offset = 0;
            while (offset < num.length && num[offset] === 0) {
                offset += 1;
            }
            var _num = new Array(num.length - offset + shift);
            for (var i = 0; i < num.length - offset; i += 1) {
                _num[i] = num[i + offset];
            }
            return _num;
        }();

        var _this = {};

        _this.get = function(index) {
            return _num[index];
        };

        _this.getLength = function() {
            return _num.length;
        };

        _this.multiply = function(e) {

            var num = new Array(_this.getLength() + e.getLength() - 1);

            for (var i = 0; i < _this.getLength(); i += 1) {
                for (var j = 0; j < e.getLength(); j += 1) {
                    num[i + j] ^= QRMath.gexp(QRMath.glog(_this.get(i) ) + QRMath.glog(e.get(j) ) );
                }
            }

            return qrPolynomial(num, 0);
        };

        _this.mod = function(e) {
            var i;

            if (_this.getLength() - e.getLength() < 0) {
                return _this;
            }

            var ratio = QRMath.glog(_this.get(0) ) - QRMath.glog(e.get(0) );

            var num = new Array(_this.getLength() );
            for (i = 0; i < _this.getLength(); i += 1) {
                num[i] = _this.get(i);
            }

            for (i = 0; i < e.getLength(); i += 1) {
                num[i] ^= QRMath.gexp(QRMath.glog(e.get(i) ) + ratio);
            }

            // recursive call
            return qrPolynomial(num, 0).mod(e);
        };

        return _this;
    }

    //---------------------------------------------------------------------
    // QRRSBlock
    //---------------------------------------------------------------------

    var QRRSBlock = function() {

        var RS_BLOCK_TABLE = [

            // L
            // M
            // Q
            // H

            // 1
            [1, 26, 19],
            [1, 26, 16],
            [1, 26, 13],
            [1, 26, 9],

            // 2
            [1, 44, 34],
            [1, 44, 28],
            [1, 44, 22],
            [1, 44, 16],

            // 3
            [1, 70, 55],
            [1, 70, 44],
            [2, 35, 17],
            [2, 35, 13],

            // 4
            [1, 100, 80],
            [2, 50, 32],
            [2, 50, 24],
            [4, 25, 9],

            // 5
            [1, 134, 108],
            [2, 67, 43],
            [2, 33, 15, 2, 34, 16],
            [2, 33, 11, 2, 34, 12],

            // 6
            [2, 86, 68],
            [4, 43, 27],
            [4, 43, 19],
            [4, 43, 15],

            // 7
            [2, 98, 78],
            [4, 49, 31],
            [2, 32, 14, 4, 33, 15],
            [4, 39, 13, 1, 40, 14],

            // 8
            [2, 121, 97],
            [2, 60, 38, 2, 61, 39],
            [4, 40, 18, 2, 41, 19],
            [4, 40, 14, 2, 41, 15],

            // 9
            [2, 146, 116],
            [3, 58, 36, 2, 59, 37],
            [4, 36, 16, 4, 37, 17],
            [4, 36, 12, 4, 37, 13],

            // 10
            [2, 86, 68, 2, 87, 69],
            [4, 69, 43, 1, 70, 44],
            [6, 43, 19, 2, 44, 20],
            [6, 43, 15, 2, 44, 16]
        ];

        var qrRSBlock = function(totalCount, dataCount) {
            var _this = {};
            _this.totalCount = totalCount;
            _this.dataCount = dataCount;
            return _this;
        };

        var _this = {};

        var getRsBlockTable = function(typeNumber, errorCorrectLevel) {

            switch(errorCorrectLevel) {
            case QRErrorCorrectLevel.L :
                return RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 0];
            case QRErrorCorrectLevel.M :
                return RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 1];
            case QRErrorCorrectLevel.Q :
                return RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 2];
            case QRErrorCorrectLevel.H :
                return RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 3];
            default :
                return undefined;
            }
        };

        _this.getRSBlocks = function(typeNumber, errorCorrectLevel) {

            var rsBlock = getRsBlockTable(typeNumber, errorCorrectLevel);

            if (typeof rsBlock == 'undefined') {
                throw new Error('bad rs block @ typeNumber:' + typeNumber +
                                '/errorCorrectLevel:' + errorCorrectLevel);
            }

            var length = rsBlock.length / 3;

            var list = [];

            for (var i = 0; i < length; i += 1) {

                var count = rsBlock[i * 3 + 0];
                var totalCount = rsBlock[i * 3 + 1];
                var dataCount = rsBlock[i * 3 + 2];

                for (var j = 0; j < count; j += 1) {
                    list.push(qrRSBlock(totalCount, dataCount) );
                }
            }

            return list;
        };

        return _this;
    }();

    //---------------------------------------------------------------------
    // qrBitBuffer
    //---------------------------------------------------------------------

    var qrBitBuffer = function() {

        var _buffer = [];
        var _length = 0;

        var _this = {};

        _this.getBuffer = function() {
            return _buffer;
        };

        _this.get = function(index) {
            var bufIndex = Math.floor(index / 8);
            return ( (_buffer[bufIndex] >>> (7 - index % 8) ) & 1) == 1;
        };

        _this.put = function(num, length) {
            for (var i = 0; i < length; i += 1) {
                _this.putBit( ( (num >>> (length - i - 1) ) & 1) == 1);
            }
        };

        _this.getLengthInBits = function() {
            return _length;
        };

        _this.putBit = function(bit) {

            var bufIndex = Math.floor(_length / 8);
            if (_buffer.length <= bufIndex) {
                _buffer.push(0);
            }

            if (bit) {
                _buffer[bufIndex] |= (0x80 >>> (_length % 8) );
            }

            _length += 1;
        };

        return _this;
    };

    //---------------------------------------------------------------------
    // qr8BitByte
    //---------------------------------------------------------------------

    var qr8BitByte = function(data) {

        var _mode = QRMode.MODE_8BIT_BYTE;
        var _data = data;
        var _bytes = qrcode.stringToBytes(data);

        var _this = {};

        _this.getMode = function() {
            return _mode;
        };

        _this.getLength = function(buffer) {
            return _bytes.length;
        };

        _this.write = function(buffer) {
            for (var i = 0; i < _bytes.length; i += 1) {
                buffer.put(_bytes[i], 8);
            }
        };

        return _this;
    };

    //=====================================================================
    // GIF Support etc.
    //

    //---------------------------------------------------------------------
    // byteArrayOutputStream
    //---------------------------------------------------------------------

    var byteArrayOutputStream = function() {

        var _bytes = [];

        var _this = {};

        _this.writeByte = function(b) {
            _bytes.push(b & 0xff);
        };

        _this.writeShort = function(i) {
            _this.writeByte(i);
            _this.writeByte(i >>> 8);
        };

        _this.writeBytes = function(b, off, len) {
            off = off || 0;
            len = len || b.length;
            for (var i = 0; i < len; i += 1) {
                _this.writeByte(b[i + off]);
            }
        };

        _this.writeString = function(s) {
            for (var i = 0; i < s.length; i += 1) {
                _this.writeByte(s.charCodeAt(i) );
            }
        };

        _this.toByteArray = function() {
            return _bytes;
        };

        _this.toString = function() {
            var s = '';
            s += '[';
            for (var i = 0; i < _bytes.length; i += 1) {
                if (i > 0) {
                    s += ',';
                }
                s += _bytes[i];
            }
            s += ']';
            return s;
        };

        return _this;
    };

    //---------------------------------------------------------------------
    // base64EncodeOutputStream
    //---------------------------------------------------------------------

    var base64EncodeOutputStream = function() {

        var _buffer = 0;
        var _buflen = 0;
        var _length = 0;
        var _base64 = '';

        var _this = {};

        var writeEncoded = function(b) {
            _base64 += String.fromCharCode(encode(b & 0x3f) );
        };

        var encode = function(n) {
            if (n < 0) {
                // error.
            } else if (n < 26) {
                return 0x41 + n;
            } else if (n < 52) {
                return 0x61 + (n - 26);
            } else if (n < 62) {
                return 0x30 + (n - 52);
            } else if (n == 62) {
                return 0x2b;
            } else if (n == 63) {
                return 0x2f;
            }
            throw new Error('n:' + n);
        };

        _this.writeByte = function(n) {

            _buffer = (_buffer << 8) | (n & 0xff);
            _buflen += 8;
            _length += 1;

            while (_buflen >= 6) {
                writeEncoded(_buffer >>> (_buflen - 6) );
                _buflen -= 6;
            }
        };

        _this.flush = function() {

            if (_buflen > 0) {
                writeEncoded(_buffer << (6 - _buflen) );
                _buffer = 0;
                _buflen = 0;
            }

            if (_length % 3 !== 0) {
                // padding
                var padlen = 3 - _length % 3;
                for (var i = 0; i < padlen; i += 1) {
                    _base64 += '=';
                }
            }
        };

        _this.toString = function() {
            return _base64;
        };

        return _this;
    };

    //---------------------------------------------------------------------
    // base64DecodeInputStream
    //---------------------------------------------------------------------

    var base64DecodeInputStream = function(str) {

        var _str = str;
        var _pos = 0;
        var _buffer = 0;
        var _buflen = 0;

        var _this = {};

        _this.read = function() {

            while (_buflen < 8) {

                if (_pos >= _str.length) {
                    if (_buflen === 0) {
                        return -1;
                    }
                    throw new Error('unexpected end of file./' + _buflen);
                }

                var c = _str.charAt(_pos);
                _pos += 1;

                if (c == '=') {
                    _buflen = 0;
                    return -1;
                } else if (c.match(/^\s$/) ) {
                    // ignore if whitespace.
                    continue;
                }

                _buffer = (_buffer << 6) | decode(c.charCodeAt(0) );
                _buflen += 6;
            }

            var n = (_buffer >>> (_buflen - 8) ) & 0xff;
            _buflen -= 8;
            return n;
        };

        var decode = function(c) {
            if (0x41 <= c && c <= 0x5a) {
                return c - 0x41;
            } else if (0x61 <= c && c <= 0x7a) {
                return c - 0x61 + 26;
            } else if (0x30 <= c && c <= 0x39) {
                return c - 0x30 + 52;
            } else if (c == 0x2b) {
                return 62;
            } else if (c == 0x2f) {
                return 63;
            } else {
                throw new Error('c:' + c);
            }
        };

        return _this;
    };

    //---------------------------------------------------------------------
    // gifImage (B/W)
    //---------------------------------------------------------------------

    var gifImage = function(width, height) {

        var _width = width;
        var _height = height;
        var _data = new Array(width * height);

        var _this = {};

        _this.setPixel = function(x, y, pixel) {
            _data[y * _width + x] = pixel;
        };

        _this.write = function(out) {

            //---------------------------------
            // GIF Signature

            out.writeString('GIF87a');

            //---------------------------------
            // Screen Descriptor

            out.writeShort(_width);
            out.writeShort(_height);

            out.writeByte(0x80); // 2bit
            out.writeByte(0);
            out.writeByte(0);

            //---------------------------------
            // Global Color Map

            // black
            out.writeByte(0x00);
            out.writeByte(0x00);
            out.writeByte(0x00);

            // white
            out.writeByte(0xff);
            out.writeByte(0xff);
            out.writeByte(0xff);

            //---------------------------------
            // Image Descriptor

            out.writeString(',');
            out.writeShort(0);
            out.writeShort(0);
            out.writeShort(_width);
            out.writeShort(_height);
            out.writeByte(0);

            //---------------------------------
            // Local Color Map

            //---------------------------------
            // Raster Data

            var lzwMinCodeSize = 2;
            var raster = getLZWRaster(lzwMinCodeSize);

            out.writeByte(lzwMinCodeSize);

            var offset = 0;

            while (raster.length - offset > 255) {
                out.writeByte(255);
                out.writeBytes(raster, offset, 255);
                offset += 255;
            }

            out.writeByte(raster.length - offset);
            out.writeBytes(raster, offset, raster.length - offset);
            out.writeByte(0x00);

            //---------------------------------
            // GIF Terminator
            out.writeString(';');
        };

        var bitOutputStream = function(out) {

            var _out = out;
            var _bitLength = 0;
            var _bitBuffer = 0;

            var _this = {};

            _this.write = function(data, length) {

                if ( (data >>> length) !== 0) {
                    throw new Error('length over');
                }

                while (_bitLength + length >= 8) {
                    _out.writeByte(0xff & ( (data << _bitLength) | _bitBuffer) );
                    length -= (8 - _bitLength);
                    data >>>= (8 - _bitLength);
                    _bitBuffer = 0;
                    _bitLength = 0;
                }

                _bitBuffer = (data << _bitLength) | _bitBuffer;
                _bitLength = _bitLength + length;
            };

            _this.flush = function() {
                if (_bitLength > 0) {
                    _out.writeByte(_bitBuffer);
                }
            };

            return _this;
        };

        var getLZWRaster = function(lzwMinCodeSize) {

            var clearCode = 1 << lzwMinCodeSize;
            var endCode = (1 << lzwMinCodeSize) + 1;
            var bitLength = lzwMinCodeSize + 1;

            // Setup LZWTable
            var table = lzwTable();

            for (var i = 0; i < clearCode; i += 1) {
                table.add(String.fromCharCode(i) );
            }
            table.add(String.fromCharCode(clearCode) );
            table.add(String.fromCharCode(endCode) );

            var byteOut = byteArrayOutputStream();
            var bitOut = bitOutputStream(byteOut);

            // clear code
            bitOut.write(clearCode, bitLength);

            var dataIndex = 0;

            var s = String.fromCharCode(_data[dataIndex]);
            dataIndex += 1;

            while (dataIndex < _data.length) {

                var c = String.fromCharCode(_data[dataIndex]);
                dataIndex += 1;

                if (table.contains(s + c) ) {

                    s = s + c;

                } else {

                    bitOut.write(table.indexOf(s), bitLength);

                    if (table.size() < 0xfff) {

                        if (table.size() == (1 << bitLength) ) {
                            bitLength += 1;
                        }

                        table.add(s + c);
                    }

                    s = c;
                }
            }

            bitOut.write(table.indexOf(s), bitLength);

            // end code
            bitOut.write(endCode, bitLength);

            bitOut.flush();

            return byteOut.toByteArray();
        };

        var lzwTable = function() {

            var _map = {};
            var _size = 0;

            var _this = {};

            _this.add = function(key) {
                if (_this.contains(key) ) {
                    throw new Error('dup key:' + key);
                }
                _map[key] = _size;
                _size += 1;
            };

            _this.size = function() {
                return _size;
            };

            _this.indexOf = function(key) {
                return _map[key];
            };

            _this.contains = function(key) {
                return typeof _map[key] != 'undefined';
            };

            return _this;
        };

        return _this;
    };

    var createImgTag = function(width, height, getPixel, alt) {

        var gif = gifImage(width, height);
        for (var y = 0; y < height; y += 1) {
            for (var x = 0; x < width; x += 1) {
                gif.setPixel(x, y, getPixel(x, y) );
            }
        }

        var b = byteArrayOutputStream();
        gif.write(b);

        var base64 = base64EncodeOutputStream();
        var bytes = b.toByteArray();
        for (var i = 0; i < bytes.length; i += 1) {
            base64.writeByte(bytes[i]);
        }
        base64.flush();

        var img = '';
        img += '<img';
        img += '\u0020src="';
        img += 'data:image/gif;base64,';
        img += base64;
        img += '"';
        img += '\u0020width="';
        img += width;
        img += '"';
        img += '\u0020height="';
        img += height;
        img += '"';
        if (alt) {
            img += '\u0020alt="';
            img += alt;
            img += '"';
        }
        img += '/>';

        return img;
    };

    //---------------------------------------------------------------------
    // returns qrcode function.

    return qrcode;
}();
