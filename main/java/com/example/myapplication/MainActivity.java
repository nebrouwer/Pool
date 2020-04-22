package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<String> players = new ArrayList<>();
    public static ArrayList<String> playersPaid = new ArrayList<>();
    public static String playerDB;

    public static Socket socket;
    public String host = "000.00.000.00";
    public final int port = 10101;
    public static BufferedReader in = null;
    public static PrintWriter out = null;

    final Context context = this;
    String alertMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        communicate();

        Fragment fr;
        fr = new login();  // your front page activity name
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction  = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_main,fr);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fr = null;
        int id = item.getItemId();

        /*if (id == R.id.action_menu1) {
            Toast.makeText(this, "action Menu1", Toast.LENGTH_SHORT).show();
            fr = new listFantasy();
        }
        if (id == R.id.action_menu2) {
            Toast.makeText(this, "action Menu2", Toast.LENGTH_SHORT).show();
            //fr = new activity for menu2();
        }*/

        if (id == R.id.action_logout) {
            Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (fr != null) {
            FragmentManager fm2 = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm2.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_main, fr);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    public static void sendMessageToServer(final String str) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
                    if (!str.isEmpty()){
                        out.println(str);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void communicate() {

        new Thread(new Runnable() {
            public void run() {

                try {
                    socket = new Socket(host, port);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.d("", "unknown host*");
                } catch (IOException e) {
                    Log.d("", "io exception*");
                    e.printStackTrace();
                }

                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    String msg = null;
                    try {
                        msg = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (msg == null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    else if(msg.equals("##")){
                        alertMessage = "User registered successfully";
                        mHandler.sendEmptyMessage(0);
                    }
                    else if (msg.equals("1")) {
                        alertMessage = "Wrong ID or Password";
                        mHandler.sendEmptyMessage(0);
                    }
                    else {
                        playerDB = msg;
                        androidx.fragment.app.Fragment fr;
                        fr = new listFantasy();
                        addPlayerList();
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction  = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_main,fr);
                        fragmentTransaction.commit();
                    }
                }  // end while

            }  // end run

        }).start();
    }

    public Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Alert")
                    .setMessage(alertMessage)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                        }
                    }).show();
        }
    };


    public void addPlayerList(){
        players.clear();
        String[] arr = playerDB.split(",");
        //ArrayList<String> playerList = new ArrayList<>();
        //playerList.addAll(Arrays.asList(arr));
        for(String str : arr){
            int i = str.indexOf("@");
            players.add(str.substring(0, i));
            playersPaid.add(str.substring(i + 1));
        }
        /*for(int i = 0; i < playerList.size(); i++){
            String str = playerList.get(i);
            int j = str.indexOf("@");
            players.add(str.substring(0, j));
        }*/
    }

}
