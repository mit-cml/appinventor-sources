# Web Runtime (Experimental)

This directory provides App Inventor's experimental web runtime implementation.

## Prerequisites

In addition to the standard prerequisites for building App Inventor, you also need to have Gambit Scheme installed on your machine and the `gsc` executable on your PATH. For macOS, this can be accomplished via Homebrew with `brew install gambitscheme`. For other ways of installing Gambitscheme, please see the [Gambitscheme](https://gambitscheme.org/) website.

## Building & Serving

1. In a terminal, cd into this directory.
2. Run `ant` to compile the system
3. Run `python3 -m http.server` to serve the runtime

## Connecting

The appengine module includes a new entry in the Connect menu titled "Test in Web". Selecting this option will open a new window hosting the runtime at localhost port 8000.

## Known Issues / Limitations

Currently, an error raised by the runtime will cause all future Scheme code execution to enter a pending state. The only way around this is to reset the connection and start a new emulator session.

The following components have been implemented, to varying degrees of completion. This list is subject to change frequently:

- Button (only certain properties)
- ChatBot (Converse only)
- Clock (Date calculation blocks tbi)
- Label
- ListPicker (just to test opening other activities)
- Notifier (Dialogs need work)
- Sound (Cleanup of old sounds doesn't work)
- SpeechRecognizer (for browsers that support the Web Speech Recognition API, e.g., Chrome)
- TextToSpeech

Major things that need to still be addressed:

- Fix thrown errors causing the Scheme runtime to pause indefinitely
- "Internal" file handling
- "External" file handling (e.g., FilePicker)
- Loading TFJS extensions (PIC, PAC, TeachableMachine)
