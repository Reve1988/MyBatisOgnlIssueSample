package kr.revelope.mybatistest;

import org.apache.commons.lang3.StringUtils;

public class TestUtils {
	public static boolean containsAny(final String origin, final Object... oth) {
		String[] str = new String[oth.length];
		for (int i = 0; i < oth.length; i++) {
			str[i] = (String)oth[i];
		}

		return StringUtils.containsAny(origin, str);
	}
}
