package com.classup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by root on 9/20/15.
 */
public class TestManagerPagerAdapter extends FragmentPagerAdapter {

    public TestManagerPagerAdapter(FragmentManager fragmentManager) {
        super (fragmentManager);
    }

    public Fragment getItem(int index) {
        switch(index)   {
            case 0:
                return new PendingTestsActivityFragment();
            case 1:
                return new CompletedTestsActivityFragment();
        }
        return null;
    }

    public int getCount()   {
        return 2;
    }
}
