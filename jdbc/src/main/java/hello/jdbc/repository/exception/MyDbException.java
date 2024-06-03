package hello.jdbc.repository.exception;

// MyDbException 예외는 RuntimeException 예외를 상속받으므로 언체크 예외가 된다.
public class MyDbException extends RuntimeException {
	public MyDbException() {
	}

	public MyDbException(String message) {
		super(message);
	}

	public MyDbException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyDbException(Throwable cause) {
		super(cause);
	}
}
