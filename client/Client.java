package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName+" connected to the chat");
        }
        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName+" disconnected from the chat");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
Client.this.notify();
            }
        }
       protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
    }
    protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType()==MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType()==MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());

                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address:");
        return ConsoleHelper.readString();
    }
    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port");
        return ConsoleHelper.readInt();
    }
    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter user name");
        return ConsoleHelper.readString();
    }
    protected  boolean shouldSendTextFromConsole() {
        return true;
    }
    protected SocketThread getSocketThread() {
        return new SocketThread();
    }
    protected void sendTextMessage(String text) {
        try {
            Message message = new Message(MessageType.TEXT,text);
            this.connection.send(message);
        } catch (IOException e) {
            clientConnected = false;
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
if (clientConnected == true) {
    ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду ‘exit");
} else {
    ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
}
while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (message.equals("exit")) break;
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
}


    }

}

