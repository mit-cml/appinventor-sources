package android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayAdapter<T> implements Adapter {
  private final Object mLock = new Object();
  private final Context mContext;
  private ArrayFilter mFilter;
  private List<T> mObjects;
  private ArrayList<T> mOriginalValues;
  private final int mResource;
  private int mDropDownResource;

  public ArrayAdapter(Context context, int resource, T[] objects) {
    mContext = context;
    mResource = mDropDownResource = resource;
    mObjects = new ArrayList<>();
    Collections.addAll(mObjects, objects);
  }

  public Filter getFilter() {
    if (mFilter == null) {
      mFilter = new ArrayFilter();
    }
    return mFilter;
  }

  public void notifyDataSetChanged() {
  }

  public void notifyDataSetInvalidated() {
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {

  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {

  }

  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public T getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public int getViewTypeCount() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  private class ArrayFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence prefix) {
      final FilterResults results = new FilterResults();

      if (mOriginalValues == null) {
        synchronized (mLock) {
          mOriginalValues = new ArrayList<>(mObjects);
        }
      }

      if (prefix == null || prefix.length() == 0) {
        final ArrayList<T> list;
        synchronized (mLock) {
          list = new ArrayList<>(mOriginalValues);
        }
        results.values = list;
        results.count = list.size();
      } else {
        final String prefixString = prefix.toString().toLowerCase();

        final ArrayList<T> values;
        synchronized (mLock) {
          values = new ArrayList<>(mOriginalValues);
        }

        final int count = values.size();
        final ArrayList<T> newValues = new ArrayList<>();

        for (int i = 0; i < count; i++) {
          final T value = values.get(i);
          final String valueText = value.toString().toLowerCase();

          // First match against the whole, non-splitted value
          if (valueText.startsWith(prefixString)) {
            newValues.add(value);
          } else {
            final String[] words = valueText.split(" ");
            for (String word : words) {
              if (word.startsWith(prefixString)) {
                newValues.add(value);
                break;
              }
            }
          }
        }

        results.values = newValues;
        results.count = newValues.size();
      }

      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      //noinspection unchecked
      mObjects = (List<T>) results.values;
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }
}
