package net.askinner.worththewatch;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GameListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private GameList gameList;
    private boolean init;

    public GameListFragment() {
        // Required empty public constructor
    }

    private void initializeView(View view) {
        try{
            view.findViewById(R.id.loadingText).setVisibility(View.INVISIBLE);

            final ListView listview = (ListView) view.findViewById(R.id.gameList);

            // Create initial adapter
            GameAdapter adapter = new GameAdapter(getActivity(), gameList);
            listview.setAdapter(adapter);

            final TextView weekText = (TextView)view.findViewById(R.id.week);
            weekText.setText(gameList.weekString());

            final CheckBox checkAll = (CheckBox)view.findViewById(R.id.checkAll);
            checkAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameAdapter adapter = (GameAdapter)listview.getAdapter();
                    adapter.checkAll(checkAll.isChecked());
                }
            });

            if(gameList.areAllChecked(getActivity())){
                checkAll.setChecked(true);
            } else {
                checkAll.setChecked(false);
            }

            Button backButton = (Button)view.findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gameList.backWeek()){
                        GameAdapter adapter = new GameAdapter(getActivity(), gameList);
                        listview.setAdapter(adapter);

                        if(gameList.areAllChecked(getActivity())){
                            checkAll.setChecked(true);
                        } else {
                            checkAll.setChecked(false);
                        }

                        weekText.setText(gameList.weekString());
                    }
                }
            });

            Button nextButton = (Button)view.findViewById(R.id.nextButton);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(gameList.nextWeek()){
                        GameAdapter adapter = new GameAdapter(getActivity(), gameList);
                        listview.setAdapter(adapter);

                        if(gameList.areAllChecked(getActivity())){
                            checkAll.setChecked(true);
                        } else {
                            checkAll.setChecked(false);
                        }

                        weekText.setText(gameList.weekString());
                    }
                }
            });

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Game game = gameList.getGames().get(position);
                    Intent intent;
                    if(game.isOver()){
                        intent = new Intent(getActivity(), AverageRatingActivity.class);
                    } else {
                        intent = new Intent(getActivity(), PredictedRatingActivity.class);
                        intent.putExtra("homeAverage",game.getHomeTeam().getFormattedAverageRating());
                        intent.putExtra("awayAverage",game.getAwayTeam().getFormattedAverageRating());
                        intent.putExtra("predicted",game.getPredictedRatingString());
                    }

                    intent.putExtra("gameID",game.getId());
                    intent.putExtra("homeTeam",game.getHomeTeamName());
                    intent.putExtra("awayTeam",game.getAwayTeamName());
                    intent.putExtra("date",game.getFormattedDate());
                    intent.putExtra("time",game.getFormattedTime());
                    intent.putExtra("stadium",game.getStadium());
                    intent.putExtra("channels",game.getFormattedChannels());

                    startActivity(intent);
                }
            });


        } catch (Exception e){
            e.printStackTrace();
        }

        init = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_list, container, false);

        if(gameList.isEmpty()){
            new RetrieveGames(this, gameList).execute();
        } else {
            initializeView(view);
        }

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh && ConnectionCheck.hasConnection(getActivity().getApplicationContext())){
            try{
                new RetrieveGames(this, gameList).execute().get();
            } catch (Exception e) {

            }
            System.out.println("Refresh");
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateAdapter() {
        if(!init){
            initializeView(getView());
        } else {
            ListView listview = (ListView) getView().findViewById(R.id.gameList);
            GameAdapter adapter = (GameAdapter)listview.getAdapter();
            adapter.update();
        }
    }

    @Override
    public void onResume() {
        // update list if changes

        super.onResume();
    }

    public void setGameList(GameList gameList) {
        this.gameList = gameList;
    }

    public static GameListFragment newInstance(int sectionNumber, GameList gameList) {
        GameListFragment fragment = new GameListFragment();
        fragment.setGameList(gameList);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}

