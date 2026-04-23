package com.mico.launcher

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class MainPagerAdapter(private val pages: List<View>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = pages[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int = pages.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}
