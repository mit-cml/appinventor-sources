package com.google.appinventor.components.runtime;
import com.google.appinventor.components.runtime.util.YailDictionary;

public abstract class BaseClassifierComponent extends BaseAiComponent{
    
    //check during the implementation of PAC components if necessary

    public BaseClassifierComponent(Form form) {
        super(form);
    }

    @Override
    public abstract void ClassifierReady();

    @Override
    public abstract void GotClassification(YailDictionary result);

    @Override
    public abstract void Error(int errorCode);

}
