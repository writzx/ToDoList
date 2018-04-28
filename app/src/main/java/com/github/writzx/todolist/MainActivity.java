package com.github.writzx.todolist;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<ToDoDateElement> dateElements = new ArrayList<>();
    public static ToDoDateAdapter adapter;

    FloatingActionButton fab;
    ListView todoView;

    void initViews() {
        todoView = findViewById(R.id.todoView);
        fab = findViewById(R.id.fab);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        ToDoTimeElement t1 = new ToDoTimeElement("FIRST TIME ELEMENT", LocalTime.parse("10:10"), false);
        ToDoTimeElement t2 = new ToDoTimeElement("SECOND TIME ELEMENT", LocalTime.parse("11:11"), false);
        ToDoTimeElement t3 = new ToDoTimeElement("THIRD TIME ELEMENT", LocalTime.parse("13:12"), false);
        ToDoTimeElement t4 = new ToDoTimeElement("FOURTH TIME ELEMENT", LocalTime.parse("17:11"), false);
        ToDoTimeElement t5 = new ToDoTimeElement("FIFTH TIME ELEMENT", LocalTime.parse("20:10"), false);

        ToDoDateElement td1 = new ToDoDateElement(LocalDate.parse("2018-03-18"), "TITLE 1", new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5)));

        ToDoTimeElement t21 = new ToDoTimeElement("FIRST TIME ELEMENT", LocalTime.parse("1:10"), false);
        ToDoTimeElement t22 = new ToDoTimeElement("SECOND TIME ELEMENT", LocalTime.parse("10:10"), false);
        ToDoTimeElement t23 = new ToDoTimeElement("THIRD TIME ELEMENT", LocalTime.parse("12:10"), false);
        ToDoTimeElement t24 = new ToDoTimeElement("FOURTH TIME ELEMENT", LocalTime.parse("13:10"), false);
        ToDoTimeElement t25 = new ToDoTimeElement("FIFTH TIME ELEMENT", LocalTime.parse("14:10"), false);
        ToDoTimeElement t26 = new ToDoTimeElement("SIXTH TIME ELEMENT", LocalTime.parse("15:10"), false);

        ToDoDateElement td2 = new ToDoDateElement(LocalDate.parse("2018-04-19"), "TITLE 2", new ArrayList<>(Arrays.asList(t21, t22, t23, t24, t25, t26)));

        dateElements = new ArrayList<>(Arrays.asList(td1, td2));

        adapter = new ToDoDateAdapter(dateElements, getApplicationContext());
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

    public static void notifyDataSetChanged() {
        Collections.sort(dateElements, new Comparator<ToDoDateElement>() {
            @Override
            public int compare(ToDoDateElement o1, ToDoDateElement o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        adapter.notifyDataSetChanged();
    }
}
