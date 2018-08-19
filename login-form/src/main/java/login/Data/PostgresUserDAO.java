package login.Data;

import login.Model.User;
import java.sql.*;

public class PostgresUserDAO implements UserDAO {

    private static final String GET_USER_BY_LOGIN =
            "SELECT login, password\n" +
                    "FROM userlogins\n" +
                    "WHERE login = ?;";

    private static final String GET_USER_BY_LOGIN_AND_PASSWORD =
            "SELECT login, password\n" +
                    "FROM userlogins\n" +
                    "WHERE login = ? AND password = ?;";

    public User getUserByLogin(String login) {

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_BY_LOGIN);

            preparedStatement.setString(1, login);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                User user = extractUserFromRow(resultSet);
                connection.close();
                preparedStatement.close();
                return user;

            } else {
                return null;
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User getUserData(String login, Integer password) {

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_BY_LOGIN_AND_PASSWORD);

            preparedStatement.setString(1, login);
            preparedStatement.setInt(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                User user = extractUserFromRow(resultSet);
                connection.close();
                preparedStatement.close();
                return user;

            } else {
                return null;
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private User extractUserFromRow(ResultSet resultSet) throws SQLException{
        String login = resultSet.getString("login");
        Integer password = resultSet.getInt("password");

        User user = new User();
        user.setLogin(login);
        user.setPassword(password);

        return user;
    }
}
