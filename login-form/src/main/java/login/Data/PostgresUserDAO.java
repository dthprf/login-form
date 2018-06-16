package login.Data;

import login.Model.User;

import java.sql.*;

public class PostgresUserDAO implements UserDAO {

    public User getUserData(Integer password, String login) {

        User user = null;

        String query = "SELECT id, login FROM userlogins WHERE login like '" + login + "' AND password=" + password + ";";

        try {
            Connection connection = DBConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                user = extractUser(resultSet);
            }


            statement.close();
            resultSet.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }


    public User getById(Integer id) {
        User user = new User();

        String query = "SELECT id, login FROM userlogins WHERE id =" + id;

        try {
            Connection connection = DBConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                user = extractUser(resultSet);
            }

            statement.close();
            resultSet.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }


    private User extractUser(ResultSet resultSet) throws SQLException {
        User user = new User();

        user.setUserId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));

        return user;
    }
}
