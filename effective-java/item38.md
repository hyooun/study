# 아이템 38. 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라
열거 타입은 기본적으로 확장이 불가능하다.
그런데 API가 제공하는 기본 연산 외에 사용자가 확장할 수 있게 하고 싶을 수 있다.
열거 타입이 임의의 인터페이스를 구현할 수 있기 때문에 연산 코드용 인터페이스를 정의하고 열거 타입이 이 인터페이스를 구현하게 하면 된다.
[아이템 34](item34.md)의 Operation를 확장할 수 있게 만들어보자.
```java
public interface Operation {
    double apply(double x, double y);
}

public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    BasicOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override public String toString() {
        return symbol;
    }
}

```
열거 타입인 BasicOperation은 확장할 수 없지만 인터페이스인 Operation은 확장할 수 있고, 이걸 연산의 타입으로 사용하면 된다.
앞의 코드에서 지수 연산(EXP)과 나머지 연산(REMINDER)을 추가해보자.
```java
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        public double apply(double x, double y) {
            return x % y;
        }
    };

    private final String symbol;

    ExtendedOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override public String toString() {
        return symbol;
    }
}

```

개별 인스턴스 수준에서뿐 아니라 타입 수준에서도, 기본 열거 타입 대신 확장된 열거 타입을 넘겨 확장된 열거 타입의 원소 모두를 사용하게 할 수도 있다.
```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(ExtendedOperation.class, x, y);
}

private static <T extends Enum<T> & Operation> void test(
    Class<T> opEnumType, double x, double y) {
    for (Operation op : opEnumType.getEnumConstants())
        System.out.printf("%f %s %f = %f%n",
                          x, op, y, op.apply(x, y));
}
```
main 메서드는 test 메서드에 ExtendedOperation의 class 리터럴을 넘겨 확장된 연산들이 무엇인지 알려준다.
여기서 class 리터럴은 한정적 타입 토큰([아이템 33](item33.md))의 역할을 한다.
opEnumType 매개변수 선언 `<T extends Enum<T> & Operation> Class<T>`는 Class 객체가 enum 타입인 동시에 Operation의 하위 타입이어야 한다는 의미이다.

Class 객체 대신 한정적 와일드카드 타입([아이템 31](item31.md))인 `Collection<? extends Operation>`을 넘길 수도 있다.
```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(Arrays.asList(ExtendedOperation.values()), x, y);
}

private static void test(Collection<? extends Operation> opSet,
        double x, double y) {
    for (Operation op : opSet)
        System.out.printf("%f %s %f = %f%n",
                          x, op, y, op.apply(x, y));
}
```
이 코드는 좀 간편해졌고 test 메서드가 유연해졌다.
여러 구현 타입의 연산을 조합해서 호출하는 것도 가능하다.

이 방법에 사소한 문제가 있는데, 열거 타입끼리는 구현을 상속할 수 없다는 점이다.
아무 상태에도 의존하지 않으면 디폴트 구현([아이템 20](item20.md))을 통해 인터페이스에 추가할 수 있다.
하지만 Operation 예시는 연산 기호를 저장하고 찾는 로직이 BasicOperation과 ExtendedOperation에 모두 들어가야 한다.
공통적인 기능이 많아진다면 별도의 helper 클래스나 static helper method로 분리하는 방식을 사용할 수 있다.
