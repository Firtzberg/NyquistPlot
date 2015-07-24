package com.hrca.nyquist.nyquistplot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hrca.nyquist.customs.displaycustoms.TransferFunctionView;


public class HistoryActivity extends Activity
        implements AdapterView.OnClickListener, AdapterView.OnLongClickListener {
    protected ListView list;
    protected HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        list = (ListView) findViewById(R.id.history_list);
        this.adapter = new HistoryAdapter(this);
        list.setAdapter(this.adapter);
    }

    public void newFunction(View v) {
        setResult(RESULT_OK, null);
        finish();
    }

    public void deleteHistory(View v) {
        new AlertDialog.Builder(this)
            .setTitle("History")
            .setMessage("Delete history?")
            .setCancelable(false)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    HistoryActivity.this.adapter.clear();
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    @Override
    public void onClick(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof TransferFunctionView) {
            Intent i = new Intent();
            i.putExtra(InputActivity.EXTRA_TRANSFER_FUNCTION, ((TransferFunctionView) parent).onSaveInstanceState());
            setResult(RESULT_OK, i);
            finish();
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        ViewParent parent = view.getParent();
        if(parent instanceof TransferFunctionView) {
            final TransferFunctionView tf = (TransferFunctionView)parent;
            new AlertDialog.Builder(this)
                    .setTitle("History")
                    .setMessage("Delete item?")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HistoryActivity.this.adapter.remove(HistoryActivity.this.list.getPositionForView(tf));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return true;
    }
}
