package com.hrca.nyquist.nyquistplot;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hrca.nyquist.customs.HistoryHelper;
import com.hrca.nyquist.customs.displaycustoms.TransferFunctionView;

import java.util.ArrayList;

/**
 * Created by Hrvoje on 21.1.2015..
 */
public class HistoryAdapter extends BaseAdapter {
    protected final HistoryActivity historyActivity;
    protected final ArrayList<? extends Parcelable> history;

    public HistoryAdapter(HistoryActivity context){
        this.historyActivity = context;
        this.history = HistoryHelper.getHistory(context);
    }

    public void clear(){
        HistoryHelper.clear(this.historyActivity);
        this.history.clear();
        this.notifyDataSetChanged();
    }

    public void remove(int position){
        if(position < 0 || position >= this.history.size())
            return;
        HistoryHelper.remove(position, historyActivity);
        this.history.remove(position);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.history.size();
    }

    @Override
    public Object getItem(int position) {
        return this.history.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TransferFunctionView tf;
        if(convertView == null || !(convertView instanceof TransferFunctionView)){
            tf = new TransferFunctionView(this.historyActivity);
            tf.setOnClickListener(this.historyActivity);
            tf.setOnLongClickListener(this.historyActivity);
        }
        else tf = (TransferFunctionView)convertView;
        tf.onRestoreInstanceState(this.history.get(position));
        return tf;
    }
}
