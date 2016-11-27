package com.example.tommyzty.railsgithub;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IssueAdapter extends BaseAdapter {
    private final Activity activity;
    private final List<Issue> list;
    private int MAX_LEN = 140;

    public IssueAdapter(Activity activity, List<Issue> list) {
        super();
        this.activity = activity;
        this.list = list;
    }

    public void insert(Issue Issue) {
        list.add(Issue);
    }

    public void insert_at(Issue Issue, int id) {
        list.remove(id);
        list.add(id, Issue);
    }

    public void sort() {
        sortByNumber();
        Collections.sort(list, new Comparator<Issue>() {
            @Override
            public int compare(Issue one, Issue two) {
                if (one.getNumber() > two.getNumber())
                    return -1;
                if (one.getNumber() < two.getNumber())
                    return 1;
                return 0;
            }
        });
    }

    private void sortByNumber() {
        Collections.sort(list, new Comparator<Issue>() {
            @Override
            public int compare(Issue one, Issue two) {
                return one.getDate().compareTo(two.getDate());
            }
        });
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

    @Override
    public Issue getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.listrow, parent, false);
        }
        TextView col0 = (TextView) convertView.findViewById(R.id.column0);
        TextView col1 = (TextView) convertView.findViewById(R.id.column1);
        TextView col2 = (TextView) convertView.findViewById(R.id.column2);
        String body = list.get(position).getBody();
        body = body.substring(0, Math.min(body.length(), MAX_LEN));
        col0.setText(String.valueOf(list.get(position).getNumber()));
        col1.setText(list.get(position).getTitle());
        col2.setText(body);

        return convertView;
    }
}