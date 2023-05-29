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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

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
            String apiUrl = "http://10.0.2.2:8000/api/visitor/self/records";
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
            JSONObject messageObject = response.getJSONObject("message");
            JSONArray activeRecordsArray = messageObject.getJSONArray("self_active_visiting_record");
            JSONArray incomingRecordsArray = messageObject.getJSONArray("self_incoming_visiting_record");
            JSONArray pastRecordsArray = messageObject.getJSONArray("self_past_visited_records");

            records.addAll(parseRecords(activeRecordsArray, "Active Visiting Record"));
            records.addAll(parseRecords(incomingRecordsArray, "Incoming Visiting Record"));
            records.addAll(parseRecords(pastRecordsArray, "Past Visiting Record"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return records;
    }

    private List<VisitorRecord> parseRecords(JSONArray recordsArray, String category) throws JSONException {
        List<VisitorRecord> records = new ArrayList<>();
        for (int i = 0; i < recordsArray.length(); i++) {
            JSONObject recordObject = recordsArray.getJSONObject(i);
            int id = recordObject.getInt("id");
            int userId = recordObject.getInt("user_id");
            String dateOfVisit = recordObject.getString("date_of_visit");
            String reasonOfVisiting = recordObject.getString("reason_of_visiting");
            String token = recordObject.getString("token"); // Retrieve the token from the JSON response

            VisitorRecord record = new VisitorRecord(id, userId, dateOfVisit, reasonOfVisiting, category, token);
            records.add(record);
        }
        return records;
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
            TextView recordName;
            TextView recordDate;
            TextView recordMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                recordName = itemView.findViewById(R.id.recordName);
                recordDate = itemView.findViewById(R.id.recordDate);
                recordMessage = itemView.findViewById(R.id.recordMessage);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            VisitorRecord record = filteredList.get(position);
            holder.recordName.setText(record.getCategory());
            holder.recordDate.setText(record.getDateOfVisit());
            holder.recordMessage.setText(record.getReasonOfVisiting());

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
                    if (record.getReasonOfVisiting().toLowerCase().contains(query)) {
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

        public VisitorRecord(int id, int userId, String dateOfVisit, String reasonOfVisiting, String category, String token) {
            this.id = id;
            this.userId = userId;
            this.dateOfVisit = dateOfVisit;
            this.reasonOfVisiting = reasonOfVisiting;
            this.category = category;
            this.token = token;
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
    }
}
