package hello.jdbc.exception.translator;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringExceptionTranslatorTest {
	DataSource dataSource;

	@BeforeEach
	void init() {
		dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
	}

	@Test
	void sqlExceptionErrorCode(){
		String sql = "select bad grammer";

		try {
			Connection connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeQuery();
		} catch (SQLException e) {
			assertThat(e.getErrorCode()).isEqualTo(42122); // h2 db일 경우 sql 문법 오류 코드는 42122이다.
			int errorCode = e.getErrorCode();
			log.info("errorCode={}", errorCode);
			log.info("error", e);
		}
	}

	@Test
	void exceptionTranslator() {
		String sql = "select bad grammer";

		try {
			Connection connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeQuery();
		} catch (SQLException sqlException){
			assertThat(sqlException.getErrorCode()).isEqualTo(42122); // h2 db일 경우 sql 문법 오류 코드는 42122이다.

			// 스프링이 제공하는 예외 변환기 사용
			SQLErrorCodeSQLExceptionTranslator exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);

			// SQLErrorCodeSQLExceptionTranslator가 예외가 발생한 SQL 문과 발생한 예외를 분석하여 적절한 예외로 변환해서 반환해준다.
			DataAccessException resultException = exceptionTranslator.translate("작업명", sql, sqlException);
			log.info("resultException", resultException);
			assertThat(resultException.getClass()).isEqualTo(BadSqlGrammarException.class);
		}
	}
}
