package com.nickb.spots.Utils;



// using the sections state pager adapter over the sections pager adapter because the settings page could
// end up having multiple settings/options.

// this is better for more options other is better for tabs

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// Account Settings Fragment (Part 12)

// houses the fragments of the profile settings page

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();

    // these hashmaps are for identifying the fragments if you have: fragment object, name or the integer
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();



    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentName) {
        mFragmentList.add(fragment);

        // fill all the hashmaps up to help for identifying fragments
        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentNumbers.put(fragmentName, mFragmentList.size()-1);
        mFragmentNames.put(mFragmentList.size()-1, fragmentName);
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentName
     * @return
     */

    public Integer getFragmentNumber(String fragmentName) {
        if(mFragmentNumbers.containsKey(fragmentName)) {
         return  mFragmentNumbers.get(fragmentName);
        } else {
            return null;
        }
    }

    /**
     * returns the fragment number with the fragment object @param
     * @param fragment
     * @return
     */

    public Integer getFragmentNumber(Fragment fragment) {
        if(mFragments.containsKey(fragment)) {
            return  mFragments.get(fragment);
        } else {
            return null;
        }
    }


    /**
     * returns the fragment name from the fragment number @param
     * @param fragmentNumber
     * @return
     */

    public String getFragmentName(Integer fragmentNumber) {
        if(mFragmentNames.containsKey(fragmentNumber)) {
            return  mFragmentNames.get(fragmentNumber);
        } else {
            return null;
        }
    }
}
