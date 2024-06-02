package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
 */
@Slf4j
class MemberServiceV3_2Test {
	public static final String MEMBER_A = "memberA";
	public static final String MEMBER_B = "memberB";
	public static final String MEMBER_EX = "ex";

	private MemberRepositoryV3 memberRepository;
	private MemberServiceV3_2 memberService;


	@BeforeEach // 각각의 테스트가 수행되기 직전에 호출
	void beforeEach() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
		memberRepository = new MemberRepositoryV3(dataSource);

		// DataSource를 직접 주입하지 않고 추상화된 transactionManager를 주입한다.
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		memberService = new MemberServiceV3_2(transactionManager, memberRepository);
	}

	@AfterEach // 각각의 테스트가 수행된 직후에 호출
	void afterEach() throws SQLException {
		List<String> memberList = List.of(MEMBER_A, MEMBER_B, MEMBER_EX);
		for (String memberId : memberList) {
			memberRepository.delete(memberId);
		}
	}

	@Test
	@DisplayName("정상 이체")
	void accountTransfer() throws SQLException {
		// given
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberB = new Member(MEMBER_B, 10000);

		memberRepository.save(memberA);
		memberRepository.save(memberB);

		// when
		log.info("start TX");
		memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
		log.info("end TX");

		// then
		Member findMemberA = memberRepository.findById(memberA.getMemberId());
		Member findMemberB = memberRepository.findById(memberB.getMemberId());
		assertThat(findMemberA.getMoney()).isEqualTo(8000);
		assertThat(findMemberB.getMoney()).isEqualTo(12000);
	}

	@Test
	@DisplayName("이제 중 예외 발생")
	void accountTransferEx() throws SQLException {
		// given
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberEx = new Member(MEMBER_EX, 10000);

		memberRepository.save(memberA);
		memberRepository.save(memberEx);

		// when & then
		assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
			.isInstanceOf(IllegalStateException.class);

		// then
		Member findMemberA = memberRepository.findById(memberA.getMemberId());
		Member findMemberB = memberRepository.findById(memberEx.getMemberId());
		assertThat(findMemberA.getMoney()).isEqualTo(10000);
		assertThat(findMemberB.getMoney()).isEqualTo(10000);
	}
}