package hello.jdbc.connection;

// 객체 생성이 필요없는 클래스(e.g. 상수 클래스)라면 추상(abstract) 클래스로 선언하여 객체 생성을 막아둔다.
public abstract class ConnectionConst {
	public static final String URL = "jdbc:h2:tcp://localhost/~/test";
	public static final String USERNAME = "sa";
	public static final String PASSWORD = "";
}