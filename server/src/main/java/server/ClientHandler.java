package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class ClientHandler {

    Server server = null;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;
    private  String nickname;
    private String login;


    public ClientHandler(Server server, Socket socket, ExecutorService service) {  // создаем конструктор который будет отвечать за подключение новых клиентов
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream()); // входной поток мы подаем в сокет
            out = new DataOutputStream(socket.getOutputStream()); // выходной поток мы подаем в сокет

//            socket.setSoTimeout(5000); //если через 5 сек ничего не сделает, то бросит ошибку



            service.execute(()  -> { // в этом потоке запускаем каждый раз работу с новым клиентом
                    try {
                        // цикл аунтификации для того что бы если пользователь ошибся то можно бы было еще раз зайти
                        while (true){
                            String str = in.readUTF();
                            server.logger.log(Level.INFO, "клиент прислал сообщение/команду");
                            if (str.startsWith("/auth")){
                                String[] token = str.split("\\s");
                                String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);
                                login = token[1];

                                if (newNick != null){
                                    if(!server.isLoginAuthenticated(token[1])) { //провереям существует ли такой логин при аунтификации
                                        nickname = newNick;
                                        sendMsg("/authok " + nickname);
                                        server.subscribe(this);
                                        System.out.println("Клиент " + nickname + " подключился");
                                        break;
                                    } else {
                                        sendMsg("С данной учетной записи уже зашли");
                                    }
                                }else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            }

                            if (str.startsWith("/reg")){                   // если регисьоация прошла успешно то ок, если нет то нет
                                String[] token = str.split("\\s");
                                if(token.length < 4){
                                    continue;
                                }
                                boolean isRegistration = server.getAuthService()
                                        .registration(token[1], token[2], token[3]);
                                if(isRegistration){
                                    sendMsg("/regok");
                                } else {
                                    sendMsg("/regno");
                                }
                            }
                        }
//                        socket.setSoTimeout(0); //что бы ошибку не бросало ставим 0
                        // цикл работы
                        while (true) {
                            String str = in.readUTF(); // мы хотим в бесконечном цикле получать данные из входного потока
                            if (str.equals("/end")) {
                                out.writeUTF("/end");

                                break;
                            }
                            server.broadCastMsg(this, str); // посылаем всем клиентам собщение
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Клиент отключился");
                        server.unsubscribe(this); // убираем отключившегося клиента
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод для отправки сообщений каждого пользователя на сервер
     * @param msg
     */
    void sendMsg(String msg) {
        try {
            out.writeUTF(msg); // отправим на сервер то что мы ввели
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getNickname(){

        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
