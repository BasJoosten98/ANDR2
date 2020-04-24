package com.flamevision.findiro.UserAndGroups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.flamevision.findiro.R;

import java.util.List;

public class CustomUserListAdapter extends BaseAdapter {
    private  List<User> users;
    private LayoutInflater layoutInflater;

    public  CustomUserListAdapter(Context context, List<User> users){
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item_select_user, parent, false);
        }

        ImageView ivPicture = convertView.findViewById(R.id.SelectUserItemPicture);
        TextView tvName = convertView.findViewById(R.id.SelectUserItemName);

        ivPicture.setImageBitmap(users.get(position).getProfilePicture());
        tvName.setText(users.get(position).getName());

        return convertView;
    }
}