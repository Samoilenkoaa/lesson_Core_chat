package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private ExecutorService service;

    private static int PORT = 8189; // создаем порт
    ServerSocket server = null; // создаем сервер
    Socket socket = null; // создаем соект которй выделяет сервер
    List<ClientHandler> clients; // создаем лист для того что бы всем отправлять сообщения
    private AuthService authService;
    Logger logger;

    public Server() throws IOException {
        clients = new Vector<>(); // создаем лист в котором будет хранить клиентов
        service = Executors.newCachedThreadPool();
        logger = Logger.getLogger(getClass().getName());
        Handler fileHandler = new FileHandler("logFile.log", true);
        logger.addHandler(fileHandler);
        try {
            authService = new SimpleAuthService();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            server = new ServerSocket(PORT); // получаем сервер сокет которому передаем порт для подключения
            System.out.println("Сервер запущен"); // выводим что сервер запущен
            logger.log(Level.INFO, "Сервер запущен");

            while (true) {
                socket = server.accept(); // начинаем ждать что бы кто нить подключился
                System.out.println("Клиент подключился"); // выводим в консоль что клиент подключился
                logger.log(Level.INFO, "Клиент подключился");
                new ClientHandler(this, socket, service); // создаем новых клиентов. передаем ему сервер и сокет
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.INFO, "Произошла ошибка");
        } finally {
            try {
                service.shutdown();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * метод для отправки сообений всем клиентам
     *
     * @param msg
     */
    void broadCastMsg(ClientHandler sender, String msg) {  // как добавить время во сколько было отправлено?
        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss"); // добавляем время во сколько было отправлено сообщение в чате
        String message = String.format("%s %s: %s", formater.format(new Date()), sender.getNickname(), msg); //ш будем видеть какой клиен отправляет сообщения
        if (msg.startsWith("/w")) {
            for (ClientHandler client : clients) { // проходимся по всему списку клиентов
                if (client.getNickname().equals(sender.getNickname()) || client.getNickname().equals(msg.split(" ")[1])) {
                    // 1) если никнейм из массива equals 2 элименту при разбиве строки на массив
                    // 2) никнейм из массива клиентов сравниваем с ником отправителя
                    message = String.format("%s : %s", sender.getNickname(), msg.split(" ",3)[2]);
                    client.sendMsg(message + "\n");
                }
            }
        } else {
            for (ClientHandler client : clients) {
                client.sendMsg(message + "\n");
            }

        }
    }


    /**
     * Производит запись нового клиента в наш лист с клиентами
     *
     * @param clientHandler
     */
    public void subscribe(ClientHandler clientHandler) {

        clients.add(clientHandler);
        broadClientList();
    }

    /**
     * Метод после выхода одного из клиентов он его будет убирать из нашего листа что бы не ьыло ошибок
     *
     * @param clientHandler
     */
    public void unsubscribe(ClientHandler clientHandler) {

        clients.remove(clientHandler);
        broadClientList();
    }


    /**
     * метод в котором будем получать доступ пользователя
     *
     * @return
     */
    public AuthService getAuthService() {

        return authService;
    }

    /**
     * меод для того что бы проверить существует ли уже такой логин или нет, что бы нельзя было 2 раза зайти под одним логином
     */
    public boolean isLoginAuthenticated(String login){
        for (ClientHandler c: clients) {
            if(c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    /**
     * метод который будет посылать список клиентов в окно с клиентами в чате
     */
    public void broadClientList() {
        StringBuilder sb = new StringBuilder("/clientList ");
        for(ClientHandler c: clients){
            sb.append(c.getNickname()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler c: clients){
            c.sendMsg(msg);
        }
    }

}

