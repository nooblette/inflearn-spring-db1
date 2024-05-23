package hello.jdbc.connection;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBConnectionUtil {
	public static Connection getConnection() {
		try {
			Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			log.info("get connection success\n connection={}\n class={}", connection, connection.getClass());

			return connection;
		} catch (SQLException e) {
			// checked exception을 Runtime exception으로 던진다.
			throw new RuntimeException(e);
		}
	}
}
