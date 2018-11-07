package ekoolab.com.show.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;

import com.baidu.mapapi.UIMsg;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import ekoolab.com.show.R;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener  {
//
//public class DatePickerFragment extends DatePickerDialog
//            implements DatePickerDialog.OnDateSetListener  {

    private Context context;

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
////        super.onCreateView(inflater, container, savedInstanceState);
//        if (inflater == null) {
//            return super.onCreateView(inflater, container, savedInstanceState);
//        }
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getDialog().getWindow().setDimAmount(0.8f);
//        View view  = inflater.inflate(R.layout.activity_birthday,container,false);
//        return view;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
//        View view = getActivity().getLayoutInflater().inflate(R.layout.activity_birthday, null);
//        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, 2011, 11, 11);
//        datePickerDialog.setView(view);
//        DatePicker datePicker = (DatePicker) view.findViewById(R.id.dpPicker);
//        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
//            @Override
//            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                datePicker.init(year, monthOfYear, dayOfMonth, this);
//            }
//        });
//
//        // Create a new instance of TimePickerDialog and return it
//        return datePickerDialog;


//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        DatePicker picker = new DatePicker(getActivity());
//
//        builder.setTitle("Birthday");
//        builder.setView(picker);
//        builder.setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dismiss();
//            }
//        });
//        builder.setPositiveButton("Set",  new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(),
//                        mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
//            }
//        });
//
//        return  builder.create();




//        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view).create();
//        alertDialog.setPo
//        return new AlertDialog.Builder(getActivity()).setView(view).create();
        return new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_DARK, this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-" + Integer.toString(day);
        System.out.println("birthday entered: " + date);
        EventBus.getDefault().post(new String(date));
    }
}
