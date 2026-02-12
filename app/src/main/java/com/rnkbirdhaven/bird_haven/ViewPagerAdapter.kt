package com.rnkbirdhaven.bird_haven

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: Fragment, private val fragments: List<Fragment>) :
    FragmentStateAdapter(fragment) {

    //Returns the total number of fragments(Stefan,2022)
    override fun getItemCount(): Int = fragments.size

    //Creates a fragment at the specified position(Stefan,2022)
    override fun createFragment(position: Int): Fragment {
        //Return the fragment at the given position from the fragments list(Stefan,2022)
        return fragments[position]
    }
}
