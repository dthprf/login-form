package login.Data;

import login.Model.User;

public interface UserDAO {
    User getUserData(String login, Integer password);
    User getUserByLogin(String login);
}
