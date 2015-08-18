package com.google.appinventor.components.runtime;


import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.view.Menu;
import android.view.MenuItem;
import android.text.SpannableString;
import android.text.Html;

/**
 * SimpleMenu Component. Non-Visible component to create an Options Menu in the Screen from a series of
 * menu items added from a comma separated set of text elements.
 * @author liyucun2012@gmail.com (Yucun Li)
 */
@DesignerComponent(version = YaVersion.SIMPLEMENU_COMPONENT_VERSION,
        category = ComponentCategory.USERINTERFACE,
        description = "<p>Non-visible component displaying options menu," +
                " all menu items text can be set using the ElementsFromString property" +
                " or using the Elements block in the blocks editor." +
                " The icon for each menu item can be set using different Icon property," +
                " or using the Elements block in the blocks editor.  </p>",
        nonVisible = true,
        iconName = "images/simpleMenu.png")
@SimpleObject
public final class SimpleMenu extends AndroidNonvisibleComponent implements OnPrepareOptionsMenuListener {

    // selected menu item text
    private String selection = "";

    // list of menu items
    private YailList items;

    // menu item icon
    private MenuItemIcon menu_item1_icon;
    private MenuItemIcon menu_item2_icon;
    private MenuItemIcon menu_item3_icon;
    private MenuItemIcon menu_item4_icon;

    // default menu item icon value, which means no icon for menu item
    private final static int NO_MENU_ICON = 0;

    /**
     * Creates a new SimpleMenu component.
     * @param container  container that the component will be placed in
     */
    public SimpleMenu (ComponentContainer container) {
        super(container.$form());
        //set up the listener
        form.registerForOnPrepareOptionsMenu(this);

        items = YailList.makeEmptyList();

        menu_item1_icon = new MenuItemIcon(NO_MENU_ICON);
        menu_item2_icon = new MenuItemIcon(NO_MENU_ICON);
        menu_item3_icon = new MenuItemIcon(NO_MENU_ICON);
        menu_item4_icon = new MenuItemIcon(NO_MENU_ICON);
    }

    /**
     * Specifies the text elements of the SimpleMenu.
     * @param itemstring a string containing a comma-separated list of the strings to be picked from
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description= "Build a menu with a series of text elements specified as a string with the " +
            "items separated by commas " +
            "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an menu item in the " +
            "Options Menu.",  category = PropertyCategory.BEHAVIOR)
    public void ElementsFromString(String itemstring) {
        items = ElementsUtil.elementsFromString(itemstring);
    }

    /**
     * Set a list of text elements to build a SimpleMenu
     * @param itemsList a YailList containing the strings to be added to the ListView
     */
    @SimpleProperty(description="List of menu items to build your options menu.", category = PropertyCategory.BEHAVIOR)
    public void Elements(YailList itemsList) {
        items = ElementsUtil.elements(itemsList, "SimpleMenu");
    }

    /**
     * Elements property getter method
     *
     * @return a YailList representing the list of strings to be picked from
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Elements() {
        return items;
    }

    /**
     * Returns the text in the SimpleMenu of the menu item clicked by user
     */
    @SimpleProperty(description="Returns the text last selected in the Menu.",
            category = PropertyCategory
                    .BEHAVIOR)
    public String Selection() {
        return selection;
    }

    /**
     * Simple event to be raised after the a menu item has been chosen in the Menu.
     * The selected item is available in the Selection property.
     * @param selected menu item text
     */
    @SimpleEvent(description = "Event called after the user selects a menu item.")
    public void AfterSelecting(String selection){
        EventDispatcher.dispatchEvent(this, "AfterSelecting", selection);
    }

    /**
     * Set the first menu item icon
     * @param menu item integer representing an icon
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
            defaultValue = "0")
    @SimpleProperty(description="Set the first menu item icon. Range of allowed integer value is 0 to 41",
            category = PropertyCategory.BEHAVIOR)
    public void Icon1(int icon) {
        chooseIcon(icon, menu_item1_icon);
    }

    /**
     * Set the second menu item icon
     * @param menu item integer representing an icon
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
            defaultValue = "0")
    @SimpleProperty(description="Set the second menu item icon. Range of allowed integer value is 0 to 41",
            category = PropertyCategory.BEHAVIOR)
    public void Icon2(int icon) {
        chooseIcon(icon, menu_item2_icon);
    }

    /**
     * Set the third menu item icon
     * @param menu item integer representing an icon
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
            defaultValue = "0")
    @SimpleProperty(description="Set the third menu item icon. Range of allowed integer value is 0 to 41",
            category = PropertyCategory.BEHAVIOR)
    public void Icon3(int icon) {
        chooseIcon(icon, menu_item3_icon);
    }

    /**
     * Set the fourth menu item icon
     * @param menu item integer representing an icon
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
            defaultValue = "0")
    @SimpleProperty(description="Set the fourth menu item icon. Range of allowed integer value is 0 to 41",
            category = PropertyCategory.BEHAVIOR)
    public void Icon4(int icon) {
        chooseIcon(icon, menu_item4_icon);
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // check if items list is not empty
        if(!items.isEmpty()){
            int size = items.size();

            // iterate items list, and create menu item for each text item
            for (int i = 1; i <= size; i++) {
                String itemString = YailList.YailListElementToString(items.get(i));
                MenuItem menuItem = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, stringToHTML(itemString));

                // specify menu item icon based on its icon value
                switch (i) {
                    case 1:
                        menuItem.setIcon(menu_item1_icon.getIconValue());
                        break;
                    case 2:
                        menuItem.setIcon(menu_item2_icon.getIconValue());
                        break;
                    case 3:
                        menuItem.setIcon(menu_item3_icon.getIconValue());
                        break;
                    case 4:
                        menuItem.setIcon(menu_item4_icon.getIconValue());
                        break;

                }

                // register click listener for menu item
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        SimpleMenu.this.selection = item.getTitle().toString();
                        SimpleMenu.this.AfterSelecting(item.getTitle().toString());

                        return true;
                    }
                });
            }
        }
    }

    /**
     * Util method to create spannable string.
     * @param message to be transformed
     */
    private static SpannableString stringToHTML(String message) {
        return new SpannableString(Html.fromHtml(message));
    }

    /**
     * Wrapper for containing menu icon integer value, which represents different menu icon
     */
    class MenuItemIcon {

        int iconValue;

        public MenuItemIcon(int iconValue) {
            this.iconValue = iconValue;
        }

        public int getIconValue() {
            return iconValue;
        }

        public void setIconValue(int iconValue) {
            this.iconValue = iconValue;
        }
    }

    /**
     * Set menu item icon based on integer value
     * @param menu item integer representing an icon, menuItemIcon containing menu icon integer
     */
    private void chooseIcon(int icon, MenuItemIcon menuItemIcon) {

        switch (icon) {
            case NO_MENU_ICON:
                menuItemIcon.setIconValue(NO_MENU_ICON);
                break;
            case 1:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_add);
                break;
            case 2:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_agenda);
                break;
            case 3:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_always_landscape_portrait);
                break;
            case 4:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_call);
                break;
            case 5:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_camera);
                break;
            case 6:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_close_clear_cancel);
                break;
            case 7:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_compass);
                break;
            case 8:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_crop);
                break;
            case 9:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_day);
                break;
            case 10:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_delete);
                break;
            case 11:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_directions);
                break;
            case 12:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_edit);
                break;
            case 13:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_gallery);
                break;
            case 14:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_help);
                break;
            case 15:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_info_details);
                break;
            case 16:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_manage);
                break;
            case 17:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_mapmode);
                break;
            case 18:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_month);
                break;
            case 19:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_more);
                break;
            case 20:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_my_calendar);
                break;
            case 21:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_mylocation);
                break;
            case 22:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_myplaces);
                break;
            case 23:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_preferences);
                break;
            case 24:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_recent_history);
                break;
            case 25:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_report_image);
                break;
            case 26:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_revert);
                break;
            case 27:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_rotate);
                break;
            case 28:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_save);
                break;
            case 29:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_search);
                break;
            case 30:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_send);
                break;
            case 31:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_set_as);
                break;
            case 32:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_share);
                break;
            case 33:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_slideshow);
                break;
            case 34:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_sort_alphabetically);
                break;
            case 35:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_sort_by_size);
                break;
            case 36:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_today);
                break;
            case 37:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_upload);
                break;
            case 38:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_upload_you_tube);
                break;
            case 39:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_view);
                break;
            case 40:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_week);
                break;
            case 41:
                menuItemIcon.setIconValue(android.R.drawable.ic_menu_zoom);
                break;
        }
    }
}