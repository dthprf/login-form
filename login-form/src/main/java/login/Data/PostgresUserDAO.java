package login.Data;

import login.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresUserDAO implements UserDAO {

    public User getUserData(Integer password, String login) {
        User user = new User();

        String query = "SELECT id, login, password FROM users WHERE login = ? AND password = ?;";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, login);
            preparedStatement.setInt(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                user.setLogin(resultSet.getString("login"));
                user.setUserId(resultSet.getInt("id"));
                connection.close();
                preparedStatement.close();
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
}
