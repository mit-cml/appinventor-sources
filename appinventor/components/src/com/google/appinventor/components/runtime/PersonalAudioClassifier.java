package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.YailDictionary;

public class PersonalAudioClassifier extends BaseAiComponent{

    public PersonalAudioClassifier(Form form) {
        super(form);
    }

    @Override
    public void ClassifierReady() {
        throw new UnsupportedOperationException("Unimplemented method 'ClassifierReady'");
    }

    @Override
    public void GotClassification(YailDictionary result) {
        throw new UnsupportedOperationException("Unimplemented method 'GotClassification'");
    }

    @Override
    public void Error(int errorCode) {
        throw new UnsupportedOperationException("Unimplemented method 'Error'");
    }

}