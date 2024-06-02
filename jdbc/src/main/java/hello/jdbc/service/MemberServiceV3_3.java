package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.annotation.Transactional;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@RequiredArgsConstructor
@Transactional // public 메서드가 호출될 때 트랜잭션을 시작한다.
public class MemberServiceV3_3 {
	private final MemberRepositoryV3 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		bizLogic(fromId, toId, money);
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
