/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.timetable

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.colorRes
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.android.timetable.adapter.DaySpinnerAdapter
import com.ohelshem.app.android.timetable.adapter.TimetableAdapter
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.model.WrappedHour
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.toast

class TimetableFragment : BaseMvpFragment<TimetableView, TimetablePresenter>(), TimetableView {
    private lateinit var recyclerView: RecyclerView
    private lateinit var headerView: ViewGroup
    private lateinit var table: TableLayout
    private lateinit var allWeek: ViewGroup
    private lateinit var menuEdit: MenuItem
    private lateinit var menuDone: MenuItem

    private var hasInitAllWeek = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = UI {
        frameLayout {
            recyclerView = customView<RecyclerView> {
                padding = dip(16)
                setHasFixedSize(true)
                clipToPadding = false
                layoutManager = LinearLayoutManager(activity)
            }
            allWeek = include<LinearLayout>(R.layout.timetable_all_week) {
                headerView = find(R.id.header_row)
                //FIXME                if (!presenter.learnsOnFriday) headerView.removeViewAt(0)
                table = find(R.id.table)
                visibility = View.GONE
            }
        }
    }.view

    override fun createPresenter(): TimetablePresenter = with(kodein()) { TimetablePresenter(instance(), instance()) }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuEdit = menu.findItem(R.id.edit)
        menuEdit.setOnMenuItemClickListener {
            toast(R.string.start_edit_mode)
            it.isVisible = false
            menuDone.isVisible = true
            presenter.isEditModeOn = true
            true
        }
        menuDone = menu.findItem(R.id.done)

        menuDone.setOnMenuItemClickListener {
            toast(R.string.finish_edit_mode)
            it.isVisible = false
            menuEdit.isVisible = true
            presenter.isEditModeOn = false
            true
        }
    }

    override fun init() {
        val spinner = screenManager.topNavigationElement
        spinner.adapter = DaySpinnerAdapter(activity, presenter.weekDays)
        spinner.post {
            spinner.onItemSelectedListener {
                onItemSelected { adapterView, view, position, id ->
                    presenter.setDay(position)
                }
            }
        }
    }

    override fun showDayTimetable() {
        recyclerView.show()
        allWeek.hide()
    }

    override fun showWeekTimetable() {
        allWeek.show()
        recyclerView.hide()
    }

    private fun initTimetable(data: Array<Array<Hour>>) {
        hasInitAllWeek = true
        val max = data.map { it.size }.max()!!
        val dp1 = dip(1)
        val dp24 = dip(24)
        val dp30 = dip(30)
        for (i in 0..max - 1) {
            val tableRow = TableRow(activity)
            val tableParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tableParams.setMargins(dp1, 0, 0, dp1)
            tableRow.layoutParams = tableParams
            table.addView(tableRow)
            for (d in data.size - 1 downTo 0) {
                val view = activity.layoutInflater.inflate(R.layout.timetable_weekly_item, tableRow, false)
                view.id = getId(d, i)
                if (data[d].size == 0) {
                    continue
                } else if (data[d].size <= i) {
                    view.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    view.setBackgroundColor(data[d][i].color)
                    (view.findViewById(R.id.text) as TextView).text = data[d][i].name
                }
                (view.layoutParams as TableRow.LayoutParams).setMargins(0, 0, dp1, 0)
                view.onClick {
                    if (data.size > d && data[d].size > i)
                        presenter.startEdit(data[d][i], d, i)
                }
                tableRow.addView(view)
            }
            val frameLayout = FrameLayout(activity)
            frameLayout.layoutParams = TableRow.LayoutParams(dp30, ViewGroup.LayoutParams.MATCH_PARENT)
            frameLayout.setBackgroundColor(activity.colorRes(R.color.colorPrimary))
            val number = TextView(activity)
            number.layoutParams = FrameLayout.LayoutParams(dp24, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            number.textSize = 15f
            number.setTypeface(null, Typeface.BOLD)
            number.setTextColor(Color.WHITE)
            number.gravity = Gravity.CENTER
            number.text = (i + 1).toString()
            frameLayout.addView(number)
            tableRow.addView(frameLayout)
        }
    }

    private fun getId(day: Int, hour: Int) = 100 + day * 10 + hour

    override fun setDay(day: Int, data: Array<Array<Hour>>) {
        screenManager.topNavigationElement.setSelection(day, false)
        if (day == 0) {
            screenManager.setToolbarElevation(false)
            if (!hasInitAllWeek)
                initTimetable(data)
        } else {
            screenManager.setToolbarElevation(true)
            recyclerView.adapter = TimetableAdapter(activity, data[day - 1]) { hour, position ->
                presenter.startEdit(hour, day - 1, position)
            }
        }

    }

    override fun flushWeek() {
        table.removeAllViews()
        hasInitAllWeek = false
    }

    override fun flushDay() {
    }

    override fun showEditScreen(hour: Hour, day: Int, position: Int) {
        val view = View.inflate(activity, R.layout.dialog_override, null)
        view.find<TextView>(R.id.currentName).text = hour.name
        view.find<TextView>(R.id.currentTeacher).text = hour.teacher
        val newName = view.find<EditText>(R.id.newName)
        val newTeacher = view.find<EditText>(R.id.newTeacher)
        val all = view.find<CheckBox>(R.id.changeAll)
        if (hour is WrappedHour) {
            newName.hint = hour.oldName
            newTeacher.hint = hour.oldTeacher
        }
        val builder = AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.apply) { dialog, which ->
                    presenter.edit(hour, day, position, newName.text.toString(), newTeacher.text.toString(), all.isChecked)
                }
                .setNegativeButton(R.string.cancel) { dialog, which ->

                }
        if (presenter.hasOverrideFor(day, position)) {
            builder.setNeutralButton(R.string.return_to_default) { dialog, which ->
                presenter.returnToDefault(hour, day, position, all.isChecked)
            }
        }
        builder.show()
    }

    override val isShowingDayView: Boolean
        get() = recyclerView.visibility == View.VISIBLE

    override var menuId: Int = R.menu.timetable


}