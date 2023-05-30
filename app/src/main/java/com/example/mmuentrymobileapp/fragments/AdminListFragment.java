package com.example.mmuentrymobileapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mmuentrymobileapp.HttpUtils;
import com.example.mmuentrymobileapp.LoginCache;
import com.example.mmuentrymobileapp.R;
import com.example.mmuentrymobileapp.RecordActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminListFragment extends Fragment {

    private RecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        SearchView searchView = view.findViewById(R.id.searchview);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        List<VisitorRecord> list = new ArrayList<>(); // Initialize the list with VisitorRecord objects

        // Create the adapter with the empty list and the current context
        adapter = new RecyclerViewAdapter(list, requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        // Fetch the visitor records from the API and update the adapter
        fetchVisitorRecords();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchVisitorRecords(); // Fetch the visitor records when the fragment is resumed
    }

    private void fetchVisitorRecords() {
        String token = LoginCache.getToken(requireContext());
        if (token != null) {
            String apiUrl = "http://10.0.2.2:8000/api/visitor/all/records";
            new FetchRecordsTask().execute(apiUrl, token);
        } else {
            // Handle case when token is null
        }
    }

    private class FetchRecordsTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String apiUrl = params[0];
            String token = params[1];

            try {
                return HttpUtils.sendHttpGetRequest(apiUrl, token);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response != null) {
                List<VisitorRecord> visitorRecords = parseApiResponse(response);
                adapter.updateRecords(visitorRecords);
            }
        }
    }

    private List<VisitorRecord> parseApiResponse(JSONObject response) {
        List<VisitorRecord> records = new ArrayList<>();
        try {
            JSONArray messageArray = response.getJSONArray("message");
            records.addAll(parseRecords(messageArray, "visitor_record"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return records;
    }

    private List<VisitorRecord> parseRecords(JSONArray recordsArray, String category) throws JSONException {
        List<VisitorRecord> activeRecords = new ArrayList<>();
        List<VisitorRecord> incomingRecords = new ArrayList<>();
        List<VisitorRecord> pastRecords = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String currentDate = sdf.format(new Date()); // Get the current date

        for (int i = 0; i < recordsArray.length(); i++) {
            JSONObject recordObject = recordsArray.getJSONObject(i).getJSONObject("visitor_record");

            int id = recordObject.getInt("id");
            int userId = recordObject.getInt("user_id");
            String dateOfVisit = recordObject.getString("date_of_visit");
            String reasonOfVisiting = recordObject.getString("reason_of_visiting");
            String token = recordObject.getString("token");

            JSONObject userObject = recordObject.getJSONObject("user");
            String visitorEmail = userObject.getString("email");
            String visitorName = userObject.getString("full_name");
            String visitorPhone = userObject.getString("contact_no");

            String recordCategory;

            // Compare the date of visit with the current date
            if (dateOfVisit.equals(currentDate)) {
                recordCategory = "Active Visitor Record";
            } else if (dateOfVisit.compareTo(currentDate) > 0) {
                recordCategory = "Incoming Visitor Record";
            } else {
                recordCategory = "Past Visitor Record";
            }

            VisitorRecord record = new VisitorRecord(id, userId, dateOfVisit, reasonOfVisiting, recordCategory, token, visitorEmail, visitorName, visitorPhone);

            // Categorize the records based on their date
            if (recordCategory.equals("Active Visitor Record")) {
                activeRecords.add(record);
            } else if (recordCategory.equals("Incoming Visitor Record")) {
                incomingRecords.add(record);
            } else {
                pastRecords.add(record);
            }
        }

        List<VisitorRecord> mergedRecords = new ArrayList<>();
        mergedRecords.addAll(activeRecords);
        mergedRecords.addAll(incomingRecords);
        mergedRecords.addAll(pastRecords);

        return mergedRecords;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {

        private List<VisitorRecord> itemList;
        private List<VisitorRecord> filteredList;
        private Context context;
        private ItemFilter itemFilter;

        public RecyclerViewAdapter(List<VisitorRecord> itemList, Context context) {
            this.itemList = itemList;
            this.filteredList = new ArrayList<>(itemList);
            this.context = context;
            this.itemFilter = new ItemFilter();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView recordTitle;
            TextView visitorEmail;
            TextView visitorName;
            TextView visitorPhone;
            TextView recordDate;
            TextView recordMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                recordTitle = itemView.findViewById(R.id.recordTitle);
                visitorEmail = itemView.findViewById(R.id.visitorEmail);
                visitorName = itemView.findViewById(R.id.visitorName);
                visitorPhone = itemView.findViewById(R.id.visitorPhone);
                recordDate = itemView.findViewById(R.id.recordDate);
                recordMessage = itemView.findViewById(R.id.recordMessage);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VisitorRecord record = filteredList.get(position);
            holder.visitorEmail.setText(record.getVisitorEmail());
            holder.visitorName.setText(record.getVisitorName());
            holder.visitorPhone.setText(record.getVisitorPhone());
            holder.recordDate.setText(record.getDateOfVisit());
            holder.recordMessage.setText(record.getReasonOfVisiting());

            // Set the record title based on the category
            if (record.getCategory().equals("Active Visitor Record")) {
                holder.recordTitle.setText("Active Visitor Record");
            } else if (record.getCategory().equals("Incoming Visitor Record")) {
                holder.recordTitle.setText("Incoming Visitor Record");
            } else {
                holder.recordTitle.setText("Past Visitor Record");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position >= 0 && position < filteredList.size()) {
                        VisitorRecord record = filteredList.get(position);
                        String token = record.getToken(); // Access the token property directly

                        // Pass the token to the RecordActivity to generate the QR code
                        Intent intent = new Intent(context, RecordActivity.class);
                        intent.putExtra("token", token);
                        context.startActivity(intent);
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        @Override
        public Filter getFilter() {
            return itemFilter;
        }

        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase();

                List<VisitorRecord> filteredItems = new ArrayList<>();
                for (VisitorRecord record : itemList) {
                    if (record.getReasonOfVisiting().toLowerCase().contains(query) ||
                            record.getVisitorEmail().toLowerCase().contains(query) ||
                            record.getVisitorName().toLowerCase().contains(query) ||
                            record.getVisitorPhone().toLowerCase().contains(query) ||
                            record.getDateOfVisit().toLowerCase().contains(query)) {
                        filteredItems.add(record);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredItems;
                results.count = filteredItems.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<VisitorRecord>) results.values;
                notifyDataSetChanged();
            }
        }


        public void updateRecords(List<VisitorRecord> records) {
            itemList.clear();
            itemList.addAll(records);
            filteredList.clear();
            filteredList.addAll(records);
            notifyDataSetChanged();
        }
    }

    public static class VisitorRecord {
        private int id;
        private int userId;
        private String dateOfVisit;
        private String reasonOfVisiting;
        private String category;
        private String token;
        private String visitorEmail;
        private String visitorName;
        private String visitorPhone;

        public VisitorRecord(int id, int userId, String dateOfVisit, String reasonOfVisiting, String category, String token, String visitorEmail, String visitorName, String visitorPhone) {
            this.id = id;
            this.userId = userId;
            this.dateOfVisit = dateOfVisit;
            this.reasonOfVisiting = reasonOfVisiting;
            this.category = category;
            this.token = token;
            this.visitorEmail = visitorEmail;
            this.visitorName = visitorName;
            this.visitorPhone = visitorPhone;
        }

        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }

        public String getDateOfVisit() {
            return dateOfVisit;
        }

        public String getReasonOfVisiting() {
            return reasonOfVisiting;
        }

        public String getCategory() {
            return category;
        }

        public String getToken() {
            return token;
        }

        public String getVisitorEmail() {
            return visitorEmail;
        }

        public String getVisitorName() {
            return visitorName;
        }

        public String getVisitorPhone() {
            return visitorPhone;
        }
    }
}
