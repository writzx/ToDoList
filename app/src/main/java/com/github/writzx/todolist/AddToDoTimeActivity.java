package com.github.writzx.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import java.util.AbstractMap;

public class AddToDoTimeActivity extends AppCompatActivity {
    TimePicker timePicker;
    EditText taskBox;
    ImageButton cancelTimeBtn;
    ImageButton deleteTimeBtn;
    ImageButton acceptTimeBtn;

    long id = -1;
    AbstractMap.SimpleEntry<Integer, Integer> indices = new AbstractMap.SimpleEntry<>(-1, -1);

    LocalTime selTime = LocalTime.now();

    void initViews() {
        timePicker = findViewById(R.id.timePicker);
        taskBox = findViewById(R.id.taskBox);
        cancelTimeBtn = findViewById(R.id.cancelTimeBtn);
        deleteTimeBtn = findViewById(R.id.deleteTimeBtn);
        acceptTimeBtn = findViewById(R.id.acceptTimeBtn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_do_time);
        setFinishOnTouchOutside(false);

        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            indices = new AbstractMap.SimpleEntry<>(extras.getInt("todo_date_index", -1), extras.getInt("todo_time_index", -1));
        }

        if (indices.getValue() != -1) {
            if (indices.getKey() != -1) {
                id = MainActivity.dateElements.get(indices.getKey()).todos.get(indices.getValue()).id;
                taskBox.setText(MainActivity.dateElements.get(indices.getKey()).todos.get(indices.getValue()).getTitle());
                selTime = MainActivity.dateElements.get(indices.getKey()).todos.get(indices.getValue()).time;
                timePicker.setCurrentHour(selTime.getHourOfDay());
                timePicker.setCurrentMinute(selTime.getMinuteOfHour());
            } else {
                assert extras != null;
                ToDoTimeElement tte = extras.getParcelable("todo_time_param");
                assert tte != null;
                id = tte.id;
                taskBox.setText(tte.title);
                selTime = tte.time;
                timePicker.setCurrentHour(selTime.getHourOfDay());
                timePicker.setCurrentMinute(selTime.getMinuteOfHour());
            }
            deleteTimeBtn.setVisibility(View.VISIBLE);
        }

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                selTime = LocalTime.fromMillisOfDay(((hourOfDay * 60) + minute) * 60 * 1000);
            }
        });

        cancelTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        acceptTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskBox.getText().toString().trim().isEmpty()) {
                    Snackbar.make(v, "ERROR: Enter a title...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                ToDoTimeElement tte = new ToDoTimeElement(taskBox.getText().toString(), selTime, false);
                if (id != -1) {
                    tte.id = id;
                }
                if (indices.getKey() != -1) {
                    if (indices.getValue() != -1) {
                        MainActivity.dateElements.get(indices.getKey()).todos.set(indices.getValue(), tte);
                    } else {
                        MainActivity.dateElements.get(indices.getKey()).todos.add(tte);
                    }
                    AddToDoDateActivity.notifyToDoChanged();
                    setResult(RESULT_OK);
                } else {
                    Intent res = new Intent();
                    res.putExtra("todo_time_index", indices.getValue());
                    res.putExtra("todo_time_result", tte);

                    setResult(RESULT_OK, res);
                }
                finish();
            }
        });

        deleteTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent res_del = new Intent();
                res_del.putExtra("todo_time_delete", indices.getValue());

                setResult(Activity.RESULT_CANCELED, res_del);
                finish();
            }
        });
    }
}
