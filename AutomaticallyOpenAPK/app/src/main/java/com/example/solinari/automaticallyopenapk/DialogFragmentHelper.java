package com.example.solinari.automaticallyopenapk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Solinari on 2016/12/4.
 */

public class DialogFragmentHelper extends DialogFragment{
    private ProgressBar progressBar;//下載新版APP時ProgressBar
    private TextView txtProgress;//下載新版APP時顯示文字百分比
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//取消Dialog title列
        getDialog().setCanceledOnTouchOutside(false);//不能點擊Dialog以外區域
        View v = inflater.inflate(R.layout.download_apk_dialog, container, false);
        progressBar = (ProgressBar) v.findViewById(R.id.download_progressBar);
        txtProgress = (TextView) v.findViewById(R.id.txtProgress);
        txtProgress.setText("0%");
        return v;
    }
    protected void setProgress(int progress){//更新進度條
        progressBar.setProgress(progress);
        txtProgress.setText(Integer.toString(progress) + "%");
    }


}
