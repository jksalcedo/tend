package com.jksalcedo.tend.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.jksalcedo.tend.R
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class TendWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TendWidgetFactory(applicationContext)
    }
}

class TendWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory, KoinComponent {

    private val personRepository: PersonRepository by inject()
    private var peopleList: List<Person> = emptyList()

    override fun onCreate() {
        // No-op
    }

    override fun onDataSetChanged() {
        runBlocking {
            try {
                peopleList = personRepository.getAllPeople().first()
            } catch (e: Exception) {
                e.printStackTrace()
                peopleList = emptyList()
            }
        }
    }

    override fun onDestroy() {
        peopleList = emptyList()
    }

    override fun getCount(): Int = peopleList.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= peopleList.size) {
            return RemoteViews(context.packageName, R.layout.tend_widget_item)
        }

        val person = peopleList[position]
        val rv = RemoteViews(context.packageName, R.layout.tend_widget_item)

        rv.setTextViewText(R.id.widget_item_name, person.name)

        val now = System.currentTimeMillis()
        val checkInDaysUntil = TimeUnit.MILLISECONDS.toDays(person.nextReminderAt - now)

        val nextEvent = person.events.map { event ->
            val nextOccurrence = DateUtils.getNextOccurrence(event.date)
            val days = DateUtils.daysUntil(nextOccurrence)
            event to days
        }.minByOrNull { it.second }

        val showEvent = nextEvent != null && nextEvent.second <= maxOf(
            nextEvent.first.leadTimeDays.toLong(),
            0L.coerceAtLeast(checkInDaysUntil)
        )

        val (displayDays, displayMessage) = if (showEvent) {
            val eventLabel = nextEvent!!.first.label
            val days = nextEvent.second
            val message = when (days) {
                0L -> "$eventLabel today!"
                1L -> "$eventLabel tomorrow"
                else -> "$eventLabel in $days days"
            }
            days to message
        } else {
            val message = when {
                checkInDaysUntil < 0 -> "Overdue by ${-checkInDaysUntil} day${if (-checkInDaysUntil != 1L) "s" else ""}"
                checkInDaysUntil == 0L -> "Due today!"
                checkInDaysUntil == 1L -> "Due tomorrow"
                else -> "Due in $checkInDaysUntil days"
            }
            checkInDaysUntil to message
        }

        rv.setTextViewText(R.id.widget_item_status, displayMessage)

        val isOverdue = displayDays < 0
        val isDueSoon = if (showEvent) {
            displayDays in 0..maxOf(3L, nextEvent!!.first.leadTimeDays.toLong())
        } else {
            displayDays in 0..3L
        }

        val bgRes = when {
            isOverdue -> R.drawable.widget_item_bg_overdue
            isDueSoon -> R.drawable.widget_item_bg_due_soon
            else -> R.drawable.widget_item_bg_normal
        }

        rv.setInt(R.id.widget_item_container, "setBackgroundResource", bgRes)

        // Set up fill-in intent for item click (opens details)
        val fillInIntent = Intent().apply {
            data = Uri.parse("tend://detail/${person.id}")
        }
        rv.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in peopleList.indices) peopleList[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
