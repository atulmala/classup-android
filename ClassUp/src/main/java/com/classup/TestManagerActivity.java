package com.classup;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class TestManagerActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private TestManagerPagerAdapter testManagerPagerAdapter;
    ActionBar actionBar;
    // tab titles
    private String[] tabs = {"Pending Tests", "Completed Tests"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_manager);
        Intent intent = getIntent();
        String exam_title = intent.getStringExtra("exam_title");
        String heading = exam_title + " Test list";
        this.setTitle(heading);

        // initialization
        viewPager = findViewById(R.id.pager);
        actionBar = getActionBar();
        testManagerPagerAdapter = new TestManagerPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(testManagerPagerAdapter);
        actionBar.setHomeButtonEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // add tabs
        for (String tab_name : tabs)    {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().pauseSession();
            SessionManager.getInstance().analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(SessionManager.getInstance().analytics != null) {
            SessionManager.getInstance().analytics.getSessionClient().resumeSession();
        }
    }

    // Create a tab listener that is called when the user changes tabs.
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
