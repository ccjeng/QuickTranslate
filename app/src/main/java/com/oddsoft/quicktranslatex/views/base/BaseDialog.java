package com.oddsoft.quicktranslatex.views.base;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.oddsoft.quicktranslatex.R;


/**
 * Created by andycheng on 2016/3/27.
 */
public class BaseDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogStyle);

    }

}
