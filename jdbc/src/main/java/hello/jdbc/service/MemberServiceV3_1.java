package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
	private final PlatformTransactionManager transactionManager;
	private final MemberRepositoryV3 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) {
		// 비즈니스 로직에서 transactionManager로 커넥션을 얻고 트랜잭션을 시작한다.
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			// 비즈니스 로직
			bizLogic(fromId, toId, money);

			// 로직이 정상적으로 수행되었다면 커밋
			transactionManager.commit(status);
		} catch (Exception e) {

			// 예외가 발생한 경우 롤백을 한다.
			transactionManager.rollback(status);
			throw  new IllegalStateException(e); // 우선은 예외가 발생하면 예외를 던진다.
		} finally {
			// 트랜잭션이 커밋되거나 롤백되는 경우 트랜잭션 매니저 내부에셔 트랜잭션을 종료하고 커넥션을 정리하므로, 비즈니스 로직에서는 리소스 정리를 신경쓰지 않아도 된다.
			// release(conn);
		}
	}

	private void bizLogic(String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepository.findById(fromId);
		Member toMember = memberRepository.findById(toId);

		// 계좌이체 출금, 트랜잭션이 없으므로 사실상 autocommit 모드로 수행된다.(SQL이 실행되고 나서 자동으로 커밋되어 데이터베이스에 수정사항을 반영한다.)
		memberRepository.update(fromId, fromMember.getMoney() - money);

		// 입금
		validation(toMember);
		memberRepository.update(toId, toMember.getMoney() + money);
	}

	private void validation(Member toMember) {
		if(toMember.getMemberId().equals("ex")){
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}
}
