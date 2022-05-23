package com.example.attendancenotifierappbyprajapatiomkar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Students> {
    public StudentAdapter(@NonNull Context context, @NonNull List<Students> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        Students currentValue = getItem(position);

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_view, parent, false);
        }

        TextView name = listItemView.findViewById(R.id.studentName);
        name.setText(currentValue.getName());
        TextView percentage = listItemView.findViewById(R.id.studentPercentage);
        percentage.setText(currentValue.getPercentage());

        return listItemView;
    }
}
