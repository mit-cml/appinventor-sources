class MockSimpleLabel extends MockVisibleExtension {
    static TYPE = "SimpleLabel"

    constructor(editor) {
        super(editor, MockSimpleLabel.TYPE)
        console.log("constructor called");

        this.label = document.createElement("p")

        this.initComponent(this.label)
    }

    onCreateFromPalette() {
        console.log("onCreateFromPalette called")
        this.changeProperty("Text", this.getName());
    }

    onPropertyChange(propertyName, newValue) {
        console.log("onPropChanged", propertyName, newValue);
        super.onPropertyChange(propertyName, newValue);

        switch (propertyName) {
            case "Text":
                this.label.textContent = newValue
                break
        }
    }

    static create(editor) {
        return new MockSimpleLabel(editor)
    }
}

MockComponentRegistry.register(MockSimpleLabel.TYPE, MockSimpleLabel.create)
