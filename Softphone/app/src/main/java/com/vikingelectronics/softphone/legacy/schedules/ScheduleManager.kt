package com.vikingelectronics.softphone.legacy.schedules

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.util.Log
import com.vikingelectronics.softphone.legacy.StartServiceReceiver
import com.vikingelectronics.softphone.legacy.StopServiceReceiver
import com.vikingelectronics.softphone.legacy.schedules.ScheduleObject
import dagger.hilt.android.qualifiers.ApplicationContext
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.io.*
import java.util.*
import javax.inject.Inject

class ScheduleManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val _schedules = mutableListOf<ScheduleObject>()
    val schedules: List<ScheduleObject> = _schedules

    // Returns list of Schedule Objects
//    fun getSchedule(): LinkedList<ScheduleObject?>? {
//        return schedule
//    }

    // Adds a Schedule Object to the list
    // Then two repeating alarms are created to start and stop the service
    @Throws(IOException::class)
    fun addSchedule(o: ScheduleObject) {
//        schedule.add(o)
        serializeSchedule()
        if (!o.interval.isAllDay) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val dt: DateTime = LocalDate.now().toDateTime(o.interval.intervalStart)
            val intent = Intent(context, StopServiceReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val dt2: DateTime = LocalDate.now().toDateTime(o.interval.intervalEnd)
            val intent2 = Intent(context, StartServiceReceiver::class.java)
            val pIntent2 = PendingIntent.getBroadcast(context, 0, intent2, 0)
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, dt.getMillis(), 86400000, pIntent)
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, dt2.getMillis(), 86400000, pIntent2)
        }
    }

    // Removes a Schedule Object from the list
    // Then it cancels its corresponding alarms
    @Throws(IOException::class)
    fun removeSchedule(o: Any?) {
//        schedule.remove(o)
        serializeSchedule()
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StartServiceReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        val intent2 = Intent(context, StopServiceReceiver::class.java)
        val pIntent2 = PendingIntent.getBroadcast(context, 0, intent2, 0)
        alarm.cancel(pIntent)
        alarm.cancel(pIntent2)
    }

    //Save Schedule Objects
    @Throws(IOException::class)
    fun serializeSchedule() {
        val fos: FileOutputStream = context.openFileOutput("schedule.ser", Context.MODE_PRIVATE)
        val os = ObjectOutputStream(fos)
//        os.writeObject(schedule)
        os.close()
    }

    //Retrieve Schedule Objects
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun deserializeSchedule() {
        val fis: FileInputStream = context.openFileInput("schedule.ser")
        val inputStream = ObjectInputStream(fis)
//        schedule = inputStream.readObject() as LinkedList<ScheduleObject?>
        inputStream.close()
        fis.close()
    }


    fun startSnooze() {
        //Add preference 'inSnooze' and set to true.  LinphoneService checks this preference when an incoming call is received.
        Log.i("START SNOOZE: ", "called")
        val prefs: SharedPreferences = context.getSharedPreferences("snooze", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("inSnooze", true)
        editor.putLong("finishTime", DateTime.now().getMillis() + getSnoozeInterval() * 60000)
        editor.commit()

        //This is just used to show a toast message and change the snooze indicator.  It has no effect on whether or not a call is accepted.
        val intent = Intent(context, StopServiceReceiver::class.java)
        intent.putExtra("startSnooze", true)
        context.sendBroadcast(intent)

        //This section sets the alarm to broadcast to StartServiceReceiver when the snooze period is over.
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent2 = Intent(context, StartServiceReceiver::class.java)
        intent2.putExtra("endSnooze", true)
        val pIntent = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_ONE_SHOT)
        alarm[AlarmManager.RTC_WAKEUP, DateTime.now().getMillis() + getSnoozeInterval() * 60000] = pIntent
    }

    //Prematurely end the snooze period
    fun stopSnooze() {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StartServiceReceiver::class.java)
        intent.putExtra("endSnooze", true)
        val pIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarm.cancel(pIntent)
        context.sendBroadcast(intent)
    }

    private fun getSnoozeInterval(): Int {
        val prefs: SharedPreferences = context.getSharedPreferences("snooze", Context.MODE_PRIVATE)
        return prefs.getInt("snoozeInterval", 10)
    }

    private fun setSnoozeInterval(t: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences("snooze", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("snoozeInterval", t)
        editor.apply()
    }

    //Takes a LocalTime object and passes the hour and minute to the private 'parseTime' function.
    fun parseTime(t: LocalTime): String? {
        if (t.millisOfSecond == 999) {
            return parseTime(0, 0, true)
        }
        val hour: Int = t.hourOfDay
        val minute: Int = t.minuteOfHour
        val isInMorning = hour < 12
        return parseTime(hour, minute, isInMorning)
    }

    //This is just a function to format hours and minutes as a string.
    private fun parseTime(hour: Int, minute: Int, isInMorning: Boolean): String? {
        //special case for midnight
        var newHour = hour
        if (newHour == 0 && minute == 0) {
            return "midnight"
        }
        return if (DateFormat.is24HourFormat(context)) {
            //Checks phone preference for time formatting.
            if (isInMorning) {
                newHour.toString() + ":" + String.format("%02d", minute)
            } else {
                newHour.toString() + ":" + String.format("%02d", minute)
            }
        } else {
            if (isInMorning) {
                newHour.toString() + ":" + String.format("%02d", minute) + "am"
            } else {
                if (newHour > 12) {
                    newHour -= 12
                }
                newHour.toString() + ":" + String.format("%02d", minute) + "pm"
            }
        }
    }
}