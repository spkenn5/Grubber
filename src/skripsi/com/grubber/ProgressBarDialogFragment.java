package skripsi.com.grubber;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProgressBarDialogFragment extends DialogFragment {

  public ProgressBarDialogFragment() {
    setStyle(STYLE_NO_FRAME, R.style.pbDialogStyle);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_progress_bar, container, false);
  }

}
