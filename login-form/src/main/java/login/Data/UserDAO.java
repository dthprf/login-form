package login.Data;

import login.Model.User;

public interface UserDAO {
    User getUserData(Integer password, String login);
}
