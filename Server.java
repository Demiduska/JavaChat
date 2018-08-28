package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Server {
    private static Map<String, Connection> connectionMap = new java.util.concurrent.ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Enter server port:");
        int port = ConsoleHelper.readInt();
            try (ServerSocket ss = new ServerSocket(port)) {
                ConsoleHelper.writeMessage("The server is running..");
                while (true) {
                    Socket socket = null;
                    try {
                        socket = ss.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    Handler handler = new Handler(socket);
                    handler.start();
                }
            }
    }
    public static void sendBroadcastMessage(Message message) {
        try {
            for (Map.Entry<String, Connection> map : connectionMap.entrySet()) {
                map.getValue().send(message);
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error");
        }
    }
    private static class Handler extends Thread {
        private Socket socket;
    public Handler(Socket socket) {
    this.socket = socket;
}
    private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
    while (true) {
    //Generate and send a user name request command
        connection.send(new Message(MessageType.NAME_REQUEST));
        //Receive the answer
       Message message =  connection.receive();
       //Check that a command with a user name was received
        if (message.getType()==MessageType.USER_NAME) {
            // check that the name wasn't empty and connectionMap doesn't have this user name
            if (!message.getData().isEmpty() && connectionMap.get(message.getData())== null) {
// add new user and connection with him in connectionMap
                connectionMap.put(message.getData(), connection);
//send to the client information
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return message.getData();
            }
        }
}
    }
        private void sendListOfUsers(Connection connection, String userName) {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                if (pair.getKey().equals(userName))
                    break;
                try {
                    connection.send(new Message(MessageType.USER_ADDED, pair.getKey()));
                } catch (IOException e) {
                    ConsoleHelper.writeMessage("There is an error while sending messages");
                }
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
        while (true) {
            Message message = connection.receive();
            if (message.getType()==MessageType.TEXT) {
                String s = userName + ": " + message.getData();

                Message formattedMessage = new Message(MessageType.TEXT, s);
                sendBroadcastMessage(formattedMessage);
            } else {
                ConsoleHelper.writeMessage("Error");

            }
            }
        }

        @Override
        public void run() {
            super.run();
            ConsoleHelper.writeMessage("Установленно соединение с адресом " + socket.getRemoteSocketAddress());
            String userName = null;
            try(Connection connection = new Connection(socket)) {
                ConsoleHelper.writeMessage("Подключение к порту: " + connection.getRemoteSocketAddress());
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
                e.printStackTrace();
                }
            if (userName !=null) {
                //После того как все исключения обработаны, удаляем запись из connectionMap
                connectionMap.remove(userName);
                //и отправлялем сообщение остальным пользователям
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");



        }
    }
}
