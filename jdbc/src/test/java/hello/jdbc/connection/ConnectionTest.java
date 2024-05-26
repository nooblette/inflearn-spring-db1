package hello.jdbc.connection;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionTest {

	@Test
	void driverManager() throws SQLException {
		// 실제 DB에 접근해서 서로 다른 2개의 connection 획득
		Connection conn1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		Connection conn2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);

		log.info("connection={}, class={}", conn1, conn1.getClass());
		log.info("connection={}, class={}", conn2, conn2.getClass());
	}

	@Test
	void dataSourceDriverManger() throws SQLException {
		// 스프링이 제공하는 DriverManagerDataSource 사용
		// DriverManagerDataSource도 내부적으로 DriverManager를 사용하므로 항상 DB 커넥션을 생성한다. (커넥션풀 사용 x)
		// 하지만 애플리케이션에서 DataSource 인터페이스에만 의존할 수 있다는 이점이 있다. (DIP, OCP)
		DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
		useDataSource(dataSource);
	}

	@Test
	void dataSourceConnectionPool() throws SQLException, InterruptedException {
		// Hikari를 사용한 Connection Pooling
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(URL);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);
		dataSource.setMaximumPoolSize(10); // 커넥션 풀 최대 개수 지정 (default = 10)
		dataSource.setPoolName("MyPool");

		useDataSource(dataSource);
		Thread.sleep(1000);
	}

	private void useDataSource(DataSource dataSource) throws SQLException {
		Connection conn1 = dataSource.getConnection();
		Connection conn2 = dataSource.getConnection();

		log.info("connection={}, class={}", conn1, conn1.getClass());
		log.info("connection={}, class={}", conn2, conn2.getClass());
	}
}
