package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


//Контроллер отвечает за отправку сообщений клиентам

public class Controller implements Initializable {   // Для того что бы инизиализировать все соединение нужно шмплементировать Initializable
    @FXML
    public TextArea textArea;  // создаем поле текстАрия
    @FXML
    public TextField textField; // создаем поле ТекстФилд

    private final String IP_ADDRESS = "localhost"; //создаем IP
    private final int PORT = 8189; // создаем порт
    @FXML
    public HBox authPanel; //создаем верхнюю панель
    @FXML
    public TextField loginField; //создаем поле для ввода логина
    @FXML
    public PasswordField passwordField; // создаем поле для ввода порола
    @FXML
    public HBox msgPanel; // создаем нижнюю панель
    @FXML
    public ListView<String> clientList; // создаем окошко с пользователями в чате

    private Socket socket; // создаем сокет
    DataInputStream in; // входной поток
    DataOutputStream out; // выходной поток

    private boolean authenticated; // булева переменная отвечающая за аунтификацию
    private String nickname;
    private final String TITLE = "ГикЧат";

    /**
     * поле для закрытия на кнопку крестик
     */
    private Stage stage;

    /**
     * для регистрации по кнопке регистрация
     */
    private Stage regStage;

    private RegController regController;



    /**
     * метод для переключения окошек из верхнего в нижнее после прохождения аунтификации
     *
     * @param authenticated
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated); // если мы не прошли аунтификацию панель на месте
        authPanel.setManaged(!authenticated); // если мы не прошли аунтификацию панель еще и место занимает
        msgPanel.setVisible(authenticated); // если мы прошли аунтификацию панель видня нижняя панель
        msgPanel.setManaged(authenticated); // если мы прошли аунтификацию нижняя панель  занимает место
        clientList.setVisible(authenticated); // если прошли аунтификацию то прявляется список с пользпвелями в чате
        clientList.setManaged(authenticated); // если мы прошли аунтификацию  панель  занимает место

        if (!authenticated) { // если аунтификация не прошла то имя ровно null
            nickname = "";
        }
        textArea.clear();
        setTitle(nickname);

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) { //отвечает за инициализацию соединения после того как отрисовываются графичиские
        setAuthenticated(false);
        createRegWindow();
        connection();
        //Platform.runLater  это обработка графики
        Platform.runLater (()-> {
            stage = (Stage) textField.getScene().getWindow();
            //метод как раз для закрытия по крестикук ниже
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("byu");
                    try {
                        out.writeUTF("/end"); //для того что бы не только закрыть по крестику но и вообще разорвать соединение
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

    }

    /**
     * создаем новое соединение
     */
    private void connection() {
        try {
            socket = new Socket(IP_ADDRESS, PORT); // создаем соединение в которые подаем IP и PORT
            in = new DataInputStream(socket.getInputStream()); // входной поток мы подаем в сокет
            out = new DataOutputStream(socket.getOutputStream()); // выходной поток мы подаем в сокет



            //Делаем программу которая будет выполнять роль Путти (подключения к серверу)
            // делаем поток что бы не блочился графический интерфейс
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // цикл аунтификации для того что бы если пользователь ошибся то можно бы было еще раз зайти
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/authok")) {
                                nickname = str.split("\\s", 2)[1];
                                setAuthenticated(true); // если прошли аунтификацию то меняем картинки
                                break;
                            }

                            if(str.startsWith("/regok")){
                                regController.addMsgToTextArea("Регистрация прошла успешно");
                            }
                            if(str.startsWith("/regno")){
                                regController.addMsgToTextArea("Регистрация не получилась \n возможно логин или пароль заняты");
                            }
                            textArea.appendText(str + "\n"); // выводим инормацию что подключились
                        }


                        //цикл работы
                        while (true) {
                            String str = in.readUTF(); // мы хотим в бесконечном цикле получать данные из входного потока

                            //??????????????
                            if(str.startsWith("/")) {
                                // если клиент ввел end  отключаеся
                                if (str.equals("end")) {
                                    System.out.println("Клиент отключился");
                                    break;
                                }
                                //  если получили список клиентов то выводим их на экран
                                if (str.startsWith("/clientList")) { //?????????
                                    String[] token = str.split("\\s+");
                                    Platform.runLater(() -> { // обработка графики
                                        clientList.getItems().clear(); //чистим поле каждый раз когда кого то добавляем или удалаяем
                                        for (int i = 1; i < token.length; i++) {
                                            clientList.getItems().add(token[i]);
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(str + "\n"); // передом  это же сообщение в текстАрию
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Мы отключились от сервера");
                        setAuthenticated(false); // при отключении меняем картинки на старые
                        try {
                            socket.close(); // закрываем соединения
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start(); // стартуем поток
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создаем метод отвечающий за отправку сообщений при нажатии на кнопку
     * @param actionEvent
     */
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText()); // отправим на сервер то что мы ввели
            textField.clear(); //чистка текста
            textField.requestFocus(); // вернуть фокусировку
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод который будет срабатывать при вводе пароля и логина в loginField и passwordField
     *
     * @param actionEvent
     */
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connection(); // если сокета нету, то мы его создаем
        }
        try {
            out.writeUTF(String.format("/auth %s %s", loginField.getText().trim().toLowerCase(),  // задает формат текста который передает сервер, если логин введен с пробелами или введен капс лок, то он приводит приводит к одному формату
                    passwordField.getText().trim())); //убираются пробелы
            passwordField.clear(); //зачищаем окно пароля
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод для того что бы было видно кто управляет клиентом
     *
     * @param nick
     */
    private void setTitle(String nick) {   //??????
        Platform.runLater(() -> {
            ((Stage) textField.getScene().getWindow()).setTitle(TITLE + " " + nick); // утоновливает титульник и ник вверху чата
        });
    }

    /**
     * метод отвечающий за нажатие на ник в окошке чата и сразу написать ему
     * @param mouseEvent
     */
    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem(); //полкчаем клиента по нажатию
        textField.setText("/w " + receiver + " "); //делаем заготовку для быстрого написания сообщения
    }

    /**
     * метод для создания окна регистрации
     */
    private void createRegWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Reg window");
            regStage.setScene(new Scene(root, 400, 250));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод который как раз занимается регистрацией клиентов
     * @param login
     * @param password
     * @param nickname
     */
    public void tryToReg(String login, String password, String nickname){
        String msg = String.format("/reg %s %s %s", login, password, nickname);

        if (socket == null || socket.isClosed()) {  // если сокета нету, то мы его создаем
            connection();
        }

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод отвечающий за клик по кнопке регистации
     * @param actionEvent
     */
    public void registration(ActionEvent actionEvent) {
        regStage.show(); // говорим ему что бы показал окно регистрации
    }
}

