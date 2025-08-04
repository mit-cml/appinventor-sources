package com.google.appinventor.components.runtime;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ViewUtil;
import com.google.appinventor.components.runtime.util.YailList;

import jsinterop.annotations.JsMethod;

import java.util.Arrays;
import java.util.List;

import android.util.Log;

@DesignerComponent(
    version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Basic ListView for webruntime",
    iconName = "images/listView.png")
@SimpleObject
public class ListView extends AndroidViewComponent {

    @JsMethod(namespace = "console")
    public static native void log(String message);

    private final View view;
    private android.widget.ListView listView;

    private YailList elements = new YailList();
    private ArrayAdapter<String> adapter;
    private String elementsFromString = "";

    public ListView(ComponentContainer container) {
        super(container);        
        log("ListView constructor called");
        Log.i("ListView Tag", "Listview constructor called");

        Context context = container.$context();

        listView = new android.widget.ListView((android.app.Activity) container.$context());
        adapter = new ArrayAdapter<>(context, 0, new String[]{});
        listView.setAdapter(adapter);

        view = listView;
        ViewUtil.setChildContainer(view);
        container.$add(this);

        Width(300);
        Height(400);

        ElementsFromString("alpha, beta, gamma");
        log("hard coded elementsfromstring called");

        Elements(YailList.makeList(Arrays.asList("one", "two", "three")));
    }

    @Override
    public View getView() {
        return view;
    }

    @SimpleProperty(
    category = PropertyCategory.BEHAVIOR,
    description = "List of text elements to show in the ListView."
    )
    public YailList Elements() {
        return elements;
    }

    @SimpleProperty
    public void Elements(YailList elements) {
        log("ListView.Elements() called with: " + elements);
        log("YailList.toString(): " + elements.toString());
        log("YailList size (manual): " + (elements == null ? "null" : elements.size()));
        this.elements = elements;

        if (elements instanceof YailList) {
            log("elements is instance of YailList");
        } else {
            log("elements is NOT a YailList instance");
        }

        log("YailList class: " + elements.getClass().getName());

        Object[] objArray;
        try {
            objArray = elements.toArray();
            if (objArray == null) {
                log("ListView.Elements: toArray() returned null");
                return;
            }
        } catch (Exception e) {
            log("ListView.Elements: toArray() threw exception: " + e.getMessage());
            return;
        }

        if (elements.size() == 0) {
            log("YailList appears empty");
        }
        log("Converted to Object[] with length: " + objArray.length);

        String[] items = new String[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            Object item = objArray[i];
            items[i] = (item == null) ? "" : item.toString();
        }

        adapter = new ArrayAdapter<>(container.$context(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        YailList testList = YailList.makeList(Arrays.asList("item 1", "item 2", "item 3"));
        Object[] objArray2 = testList.toArray();
        log("HARD CODE fallback array size: " + objArray2.length);
    }

    @SimpleProperty(
        description = "Sets ListView to items from comma-separated string"
    )
    public void ElementsFromString(String itemstring) {
        elementsFromString = itemstring;
        if (itemstring.isEmpty()) {
            Elements(YailList.makeEmptyList());
            return;
        }

        String[] items = itemstring.split("\\s*,\\s*");
        YailList list = YailList.makeList(Arrays.asList(items));
        Elements(list);
    }

    @SimpleProperty(
        category = PropertyCategory.BEHAVIOR,
        description = "Gets comma-separated string from ListView"
    )
    public String ElementsFromString() {
        return elementsFromString;
    }
}
