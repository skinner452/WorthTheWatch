package net.askinner.worththewatch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class YourTableFragment extends Fragment {
    private GameList gameList;

    public YourTableFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_table, container, false);

        try{
            gameList = new RetrieveGames().execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }

        Table table = new Table(getActivity(), gameList);

        final ListView listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(new TableAdapter(getActivity(), table.getTeams()));

        ArrayList<String> tableOptions = new ArrayList<>();
        tableOptions.add("Supporters' Shield");
        tableOptions.add("Western Conference");
        tableOptions.add("Eastern Conference");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,tableOptions);
        Spinner spinner = (Spinner)view.findViewById(R.id.spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TableAdapter tableAdapter = (TableAdapter)listView.getAdapter();
                if(position == 0){
                    // Supporters Shield
                    tableAdapter.setDivision('S');
                } else if (position == 1){
                    // East
                    tableAdapter.setDivision('W');
                } else if (position == 2){
                    // West
                    tableAdapter.setDivision('E');
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

}
