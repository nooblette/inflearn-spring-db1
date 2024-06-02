package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {
	private final TransactionTemplate transactionTemplate;
	private final MemberRepositoryV3 memberRepository;

	public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
		// transactionTemplate을 사용하려면 transactionManager가 필요하다.
		// transactionManager를 의존관계로 주입받고 생성자 내부에서 transactionTemplate을 생성하는 패턴을 많이 사용한다. (관례)
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.memberRepository = memberRepository;
	}

	public void accountTransfer(String fromId, String toId, int money) {
		// executeWithoutResult 메서드가 시작하면서 트랜잭션을 시작하고 비즈니스 로직을 수행한다, 비즈니스 로직을 모두 수행하면 트랜잭션을 종료한다.
		transactionTemplate.executeWithoutResult((transactionStatus) -> {
			try {
				bizLogic(fromId, toId, money);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	private void bizLogic(String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepository.findById(fromId);
		Member toMember = memberRepository.findById(toId);

		// 계좌이체 출금
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
