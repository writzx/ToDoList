package com.github.writzx.todolist;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "ToDoTaskNotify";
    public static ArrayList<ToDoDateElement> dateElements = new ArrayList<>();
    public static ToDoDateAdapter adapter;

    public static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("d MMM y");
    public static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("h:mm a");

    public static final String JSON_FILENAME = "data.json";
    public static final String NOTIF_FILENAME = "notif.json";
    public static ObjectMapper MAPPER = new ObjectMapper();

    public static Handler HANDLER = new Handler();
    public static File JSON_DATA;
    public static File NOTIF_DATA;
    public static AlarmManager ALARM_MANAGER;

    public static WeakReference<Context> CONTEXT;

    FloatingActionButton fab;
    ListView todoView;

    static {
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(LocalDate.class, new DateDeserializer());
        mod.addDeserializer(LocalTime.class, new TimeDeserializer());
        ;
        mod.addSerializer(LocalDate.class, new DateSerializer());
        mod.addSerializer(LocalTime.class, new TimeSerializer());

        MAPPER.registerModule(mod);
    }

    void initViews() {
        todoView = findViewById(R.id.todoView);
        fab = findViewById(R.id.fab);
    }

    public static void notifyDataSetChanged() {
        Collections.sort(dateElements, new Comparator<ToDoDateElement>() {
            @Override
            public int compare(ToDoDateElement o1, ToDoDateElement o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        adapter.notifyDataSetChanged();
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MAPPER.writeValue(JSON_DATA, dateElements);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 100);
        saveNotifications();
    }

    public static void saveNotifications() {
        // read all saved notifications
        ArrayList<Notification> notifs = new ArrayList<>();
        try {
            notifs = MAPPER.readValue(NOTIF_DATA, new TypeReference<ArrayList<Notification>>() {
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            ArrayList<Notification> newNotifs = new ArrayList<>();
            Multimap<Integer, Integer> oldPairs = ArrayListMultimap.create();

            if (notifs.size() > 0) {
                ArrayList<Integer> doneNotifs = new ArrayList<>();

                for (int k = 0; k < dateElements.size(); k++) {
                    ToDoDateElement tde = dateElements.get(k);
                    for (int j = 0; j < tde.todos.size(); j++) {
                        ToDoTimeElement tte = tde.todos.get(j);
                        for (int i = 0; i < notifs.size(); i++) {
                            if (doneNotifs.contains(i)) continue;
                            Notification nf = notifs.get(i);
                            if (tte.id == nf.id) {
                                if (tde.date != nf.date || tte.time != nf.time) {
                                    // cancel the old one
                                    Intent alarmIntent = new Intent(CONTEXT.get(), AlarmReceiver.class);
                                    alarmIntent.setData(Uri.parse("todo://" + tde.date + "/" + tte.time + "/" + tte.id));
                                    alarmIntent.setAction(String.valueOf(tte.id));

                                    PendingIntent dispInt = PendingIntent.getBroadcast(CONTEXT.get(), 0, alarmIntent, 0);

                                    ALARM_MANAGER.cancel(dispInt);

                                    nf.time = tte.time;
                                    nf.date = tde.date;

                                    // re-add a new one
                                    long millis = nf.date.toLocalDateTime(nf.time).toDateTime().getMillis();

                                    ALARM_MANAGER.setExact(AlarmManager.RTC_WAKEUP, millis, dispInt);

                                }
                                newNotifs.add(nf);
                                doneNotifs.add(i);
                                oldPairs.put(k, j);
                                break;
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < dateElements.size(); i++) {
                ToDoDateElement tde = dateElements.get(i);
                for (int j = 0; j < tde.todos.size(); j++) {
                    ToDoTimeElement tte = tde.todos.get(j);
                    // exclude adding already present notifications
                    if (!oldPairs.get(i).contains(j)) {
                        // new notification, add to list
                        Notification nf = new Notification();
                        nf.id = tte.id;
                        nf.date = tde.date;
                        nf.time = tte.time;

                        // add the notification alarm
                        Intent alarmIntent = new Intent(CONTEXT.get(), AlarmReceiver.class);
                        alarmIntent.setData(Uri.parse("todo://" + tde.date + "/" + tte.time + "/" + tte.id));
                        alarmIntent.setAction(String.valueOf(tte.id));

                        PendingIntent dispInt = PendingIntent.getBroadcast(CONTEXT.get(), 0, alarmIntent, 0);

                        long millis = nf.date.toLocalDateTime(nf.time).toDateTime().getMillis();
                        ALARM_MANAGER.setExact(AlarmManager.RTC_WAKEUP, millis, dispInt);

                        DateTime dt = new DateTime(millis);
                        System.out.println(millis + "\t" + dt.toString(MainActivity.dateFormat) + "\n" + dt.toString(MainActivity.timeFormat));

                        dt = new DateTime(System.currentTimeMillis());
                        System.out.println(System.currentTimeMillis() + "\t" + dt.toString(MainActivity.dateFormat) + "\n" + dt.toString(MainActivity.timeFormat));

                        newNotifs.add(nf);
                    }
                }
            }

            MAPPER.writeValue(NOTIF_DATA, newNotifs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Task Reminders";
            String description = "Reminders associated with every task in the ToDo List";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        JSON_DATA = new File(getFilesDir(), JSON_FILENAME);
        NOTIF_DATA = new File(getFilesDir(), NOTIF_FILENAME);

        if (!JSON_DATA.exists()) {
            try {
                JSON_DATA.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!NOTIF_DATA.exists()) {
            try {
                NOTIF_DATA.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ALARM_MANAGER = (AlarmManager) getSystemService(ALARM_SERVICE);
        CONTEXT = new WeakReference<>(getApplicationContext());

        try {
            dateElements = MAPPER.readValue(JSON_DATA, new TypeReference<ArrayList<ToDoDateElement>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ToDoTimeElement t1 = new ToDoTimeElement("FIRST TIME ELEMENT", LocalTime.parse("10:10"), false);
//        ToDoTimeElement t2 = new ToDoTimeElement("SECOND TIME ELEMENT", LocalTime.parse("11:11"), false);
//        ToDoTimeElement t3 = new ToDoTimeElement("THIRD TIME ELEMENT", LocalTime.parse("13:12"), false);
//        ToDoTimeElement t4 = new ToDoTimeElement("FOURTH TIME ELEMENT", LocalTime.parse("17:11"), false);
//        ToDoTimeElement t5 = new ToDoTimeElement("FIFTH TIME ELEMENT", LocalTime.parse("20:10"), false);
//
//        ToDoDateElement td1 = new ToDoDateElement(LocalDate.parse("2018-03-18"), "TITLE 1", new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5)));
//
//        ToDoTimeElement t21 = new ToDoTimeElement("FIRST TIME ELEMENT", LocalTime.parse("1:10"), false);
//        ToDoTimeElement t22 = new ToDoTimeElement("SECOND TIME ELEMENT", LocalTime.parse("10:10"), false);
//        ToDoTimeElement t23 = new ToDoTimeElement("THIRD TIME ELEMENT", LocalTime.parse("12:10"), false);
//        ToDoTimeElement t24 = new ToDoTimeElement("FOURTH TIME ELEMENT", LocalTime.parse("13:10"), false);
//        ToDoTimeElement t25 = new ToDoTimeElement("FIFTH TIME ELEMENT", LocalTime.parse("14:10"), false);
//        ToDoTimeElement t26 = new ToDoTimeElement("SIXTH TIME ELEMENT", LocalTime.parse("15:10"), false);
//
//        ToDoDateElement td2 = new ToDoDateElement(LocalDate.parse("2018-04-19"), "TITLE 2", new ArrayList<>(Arrays.asList(t21, t22, t23, t24, t25, t26)));
//
//        dateElements = new ArrayList<>(Arrays.asList(td1, td2));
//
//        try {
//            MAPPER.writeValue(jsonData, dateElements);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        adapter = new ToDoDateAdapter(dateElements, new WeakReference<>(getApplicationContext()));
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(MainActivity.this, AddToDoDateActivity.class);
                i.putExtra("todo_date_index", (int) v.getTag());
                startActivity(i);
                return true;
            }
        });
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Tap and hold to edit...", Snackbar.LENGTH_LONG).show();
            }
        });
        todoView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddToDoDateActivity.class);
                startActivity(i);
            }
        });
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = Long.valueOf(intent.getAction());
            ToDoDateElement tde = null;
            ToDoTimeElement tte = null;
            for (int k = 0; k < dateElements.size(); k++) {
                tde = dateElements.get(k);
                for (int j = 0; j < tde.todos.size(); j++) {
                    tte = tde.todos.get(j);
                    if (tte.id == id) {
                        break;
                    }
                }
            }

            if (tde == null || tte == null) return;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(CONTEXT.get(), CHANNEL_ID)
                    .setContentTitle(tde.date.toString(MainActivity.dateFormat) + "\t" + tde.title)
                    .setContentText(tte.time.toString(MainActivity.timeFormat) + "\t" + tte.title)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(R.drawable.ic_doneall_green)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(Joiner.on("\n").join(
                            Iterables.transform(tde.todos,
                                    new Function<ToDoTimeElement, String>() {
                                        @Override
                                        public String apply(@NonNull ToDoTimeElement input) {
                                            return input.time.toString(MainActivity.timeFormat) + "\t" + input.title;
                                        }
                                    })))); // todo set tap action

            NotificationManagerCompat nm = NotificationManagerCompat.from(MainActivity.CONTEXT.get());

            nm.notify((int) tte.id, builder.build());
            System.out.println(System.currentTimeMillis());
        }
    }

    public static class Notification {
        long id;
        LocalDate date;
        LocalTime time;
    }

    public static class TimeSerializer extends StdSerializer<LocalTime> {
        public TimeSerializer() {
            this(null);
        }

        protected TimeSerializer(Class<LocalTime> t) {
            super(t);
        }

        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString(MainActivity.timeFormat));
        }
    }

    public static class DateSerializer extends StdSerializer<LocalDate> {
        public DateSerializer() {
            this(null);
        }

        protected DateSerializer(Class<LocalDate> t) {
            super(t);
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString(MainActivity.dateFormat));
        }
    }

    public static class TimeDeserializer extends StdDeserializer<LocalTime> {
        public TimeDeserializer() {
            this(null);
        }

        protected TimeDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return LocalTime.parse(p.getText(), MainActivity.timeFormat);
        }
    }

    public static class DateDeserializer extends StdDeserializer<LocalDate> {
        public DateDeserializer() {
            this(null);
        }

        protected DateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return LocalDate.parse(p.getText(), MainActivity.dateFormat);
        }
    }
}
