package uw.classwatcher

import android.app.IntentService
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.endercrest.uwaterlooapi.UWaterlooAPI
import com.endercrest.uwaterlooapi.terms.models.TermCourseSchedule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

/**
 * A constructor is required, and must call the super [IntentService]
 * constructor with a name for the worker thread.
 */
class WatcherService : IntentService("WatcherService") {
    private lateinit var api: UWaterlooAPI
    private lateinit var classes: Set<Course>

    override fun onCreate() {
        super.onCreate()

        val mapper: ObjectMapper = XmlMapper()
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)

        val config =
            mapper.readValue<Config>(this.resources.openRawResource(R.raw.app_config))

        api = UWaterlooAPI(config.token)
        classes = config.classes
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    override fun onHandleIntent(intent: Intent?) {
        Toast.makeText(this, "Checking classes...", Toast.LENGTH_LONG)

        val free = kotlin.runCatching {
            classes.map {
                api.termsAPI.getCouresSchedules(it.term, it.subject, it.catalogNumber)
                    .data.single { data -> it.classSection == data.classNumber }
            }.filter { it.enrollmentTotal < it.enrollmentCapacity }
        }.getOrNull()

        if (free != null && free.isNotEmpty()) {
            if (free.size == 1) {
                singleClass(free.single())
            } else {
                multiClass(free)
            }
        }
    }

    private fun formatClass(schedule: TermCourseSchedule) =
        schedule.subject + " " + schedule.catalogNumber + "(" + schedule.section + ")"

    private fun singleClass(schedule: TermCourseSchedule) {
        showNotification("Class available: " + formatClass(schedule))
    }

    private fun multiClass(schedules: List<TermCourseSchedule>) {
        showNotification("Classes available: " + schedules.joinToString(separator = ", ", transform = ::formatClass))
    }

    private fun showNotification(text: String) {
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Class Available!")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, builder.build())
        }
    }

    data class Course(
        @JacksonXmlProperty(isAttribute = true) val subject: String, @JacksonXmlProperty(isAttribute = true) val catalogNumber: String,
        @JacksonXmlProperty(isAttribute = true) val classSection: Int, @JacksonXmlProperty(isAttribute = true) val term: String = "1201"
    )

    @JacksonXmlRootElement(localName = "app-config")
    data class Config(
        @JacksonXmlProperty(isAttribute = true) val token: String,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "class")
        val classes: Set<Course>
    )
}