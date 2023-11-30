package com.example.mapswarm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
        private ArrayList<String> messagesArrayList;
        private Context context;

        public MessageListAdapter(ArrayList<String> messagesArrayList, Context context) {
                this.messagesArrayList = messagesArrayList;
                this.context = context;
        }

        @NonNull
        @Override
        public MessageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // this method is use to inflate the layout file
                // which we have created for our recycler view.
                // on below line we are inflating our layout file.
                View view = LayoutInflater.from(this.context).inflate(R.layout.message, parent, false);

                // at last we are returning our view holder
                // class with our item View File.
                return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageListAdapter.ViewHolder holder, int position) {
                // on below line we are setting text to our text view.

                holder.messageTV.setText(messagesArrayList.get(position));
                if(position == messagesArrayList.size()-1){
                        holder.messageTV.setTextColor(Color.BLACK);
                } else {
                        holder.messageTV.setTextColor(Color.GRAY);

                }
                DateTimeFormatter formatter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        formatter = DateTimeFormatter.ofPattern("HH:mm");
                        ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.systemDefault());
                        String formattedDate = dateTime.format(formatter);
                        holder.messageTime.setText(formattedDate);
                }
        }

        @Override
        public int getItemCount() {
                return messagesArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
                // on below line we are creating variable.
                private TextView messageTV;
                private TextView messageTime;

                public ViewHolder(@NonNull View itemView) {
                        super(itemView);
                        // on below line we are initialing our variable.
                        messageTV = itemView.findViewById(R.id.textMessage);
                        messageTime = itemView.findViewById(R.id.messageTime);
                }
        }

        private void appendColoredText(TextView tv, String text) {
                int start = tv.getText().length();
                tv.append(text);
                int end = tv.getText().length();

                Spannable spannableText = (Spannable) tv.getText();
                spannableText.setSpan(new AbsoluteSizeSpan(50), 0, start, 0); // set size
                spannableText.setSpan(new ForegroundColorSpan(Color.parseColor("#808080")),
                        0, start, 0);
                spannableText.setSpan(new AbsoluteSizeSpan(60), start, end, 0); // set size
                spannableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
        }
}