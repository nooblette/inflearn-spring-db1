package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV0Test {
	MemberRepositoryV0 repository = new MemberRepositoryV0();

	@Test
	void crud() throws SQLException {
		// save
		Member member = new Member("memberV100", 10000);
		repository.save(member);

		// findById
		Member findMember = repository.findById(member.getMemberId());
		log.info("find member : {}", findMember);

		// 검증을 매번 눈으로 다 할 수 없으니 테스트 코드를 작성하자.
		// member 객체와 findMember 객체는 다른 인스턴스이므로 서로 참조값이 다르다. (member == findMember : false)
		// 하지만 @Data 애노테이션이 생성하는 equals()에 의해 equals()는 true를 반환한다.(객체의 값 만으로 비교한다) (member.equals(findMember) : true)
		assertThat(findMember).isEqualTo(member);

		// update(money : 10000 -> 20000)
		repository.update(member.getMemberId(), 20000);
		Member updatedMember = repository.findById(member.getMemberId());
		assertThat(updatedMember.getMoney()).isEqualTo(20000);

		// delete - 테스트 후 데이터를 제거했으므로 이제 동일한 pk로 반복하여 테스트를 실행할 수 있다(동일한 테스트 코드는 반복 수행해도 항상 동일한 결과를 낳아야한다)
		repository.delete(member.getMemberId());
		// 조회가 되는지 여부를 확인해서 delete() 메서드를 검증한다(NoSuchElementException exception이 발생해야한다(조회가 되면 안된다)).
		assertThatThrownBy(() -> repository.findById(member.getMemberId()))
			.isInstanceOf(NoSuchElementException.class);
	}
}