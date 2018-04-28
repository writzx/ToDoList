package com.github.writzx.todolist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;

public class ToDoDateAdapter extends ArrayAdapter<ToDoDateElement> {
    Context context;

    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    private static class ViewHolder {
        TextView tDate;
        TextView tTitle;
        ImageView tExpand;
        ListView tTimeList;
    }

    public ToDoDateAdapter(ArrayList<ToDoDateElement> tDateElements, Context context) {
        super(context, R.layout.todo_date_element, tDateElements);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ToDoDateElement item = getItem(position);
        final ViewHolder holder;

        final View result;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.todo_date_element, parent, false);

            holder.tDate = convertView.findViewById(R.id.todoDate);
            holder.tTitle = convertView.findViewById(R.id.todoTitle);
            holder.tExpand = convertView.findViewById(R.id.todoExpand);
            holder.tTimeList = convertView.findViewById(R.id.todoTimeList);

            result = convertView;

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        holder.tDate.setText(item.getDate().toString()); // todo format
        holder.tTitle.setText(item.getTitle());

        holder.tDate.setOnClickListener(clickListener);
        holder.tTitle.setOnClickListener(clickListener);
        holder.tDate.setOnLongClickListener(longClickListener);
        holder.tTitle.setOnLongClickListener(longClickListener);

        holder.tExpand.setOnClickListener(new View.OnClickListener() {
            boolean expanding = false;
            boolean rotating = false;
            @Override
            public void onClick(View v) {
                if (!expanding && !rotating) {
                    expanding = rotating = true;
                    if (holder.tTimeList.getVisibility() == View.VISIBLE) {
                        ValueAnimator expander = UIUtils.getExpandAnimator(holder.tTimeList, false);
                        expander.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                expanding = false;
                            }
                        });
                        holder.tExpand.animate().rotationBy(-180).setDuration(300).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                rotating = false;
                            }
                        }).start();;

                        expander.start();
                    } else {
                        ValueAnimator expander = UIUtils.getExpandAnimator(holder.tTimeList, true);
                        expander.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                expanding = false;
                            }
                        });
                        holder.tExpand.animate().rotationBy(180).setDuration(300).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                rotating = false;
                            }
                        }).start();

                        expander.start();
                    }
                }
            }
        });
        ToDoTimeAdapter adapter = new ToDoTimeAdapter(position, new ArrayList<>(item.todos), getContext());
        adapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean onLongClick(View v) {
                AbstractMap.SimpleImmutableEntry<Integer, Integer> indices = (AbstractMap.SimpleImmutableEntry<Integer, Integer>) v.getTag();
                Intent i = new Intent(context, AddToDoTimeActivity.class);
                i.putExtra("todo_date_index", indices.getKey());
                i.putExtra("todo_time_index", indices.getValue());
                context.startActivity(i);
                return true;
            }
        });
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Tap and hold to edit...", Snackbar.LENGTH_LONG).show();
            }
        });
        holder.tTimeList.setAdapter(adapter);
        UIUtils.setListViewHeightBasedOnItems(holder.tTimeList);

        holder.tExpand.setTag(position);
        holder.tDate.setTag(position);
        holder.tTitle.setTag(position);

        return result;
    }

    public void setOnLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.clickListener = onClickListener;
    }
}
