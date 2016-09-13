package adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import svecw.svecw.AlumniActivity;
/**
 * Created by Pavan on 7/6/15.
 */
public class WallTabsAdapter extends FragmentPagerAdapter {

    public WallTabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                //return new StudentActivity();
            case 1:
                // Games fragment activity
                return new AlumniActivity();

        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

}
