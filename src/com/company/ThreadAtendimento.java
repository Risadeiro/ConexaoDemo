package com.company;

import java.io.*;
import java.net.Socket;

public class ThreadAtendimento {

    private Socket no = null;
    public ThreadAtendimento(Socket node){
        no = node;
    }
    public void run(){
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(no.getInputStream());
            BufferedReader reader = new BufferedReader(is);

            OutputStream os = no.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);

            String texto = reader.readLine();

            writer.writeBytes(texto.toUpperCase()+"\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
