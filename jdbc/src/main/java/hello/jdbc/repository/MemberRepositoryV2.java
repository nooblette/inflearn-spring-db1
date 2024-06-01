package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.jdbc.support.JdbcUtils;

import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * JDBC- DB 커넥션을 파라미터로 전달받는다.
 */
@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryV2 {

	private final DataSource dataSource;

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

	public Member findById(Connection conn, String memberId) throws SQLException {
		String sql = "select * from member where member_id = ?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// conn = getConnection(); // getConnection()이 아닌 파라미터로 넘어온 connection을 사용해야한다.(getConnection을 호출하면 새로운 커넥션을 생성하여 다른 트랜잭션으로 잡히게 된다.)
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
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(pstmt);
			// 커넥션은 서비스 계층에서 열어서 리포지토리 계층으로 파라미터로 전달한다. 따라서 서비스 계층에서 커넥션을 닫아야지 리포지토리 계층에서 닫아버리면 안된다.
			// 리포지토리에서 커넥션을 닫아버리면 서비스 레이어의 비즈니스 로직을 동일한 트랜잭션에서 수행할 수 없다.
			// JdbcUtils.closeConnection(conn); // 리포지토리에서 커넥션을 닫으면 안된다. (주의)
		}
	}

	public void update(String memberId, int money) throws SQLException {
		String sql = "update member set money = ? where member_id = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, money); // PreparedStatement에 파라미터 세팅
			pstmt.setString(2, memberId);

			// executeUpdate() : 영향받은 row의 수를 반환
			int resultSize = pstmt.executeUpdate();
			log.info("reulstSize={}", resultSize);
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			// 선언 순서 : connection -> statement -> resultSet
			// 리소스 해제 순서 : resultSet -> statement -> connection
			close(conn, pstmt, null);
		}
	}

	public void update(Connection conn, String memberId, int money) throws SQLException {
		String sql = "update member set money = ? where member_id = ?";

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, money); // PreparedStatement에 파라미터 세팅
			pstmt.setString(2, memberId);

			// executeUpdate() : 영향받은 row의 수를 반환
			int resultSize = pstmt.executeUpdate();
			log.info("reulstSize={}", resultSize);
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			JdbcUtils.closeStatement(pstmt);
			// JdbcUtils.closeConnection(conn); // findById(conn, memberId) 메서드와 동일하게 커넥션을 리포지토리에서 닫지 않는다.
		}
	}


	public void delete(String memberId) throws SQLException {
		String sql = "delete from member where member_id = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memberId); // PreparedStatement에 파라미터 세팅

			// executeUpdate() : 영향받은 row의 수를 반환
			int resultSize = pstmt.executeUpdate();
			log.info("reulstSize={}", resultSize);
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			// 선언 순서 : connection -> statement -> resultSet
			// 리소스 해제 순서 : resultSet -> statement -> connection
			close(conn, pstmt, null);
		}
	}

	private void close(Connection conn, Statement stmt, ResultSet rs) {
		// 개발자가 직접 작성할때 보다 더 안정적으로 null, 예외 처리 등이 구현되어 있다. (코드도 훨씬 간결해진다)
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(stmt);
		JdbcUtils.closeConnection(conn);
	}

	private Connection getConnection() throws SQLException {
		Connection conn = dataSource.getConnection();
		log.info("get conn={}, class={}", conn, conn.getClass());
		return conn;
	}
}
