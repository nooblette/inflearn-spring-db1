package hello.jdbc.service;

import java.sql.SQLException;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

// 계좌이체와 같은 비즈니스 로직이 필요하므로 서비스 레이어를 구현한다.
@RequiredArgsConstructor
public class MemberServiceV1 {
	private final MemberRepositoryV1 memberRepository;

	// 실제로는 Checked Exception(e.g. SQLException)을 던지는게 아니라 적절히 처리해주어야한다.
	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		Member fromMember = memberRepository.findById(fromId);
		Member toMember = memberRepository.findById(toId);

		// 계좌이체 출금, 트랜잭션이 없으므로 사실상 autocommit 모드로 수행된다.(SQL이 실행되고 나서 자동으로 커밋되어 데이터베이스에 수정사항을 반영한다.)
		memberRepository.update(fromId, fromMember.getMoney() - money);

		// 입금
		validation(toMember);
		memberRepository.update(toId, toMember.getMoney() + money);
	}

	private static void validation(Member toMember) {
		if(toMember.getMemberId().equals("ex")){
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}
}
