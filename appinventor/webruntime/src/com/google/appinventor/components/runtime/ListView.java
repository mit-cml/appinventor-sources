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

import java.util.Arrays;

@DesignerComponent(
    version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Basic ListView for webruntime",
    iconName = "images/listView.png")
@SimpleObject
public class ListView extends AndroidViewComponent {

    private final View view;
    private android.widget.ListView listView;

    private YailList elements = new YailList();
    private ArrayAdapter<String> adapter;
    private String elementsFromString = "";

    public ListView(ComponentContainer container) {
        super(container);

        Context context = container.$context();

        listView = new android.widget.ListView((android.app.Activity) container.$context());
        adapter = new ArrayAdapter<>(context, 0, new String[]{});
        listView.setAdapter(adapter);

        view = listView;
        ViewUtil.setChildContainer(view);
        container.$add(this);

        Width(300);
        Height(400);
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
        this.elements = elements;

        int size = elements.size();
        String[] items = new String[size];
        for (int i = 0; i < size; i++) {
            Object item = elements.getObject(i + 1);
            items[i] = (item == null) ? "" : item.toString();
        }

        adapter = new ArrayAdapter<>(container.$context(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
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
