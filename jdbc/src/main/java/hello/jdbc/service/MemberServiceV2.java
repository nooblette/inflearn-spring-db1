package hello.jdbc.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 계좌이체(비즈니스 로직)을 한 트랜잭션 내에서 수행 - 파라미터 연동, 커넥션 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {
	private final DataSource dataSource;
	private final MemberRepositoryV2 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		// 비즈니스 로직에서 커넥션을 얻고 트랜잭션을 시작한다.
		Connection conn = dataSource.getConnection();
		try {
			// 트랙잭션 시작, set autocommit false;와 동일한 동작
			conn.setAutoCommit(false);

			// 비즈니스 로직
			bizLogic(conn, fromId, toId, money);

			// 로직이 정상적으로 수행되었다면 커밋
			conn.commit(); // commit;와 동일한 동작
		} catch (Exception e) {
			// 예외가 발생한 경우 롤백을 한다.
			conn.rollback();
			throw  new IllegalStateException(e); // 우선은 예외가 발생하면 예외를 던진다.
		} finally {
			// 서비스 레이어에서 사용한 리소스를 정리(반납)한다
			release(conn);
		}
	}

	private void bizLogic(Connection conn, String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepository.findById(conn, fromId);
		Member toMember = memberRepository.findById(conn, toId);

		// 계좌이체 출금, 트랜잭션이 없으므로 사실상 autocommit 모드로 수행된다.(SQL이 실행되고 나서 자동으로 커밋되어 데이터베이스에 수정사항을 반영한다.)
		memberRepository.update(conn, fromId, fromMember.getMoney() - money);

		// 입금
		validation(toMember);
		memberRepository.update(conn, toId, toMember.getMoney() + money);
	}

	private void release(Connection conn) {
		if(conn != null) {
			try {
				// 커넥션 풀을 사용하는 경우, 계좌이체 로직에서 사용한 커넥션은 사용 이후 풀로 반납하고 이후 다른 로직에서 사용된다.
				// 이때, 바로 커넥션을 반납하면 autoCommit 모드가 false인채로 남아있고, 다른 곳에서도 false인 채로 사용하게 된다.
				// 하지만 autoCommit의 디폴트 값은 true이므로 false 인채로 남아있다면 다른 곳에서 의도치 않은 문제가 발생할 수 있으므로 true로 원복하고 커넥션을 반납한다.
				conn.setAutoCommit(true);
				conn.close();
			} catch (Exception e){
				log.info("error", e);
			}
		}
	}

	private void validation(Member toMember) {
		if(toMember.getMemberId().equals("ex")){
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}
}
