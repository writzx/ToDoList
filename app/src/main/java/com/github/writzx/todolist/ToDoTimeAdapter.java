package com.github.writzx.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;

public class ToDoTimeAdapter extends ArrayAdapter<ToDoTimeElement> implements View.OnClickListener {
    int dpos;

    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    boolean editor = false;

    private static class ViewHolder {
        TextView tTime;
        TextView tTitle;
        ImageView tDone;
    }

    public boolean isEditor() {
        return editor;
    }

    public void setEditor(boolean editor) {
        this.editor = editor;
    }

    public ToDoTimeAdapter(int dpos, ArrayList<ToDoTimeElement> tTimeElements, Context context) {
        super(context, R.layout.todo_time_element, tTimeElements);
        this.dpos = dpos;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ToDoTimeElement item = getItem(position);

        ViewHolder holder;

        final View result;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView = inflater.inflate(R.layout.todo_time_element, parent, false);

            holder.tTime = convertView.findViewById(R.id.todoTime);
            holder.tTitle = convertView.findViewById(R.id.todoTimeTitle);

            holder.tTitle.setOnLongClickListener(longClickListener);
            holder.tTime.setOnLongClickListener(longClickListener);
            holder.tTitle.setOnClickListener(clickListener);
            holder.tTime.setOnClickListener(clickListener);

            holder.tDone = convertView.findViewById(R.id.todoDone);
            if (editor) {
                holder.tDone.setVisibility(View.GONE);
            }

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        holder.tTime.setText(item.getTime().toString("HH:mm")); // todo format
        holder.tTitle.setText(item.getTitle());
        if (item.isDone()) {
            holder.tDone.setImageResource(R.drawable.ic_done_green);
        } else {
            holder.tDone.setImageResource(R.drawable.ic_done_gray);
        }

        holder.tDone.setOnClickListener(this);
        holder.tDone.setTag(new AbstractMap.SimpleImmutableEntry<>(dpos, position));
        holder.tTime.setTag(new AbstractMap.SimpleImmutableEntry<>(dpos, position));
        holder.tTitle.setTag(new AbstractMap.SimpleImmutableEntry<>(dpos, position));

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v) {
        AbstractMap.SimpleImmutableEntry<Integer, Integer> indices = (AbstractMap.SimpleImmutableEntry<Integer, Integer>) v.getTag();
        switch (v.getId()) {
            case R.id.todoDone:
                if (indices != null) {
                    boolean te = MainActivity.dateElements.get(indices.getKey()).todos.get(indices.getValue()).done;
                    MainActivity.dateElements.get(indices.getKey()).todos.get(indices.getValue()).done = !te;
                    // notify change
                    MainActivity.notifyDataSetChanged();
                }
                break;
        }
    }

    public void setOnLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.clickListener = onClickListener;
    }
}
