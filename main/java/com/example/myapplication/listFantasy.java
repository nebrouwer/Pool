package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import java.util.HashMap;

public class listFantasy extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_list_fantasy,null);

        Context context;
        final ListView listView;
        Button remove, cancel, paid;
        Switch paidTest;

        Button addNew;
        final EditText newName;

        context = container.getContext();  // context in Fragment

        final ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_single_choice, MainActivity.players);
        listView = view.findViewById(R.id.list_player);
        listView.setChoiceMode(1);
        listView.setAdapter(adapter);

        int count = 0;
        for(String str : MainActivity.playersPaid){
            if(str.equals("Yes")){
                String temp = MainActivity.players.get(count);
                MainActivity.players.remove(count);
                MainActivity.players.add(count, temp + " (Paid)");
                adapter.notifyDataSetChanged();
                count++;
            }else{
                count++;
            }
        }
        /*if(listView.getCheckedItemPosition() == 1){
            paidTest.setVisibility(View.VISIBLE);
        }else{
            paidTest.setVisibility(View.GONE);
        }*/

        remove = view.findViewById(R.id.btn_remove);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position;
                    position = listView.getCheckedItemPosition();
                    System.out.println(MainActivity.players.get(position));
                    MainActivity.sendMessageToServer("#remove" + MainActivity.players.get(position) + " ");
                    MainActivity.players.remove(position);
                    listView.clearChoices();
                    adapter.notifyDataSetChanged();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        paid = view.findViewById(R.id.btn_paid);
        paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                try {
                    int position;
                    position = listView.getCheckedItemPosition();
                    String str = MainActivity.players.get(position);
                    if (!str.contains("(Paid)")) {
                        MainActivity.players.remove(position);
                        MainActivity.players.add(position, str + " (Paid)");
                        MainActivity.sendMessageToServer("#paid" + str + "@" + "Yes");
                        listView.clearChoices();
                        adapter.notifyDataSetChanged();
                    } else {
                        int i = str.indexOf(" ");
                        String temp = str.substring(0, i);
                        MainActivity.players.remove(position);
                        MainActivity.players.add(position, temp);
                        MainActivity.sendMessageToServer("#paid" + temp + "@" + "No");
                        listView.clearChoices();
                        adapter.notifyDataSetChanged();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                /*if(!paid.isChecked()){
                    String str = MainActivity.players.get(position);
                    MainActivity.players.remove(position);
                    MainActivity.players.add(position, str + "  (Paid)");
                    listView.clearChoices();
                    paid.toggle();
                    adapter.notifyDataSetChanged();
                }else{
                    MainActivity.playersPaid.set(position, "No");
                }*/
            }
        });

        cancel = view.findViewById(R.id.btn_Cancel);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                listView.clearChoices();
                adapter.notifyDataSetChanged();
            }
        });

        /*paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                int position;
                position = listView.getCheckedItemPosition();
                //paid.setVisibility(View.VISIBLE);
                if(paid.isChecked()){
                    String str = MainActivity.players.get(position);
                    MainActivity.players.remove(position);
                    MainActivity.players.add(position, str + "  (Paid)");
                    listView.clearChoices();
                    paid.toggle();
                    adapter.notifyDataSetChanged();
                }
            }
        });*/

        newName = (EditText) view.findViewById(R.id.input_newName);
        addNew = (Button) view.findViewById(R.id.btn_addNew);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = newName.getText().toString();
                if(!s.isEmpty()) {
                    MainActivity.players.add(s);
                    MainActivity.sendMessageToServer("#add" + s);
                    adapter.notifyDataSetChanged();
                    newName.setText("");
                }
            }
        });
        return view;
    }
}
