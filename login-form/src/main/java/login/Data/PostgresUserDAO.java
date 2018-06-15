package login.Data;

import login.Model.User;

import javax.jws.soap.SOAPBinding;
import java.sql.*;

public class PostgresUserDAO implements UserDAO {

    public User getUserData(Integer password, String login) {
        User user = null;

        String query = "SELECT id, login FROM userlogins WHERE login = ? AND password = ?;";

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

    public User getById(Integer id) {
        User user = new User();

        String query = "SELECT id, login FROM users WHERE id =" + id;


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
