package com.github.writzx.todolist;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalTime;

public class ToDoTimeElement implements Parcelable {
    public long id;
    public String title;
    public LocalTime time;
    public boolean done;

    public ToDoTimeElement() {
    }

    public ToDoTimeElement(String title, LocalTime time, boolean done) {
        id = DateTimeUtils.currentTimeMillis();
        this.title = title;
        this.time = time;
        this.done = done;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(time.toString(MainActivity.timeFormat));
        dest.writeByte((byte) (done ? 1 : 0));
    }

    public static final Parcelable.Creator<ToDoTimeElement> CREATOR = new Parcelable.Creator<ToDoTimeElement>() {
        @Override
        public ToDoTimeElement createFromParcel(Parcel source) {
            return new ToDoTimeElement(source);
        }

        @Override
        public ToDoTimeElement[] newArray(int size) {
            return new ToDoTimeElement[size];
        }
    };

    private ToDoTimeElement(Parcel in) {
        id = in.readLong();
        title = in.readString();
        time = LocalTime.parse(in.readString(), MainActivity.timeFormat);
        done = in.readByte() != 0;
    }
}
