package ru.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        String messageForUser = "";
        String userTo = "";
        String userFrom = "";
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                int indexFirstSpase = messageFromClient.indexOf(" ");
                String messageToSend = messageFromClient.substring(indexFirstSpase + 1);
                userFrom = messageFromClient.substring(0, indexFirstSpase - 1);
                String firstElementFromMessageToSend = messageToSend.substring(0, 1);
                if (firstElementFromMessageToSend.equals("@")) {
                    int indexFirstSpaseInToMessageToSend = messageToSend.indexOf(" ");
                    messageForUser = messageToSend.substring(indexFirstSpaseInToMessageToSend + 1);
                    userTo = messageToSend.substring(1, indexFirstSpaseInToMessageToSend);
                    sendPersonalMessage(userFrom, messageForUser, userTo);
                }
                 else {
                     broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void sendPersonalMessage(String userFrom, String messageForUser, String userTo) {
        for (ClientManager client : clients) {
            String result = userFrom + ": " + messageForUser;
            try {
                if (client.name.equals(userTo)) {
                    client.bufferedWriter.write(result);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void broadcastMessage(String message) {
            for (ClientManager client : clients) {
                try {
                    if (!client.name.equals(name)) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }

    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }

}




