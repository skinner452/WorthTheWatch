package net.askinner.worththewatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private GameList gameList;

    private boolean initialConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = "Worth the Watch";

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        checkFirstRun();
    }

    private void displayHelp() {
        new AlertDialog.Builder(this)
                .setTitle("Welcome to Worth the Watch!")
                .setMessage("Matches:\n" +
                        "Each match displays either an average rating or a predicted rating depending on if the game is over or not. " +
                        "These ratings are based off how good a match was based off other users ratings of it. " +
                        "Tap on the game to get a more detailed look at how a match received a rating or how a predicted rating was computed. " +
                        "You can check off each game as you watch them or if you don't plan on watching them. This allows you to see other " +
                        "scores without getting any spoilers for the games you plan on watching later. When a game ends, you can tap on it " +
                        "to submit your own rating.\n\n" +
                        "Rating a game:\n" +
                        "Rating a game is very simple. You just need to select a rating from 1-10 and select the characteristics " +
                        "that define the match from your perspective. Ratings are very much appreciated as they help others determine " +
                        "if a match is worth the watch. " +
                        "Please try to be as unbiased as possible when submitting your rating.\n\n" +
                        "Table:\n" +
                        "This feature allows you to get a view of the table based off the games that YOU have watched. It will simply " +
                        "take the games that you have checked off from the Matches tab and generate a table just for you. " +
                        "You can select to see the table from each side of the conference or all of the teams together in the Supporters' Shield.")
                .setNeutralButton("Got it!",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void checkFirstRun() {
        final SharedPreferences sharedPreferences = getSharedPreferences("firstRun", 0);
        boolean firstRun = sharedPreferences.getBoolean("firstRun", true);
        if(firstRun){
            displayHelp();
            sharedPreferences.edit().putBoolean("firstRun", false).apply();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        replaceFragment(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh && !initialConnection){
            replaceFragment(0);
        }

        if(item.getItemId() == R.id.action_help){
            displayHelp();
        }

        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(int position){
        if(gameList == null){
            gameList = new GameList();
        }

        if(gameList.isEmpty()){
            if(!ConnectionCheck.hasConnection(getApplicationContext())){
                return;
            } else {
                initialConnection = true;
            }
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position){
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container,GameListFragment.newInstance(1,gameList))
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, YourTableFragment.newInstance(2,gameList))
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "Matches";
                break;
            case 2:
                mTitle = "My Table";
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
}
