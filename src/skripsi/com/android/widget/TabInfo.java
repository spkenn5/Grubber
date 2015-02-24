package skripsi.com.android.widget;

import android.os.Bundle;

final public class TabInfo {
  final public Class<?> clss;
  final public Bundle args;

  public TabInfo(Class<?> _class, Bundle _args) {
    clss = _class;
    args = _args;
  }
}