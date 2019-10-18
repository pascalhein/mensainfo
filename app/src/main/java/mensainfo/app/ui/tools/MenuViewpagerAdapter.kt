package mensainfo.app.ui.tools

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.tab_custom.view.*
import mensainfo.app.R
import mensainfo.app.openmensa_api.Day
import mensainfo.app.ui.MainActivity
import mensainfo.app.ui.MainFragment
import java.lang.Integer.max
import java.text.SimpleDateFormat
import java.util.*

class MenuViewpagerAdapter(frag: FragmentManager, private val canteenId: Int, private val days: List<Day>) : FragmentStatePagerAdapter(frag, RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): Fragment = MainFragment.newInstance(canteenId, days[position].date)

    override fun getPageTitle(position: Int): CharSequence? = DateFormatter.dateFormat.format(days[position].parsedDate)

    fun setupTabs(pager: ViewPager, tabs: TabLayout, previousItem: Int, previousAdapter: PagerAdapter?) {

        pager.adapter = this
        pager.currentItem = if (previousAdapter is MenuViewpagerAdapter
                && previousItem >= 0
                && previousItem < previousAdapter.days.size) {
            val date = previousAdapter.days[previousItem].parsedDate
            days.indexOfFirst { it.parsedDate == date }
        } else {
            todayPosition
        }

        (0 until tabs.tabCount).map(tabs::getTabAt).requireNoNulls().forEachIndexed { i, tab ->
            tab.setCustomView(R.layout.tab_custom)
            with(tab.customView!!) {
                tab_day_of_week.text = getDayOfWeek(i)
            }
        }

        tabs.getTabAt(todayPosition)?.customView?.tab_icon?.setImageResource(R.drawable.calendar_today)
    }

    private val todayPosition: Int
        get() {
            val today = Calendar.getInstance()
            val todayIndex = days.indexOfFirst { today.after(Calendar.getInstance().apply { time = it.parsedDate }) }
            return max(todayIndex, 0)
        }

    private fun getDayOfWeek(position: Int): CharSequence = DateFormatter.dowFormat.format(days[position].parsedDate)
}