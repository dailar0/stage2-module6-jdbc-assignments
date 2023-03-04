package jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@Setter
public class CustomDataSource implements DataSource {
    private static volatile CustomDataSource instance = new CustomDataSource();
    private final String driver;
    private final String url;
    private final String name;
    private final String password;
    private final CustomConnector connector = new CustomConnector();

    @SneakyThrows
    private CustomDataSource() {
        Properties properties;

        try (InputStream resource = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            properties = new Properties();
            properties.load(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        url = properties.getProperty("postgres.url");
        name = properties.getProperty("postgres.name");
        password = properties.getProperty("postgres.password");
        driver = properties.getProperty("postgres.driver");
        //init();
    }

    private void init() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("drop database if exists myfirstdb; create database myfirstdb; " +
                    "CREATE TABLE if not exists myusers (id bigint unique GENERATED ALWAYS AS IDENTITY, firstname text,lastname text,age smallint);");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static CustomDataSource getInstance() {
        return instance;
    }


    @Override
    public Connection getConnection() throws SQLException {
        return connector.getConnection(url,name,password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return connector.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {

    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(getClass())) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(getClass());
    }
}
