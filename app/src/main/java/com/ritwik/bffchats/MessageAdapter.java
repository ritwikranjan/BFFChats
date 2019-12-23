package com.ritwik.bffchats;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageAdapter extends ArrayAdapter {
    public MessageAdapter(@NonNull Context context, int resource, List<Messages> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View currentView, @NonNull ViewGroup parent) {
        if (currentView == null) {
            currentView = ((Activity) getContext( )).getLayoutInflater( ).inflate(R.layout.item_view, parent, false);
        }
        ImageView dp = currentView.findViewById(R.id.imageViewDP);
        TextView userName = currentView.findViewById(R.id.textViewUsername);
        TextView message = currentView.findViewById(R.id.textViewMessage);

        Messages mMessages = (Messages) getItem(position);

        userName.setText(mMessages.getName( ));
        message.setText(mMessages.getMessage( ));
        Glide.with(getContext( )).load(mMessages.photoUrl).into(dp);

        return currentView;
    }

}
