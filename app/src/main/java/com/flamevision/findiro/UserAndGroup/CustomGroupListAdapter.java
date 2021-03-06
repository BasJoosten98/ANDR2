package com.flamevision.findiro.UserAndGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.flamevision.findiro.R;

import java.util.List;

public class CustomGroupListAdapter extends BaseAdapter {
    private List<Group> groups;
    private LayoutInflater layoutInflater;

    public  CustomGroupListAdapter(Context context, List<Group> groups){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groups = groups;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item_select_group, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.selectGroupName);
        TextView tvMembers = convertView.findViewById(R.id.selectGroupMemberAmount);
        TextView tvCreator = convertView.findViewById(R.id.selectGroupCreator);

        tvName.setText(groups.get(position).getName());
        tvMembers.setText("Members: " + groups.get(position).getMembers().size());
        tvCreator.setText("Group Creator: " + groups.get(position).getGroupCreator());

        return convertView;
    }
}
