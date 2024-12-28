# 아이템 14. Comparable을 구현할지 고려하라
Comparable 인터페이스의 메서드는 compareTo 단 한개이다.
compareTo는 Object의 메서드가 아니고 equals와 약간의 차이가 있다.
compareTo는 단순 동치성 비교에 더해 순서까지 비교할 수 있으며, 제네릭하다.

Comparable을 구현했다는 것은 그 클래스의 인스턴스들에는 자연적인 순서(natural order)가 있음을 뜻한다.
그래서 Comparable을 구현한 객체들의 배열은 다음처럼 손쉽게 정렬할 수 있다.

`Arrays.sort(a);`

검색, 극단값 계산, 자동 정렬되는 컬렉션 관리도 쉬워진다.
다음 프로그램은 명령줄 인수들을 중복을 제거하고 알파벳순으로 출력한다.
String이 Comparable을 구현하고 있기 때문이다.
```java
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, args);
        System.out.println(s);
    }
}
```

사실상 자바 플랫폼 라이브러리의 모든 값 클래스와 열거 타입([아이템 34](item34.md))이 Comparable을 구현했다.
알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스를 작성한다면 반드시 Comparable을 구현하자.
```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

compareTo 메서드의 일반 규약은 equals의 규약과 비슷하다.
> 이 객체와 주어진 객체의 순서를 비교한다. 이 객체가 주어진 객체보다 작으면 음의 정수를, 같으면 0을, 크면 양의 정수를 반환한다.
> 이 객체와 비교할 수 없는 타입의 객체가 주어지면 ClassCastException을 던진다.
>
> 다음 설명에서 sgn(표현식) 표기는 수학에서 말하는 부호 함수(signum function)를 뜻하며, 표현식의 값이 음수, 0, 양수일 때 -1, 0, 1을 반환하도록 정의했다.
>
> - Comparable을 구현한 클래스는 모든 x, y에 대해 sgn(x.compareTo(y)) == -sgn(y.compareTo(x))여야 한다. 따라서 x.compareTo(y)는 y.compareTo(x)가 예외를 던질 때에 한해 예외를 던져야 한다.
> - Comparable을 구현한 클래스는 추이성을 보장해야 한다. 즉, x.compareTo(y) > 0 && y.compareTo(z) > 0이면 x.compareTo(z) > 0이다.
> - Comparable을 구현한 클래스는 모든 z에 대해 x.compareTo(y) == 0이면 sgn(x.compareTo(z)) == sgn(y.compareTo(z))다.
> - 이번 권고가 필수는 아니지만 꼭 지키는 게 좋다. (x.compareTo(y) == 0) == (x.equals(y))여야 한다. Comparable을 구현하고 이 권고를 지키지 않는 모든 클래스는 다음과 같이 명시해야 한다.</br> "주의: 이 클래스의 순서는 equals 메서드와 일관되지 않다."

모든 객체에 대해 전역 동치관계를 부여하는 equals 메서드와 달리, compareTo는 타입이 다른 객체를 신경쓰지 않아도 된다.
타입이 다른 객체가 주어지면 간단히 ClassCastException을 던져도 되며, 대부분 그렇게 한다.

물론 이 규약에서는 다른 타입 사이의 비교도 허용하는데, 보통은 비교할 객체들이 구현한 공통 인터페이스를 매개로 이뤄진다.
hashCode 규약을 지키지 못하면 해시를 사용하는 클래스와 어울리지 못하듯, compareTo 규약을 지키지 못하면 비교를 활용하는 클래스와 어울리지 못한다. 
비교를 활용하는 클래스의 예로는 정렬된 컬렉션인 TreeSet과 TreeMap, 검색과 정렬 알고리즘을 활용하는 유틸리티 클래스인 Collections와 Arrays가 있다.

compareTo의 세 규약은 동치성 검사도 equals 규약과 똑같이 반사성, 대칭성, 추이성을 충족해야 함을 뜻한다.
그래서 주의사항도 똑같다.
기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트를 추가했다면 추상화의 이점을 포기하지 않고는 compareTo 규약을 지킬 방법이 없다.
우회법도 같다. Comparable을 구현한 클래스를 확장해 값 컴포넌트를 추가하고 싶다면, 확장하는 대신 독립된 클래스를 만들고 이 클래스에 원래 클래스의 인스턴스를 가리키는 필드를 둔다.
그 다음 내부 인스턴스를 반환하는 '뷰'메서드를 제공하면 된다.
이렇게 하면 바깥 클래스에 우리가 원하는 compareTo 메서드를 구현해넣을 수 있다.

마지막 규약은 필수는 아니지만 지키는 것이 좋다.
이를 잘 지키면 compareTo로 정렬한 순서와 equals로 정렬한 순서가 일치한다.
이를 지키지 않아도 동작은 하지만, 이 클래스의 객체를 정렬된 컬렉션에 넣으면 해당 컬렉션이 구현한 인터페이스(Collection, Set, Map 등)에 정의된 동작과 엇박자를 낼 수 있다.

BigDecimal 클래스는 compareTo와 equals가 일관되지 않게 구현되어 있다.
빈 HashSet 인스턴스를 생성한 다음 `new BigDecimal("1.0")과 new BigDecimal("1.00")을 차례로 추가한다.
이 두 BigDecimal은 equals 메서드로 비교하면 서로 다르기 때문에 HashSet에 원소 2개를 갖게 된다.
하지만 HashSet 대신 TreeSet을 사용하면 compareTo로 비교하고, 두 값이 동일하다고 판단하여 원소를 한개만 갖게 된다.

compareTo 메서드 작성 요령은 equals와 비슷하다.
Comparable은 타입을 인수로 받는 제네릭 인터페이스이므로 compareTo 메서드의 인수 타입은 컴파일타임에 정해진다.
입력 인수의 타입을 확인하거나 형변환할 필요가 없다는 뜻이다.
인수의 타입이 잘못됐다면 컴파일 자체가 되지 않는다.
또한 null을 인수로 넣어 호출하면 NullPointerException을 던져야 한다.

compareTo 메서드는 각 필드가 동치인지를 비교하는게 아니라 그 순서를 비교한다.
객체 참조 필드를 비교하려면 compareTo 메서드를 재귀적으로 호출한다.
Comparable을 구현하지 않은 필드나 표준이 아닌 순서로 비교해야 한다면 Comparator를 대신 사용한다.
Comparator는 직접 만들거나 자바가 제공하는 것 중에서 골라 쓰면 된다.

다음 코드는 [아이템 10](item10.md)에서 구현한 CaseInsensitiveString용 compareTo 메서드로, 자바가 제공하는 Comparator를 사용하고 있다.
```java
public final class CaseInsensitiveString
        implements Comparable<CaseInsensitiveString> {
    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s):
    }
    // 나머지 코드 생략
}
```
CaseInsensitiveString이 Comparable<CaseInsensitiveString>을 구현한 것에 주목하자.
CaseInsensitiveString의 참조는 CaseInsensitiveString 참조와만 비교할 수 있다는 뜻으로, Comparable을 구현할 때 일반적으로 따르는 패턴이다.

클래스의 핵심 필드가 여러개라면 어느 것을 먼저 비교하느냐가 중요해진다.
가장 핵심적인 필드부터 먼저 비교하고 결과가 0이 아니라면 그 결과를 반환한다.
가장 핵심이 되는 필드가 똑같다면, 똑같지 않은 필드를 찾을 때까지 그다음으로 중요한 필드를 비교해나간다.

다음은 [아이템 10](item10.md)의 PhoneNumber 클래스용 compareTo 메서드를 이 방식으로 구현한 모습이다.
```java
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode);    // 가장 중요한 필드
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix);        // 두 번째로 중요한 필드
        if (result == 0)
            result = Short.compare(lineNum, pn.lineNum);  // 세 번째로 중요한 필드
    }
    return result;
}
```

자바 8에서는 Comparator 인터페이스가 일련의 comparator construction method와 팀을 꾸려 연쇄 방식으로 Comparator를 생성할 수 있게 되었다.
그리고 이 Comparator들을 Comparable 인터페이스가 원하는 compareTo 메서드를 구현하는 데 활용할 수 있다.
이 방식을 사용하면 코드는 깔끔해지지만 약간의 성능 저하가 뒤따른다.
```java
private static final Comparator<PhoneNumber> COMPARATOR =
        comparingInt((PhoneNumber pn) -> pn.areaCode)
            .thenComparingInt(pn -> pn.prefix)
            .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```
이 코드는 클래스를 초기화할 때 Comparator 생성 메서드 2개를 이용해 Comparator를 생성한다.
그 첫 번째인 comparingInt는 객체 참조를 int 타입 키에 매핑하는 키 추출 함수(key extractor function)를 인수로 받아 그 키를 기준으로 순서를 정하는 static 메서드다.
앞의 예에서 comparingInt는 람다를 인수로 받으며, 이 람다는 PhoneNumber에서 추출한 지역 코드를 기준으로 전화번호의 순서를 정하는 Comparator<PhoneNumber>를 반환한다.
이 람다에서 입력 인수의 타입(PhoneNumber pn)을 명시한 이유는 자바의 타입 추론 능력이 이 상황에서 알아낼 만큼 강력하지 않기 때문이다.

두 전화번호의 지역 코드가 같은 경우, 두 번째 비교자 생성 메서드인 thenComparingInt가 프리픽스와 가입자 번호를 차례대로 비교한다.
Comparator는 타입에 따라 comparingLong이나 comparingDoulbe과 같은 메서드를 제공하고 short처럼 int보다 작은 정수 타입에는 comparingInt를 사용하면 된다.

객체 참조용 Comparator 생성 메서드도 있다. 
comparing이라는 static 메서드 2개가 다중정의되어 있고 thenComparing이란 인스턴스 메서드 3개가 다중정의되어 있다.
```java
public static <T, U> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor,
        Comparator<? super U> keyComparator)
{
    Objects.requireNonNull(keyExtractor);
    Objects.requireNonNull(keyComparator);
    return (Comparator<T> & Serializable)
        (c1, c2) -> keyComparator.compare(keyExtractor.apply(c1),
                                          keyExtractor.apply(c2));
}

public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor)
{
    Objects.requireNonNull(keyExtractor);
    return (Comparator<T> & Serializable)
        (c1, c2) -> keyExtractor.apply(c1).compareTo(keyExtractor.apply(c2));
}
```
```java
default Comparator<T> thenComparing(Comparator<? super T> other) {
    Objects.requireNonNull(other);
    return (Comparator<T> & Serializable) (c1, c2) -> {
        int res = compare(c1, c2);
        return (res != 0) ? res : other.compare(c1, c2);
    };
}

default <U> Comparator<T> thenComparing(
        Function<? super T, ? extends U> keyExtractor,
        Comparator<? super U> keyComparator)
{
    return thenComparing(comparing(keyExtractor, keyComparator));
}


default <U extends Comparable<? super U>> Comparator<T> thenComparing(
        Function<? super T, ? extends U> keyExtractor)
{
    return thenComparing(comparing(keyExtractor));
}
```

이따금 '값의 차'를 기준으로 첫 번째 값이 두 번째 값보다 작으면 음수를, 두 값이 같으면 0을, 첫 번째 값이 크면 양수를 반환하는 compareTo나 compare 메서드와 마주할 것이다.
```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
};
```
이 방식은 사용하면 안 된다.
정수 오버플로를 일으키거나 IEEE 754 부동소수점 계산 방식에 따른 오류를 낼 수 있다.
그렇다고 이번 아이템에서 설명한 방법대로 구현한 코드보다 월등히 빠르지도 않다.
대신 다음의 두 방식 중 하나를 사용하자

## static compare 메서드를 활용한 Comparator
```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
};
```
## Comparator 생성 메서드를 활용한 Comparator
```java
static Comparator<Object> hashCodeOrder =
        Comparator.comparingInt(o -> o.hashCode());
```
