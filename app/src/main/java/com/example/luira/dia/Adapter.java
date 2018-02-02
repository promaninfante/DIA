package com.example.luira.dia;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pedroroman on 1/31/18.
 */
public class Adapter extends  RecyclerView.Adapter<Adapter.MessageViewHolder> {
    List<ChatMessage> messages;




    public Adapter(List<ChatMessage> messages){
        this.messages = messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.messageText.setText(messages.get(position).getMessageText());
        holder.messageUser.setText(messages.get(position).getMessageUser());
        holder.messageTime.setText(messages.get(position).getMessageTime());
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {


        TextView messageText, messageUser, messageTime;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            messageUser = itemView.findViewById(R.id.message_user);
            messageTime = itemView.findViewById(R.id.message_time);

        }

    }
}
