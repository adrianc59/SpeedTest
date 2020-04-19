package com.example.speedtest;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private String[] numberWord;
    private int[] numberImage;
    private int[] numberProgress;

    public MainAdapter(Context c,String[] numberWord, int[] numberImage, int[] numberProgress ){
        context = c;
        this.numberWord = numberWord;
        this.numberImage = numberImage;
        this.numberProgress = numberProgress;
    }

    @Override
    public int getCount() {
        return numberWord.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(inflater == null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null){
            convertView = inflater.inflate(R.layout.row_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.image_view);
        TextView textView = convertView.findViewById(R.id.text_view);
        ProgressBar progressBar = convertView.findViewById(R.id.progress);



        imageView.setImageResource(numberImage[position]);
        textView.setText(numberWord[position]);
        progressBar.setProgress(Math.abs(100 - numberProgress[position]));

        return convertView;
    }
}
