# MyBatis 가변인자 이슈

증상 : test문에 가변인자를 받는 static method를 수행하면 missmatch 오류가 발생함

원인 : ognl버전이 변경되면서 가변인자를 지원, 가변인자에 무조건 Object를 넣는 현상이 발생하는것으로 추정

- MyBatis 3.2.8 > 3.3.0 으로 변경되면서 문제가 발생
- MyBatis changelog : https://github.com/mybatis/mybatis-3/compare/mybatis-3.3.0...master
- MyBatis ognl change : https://github.com/mybatis/mybatis-3/commit/b4e7cb16261d2b7c763ae4cb31938aee3e424527
- 문제가 되는 함수 : org.apache.ibatis.ognl.OgnlRuntime.callAppropriateMethod()

실행 가능한 TC : kr.revelope.mybatistest.TestMapperTest.selectTest()

---

pom.xml 

3.2.8
```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.2.8</version>
</dependency>
```

3.3.0
```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.3.0</version>
</dependency>
```

---

TestMapper.xml

3.2.8에서 가변인자 사용법 및 성공 케이스
```xml
<when test="@org.apache.commons.lang3.StringUtils@containsAny(arg, new String[]{'AAA', 'BBB'})">
```

3.3.0에서 가변인자 사용법 및 성공 케이스
```xml
<when test="@kr.revelope.mybatistest.TestUtils@containsAny(arg, 'AAA', 'BBB')">
```

3.3.0에서 가변인자 사용법 및 실패 케이스
```xml
<when test="@org.apache.commons.lang3.StringUtils@containsAny(arg, 'AAA', 'BBB')">
```

---

OgnlRuntime

3.2.8
```java
    public static Object callAppropriateMethod(OgnlContext context, Object source, Object target, String methodName, String propertyName, List methods, Object[] args) throws MethodFailedException {
        Throwable reason = null;
        Object[] actualArgs = objectArrayPool.create(args.length);

        Object var14;
        try {
            Method method = getAppropriateMethod(context, source, target, methodName, propertyName, methods, args, actualArgs);
            if (method == null || !isMethodAccessible(context, source, method, propertyName)) {
                StringBuffer buffer = new StringBuffer();
                if (args != null) {
                    int i = 0;

                    for(int ilast = args.length - 1; i <= ilast; ++i) {
                        Object arg = args[i];
                        buffer.append(arg == null ? NULL_STRING : arg.getClass().getName());
                        if (i < ilast) {
                            buffer.append(", ");
                        }
                    }
                }

                throw new NoSuchMethodException(methodName + "(" + buffer + ")");
            }

            var14 = invokeMethod(target, method, actualArgs);
        } catch (NoSuchMethodException var21) {
            reason = var21;
            throw new MethodFailedException(source, methodName, (Throwable)reason);
        } catch (IllegalAccessException var22) {
            reason = var22;
            throw new MethodFailedException(source, methodName, (Throwable)reason);
        } catch (InvocationTargetException var23) {
            reason = var23.getTargetException();
            throw new MethodFailedException(source, methodName, (Throwable)reason);
        } finally {
            objectArrayPool.recycle(actualArgs);
        }

        return var14;
    }
```

3.3.0
```java
    public static Object callAppropriateMethod(OgnlContext context, Object source, Object target, String methodName, String propertyName, List methods, Object[] args) throws MethodFailedException {
        Throwable reason = null;
        Object[] actualArgs = _objectArrayPool.create(args.length);

        try {
            Method method = getAppropriateMethod(context, source, target, propertyName, methods, args, actualArgs);
            int i;
            if (method == null || !isMethodAccessible(context, source, method, propertyName)) {
                StringBuffer buffer = new StringBuffer();
                String className = "";
                if (target != null) {
                    className = target.getClass().getName() + ".";
                }

                i = 0;

                for(int ilast = args.length - 1; i <= ilast; ++i) {
                    Object arg = args[i];
                    buffer.append(arg == null ? NULL_STRING : arg.getClass().getName());
                    if (i < ilast) {
                        buffer.append(", ");
                    }
                }

                throw new NoSuchMethodException(className + methodName + "(" + buffer + ")");
            }

            // ***********이 부분에 convert 과정이 추가되는것을 원인으로 추정*****************//
            Object[] convertedArgs = actualArgs;
            if (isJdk15() && method.isVarArgs()) {
                Class[] parmTypes = method.getParameterTypes();

                for(i = 0; i < parmTypes.length; ++i) {
                    if (parmTypes[i].isArray()) {
                        convertedArgs = new Object[i + 1];
                        System.arraycopy(actualArgs, 0, convertedArgs, 0, convertedArgs.length);
                        Object[] varArgs;
                        if (actualArgs.length <= i) {
                            varArgs = new Object[0];
                        } else {
                            ArrayList varArgsList = new ArrayList();

                            for(int j = i; j < actualArgs.length; ++j) {
                                if (actualArgs[j] != null) {
                                    varArgsList.add(actualArgs[j]);
                                }
                            }

                            varArgs = varArgsList.toArray();
                        }

                        convertedArgs[i] = varArgs;
                        break;
                    }
                }
            }

            Object var26 = invokeMethod(target, method, convertedArgs);
            return var26;
        } catch (NoSuchMethodException var21) {
            reason = var21;
        } catch (IllegalAccessException var22) {
            reason = var22;
        } catch (InvocationTargetException var23) {
            reason = var23.getTargetException();
        } finally {
            _objectArrayPool.recycle(actualArgs);
        }

        throw new MethodFailedException(source, methodName, (Throwable)reason);
    }
```