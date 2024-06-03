package hello.jdbc.exception.basic;

import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnCheckedAppTest {
	@Test
	void unchecked(){
		Controller controller = new Controller();
		Assertions.assertThatThrownBy(() -> controller.request())
			.isInstanceOf(RuntimeSQLException.class);
	}

	static class Controller {
		Service service = new Service();


		public void request() {
			service.logic();
		}
	}

	static class Service {
		Repository repository = new Repository();
		NetworkClient networkClient = new NetworkClient();

		public void logic() {
			repository.call();
			networkClient.call();
		}
	}

	static class NetworkClient {
		public void call() {
			throw new RuntimeConnectException("연결 실패");
		}
	}

	static class Repository {
		public void call() {
			try {
				runSQL();
			} catch (SQLException e) {
				// 체크 예외를 런타임 예외로 변환해서 던진다.
				// 이때 기존에 예외 인스턴스(e)를 그대로 던져준다 (stacktrace를 확인하기 위함)
				log.info("message: {}", e.getMessage(), e);
				throw new RuntimeSQLException(e);
			}
		}

		public void runSQL() throws SQLException {
			throw new SQLException("ex");
		}
	}

	static class RuntimeConnectException extends RuntimeException {
		public RuntimeConnectException(String message) {
			super(message);
		}
	}

	static class RuntimeSQLException extends RuntimeException {
		// Throwable error : 이전에 발생한 예외 정보(스택 트레이스)를 그대로 가져온다.
		public RuntimeSQLException(Throwable error) {
			super(error);
		}
	}
}
