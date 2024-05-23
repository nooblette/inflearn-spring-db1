package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

/***
 * Member 객체를 JDBC를 통해 데이터베이스에 접근한다.
 * JDBC- DriverManager 사용(굉장한 로우 레벨부터 차근차근 개발한다)
 */
@Slf4j
public class MemberRepositoryV0 {
	public Member save(Member member) throws SQLException {
		String sql = "insert into member(member_id, money) values (?, ?)";

		Connection conn = null;
		PreparedStatement pstmt = null; // DB에 쿼리를 날리기 위함

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			// SQL의 파라미터 바인딩
			pstmt.setString(1, member.getMemberId());
			pstmt.setInt(2, member.getMoney());
			pstmt.executeUpdate();
			return member;
		} catch (SQLException e) {
			// SQLException는 Checked Exception이므로 예외 처리를 작성해줘야한다.(혹은 상위 메서드로 넘기거나)
			log.error("db error", e);
			throw e;
		} finally {
			// PreparedStatement와 DB Connection을 사용한 이후에는 선언한 순서의 역순으로 close 해주어야한다.
			// 실제 PreparedStatement와 Connection은 외부 리소스(TCP/IP Connection)을 사용하고 있기 때문에 계속 열어두면 장애로 이어질 수 있다.
			// 하지만 만약 close() 중에 Excpetion이 터지면? close() 자체가 호출되지 않고 connection은 리소스를 계속 점유하게 된다.
			// 결국 close()도 각각 try-catch로 묶어주어야 한다.
			// pstmt.close();
			// conn.close();
			close(conn, pstmt, null);
		}
	}

	public Member findById(String memberId) throws SQLException {
		String sql = "select * from member where member_id = ?";

		Connection conn = null; // finally 구문에서 conn 참조 변수에 접근해야하기 때문에, null로 초기화하여 선언한다.
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memberId); // PreparedStatement에 파라미터 세팅

			// executeUpdate() : 데이터를 넣거나 변경할때 호출, 영향받은 row의 수를 반환한다.
			// executeQuery() : 조화쿼리 호출시 사용, ResultSet을 반환한다(ResultSet = SELECT 쿼리의 결과를 담고있는 통)
			rs = pstmt.executeQuery();

			// ResultSet 첫 1회는 next() 호출
			if(rs.next()) {
				Member member = new Member();
				member.setMemberId(rs.getString("member_id"));
				member.setMoney(rs.getInt("money"));

				return member;
			} else {
				// 예외를 던질때는 에러와 로깅 메시지를 잘 작성하는 것이 중요하다. (모니터링하면서 어디서 문제가 생겼는지 간결하고 정화하고 빠르게 파악하기 위함)
				throw new NoSuchElementException("member not found member id=" + memberId);
			}
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			// 선언 순서 : connection -> statement -> resultSet
			// 리소스 해제 순서 : resultSet -> statement -> connection
			close(conn, pstmt, rs);
		}
	}

	private void close(Connection conn, Statement stmt, ResultSet rs) {
		// Statement : SQL을 그대로 실행
		// PreparedStatement와 : SQL에 파라미터를 바인딩하고 실행할 수 있다, Statement를 상속받는다.
		if(rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error("rs close error", e);
			}
		}

		if(stmt != null){
			try {
				stmt.close(); // SQLException이 발생하면?
			} catch (SQLException e) {
				// log만 찍고 끝이 난다, connection을 close() 하는데 영향을 주지 않는다.
				log.error("stmt close error", e);
			}
		}

		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("conn close error", e);
			}
		}

	}

	private Connection getConnection() {
		return DBConnectionUtil.getConnection();
	}
}
