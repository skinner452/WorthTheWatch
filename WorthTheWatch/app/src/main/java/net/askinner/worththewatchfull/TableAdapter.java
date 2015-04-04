package net.askinner.worththewatchfull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alec on 4/2/2015.
 */
public class TableAdapter extends BaseAdapter{
    private ArrayList<Team> allTeams;
    private ArrayList<Team> teams;
    private Context context;
    private char currentDivision;

    public TableAdapter (Context context, ArrayList<Team> teams) {
        this.context = context;
        this.allTeams = teams;
        this.teams = allTeams;
        currentDivision = 'S';
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TableViewHolder viewHolder;
        if (convertView == null) { // no view to re-use, create new
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.table_item, parent, false);

            viewHolder = new TableViewHolder();
            viewHolder.position = (TextView)convertView.findViewById(R.id.position);
            viewHolder.teamName = (TextView)convertView.findViewById(R.id.teamName);
            viewHolder.played = (TextView)convertView.findViewById(R.id.played);
            viewHolder.wins = (TextView)convertView.findViewById(R.id.wins);
            viewHolder.draws = (TextView)convertView.findViewById(R.id.draws);
            viewHolder.losses = (TextView)convertView.findViewById(R.id.losses);
            viewHolder.goalsFor = (TextView)convertView.findViewById(R.id.goalsFor);
            viewHolder.goalsAgainst = (TextView)convertView.findViewById(R.id.goalsAgainst);
            viewHolder.goalDifference = (TextView)convertView.findViewById(R.id.goalDifference);
            viewHolder.points = (TextView)convertView.findViewById(R.id.points);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TableViewHolder)convertView.getTag();
        }

        if(currentDivision == 'S'){
            if(position == 0){
                convertView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.divider).setVisibility(View.INVISIBLE);
            }
        } else {
            if(position == 4){
                convertView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.divider).setVisibility(View.INVISIBLE);
            }
        }

        Team team = teams.get(position);
        viewHolder.position.setText((position+1) + "");
        viewHolder.teamName.setText(team.getName());
        viewHolder.played.setText(team.getGamesPlayed() + "");
        viewHolder.wins.setText(team.getWins() + "");
        viewHolder.draws.setText(team.getDraws() + "");
        viewHolder.losses.setText(team.getLosses() + "");
        viewHolder.goalsFor.setText(team.getGoalsFor() + "");
        viewHolder.goalsAgainst.setText(team.getGoalsAgainst() + "");
        viewHolder.goalDifference.setText(team.getGoalDifference() + "");
        viewHolder.points.setText(team.getPoints() + "");

        return convertView;
    }

    public void setDivision(char c){
        currentDivision = c;
        if(c == 'S'){
            teams = allTeams;
        } else {
            teams = new ArrayList<Team>();
            for (Team team : allTeams){
                if(team.getDivision() == c){
                    teams.add(team);
                }
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return teams.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return teams.size();
    }
}

class TableViewHolder {
    TextView position;
    TextView teamName;
    TextView played;
    TextView wins;
    TextView draws;
    TextView losses;
    TextView goalsFor;
    TextView goalsAgainst;
    TextView goalDifference;
    TextView points;

}
