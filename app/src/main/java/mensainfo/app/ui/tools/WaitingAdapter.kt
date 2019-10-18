package mensainfo.app.ui.tools

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import mensainfo.app.ui.WaitingFragment

class WaitingAdapter(frag: FragmentManager) : FragmentStatePagerAdapter(frag, RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = 1

    override fun getItem(position: Int): Fragment = WaitingFragment()

    override fun getPageTitle(position: Int): CharSequence? = "Loading"
}