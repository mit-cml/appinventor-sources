package android.widget;

import android.view.View;

public abstract class AdapterView<T extends Adapter> {

  public static final int ITEM_VIEW_TYPE_IGNORE = -1;

  public abstract T getAdapter();

  /**
   * Interface definition for a callback to be invoked when an item in this
   * AdapterView has been clicked.
   */
  public interface OnItemClickListener {

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need to
     * access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    void onItemClick(AdapterView<?> parent, View view, int position, long id);
  }

  public static interface OnItemSelectedListener {
    abstract void onItemSelected(AdapterView<?> parent, View view, int position, long id);

    abstract void onNothingSelected(AdapterView<?> parent);
  }

}
