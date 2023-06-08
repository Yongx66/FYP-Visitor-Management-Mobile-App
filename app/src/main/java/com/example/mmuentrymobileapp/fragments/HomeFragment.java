package com.example.mmuentrymobileapp.fragments;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mmuentrymobileapp.HttpUtils;
import com.example.mmuentrymobileapp.LoginCache;
import com.example.mmuentrymobileapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    EditText date;
    EditText username;
    EditText contact;
    EditText reason;
    DatePickerDialog datePickerDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        date = view.findViewById(R.id.date);
        username = view.findViewById(R.id.username);
        contact = view.findViewById(R.id.contact);
        reason = view.findViewById(R.id.reason);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Format the selected date as "YYYY-MM-DD"
                                String formattedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);

                                // Set the formatted date in the EditText
                                date.setText(formattedDate);
                            }
                        }, mYear, mMonth, mDay);
                // Set the minimum date to today's date
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        view.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = username.getText().toString();
                String contactNo = contact.getText().toString();
                String visitDate = date.getText().toString();
                String reasonOfVisiting = reason.getText().toString();
                String token = LoginCache.getToken(requireContext());

                if (fullName.isEmpty() || contactNo.isEmpty() || visitDate.isEmpty() || reasonOfVisiting.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (token == null) {
                    Toast.makeText(requireContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    // Call the register visitor API
                    RegisterVisitorTask task = new RegisterVisitorTask();
                    task.execute(fullName, contactNo, visitDate, reasonOfVisiting, token);
                }
            }
        });

        return view;
    }

    private class RegisterVisitorTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String fullName = params[0];
            String contactNo = params[1];
            String visitDate = params[2];
            String reasonOfVisiting = params[3];
            String token = params[4];
            String apiUrl = "http://10.0.2.2:8000/api/visitor/register";

            try {
                JSONObject requestData = new JSONObject();
                requestData.put("full_name", fullName);
                requestData.put("contact_no", contactNo);
                requestData.put("date_of_visit", visitDate);
                requestData.put("reason_of_visiting", reasonOfVisiting);

                return HttpUtils.sendHttpPostRequest(apiUrl, token, requestData);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response != null) {
                // Handle the response here
                Toast.makeText(requireContext(), "Visitor record created successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Show an error message if the response is null
                Toast.makeText(requireContext(), "A visitor record of the same date is already exist", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
