package android.widget;

public abstract class Filter {
  public final void filter(CharSequence constraint) {
  }
  protected abstract FilterResults performFiltering(CharSequence constraint);
  protected abstract void publishResults(CharSequence constraint,
      FilterResults results);
  protected static class FilterResults {
    public Object values;
    public int count;
  }
}
