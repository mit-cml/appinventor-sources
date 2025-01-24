package android.view;

public class MenuItem {
  public interface OnMenuItemClickListener {
    /**
     * Called when a menu item has been invoked.  This is the first code
     * that is executed; if it returns true, no other callbacks will be
     * executed.
     *
     * @param item The menu item that was invoked.
     *
     * @return Return true to consume this click and prevent others from
     *         executing.
     */
    public boolean onMenuItemClick(MenuItem item);
  }

  public static final int SHOW_AS_ACTION_NEVER = 0;
  public static final int SHOW_AS_ACTION_IF_ROOM = 1;
  public static final int SHOW_AS_ACTION_ALWAYS = 2;
  public static final int SHOW_AS_ACTION_WITH_TEXT = 4;
  public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;

  int groupId;
  int itemId;
  int order;
  int titleId;
  String titleString;
  int showAsAction;
  int icon;
  private OnMenuItemClickListener mClickListener;

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getTitle() {
    return titleId;
  }

  public void setTitle(int title) {
    this.titleId = title;
  }

  public String getTitleString() {
    return titleString;
  }

  public void setTitleString(String titleString) {
    this.titleString = titleString;
  }

  public int getShowAsAction() {
    return showAsAction;
  }

  public void setShowAsAction(int showAsAction) {
    this.showAsAction = showAsAction;
  }

  public int getIcon() {
    return icon;
  }

  public void setIcon(int icon) {
    this.icon = icon;
  }

  public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener listener) {
    mClickListener = listener;
    return this;
  }
}
