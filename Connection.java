package com.javarush.task.task30.task3008;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket;
    private final  ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    public void send(Message message) throws IOException {
synchronized (out) {
    this.out.writeObject(message);
}
    }
public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
           return (Message) this.in.readObject();
        }
}
public SocketAddress getRemoteSocketAddress() {
        return this.socket.getRemoteSocketAddress();
}
public void close() throws IOException {
       this.in.close();
        this.out.close();
        this.socket.close();
}
}
