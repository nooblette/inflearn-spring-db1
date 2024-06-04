package hello.jdbc.exception.translator;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.exception.MyDbException;
import hello.jdbc.repository.exception.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExTranslatorV1Test {
	Repository repository;
	Service service;

	// 의존관계 조립
	@BeforeEach
	void init(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
		repository = new Repository(dataSource);
		service = new Service(repository);
	}

	@Test
	void duplicateKeySave(){
		// given
		service.create("myId");

		// when
		service.create("myId");

	}

	@RequiredArgsConstructor
	public class Service {
		private final Repository repository;

		public void create(String memberId) {
			try {
				Member save = repository.save(new Member(memberId, 0));
				log.info("saveId= {}", save.getMemberId());
			} catch (MyDuplicateKeyException e){
				log.info("키 중복 오류 발생 복구 시도");
				String retryId = generateNewId(memberId);
				log.info("retryId= {}", retryId);
				repository.save(new Member(retryId, 0));
			} catch (MyDbException e){
				log.info("데이터 접근 계층 예외");
				throw e;
			}
		}

		private String generateNewId(String memberId){
			return memberId + new Random().nextInt(10000);
		}
	}

	@RequiredArgsConstructor
	static class Repository {
		private final DataSource dataSource;
		public Member save(Member member){
			String sql = "insert into member(member_id, money) values (?, ?)";
			Connection conn = null;
			PreparedStatement pstmt = null;

			try {
				// DB Conection 획득
				conn = dataSource.getConnection();
				pstmt = conn.prepareStatement(sql);

				// 쿼리 매개변수 세팅
				pstmt.setString(1, member.getMemberId());
				pstmt.setDouble(2, member.getMoney());

				// 쿼리 실행
				pstmt.executeUpdate();
				return member;
			} catch (SQLException e){
				// h2 db의 duplicate key exception 발생시
				if(e.getErrorCode() == 23505) {
					throw new MyDuplicateKeyException(e);
				}

				throw new MyDbException(e);
			} finally {
				JdbcUtils.closeStatement(pstmt);
				JdbcUtils.closeConnection(conn);
			}
		}
	}
}
