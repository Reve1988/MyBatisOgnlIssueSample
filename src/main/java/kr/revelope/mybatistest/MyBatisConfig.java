package kr.revelope.mybatistest;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MyBatisConfig {
	@Bean
	public DataSource commonDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		// 이곳에 테스트 DB 정보 입력

		return dataSource;
	}

	@Bean
	public SqlSessionFactoryBean commonSqlSessionFactoryBean(DataSource commonDataSource) throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(commonDataSource);
		sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/kr/revelope/**/mapper/*.xml"));
		sessionFactory.setConfigLocation(new ClassPathResource("mybatisConfiguration.xml"));

		return sessionFactory;
	}

	@Bean
	public DataSourceTransactionManager commonTransactionManager(DataSource commonDataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(commonDataSource);
		transactionManager.setGlobalRollbackOnParticipationFailure(false);

		return transactionManager;
	}

	@Bean
	public MapperScannerConfigurer commonMapperScannerConfigurer() {
		MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
		mapperScannerConfigurer.setBasePackage("kr.revelope.**.mapper");
		mapperScannerConfigurer.setSqlSessionFactoryBeanName("commonSqlSessionFactoryBean");

		return mapperScannerConfigurer;
	}
}
