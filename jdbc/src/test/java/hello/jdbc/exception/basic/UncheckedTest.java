package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UncheckedTest {

	@Test
	void unchecked_catch() {
		Service service = new Service();
		service.callCatch();
	}

	@Test
	void unchecked_throw(){
		Service service = new Service();
		Assertions.assertThatThrownBy(() -> service.callThrow())
			.isInstanceOf(MyUncheckedException.class);
	}
	/**
	 * RuntimeException을 상속받는 예외는 Unchecked Exception이 된다.
	 */
	static class MyUncheckedException extends RuntimeException {
		public MyUncheckedException(String message) {
			super(message);
		}
	}

	/**
	 * Unchecked Exception는 예외를 잡거나 던지지 않아도 된다.
	 * 예외를 잡지 않으면 자동으로 밖으로 던진다.
	 */
	static class Service {
		Repository repository = new Repository();

		/**
		 * 필요한 경우 예외를 잡아서 처리하면 된다.
		 */
		public void callCatch(){
			try {
				repository.call();
			} catch (MyUncheckedException e) {
				// 예외 처리 로직
				log.info("예외 처리, message= {}", e.getMessage(), e);
			}
		}

		/**
		 * 예외를 명시적으로 잡지 않아도 된다.
		 * Checked Excetpion과 다르게 throws 예외 선언을 하지 않아도 된다.
		 */
		public void callThrow(){
			repository.call();
		}
	}

	static class Repository {
		public void call() {
			throw new MyUncheckedException("ex");
		}
	}
}
