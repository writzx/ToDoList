package com.github.writzx.todolist;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.LocalDate;

import java.util.ArrayList;

public class ToDoDateElement implements Parcelable {
    public LocalDate date;
    public String title;
    public ArrayList<ToDoTimeElement> todos;

    public ToDoDateElement() {
    }

    public ToDoDateElement(LocalDate date, String title, ArrayList<ToDoTimeElement> todos) {
        this.date = date;
        this.title = title;
        this.todos = todos;
    }

    public LocalDate getDate() {
        return date;
    }

    public ArrayList<ToDoTimeElement> getTodos() {
        return todos;
    }

    public void addTodo(ToDoTimeElement todo) {
        todos.add(todo);
    }

    public void removeTodo(ToDoTimeElement todo) {
        todos.remove(todo);
    }

    public void removeTodo(int index) {
        todos.remove(index);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date.toString(MainActivity.dateFormat));
        dest.writeString(title);
        dest.writeArray(todos.toArray());
    }

    @SuppressWarnings("unchecked")
    private ToDoDateElement(Parcel in) {
        date = LocalDate.parse(in.readString(), MainActivity.dateFormat);
        title = in.readString();
        todos = new ArrayList<>();
        todos = in.readArrayList(ToDoTimeElement.class.getClassLoader());
    }

    public static final Creator<ToDoDateElement> CREATOR = new Creator<ToDoDateElement>() {
        @Override
        public ToDoDateElement createFromParcel(Parcel in) {
            return new ToDoDateElement(in);
        }

        @Override
        public ToDoDateElement[] newArray(int size) {
            return new ToDoDateElement[size];
        }
    };
}
