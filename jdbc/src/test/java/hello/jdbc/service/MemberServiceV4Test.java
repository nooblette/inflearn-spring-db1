package hello.jdbc.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 * MemberRepository 인터페이스에 의존
 */
@Slf4j
@SpringBootTest // 테스트를 수행할때 스프링을 띄워고 필요한 스프링 빈을 등록하고 의존관계를 주입한다.
class MemberServiceV4Test {
	public static final String MEMBER_A = "memberA";
	public static final String MEMBER_B = "memberB";
	public static final String MEMBER_EX = "ex";

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberServiceV4 memberService;

	/**
	 * 테스트 코드 중 트랜잭션 AOP를 적용하는데 필요한 객체들을 스프링 빈으로 등록한다.
	 */
	@TestConfiguration
	static class TestConfig{
		private final DataSource dataSource;

		// 스프링 컨테이너에 등록된 DataSource를 자동으로 주입해준다.
		public TestConfig(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		// 데이터 소스와 트랜잭션 매니저는 스프링 부트가 자동으로 주입해준다.
		@Bean
		MemberRepositoryV4_1 memberRepositoryV4_1(){
			return new MemberRepositoryV4_1(dataSource);
		}

		@Bean
		MemberServiceV4 memberServiceV4(){
			return new MemberServiceV4(memberRepositoryV4_1());
		}
	}

	@AfterEach // 각각의 테스트가 수행된 직후에 호출
	void afterEach() {
		List<String> memberList = List.of(MEMBER_A, MEMBER_B, MEMBER_EX);
		for (String memberId : memberList) {
			memberRepository.delete(memberId);
		}
	}

	@Test
	void AopCheck() {
		// AOP 트랜잭션이 적용되는지 검증
		log.info("memberService class={}", memberService.getClass());
		log.info("memberRepository={}", memberRepository.getClass());
		Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
		Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
	}

	@Test
	@DisplayName("정상 이체")
	void accountTransfer() {
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
	void accountTransferEx() {
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