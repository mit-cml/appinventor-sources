let MVE = com.google.appinventor.client.editor.simple.components.MockVisibleExtension
let MCR = com.google.appinventor.client.editor.simple.MockComponentRegistry

class MockSimpleLabel extends MVE {
    static TYPE = "SimpleLabel"

    constructor(editor) {
        super(editor, MockSimpleLabel.TYPE)
        console.log("constructor called");
    }

    onCreateFromPalette() {
        console.log("onCreateFromPalette called")
    }

    onPropertyChange(propertyName, newValue) {
        console.log("onPropChanged", propertyName, newValue);
    }

    static create(editor) {
        return new MockSimpleLabel(editor)
    }
}

MCR.register(MockSimpleLabel.TYPE, MockSimpleLabel.create)