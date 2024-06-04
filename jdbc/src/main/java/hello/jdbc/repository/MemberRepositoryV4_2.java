package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.exception.MyDbException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * 스프링 예외 추상화 적용
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

	private final DataSource dataSource;
	private final SQLExceptionTranslator exceptionTranslator; // 인터페이스로 주입받을 것(DIP)

	public MemberRepositoryV4_2(DataSource dataSource) {
		this.dataSource = dataSource;

		// 데이터베이스의 Error code를 보고 적절한 스프링 예외로 변환하기 위해선 DB 정보가 필요하다, 따라서 dataSource를 넘겨준다.
		this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
	}

	@Override // 오버라이딩한 메서드이ㅡ 문법(파라미터 타입, 개수) 등을 준수했는지 컴파일러가 검사
	public Member save(Member member) {
		String sql = "insert into member(member_id, money) values (?, ?)";

		Connection conn = null;
		PreparedStatement pstmt = null; // DB에 쿼리를 날리기 위함

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, member.getMemberId());
			pstmt.setInt(2, member.getMoney());
			pstmt.executeUpdate();
			return member;
		} catch (SQLException sqlException) {
			throw exceptionTranslator.translate("save", sql, sqlException);
		} finally {
			close(conn, pstmt, null);
		}
	}

	@Override
	public Member findById(String memberId) {
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
		} catch (SQLException sqlException) {
			throw exceptionTranslator.translate("findById", sql, sqlException);
		} finally {
			close(conn, pstmt, rs);
		}
	}

	@Override
	public void update(String memberId, int money) {
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
		} catch (SQLException sqlException) {
			throw exceptionTranslator.translate("update", sql, sqlException);
		} finally {
			close(conn, pstmt, null);
		}
	}

	@Override
	public void delete(String memberId) {
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
		} catch (SQLException sqlException) {
			throw exceptionTranslator.translate("delete", sql, sqlException);
		} finally {
			close(conn, pstmt, null);
		}
	}

	private void close(Connection conn, Statement stmt, ResultSet rs) {
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(stmt);

		// 트랜잭션 동기화를 위해서 DataSourceUtils를 사용해서 트랜잭션을 종료한다.
		DataSourceUtils.releaseConnection(conn, dataSource);
	}

	private Connection getConnection() {
		// 스프링이 제공하는 DataSourceUtils를 사용해서 트랜잭션 동기화 기능을 사용한다.
		Connection conn = DataSourceUtils.getConnection(dataSource);
		log.info("get conn={}, class={}", conn, conn.getClass());
		return conn;
	}
}
