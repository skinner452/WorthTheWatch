package net.askinner.worththewatchfull;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdRegistration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GameListFragment extends Fragment {
    private int currentWeek;
    private int maxWeeks;
    private GameList gameList;

    public GameListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game_list, container, false);

        try{
            gameList = new RetrieveGames().execute().get();
            maxWeeks = gameList.getMaxWeeks();

            // find the current week
            currentWeek = gameList.getCurrentWeek();

            final ListView listview = (ListView) view.findViewById(R.id.gameList);

            final TextView weekText = (TextView)view.findViewById(R.id.week);
            weekText.setText("Week " + (currentWeek+1));

//            GameAdapter adapter = new GameAdapter(getActivity(), gameList, currentWeek);
//            listview.setAdapter(adapter);

            final CheckBox checkAll = (CheckBox)view.findViewById(R.id.checkAll);
            checkAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameAdapter adapter = (GameAdapter)listview.getAdapter();
                    adapter.checkAll(checkAll.isChecked());
                }
            });

            if(gameList.areAllChecked(getActivity(), currentWeek)){
                checkAll.setChecked(true);
            } else {
                checkAll.setChecked(false);
            }

            Button backButton = (Button)view.findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentWeek > 0){
                        currentWeek--;

                        if(gameList.areAllChecked(getActivity(), currentWeek)){
                            checkAll.setChecked(true);
                        } else {
                            checkAll.setChecked(false);
                        }

                        GameAdapter adapter = new GameAdapter(getActivity(), gameList, currentWeek);
                        listview.setAdapter(adapter);

                        weekText.setText("Week " + (currentWeek+1));
                    }
                }
            });

            Button nextButton = (Button)view.findViewById(R.id.nextButton);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentWeek < maxWeeks) {
                        currentWeek++;

                        if(gameList.areAllChecked(getActivity(), currentWeek)){
                            checkAll.setChecked(true);
                        } else {
                            checkAll.setChecked(false);
                        }

                        GameAdapter adapter = new GameAdapter(getActivity(), gameList, currentWeek);
                        listview.setAdapter(adapter);

                        weekText.setText("Week " + (currentWeek + 1));
                    }
                }
            });

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Game game = gameList.getGames(currentWeek).get(position);
                    Intent intent;
                    if(game.isOver()){
                        intent = new Intent(getActivity(), AverageRatingActivity.class);
                    } else {
                        intent = new Intent(getActivity(), PredictedRatingActivity.class);
                        intent.putExtra("homeAverage",game.getHomeTeam().getFormattedAverageRating());
                        intent.putExtra("awayAverage",game.getAwayTeam().getFormattedAverageRating());
                        intent.putExtra("predicted",game.getPredictedRating());
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

        return view;
    }

    @Override
    public void onResume() {
        // update list
        try{
            final ListView listview = (ListView) getView().findViewById(R.id.gameList);
            gameList = new RetrieveGames().execute().get();
            GameAdapter adapter = new GameAdapter(getActivity(), gameList, currentWeek);
            listview.setAdapter(adapter);
        } catch (Exception e){
            // leave it how it was
        }

        super.onResume();
    }
}

class RetrieveGames extends AsyncTask<Void,Void,GameList> {

    @Override
    protected GameList doInBackground(Void... params) {
        GameList games = new GameList();
        try {
            URL url = new URL("http://askinner.net/wtw/games.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;

            while((line = in.readLine()) != null){
                games.addLine(line);
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return games;
    }
}