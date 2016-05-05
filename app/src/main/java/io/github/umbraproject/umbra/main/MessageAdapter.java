package io.github.umbraproject.umbra.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.umbraproject.umbra.R;
import io.github.umbraproject.umbra.main.model.Message;

/**
 * Created by matt on 5/4/16.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    List<Message> messageList;

    public MessageAdapter (List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.from.setText(message.getFrom());
        holder.message.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView from, message;

        public MessageViewHolder(View itemView) {
            super(itemView);

            from = (TextView) itemView.findViewById(R.id.from_textView);
            message = (TextView) itemView.findViewById(R.id.message_textView);
        }
    }

}
