package cn.colintree.aix.TranslationTest;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(version = 0,
    description = "Translation Test",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")
@SimpleObject(external = true)
public final class TranslationTest extends AndroidNonvisibleComponent {
    
    public TranslationTest(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction
    public void MethodTest(String paramTest) {
        //
    }

    @SimpleEvent
    public void EventTest(String paramTest) {
        //
    }

    @SimpleProperty
    @DesignerProperty
    public void PropertyTest(String property) {
        //
    }

}