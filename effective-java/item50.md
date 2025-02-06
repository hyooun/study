# 아이템 50. 적시에 방어적 복사본을 만들라
항상 클라이언트로부터의 공격을 생각하고 방어적으로 프로그래밍 해야 한다.
악의적인 의도를 가지고 시스템 보안을 뚫으려는 시도를 할 수도 있고, 실수로 오작동을 유발할 수도 있다.

어떤 객체든 그 객체의 허락 없이는 외부에서 내부를 수정하지 못하게 해야 한다.
Period를 표현하는 다음과 같은 클래스가 있다고 해보자.
```java
public final class Period {
    private final Date start;
    private final Date end;

    /**
     * @param start the beginning of the period
     * @param end the end of the period; must not precede start
     * @throws IllegalArgumentException if start is after end
     * @throws NullPointerException if start or end is null
     */
    public Period(Date start, Date end) {
        if (start.compareTo(end) > 0)
            throw new IllegalArgumentException(
                start + " after " + end);
        this.start = start;
        this.end = end;
    }

    public Date start() {
        return start;
    }

    public Date end() {
        return end;
    }
    ... // Remainder omitted
}
```
이 클래스는 불변인 것 처럼 보이고 불변식이 잘 지켜질 것 같지만, Date가 가변이기 때문에 다음과 같이 사용하면 불변식이 깨진다.
```java
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78); // Modifies internals of p!
```
자바 8 이후로는 Date 대신 불변([아이템 17](item17.md))인 Instant를 사용하면 된다.
혹은 LocalDateTime이나 ZonedDateTime을 사용해도 된다.
Date는 오래된 API이니 새로운 코드를 작성할 때는 더 이상 사용하면 안 된다.

하지만 이전에 작성된 코드들 중에서 분명 Date를 사용한 코드가 있을 것이다.
외부 공격으로부터 Period 인스턴스의 내부를 보호하려면 생성자에서 받은 가변 매개변수 각각을 방어적 복사(defensive copy)해야 한다.
그런 다음 Period 인스턴스 안에서는 원본이 아닌 복사본을 사용한다.
```java
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());

    if (this.start.compareTo(this.end) > 0)
        throw new IllegalArgumentException(
            this.start + " after " + this.end);
}
```
새로 작성한 생성자를 사용하면 이전과 같이 Period 인스턴스의 start나 end 값을 변경할 수 없다.
여기서 매개변수의 복사본을 먼저 만들고 유효성 검사([아이템 49](item49.md))한 점에 주목하자.
순서가 부자연스러워 보일 수 있지만, 멀티스레딩 환경이라면 원본 객체의 유효성을 검사한 후 복사본을 만드는 취약한 순간에 다른 스레드가 원본 객체를 수정할 위험이 있기 때문에 이렇게 하는 것이 안전하다.
이를 검사시점/사용시점 공격(time-of-check/time-of-use), 줄여서 TOCTOU 공격이라고 한다.

방어적 복사에 Date의 clone 메서드를 사용하지 않은 것도 중요하다.
Date는 final이 아니므로 clone이 Date가 정의한 게 아닐 수 있다.
Date를 상속한 하위 클래스에서 clone을 악의적인 의도로 overriding 할 가능성도 고려해야 한다.
이러한 공격을 막기 위해서는 매개변수가 제3자에 의해 확장될 수 있는 타입이라면 방어적 복사본을 만들 때 clone을 사용해서는 안 된다.

생성자를 수정하면 앞서의 공격은 막아낼 수 있지만, Period 인스턴스는 접근자 메서드가 내부의 가변 정보를 직접 드러내기 때문에 변경이 가능하다.
```java
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78); // Modifies internals of p!
```
이와 같은 공격을 막아내려면 접근자가 가변 필드의 방어적 복사본을 반환하면 된다.
```java
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```
이렇게 수정하면 Period를 완벽한 불변으로 만들 수 있다.
Period 자신을 제외하고는 가변 필드에 접근할 방법이 없다.
생성자와 달리 접근자 메서드에서는 방어적 복사에 clone을 사용해도 된다.
Period가 가지고 있는 Date 객체는 java.util.Date임이 확실하기 때문이다.
그래도 [아이템 13](item13.md)에서 설명한 이유들로 인스턴스를 복사할 때는 생성자나 static 팩터리를 사용하는 것이 좋다.

메서드든 생성자든 클라이언트가 제공한 객체의 참조를 내부의 자료구조에 보관해야 하는 경우, 그 객체가 잠재적으로 변경 될 가능성이 있는지를 고려해야 한다.
변경될 수 있는 객체라면 그 객체가 클래스에 넘겨진 뒤 임의로 변경되어도 그 클래스가 문제없이 동작할지를 따져봐야 한다.
확신할 수 없다면 복사본을 만들어 저장해야 한다.

예를 들어 클라이언트가 건네준 객체를 내부 Set의 인스턴스에 저장하거나 Map 인스턴스의 키로 사용한다면, 나중에 그 객체가 변경됐을 때 Set이나 Map의 불변식이 깨질 것이다.
배열의 길이가 1 이상인 경우 무조건 가변이므로 클라이언트에 반환할 때 항상 방어적 복사를 수행해야 한다.(배열의 불변 뷰를 만드는 방법도 있다)

가능하면 불변 객체들을 조합해 객체를 구성하자.
그래야 방어적 복사를 할 일이 줄어든다.
방어적 복사에는 성능 저하가 따르고, 항상 사용할 수 있는 것도 아니다.
같은 패키지에 속하는 등의 이유로 호출자가 내부 컴포넌트를 수정하지 않으리라는 보장이 있다면 방어적 복사를 생략해도 된다.
이러한 상황이라도 호출자가 매개변수나 반환값을 수정하지 말아야 함을 명시하는 문서화는 필요하다.

그 객체의 통제권을 넘기는 경우엔 방어적 복사를 생략할 수 있다.
통제권을 넘겨받기로 한 메서드나 생성자를 가진 클래스들은 악의적인 공격에 취약하므로, 방어적 복사를 생략하는 경우는 해당 클래스와 그 클라이언트가 서로 신뢰할 수 있을 때, 혹은 불변식이 깨지더라도 영향이 클라이언트에게만 있을 때 이다.
그 예시로 래퍼 클래스 패턴([아이템 18](item18.md))을 들 수 있다.
래퍼 클래스의 특성상 클라이언트는 래퍼에 넘긴 객체에 접근할 수 있어 불변식을 쉽게 파괴할 수 있지만, 그 영향을 오직 클라이언트만 받는다.
