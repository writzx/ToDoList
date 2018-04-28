package com.github.writzx.todolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.joda.time.LocalDate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddToDoDateActivity extends AppCompatActivity {
    ListView todoEditView;
    FloatingActionButton dateFab;
    EditText titleBox;
    EditText dateBox;
    ImageButton cancelBtn;
    ImageButton deleteBtn;
    ImageButton acceptBtn;
    ImageButton dateBtn;

    static int editIndex = -1;
    static ArrayList<ToDoTimeElement> todos = new ArrayList<>();
    static ToDoTimeAdapter adapter;

    void initViews() {
        todoEditView = findViewById(R.id.todoEditView);
        dateFab = findViewById(R.id.dateFab);
        titleBox = findViewById(R.id.titleBox);
        dateBox = findViewById(R.id.dateBox);

        dateBox.setInputType(InputType.TYPE_NULL);
        dateBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;  // Blocks input from hardware keyboards.
            }
        });
        dateBox.setFocusable(false);

        dateBtn = findViewById(R.id.dateBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        acceptBtn = findViewById(R.id.acceptBtn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_do_date);
        setFinishOnTouchOutside(false);

        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editIndex = extras.getInt("todo_date_index", -1);
        } else {
            editIndex = -1;
        }

        todos = new ArrayList<>();

        dateBox.setText(LocalDate.now().toString());

        if (editIndex != -1) { // edit mode
            todos = MainActivity.dateElements.get(editIndex).todos;

            dateBox.setText(MainActivity.dateElements.get(editIndex).getDate().toString());
            titleBox.setText(MainActivity.dateElements.get(editIndex).getTitle());

            deleteBtn.setVisibility(View.VISIBLE);
        }

        adapter = new ToDoTimeAdapter(editIndex, todos, this);
        adapter.setEditor(true);
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean onLongClick(View v) {
                AbstractMap.SimpleImmutableEntry<Integer, Integer> indices = (AbstractMap.SimpleImmutableEntry<Integer, Integer>) v.getTag();
                Intent i = new Intent(AddToDoDateActivity.this, AddToDoTimeActivity.class);
                i.putExtra("todo_date_index", indices.getKey());
                i.putExtra("todo_time_index", indices.getValue());
                if (indices.getKey() == -1) {
                    i.putExtra("todo_time_param", todos.get(indices.getValue()));
                }
                startActivityForResult(i, 69);
                return true;
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Tap and hold to edit...", Snackbar.LENGTH_LONG).show();
            }
        });
        todoEditView.setAdapter(adapter);

        dateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddToDoDateActivity.this, AddToDoTimeActivity.class);
                i.putExtra("todo_date_index", editIndex);
                startActivityForResult(i, 69);
            }
        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDate dt = LocalDate.parse(dateBox.getText().toString());
                DatePickerDialog dpd = new DatePickerDialog(AddToDoDateActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                        dateBox.setText(date);
                    }
                }, dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth());
                dpd.show();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleBox.getText().toString().trim().isEmpty()) {
                    Snackbar.make(v, "ERROR: Enter a title...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                try {
                    LocalDate.parse(dateBox.getText().toString());
                } catch (Exception ex) {
                    Snackbar.make(v, "ERROR: Enter date in correct format...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (todos.size() == 0) {
                    Snackbar.make(v, "ERROR: Enter some tasks...", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (editIndex != -1) {
                    MainActivity.dateElements.set(editIndex, new ToDoDateElement(LocalDate.parse(dateBox.getText().toString()), titleBox.getText().toString(), todos));
                } else {
                    MainActivity.dateElements.add(new ToDoDateElement(LocalDate.parse(dateBox.getText().toString()), titleBox.getText().toString(), todos));
                }

                MainActivity.notifyDataSetChanged();

                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.dateElements.remove(editIndex);
                MainActivity.notifyDataSetChanged();

                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 69:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            ToDoTimeElement tte = data.getParcelableExtra("todo_time_result");
                            int idx = data.getIntExtra("todo_time_index", -1);
                            if (tte != null) {
                                if (idx != -1) {
                                    todos.set(idx, tte);
                                } else {
                                    todos.add(tte);
                                }
                                Collections.sort(todos, new Comparator<ToDoTimeElement>() {
                                    @Override
                                    public int compare(ToDoTimeElement o1, ToDoTimeElement o2) {
                                        return o1.getTime().compareTo(o2.getTime());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        if (data != null) {
                            int delId = data.getIntExtra("todo_time_delete", -1);
                            if (delId != -1) {
                                todos.remove(delId);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        break;
                }
                break;
        }
    }

    public static void notifyToDoChanged() {
        if (editIndex != -1) { // edit mode
            todos = MainActivity.dateElements.get(editIndex).todos;
        }
        Collections.sort(todos, new Comparator<ToDoTimeElement>() {
            @Override
            public int compare(ToDoTimeElement o1, ToDoTimeElement o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        MainActivity.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }
}
