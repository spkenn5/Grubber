package skripsi.com.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EnhancedViewPager extends ViewPager {
  private boolean swipeEnabled = true;

  public EnhancedViewPager(Context context) {
    super(context);
    this.swipeEnabled = true;
  }

  public EnhancedViewPager(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    this.swipeEnabled = true;
  }

  public boolean isSwipeEnabled() {
    return swipeEnabled;
  }

  public void setSwipeEnabled(boolean swipeEnabled) {
    this.swipeEnabled = swipeEnabled;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent arg0) {
    return swipeEnabled && super.onInterceptTouchEvent(arg0);
  }

  @Override
  public boolean onTouchEvent(MotionEvent arg0) {
    return swipeEnabled && super.onTouchEvent(arg0);
  }

}
