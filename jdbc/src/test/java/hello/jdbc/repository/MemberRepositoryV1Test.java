package hello.jdbc.repository;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.zaxxer.hikari.HikariDataSource;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV1Test {
	MemberRepositoryV1 repository;

	@BeforeEach // 각 테스트가 실행되기 직전에 한번씩 호출
	void beforeEach(){
		/**
		 * 기본 DriverManager(스프링이 제공하는 DriverManagerDataSource)를 통해 항상 새로운 커넥션을 획득
		 */
		// DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

		/**
		 * HikariCP 커넥션 풀을 사용한 Connection Pooling
		 */
		// DataSource 인터페이스에는 URL, USERNAME 등의 설정 메서드를 제공하지 않기때문에 HikariDataSource 타입으로 선언한다.
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(URL);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);

		// 의존관계 주입은 DataSource 인터페이스 타입으로 가능하다(HikariDataSource는 결국 DataSource 인터페이스의 자식 타입이며, 부모 타입은 자식 타입을 받을 수 있다)
		repository = new MemberRepositoryV1(dataSource);
	}
	@Test
	void crud() throws SQLException, InterruptedException {
		Member member = new Member("memberV100", 10000);
		repository.save(member);

		Member findMember = repository.findById(member.getMemberId());
		log.info("find member : {}", findMember);

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

		Thread.sleep(1000);
	}
}