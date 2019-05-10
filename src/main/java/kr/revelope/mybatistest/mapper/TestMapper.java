package kr.revelope.mybatistest.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMapper {
	String selectTest(@Param("arg") String arg);
}
