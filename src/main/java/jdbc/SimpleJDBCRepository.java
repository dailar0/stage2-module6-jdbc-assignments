package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private DataSource dataSource = CustomDataSource.getInstance();

    private static final String createUserSQL = "insert into myusers (firstname,lastname,age) values(?,?,?)";
    private static final String updateUserSQL = "update myusers set firstname=?,lastname=?,age=? where id=?";
    private static final String deleteUser = "delete from myusers where id=?";
    private static final String findUserByIdSQL = "select * from myusers where id=?";
    private static final String findUserByNameSQL = "select * from myusers where firstname like ?";
    private static final String findAllUserSQL = "select * from myusers";

    public Long createUser(User user) {
        long id = 0;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(createUserSQL,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            id = resultSet.getLong(1);
        } catch (SQLException ignored) {
        }
        return id;
    }

    public User findUserById(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(findUserByIdSQL);
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return parseUser(resultSet);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public User findUserByName(String userName) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(findUserByNameSQL);
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return parseUser(resultSet);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public List<User> findAllUser() {
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(findAllUserSQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = parseUser(resultSet);
                users.add(user);
            }
        } catch (SQLException ignored) {
        }
        return users;
    }

    public User updateUser(User user) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(updateUserSQL);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.setLong(4, user.getId());

            preparedStatement.executeUpdate();

        } catch (SQLException ignored) {
        }
        return findUserById(user.getId());
    }

    public void deleteUser(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteUser);
            preparedStatement.setLong(1, userId);
            preparedStatement.execute();
        } catch (SQLException ignored) {
        }
    }


    private static User parseUser(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong(1);
        String firstName = resultSet.getString(2);
        String lastName = resultSet.getString(3);
        int age = resultSet.getInt(4);
        return new User(id, firstName, lastName, age);
    }
}
