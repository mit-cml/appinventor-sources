package com.google.appinventor.components.runtime;

import java.util.List;

public interface ChartDataSourceListListener {
  public void onValueChange(ChartDataSource component, List oldValue, List newValue);
}
