package com.example.okey.okeylogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;


public class SingleChoiceDialogFragment extends DialogFragment {

    public static int choiceInter;
    public static int choiceOrient;
    public String mode = "";

    public interface  ChoiceDialogListener{

        public void onItemChosen(DialogFragment dialog, int choice, String mode);

    }

    SingleChoiceDialogFragment.ChoiceDialogListener clistener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{

            clistener = (ChoiceDialogListener) activity;
        }catch (ClassCastException e){

            throw new ClassCastException(activity.toString() + "must implement ChoiceDialogListener");

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(getActivity());

        if(mode == "interStyle") {
            choiceBuilder.setTitle("Interaction style:")

                    .setSingleChoiceItems(R.array.choice, choiceInter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("tag", String.valueOf(which));
                            choiceInter = which;

                        }
                    })

                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("tag", String.valueOf(which));
                            //Log.d("tag", String.valueOf(choice));
                            clistener.onItemChosen(SingleChoiceDialogFragment.this, choiceInter, mode);
                        }
                    });
        }else{

            choiceBuilder.setTitle("Orientation:")

                    .setSingleChoiceItems(R.array.orientation, choiceOrient, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("tag", String.valueOf(which));
                            choiceOrient = which;

                        }
                    })

                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("tag", String.valueOf(which));
                            //Log.d("tag", String.valueOf(choice));
                            clistener.onItemChosen(SingleChoiceDialogFragment.this, choiceOrient, mode);
                        }
                    });

        }

        return choiceBuilder.create();
    }
}
