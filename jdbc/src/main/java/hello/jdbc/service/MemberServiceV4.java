package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.annotation.Transactional;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 * MemberRepository 인터페이스에 의존
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberServiceV4 {
	private final MemberRepository memberRepository;

	public void accountTransfer(String fromId, String toId, int money) {
		bizLogic(fromId, toId, money);
	}

	private void bizLogic(String fromId, String toId, int money) {
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
