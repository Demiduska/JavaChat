package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    public class BotSocketThread extends SocketThread {
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            String senderName = "";
            String senderMessageText;
            if (message.contains(": ")) {
                senderName = message.substring(0, message.indexOf(": "));
                senderMessageText = message.substring(message.indexOf(": ") + 2);
            } else {
                senderMessageText = message;
            }
            SimpleDateFormat format = null;
            if ("дата".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("d.MM.YYYY");
            } else if ("день".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("d");
            } else if ("месяц".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("MMMM");
            } else if ("год".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("YYYY");
            } else if ("время".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("H:mm:ss");
            } else if ("час".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("H");
            } else if ("минуты".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("m");
            } else if ("секунды".equalsIgnoreCase(senderMessageText)) {
                format = new SimpleDateFormat("s");
            }

            if (format != null) {
                sendTextMessage("Информация для " + senderName + ": " + format.format(Calendar.getInstance().getTime()));
            }

        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }
    protected boolean shouldSendTextFromConsole() {
        return false;
    }
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
