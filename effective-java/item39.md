# 아이템 39. 명명 패턴보다 애너테이션을 사용하라
전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 구분되는 명명 패턴을 적용해왔다.
테스트 프레임워크인 JUnit은 버전 3까지 테스트 메서드 이름을 test로 시작하게끔 했다.
효과적인 방법이지만 여러 단점도 존재한다.

첫 번째로 오타가 나면 안 된다.
실수로 이름을 tsetSafetyOverride로 지으면 JUnit3는 이 메서드를 무시하기 때문에 개발자는 테스트가 통과했다고 오해할 수 있다.

두 번째로 올바른 프로그램 요소에만 사용된다는 보장이 없다. 
메서드가 아닌 클래스 이름을 TestSafetyMechanisms로 지어 JUnit에 줬다고 해보자.
개발자는 이 클래스에 정의된 테스트 메서드들을 수행하길 바라겠지만 JUnit은 클래스 이름으로 판단하지 않아서 어떠한 경고 메시지 없이 테스트가 수행되지 않는다.

마지막으로 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다.
특정 예외를 던져야만 성공하는 테스트가 있다고 했을 때, 해당 예외 타입을 테스트에 매개변수로 전달해야 할 것이다.
예외의 이름을 테스트 메서드 이름에 붙이는 방법도 있지만, 좋은 방법은 아니다.
컴파일러는 메서드 이음리 덧붙인 문자열이 예외를 가리키는지 알 도리가 없다.
테스트를 실행하기 전에는 그런 이름의 클래스가 존재하는지, 예외인지 알 수 없다.

애너테이션을 사용하면 이 모든 문제들을 해결할 수 있다.
JUnit도 버전 4부터 사용하고 있다.
Test라는 이름의 애너테이션을 정의한다고 해보자.
자동으로 수행되는 간단한 테스트용 애너테이션으로, 예외가 발생하면 해당 테스트를 실패로 처리한다.
```java
import java.lang.annotation.*;

/**
 * 테스트 메서드임을 선언하는 애너테이션이다.
 * 매개변수가 없는 static 메서드 전용이다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```
`@Test` 애너테이션 타입 선언에도 `@Retention`과 `@Target`이라는 두 가지의 다른 애너테이션이 달려 있다.
이들을 메타 애너테이션(meta-annotation)이라고 한다.
`@Retention(RetentionPolicy.RUNTIME)`은 `@Test`가 런타임에도 유지되어야 한다는 의미다.
이 애너테이션을 생략하면 테스트 도구는 `@Test`를 인식할 수 없다.
`Target(ElementType.METHOD)`은 `@Test`가 메서드 선언에서만 사용돼야 한다고 알려준다.
클래스 선언, 필드 선언 등 다른 프로그램 요소에는 달 수 없다.

주석에 적힌 "매개변수가 없는 static 메서드 전용이다."는, 컴파일러가 강제하려면 적절한 애너테이션 처리기를 직접 구현해야 한다.
javax.annotation.processing API 문서를 참고하라.
적절한 애너테이션 처리기 없이 인스턴스 메서드나 매개변수가 있는 메서드에 달면 컴파일은 되겠지만, 테스트 도구를 실행할 때 문제가 된다.

다음은 `@Test` 애너테이션을 적용한 예시이다.
이와 같은 애너테이션을 아무 매개변수 없이 단순히 대상에 마킹한다는 의미로 마커(marker) 애너테이션이라 한다.
프로그래머가 Test 이름에 오타를 내거나 메서드 선언 외의 프로그램 요소에 달면 컴파일 오류가 발생한다.
```java
public class Sample {
    @Test public static void m1() { } // Test should pass
    public static void m2() { }
    @Test public static void m3() {   // Test should fail
        throw new RuntimeException("Boom");
    }
    public static void m4() { }
    @Test public void m5() { } // INVALID USE: nonstatic method
    public static void m6() { }
    @Test public static void m7() { // Test should fail
        throw new RuntimeException("Crash");
    }
    public static void m8() { }
}
```
Sample 클래스에는 static 메서드가 7개이고, 그 중 4개에 `@Test`를 달았다.
m3와 m7 메서드는 예외를 던지고, m1는 정상적으로 통과된다.
그리고 m5는 인스턴스 메서드이기 때문에 `@Test`를 잘못 사용한 경우이다.
나머지 메서드는 테스트 도구가 무시한다.

`@Test` 애너테이션이 Simple 클래스의 의미에 직접적인 영향을 주지는 않는다. 
이 애너테이션이 관심 있는 프로그램에 추가 정보를 제공할 뿐이다.
해당 애너테이션에 관심 있는 도구에서 특별한 처리를 할 수 있게 한다.
```java
import java.lang.reflect.*;

public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                tests++;
                try {
                    m.invoke(null);
                    passed++;
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " failed: " + exc);
                } catch (Exception exc) {
                    System.out.println("Invalid @Test: " + m);
                }
            }
        }
        System.out.printf("Passed: %d, Failed: %d%n",
                          passed, tests - passed);
    }
}
```
이 테스트 러너는 명령줄로부터 정규화된 클래스 이름을 받아 그 클래스에서 `@Test` 애너테이션이 달린 메서드를 차례대로 호출한다.
isAnnotationPresent가 실행할 메서드를 찾아주는 메서드다.
테스트 메서드가 예외를 던지면 리플렉션 메커니즘이 InvocationTargetException으로 감싸서 다시 던진다.
그래서 이 프로그램에서 InvocationTargetException을 catch하여 getCause를 호출해 실패 정보를 출력한다.

InvocationTargetException이 아닌 다른 예외가 발생했다면 `@Test`를 잘못 사용했다는 뜻이다.
인스턴스 메서드, 매개변수가 있는 메서드, 호출할 수 없는 메서드 등에 `@Test`를 달았을 경우이며, 두 번째 catch 블록에서 이 경우를 잡아 오류 메시지를 출력한다.
이를 실행한 경우 다음과 같은 출력이 나온다.
```java
public static void Sample.m3() failed: RuntimeException: Boom
Invalid @Test: public void Sample.m5()
public static void Sample.m7() failed: RuntimeException: Crash
Passed: 1, Failed: 3
```

특정 예외를 던져야만 성공하는 테스트를 작성해보자.
새로운 애너테이션 타입이 필요하고 다음과 같이 작성할 수 있다.
```java
import java.lang.annotation.*;
/**
 * Indicates that the annotated method is a test method that
 * must throw the designated exception to succeed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

```
이 애너테이션의 매개변수 타입은 `Class<? extends Throwable>`이다.
Throwable을 확장한 클래스의 Class 객체 라는 뜻이며, 모든 예외와 오류 타입을 다 수용한다.
이는 한정적 타입 토큰([아이템 33](item33.md))을 활용한 것이다.
```java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() { // Test should pass
        int i = 0;
        i = i / i;
    }
    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // Should fail (wrong exception)
        int[] a = new int[0];
        int i = a[1];
    }
        @ExceptionTest(ArithmeticException.class)
        public static void m3() { } // Should fail (no exception)
}

```
이 애너테이션을 활용하기 위해 이전 테스트에서 main 메서드를 다음과 같이 수정해보자.
```java
if (m.isAnnotationPresent(ExceptionTest.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("Test %s failed: no exception%n", m);
    } catch (InvocationTargetException wrappedEx) {
        Throwable exc = wrappedEx.getCause();
        Class<? extends Throwable> excType =
            m.getAnnotation(ExceptionTest.class).value();
        if (excType.isInstance(exc)) {
            passed++;
        } else {
            System.out.printf(
                "Test %s failed: expected %s, got %s%n",
                m, excType.getName(), exc);
        }
    } catch (Exception exc) {
        System.out.println("Invalid @Test: " + m);
    }
}
```
`@Test` 애너테이션의 RunTests와의 차이점은 애너테이션 매개변수 값을 추출해 테스트 메서드가 올바른 예외를 던지는지 확인한다.
형변환이 없으니 ClassCastException이 발생하는 경우는 없다.
해당 예외 클래스 파일이 컴파일타임에는 존재했으나 런타임에는 존재하지 않을 수 있다.
이 경우는 TypeNotPresentException이 발생한다.

예외를 여러 개 명시하고 그중 하나가 발생하면 성공하게 만들 수도 있다.
`@ExceptionTest` 애너테이션의 매개변수 타입을 Class 객체 배열로 수정해보자.
```java
// Annotation type with an array parameter
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Exception>[] value();
}
```
새로운 `@ExceptionTest`를 사용하는 코드를 작성해보자.
```java
@ExceptionTest({ IndexOutOfBoundsException.class,
                  NullPointerException.class })
public static void doublyBad() {
    List<String> list = new ArrayList<>();

    // The spec permits this method to throw either
    // IndexOutOfBoundsException or NullPointerException
    list.addAll(5, null);
}
```
다음은 테스트 러너를 수정한 코드이다.
```java
if (m.isAnnotationPresent(ExceptionTest.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("Test %s failed: no exception%n", m);
    } catch (Throwable wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        int oldPassed = passed;
        Class<? extends Exception>[] excTypes =
            m.getAnnotation(ExceptionTest.class).value();
        for (Class<? extends Exception> excType : excTypes) {
            if (excType.isInstance(exc)) {
                passed++;
                break;
        }
    }
    if (passed == oldPassed)
        System.out.printf("Test %s failed: %s %n", m, exc);
    }
}
```
자바 8에서는 여러 개의 값을 받는 애너테이션을 다른 방식으로도 만들 수 있다.
배열 매개변수를 사용하는 것 대신에 애터네이션에 `@Repeatable` 메타 애너테이션을 추가하면 된다.
이렇게 하면 한 프로그램 요소에 여러 번 달 수 있다.
이 방법을 사용할 때는 `@Repeatable` 애너테이션을 단 애너테이션을 반환하는 컨테이너 애너테이션을 하나 더 정의하고, `@Repeatable`에 이 컨테이너 애너테이션의 class 객체를 매개변수로 전달해야 한다.
컨테이너 애너테이션은 내부 애너테이션 타입의 배열을 반환하는 value 메서드를 정의해야 하며, `@Retention`으로 적절한 정책과 `@Target`으로 적용 대상을 명시해야 한다.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Exception> value();
}

// 컨테이너 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTestContainer {
    ExceptionTest[] value();
}
```
배열 방식 대신 `@Repeatable`을 적용한 코드이다.
```java
@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() { ... }
```
`@Repetable` 애너테이션을 여러 개 달면 하나만 달았을 때와 구분하기 위해 컨테이너 애너테이션 타입이 적용된다.
getAnnotationsByType 메서드는 이 둘을 구분하지 않아 모두 가져오지만, isAnnotationPresent 메서드는 둘을 명확히 구분한다.

RunTests를 `@Repeatable`을 사용하는 버전으로 수정하면 다음과 같다.
```java
if (m.isAnnotationPresent(ExceptionTest.class)
    || m.isAnnotationPresent(ExceptionTestContainer.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("Test %s failed: no exception%n", m);
    } catch (Throwable wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        int oldPassed = passed;
        ExceptionTest[] excTests =
                m.getAnnotationsByType(ExceptionTest.class);
    for (ExceptionTest excTest : excTests) {
        if (excTest.value().isInstance(exc)) {
            passed++;
            break;
        }
    }
    if (passed == oldPassed)
        System.out.printf("Test %s failed: %s %n", m, exc);
    }
}
```
