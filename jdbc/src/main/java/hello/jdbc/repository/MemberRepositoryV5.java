package hello.jdbc.repository;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

/***
 * JdbcTemplate 적용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

	// 지금까지 했던 커넥션 획득, 커넥션 동기화, 반납, 스프링 예외로 변환 등 모든 과정을 JdbcTemplate이 해준다.
	private final JdbcTemplate jdbcTemplate;

	public MemberRepositoryV5(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override // 오버라이딩한 메서드의 문법(파라미터 타입, 개수) 등을 준수했는지 컴파일러가 검사
	public Member save(Member member) {
		String sql = "insert into member(member_id, money) values (?, ?)";
		jdbcTemplate.update(sql, member.getMemberId(), member.getMoney()); // 영향받은 row의 수를 반환한다.
		return member;
	}

	@Override
	public Member findById(String memberId) {
		String sql = "select * from member where member_id = ?";
		return jdbcTemplate.queryForObject(sql, memberRowMapper(), memberId);
	}

	private RowMapper<Member> memberRowMapper() {
		return (rs, rowNum) -> {
			Member member = new Member();
			member.setMemberId(rs.getString("member_id"));
			member.setMoney(rs.getInt("money"));

			return member;
		};
	}

	@Override
	public void update(String memberId, int money) {
		String sql = "update member set money = ? where member_id = ?";
		jdbcTemplate.update(sql, money, memberId); // 영향받은 row의 수를 반환한다.
	}

	@Override
	public void delete(String memberId) {
		String sql = "delete from member where member_id = ?";
		jdbcTemplate.update(sql, memberId);

	}
}
