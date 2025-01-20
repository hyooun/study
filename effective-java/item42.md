# 아이템 42. 익명 클래스보다는 람다를 사용하라
함수 객체를 만드는 수단으로 익명 클래스를 사용해왔다.
```java
Collections.sort(words, new Comparator<String>() {
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
});
```
자바 8부터 추상 메서드 하나짜리 인터페이스는 인스턴스를 람다식(lambda expression)을 사용해 만들 수 있게 되었다.
람다는 함수나 익명 클래스와 개념은 비슷하지만 훨씬 간결하다.

```java
Collections.sort(words,
        (s1, s2) -> Integer.compare(s1.length(), s2.length()));
```
여기서 람다, 매개변수(s1, s2), 반환값의 타입은 각각 `(Comparator<String>)`, String, int지만 코드에는 나타나지 않는다.
컴파일러가 문맥을 통해 이 타입들을 추론한다.
상황에 따라 컴파일러가 타입을 결정하지 못할 수도 있는데, 그 때는 프로그래머가 직접 명시해야 한다.

기본적으로 타입을 명시해야 코드가 더 명확한 경우를 제외하고는 람다의 모든 매개변수 타입은 생략한다.
컴파일러가 타입을 알 수 없다는 오류를 표시하는 경우에만 해당 타입을 명시하면 된다.

람다 자리에 비교자 생성 메서드를 사용하면 이 코드를 더 간결하게 만들 수 있다.
```java
Collections.sort(words, comparingInt(String::length));
```
자바 8에서 List 인터페이스에 추가된 sort 메서드를 사용하면 더 짧아진다.
```java
words.sort(comparingInt(String::length));
```

[아이템 34](item34.md)의 Operation 열거 타입에서 apply 메서드의 동작이 상수마다 달라야 하니 constant-specific class body에서 각각의 apply 메서드를 재정의했었다.
```java
public enum Operation {
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

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }
    public abstract double apply(double x, double y);
}

```
[아이템 34](item34.md)에서 constant-specific class body를 구현하는 방식보다 열거 타입에 인스턴스 필드를 두는 것이 낫다고 했다.
람다를 사용하면 열거 타입의 인스턴스 필드를 이용하는 방식으로 상수별로 다르게 동작하는 코드를 구현하기 편리하다.
각 열거 타입 상수의 동작을 람다로 구현해 생성자에 넘기고 인스턴스 필드로 저장해둔다.
그 다음 apply 메서드에서 필드에 저장된 람다를 호출하기만 하면 된다.
```java
public enum Operation {
    PLUS ("+", (x, y) -> x + y),
    MINUS ("-", (x, y) -> x - y),
    TIMES ("*", (x, y) -> x * y),
    DIVIDE("/", (x, y) -> x / y);

    private final String symbol;
    private final DoubleBinaryOperator op;

    Operation(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }

    @Override public String toString() { return symbol; }

    public double apply(double x, double y) {
        return op.applyAsDouble(x, y);
    }
}

```

이렇게 간결하게 표현 가능한 람다도 단점은 존재한다.
람다는 이름이 없고 문서화도 어렵다.
때문에 코드 자체로 동작이 명확하게 설명되지 않거나 코드가 많아지면 람다를 쓰지 말아야 한다.

열거 타입 생성자에 넘겨지는 인수들의 타입도 컴파일타입에 추론되기 때문에 열거 타입 생성자 안의 람다는 열거 타입 인스턴스 멤버에 접근할 수 없다(인스턴스는 런타임에 만들어지기 때문이다).
때문에 인스턴스 필드나 메서드를 사용해야 하는 상황이라면 constant-specific class body를 구현하는 방식을 사용해야 한다.

람다는 자신을 참조할 수 없어서 this 키워드는 바깥 인스턴스를 가리킨다.
반면 익명 클래스에서의 this는 익명 클래스의 인스턴스 자신을 가리키기 때문에 함수 객체가 자신을 사용해야 한다면 익명 클래스를 사용해야 한다.

또한 람다는 익명 클래스처럼 직렬화 형태가 구현별로 다를 수 있기에 람다를 직렬화하는 것은 피해야 한다.
직렬화해야만 하는 함수 객체가 있다면(Comparator) private static nested class([아이쳄 24](item24.md))를 인스턴스를 사용하자.
