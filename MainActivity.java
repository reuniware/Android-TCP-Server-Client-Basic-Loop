package fr.reuniware.www.android_tcp_demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Thread tcpServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try{
                    serverSocket = new ServerSocket(1080);
                    Log.d("SERVEUR", "Serveur démarré en écoute sur le port 1080");
                } catch (IOException e){
                    Log.d("SERVEUR", e.getMessage());
                    e.printStackTrace();
                }

                Socket socket = null;
                while(!Thread.currentThread().isInterrupted()) {

                    try{
                        socket = serverSocket.accept();

                        Log.d("SERVEUR", "Serveur a réçu une connexion client qui va être passée à CommunicationThread");

                        CommunicationThread communicationThread = new CommunicationThread(socket);
                        new Thread(communicationThread).start();

                    } catch (IOException e){
                        Log.d("SERVEUR", e.getMessage());
                        e.printStackTrace();
                    }

                }
            }
        });

        tcpServer.start();
        Log.d("SERVEUR","Serveur démarré");

        // client
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //InetAddress serverAddr = InetAddress.getByName("10.0.2.15");
                    InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
                    Socket socket = new Socket(serverAddr, 1080);

                    for (int i=0;i<100;i++) {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        out.write("Contenu du message provenant du client");
                        out.newLine();
                        out.flush();
                        Log.d("CLIENT OUT", "envoi vers serveur OK " + i);

                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String read = input.readLine();
                        if (read == null) {
                            Log.d("CLIENT IN", "reçu du serveur : NULL");
                        } else {
                            Log.d("CLIENT IN", "reçu du serveur : " + read);
                        }
                    }

                    } catch (UnknownHostException e1) {
                    Log.d("CLIENT", e1.getMessage());
                    e1.printStackTrace();
                } catch (IOException e1) {
                    Log.d("CLIENT", e1.getMessage());
                    e1.printStackTrace();
                }
            }
        });

        clientThread.start();

    }

}

/**
 * CommunicationThread : Appelé par le serveur (le serveur reçoit la socket client et la passe à CommunicationThread)
 *      Reçoit le flux client
 *      Lit le flux client
 *      Répond au flux client
 */
class CommunicationThread implements Runnable{

    Socket clientSocket = null;
    public CommunicationThread(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void run(){

        while (!Thread.currentThread().isInterrupted()){
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String read = input.readLine();
                if (read == null){
                    Log.d("COMMUNICATIONTHREAD IN","reçu du client : NULL");
                    Thread.currentThread().interrupt();
                } else {
                    Log.d("COMMUNICATIONTHREAD IN", "reçu du client : " + read);

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    out.write("Contenu du message provenant du serveur");
                    out.newLine();
                    out.flush();
                    Log.d("COMMUNICATIONTHREAD OUT","envoi vers client OK");
                }
            }catch (IOException e){
                Log.d("COMMUNICATIONTHREAD", e.getMessage());
                e.printStackTrace();
            }

        }

    }

}
