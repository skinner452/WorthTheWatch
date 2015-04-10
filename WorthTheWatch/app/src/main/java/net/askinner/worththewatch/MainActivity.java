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

    private void checkFirstRun() {
        final SharedPreferences sharedPreferences = getSharedPreferences("firstRun", 0);
        boolean firstRun = sharedPreferences.getBoolean("firstRun", true);
        if(firstRun){
            new AlertDialog.Builder(this)
                    .setTitle("Welcome to Worth the Watch!")
                    .setMessage("Matches:\n" +
                            "Lists all of the matches for the season in a weekly order\n" +
                            "Press the left and right arrows to go through weeks\n" +
                            "Tap the week text to go back to the current week\n" +
                            "Check each match to reveal the scores if they are available (Should be available within 5 minutes of the final score being posted)\n" +
                            "The check at the top of the screen will check off the entire week at once\n" +
                            "When the games aren't checked, they will display one of two things\n" +
                            "a) If the game is over it will show an average rating by the users\n" +
                            "b) If the game isn't over it will show a predicted rating based off previous matches by those teams\n" +
                            "Tapping a game in the list will either give you a break down of how the match got its average rating or predicted rating\n" +
                            "If the game is over, the user has the option of rating a game by tapping the rate button\n\n" +
                            "Rating a game:\n" +
                            "You can only rate each game once per device\n" +
                            "Use the slider to select a rating from 1-10\n" +
                            "Select the characteristics that fit the match\n" +
                            "Once you're done, press the submit button\n" +
                            "If the rating was successful, it will show an updated average rating with your rating included\n\n" +
                            "Table:\n" +
                            "Generates a table based off your check marks from the Matches section\n" +
                            "Allows you to look at a table without any spoilers at all\n" +
                            "Select between Supporters' Shield, Eastern Conference and Western Conference\n")
                    .setNeutralButton("Got it!",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().putBoolean("firstRun", false).apply();
                            dialog.dismiss();
                        }
                    })
                    .show();
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
