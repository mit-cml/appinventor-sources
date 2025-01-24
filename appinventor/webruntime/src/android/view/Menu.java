package android.view;

import java.util.ArrayList;

public class Menu {
  /**
   * Value to use for group and item identifier integers when you don't care
   * about them.
   */
  public static final int NONE = 0;
  /**
   * First value for group and item identifier integers.
   */
  public static final int FIRST = 1;

  public ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();

  public MenuItem add(int title) {
    MenuItem item = new MenuItem();
    item.setTitle(title);
    menuItems.add(item);
    return item;
  }

  public MenuItem add(int groupId, int itemId, int order, int title) {
    MenuItem item = new MenuItem();
    item.setGroupId(groupId);
    item.setItemId(itemId);
    item.setOrder(order);
    item.setTitle(title);
    menuItems.add(item);
    return item;
  }

  public MenuItem add(int groupId, int itemId, int order, String title) {
    MenuItem item = new MenuItem();
    item.setGroupId(groupId);
    item.setItemId(itemId);
    item.setOrder(order);
    item.setTitleString(title);
    menuItems.add(item);
    return item;
  }

  void add(MenuItem item) {
    menuItems.add(item);
  }
}
