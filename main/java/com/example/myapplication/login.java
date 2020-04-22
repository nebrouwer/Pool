package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class login extends Fragment {

    TextView textView;
    Button register, login;
    EditText id, pwd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_login,null);

        id = (EditText)view.findViewById(R.id.inut_id);
        pwd = (EditText)view.findViewById(R.id.inut_pwd);
        login = (Button)view.findViewById(R.id.btn_login);
        register = (Button)view.findViewById(R.id.btn_register);

        /*exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closefragment();
            }
        });*/

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idpwd = "#login" + id.getText().toString() + "@" + pwd.getText().toString();

                MainActivity.sendMessageToServer(idpwd);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idpwd = "#register" + id.getText().toString() + "@" + pwd.getText().toString();

                MainActivity.sendMessageToServer(idpwd);
            }
        });
        return view;
    }

    private void closefragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
