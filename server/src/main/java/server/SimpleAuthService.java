package server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static server.SQutils.*;

// клас Простой Сервис Аутентификации
public class SimpleAuthService implements AuthService {

    /**
     * класс в котором есть логин, пароль и никнейм
     */
    private class UserData{
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) { // конструктор для класса выше
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    List<UserData> users; //список наших юзеров

    public SimpleAuthService() throws SQLException, ClassNotFoundException {  // метод для того что бы заполнить его юзерами
        users = new ArrayList<>(); // создадим объект юзер
        List<Map<String, String>> list =  SQutils.getAllUsers();
        for(Map<String, String> map : list) {
            String nick = map.get(NAME);
            String login = map.get(LOGIN);
            String password = map.get(PASSWORD);
            users.add(new UserData(login, password, nick));
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) { // имплеминтация интерфейса
        for (UserData user: users) {  // пробежимся по списку наших юзеров
            if (user.login.equals(login) && user.password.equals(password)){  // если есть совпадение по логину и паролю то вернем никнейм
                return user.nickname;
            }
        }
        return null; // если не нашли совпадение то возвращаеи null
    }

    /**
     * метод регистрации клиентов
     * @param login
     * @param password
     * @param nickname
     * @return
     */
    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData user : users){
            if(user.login.equals(login) || user.nickname.equals(nickname)){ //если есть такой логина, то выходим
                return false;
            }
        }
        users.add(new UserData(login, password, nickname)); //если нет такого логина, то добавляем
        SQutils.saveUser(nickname, login, password);
        return true;
    }
}

