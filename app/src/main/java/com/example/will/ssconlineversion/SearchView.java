package com.example.will.ssconlineversion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchView extends AppCompatActivity {
    private List<Route> possibleRoutes;
    private List<String> results;
    private ArrayAdapter resultListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);

        final ListView routesList = (ListView) findViewById(R.id.list);
        results = new ArrayList<>();
        resultListAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, results);
        routesList.setAdapter(resultListAdapter);

        EditText editText = (EditText) findViewById(R.id.editText);
        final TextView recent = (TextView) findViewById(R.id.recent);
        final Button clear = (Button) findViewById(R.id.clear);
        if (editText.length() == 0) {
            recent.setVisibility(View.VISIBLE);
            clear.setVisibility(View.VISIBLE);
            getNames(RouteManager.getInstance().getRecentSearched());
            resultListAdapter.notifyDataSetChanged();
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                possibleRoutes = RouteManager.getInstance().searchRoute(charSequence);
                getNames(possibleRoutes);
                resultListAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    recent.setVisibility(View.VISIBLE);
                    clear.setVisibility(View.VISIBLE);
                    getNames(RouteManager.getInstance().getRecentSearched());
                    resultListAdapter.notifyDataSetChanged();
                } else {
                    recent.setVisibility(View.INVISIBLE);
                    clear.setVisibility(View.INVISIBLE);
                }
            }
        });

        final Intent intent = this.getIntent();
        routesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String name = (String) adapterView.getItemAtPosition(position);
                if (intent.hasExtra("Route"))
                    intent.removeExtra("Route");
                intent.putExtra("Route", name);
                RouteManager.getInstance().addSearch(name);
                setResult(101, intent);
                finish();
            }
        });
    }

    private void getNames(List<Route> possibleRoutes) {
        results.clear();
        for (Route route : possibleRoutes)
            results.add(route.getNumber());
    }

    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void clear(View view) {
        Button clear = (Button) findViewById(R.id.clear);
        if (clear.getVisibility() == View.VISIBLE) {
            RouteManager.getInstance().clearResult();
            getNames(RouteManager.getInstance().getRecentSearched());
            resultListAdapter.notifyDataSetChanged();
        }
    }
}
