package hello.jdbc.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Member {
	private String memberId;
	private int money;

	public Member() {
		log.info("create member");
	}

	public Member(String memberId, int money) {
		log.info("create member: memberId={}, money={}", memberId, money);
		this.memberId = memberId;
		this.money = money;
	}
}
