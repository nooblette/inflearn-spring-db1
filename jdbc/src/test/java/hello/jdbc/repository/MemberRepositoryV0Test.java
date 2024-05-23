package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV0Test {
	MemberRepositoryV0 repository = new MemberRepositoryV0();

	@Test
	void crud() throws SQLException {
		Member member = new Member("memberV0", 10000);
		repository.save(member);

		Member findMember = repository.findById(member.getMemberId());
		log.info("find member : {}", findMember);

		// 검증을 매번 눈으로 다 할 수 없으니 테스트 코드를 작성하자.
		// member 객체와 findMember 객체는 다른 인스턴스이므로 서로 참조값이 다르다. (member == findMember : false)
		// 하지만 @Data 애노테이션이 생성하는 equals()에 의해 equals()는 true를 반환한다.(객체의 값 만으로 비교한다) (member.equals(findMember) : true)

		assertThat(findMember).isEqualTo(member);
	}
}