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

package com.ohelshem.app.android.dashboard

import com.hannesdorfmann.mosby3.mvp.MvpView
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.app.model.HourData

interface DashboardView : MvpView {
    fun showLessonInfo(data: HourData, isEndOfDay: Boolean, isTomorrow: Boolean, isFuture: Boolean, changes: List<Change>?)

    fun showHolidayInfo(isTomorrow: Boolean, isFuture: Boolean): Boolean

    fun showEmptyCard()

    fun showTests(tests: List<Test>)
}