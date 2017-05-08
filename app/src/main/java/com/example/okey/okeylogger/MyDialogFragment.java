package com.example.okey.okeylogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public  class MyDialogFragment extends DialogFragment {

    public static String username;

    public interface  NoticeDialogListener{

        public void onDialogPositiveClick(DialogFragment dialog, String username);
        public void onDialogNegativeClick(DialogFragment dialog);

    }

    NoticeDialogListener mlistener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{

            mlistener = (NoticeDialogListener) activity;

        }catch(ClassCastException e){

            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.user_dialog, null);
        final EditText user = (EditText)v.findViewById(R.id.editText);

        user.setText(getArguments().getString("currentName", ""));
        user.setSelectAllOnFocus(true);

        builder.setView(v)

                .setTitle("User info")
                .setMessage("Enter username:")
                .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        username = user.getText().toString();
                        //Log.d("string", String.valueOf(username));
                        if(username.length() != 0) {

                            mlistener.onDialogPositiveClick(MyDialogFragment.this, username);
                            
                        }else{

                            Toast.makeText(getActivity().getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();
                        }

                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();

    }


}
