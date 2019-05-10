package kr.revelope.mybatistest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import kr.revelope.mybatistest.mapper.TestMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyBatisConfig.class})
class TestMapperTest {
	@Autowired
	private TestMapper testMapper;

	@Test
	void selectTest() {
		testMapper.selectTest("AAA");
	}
}