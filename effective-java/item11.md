# 아이템 11. equals를 재정의하려거든 hashCode도 재정의하라
equals를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다.
그렇지 않으면 일반 규약을 어기게 되어 해당 클래스의 인스턴스를 HashMap이나 HashSet같은 컬렉션의 원소로 사용할 때 문제가 발생한다.
Object 명세에 다음과 같이 규정되어 있다.
> - equals 비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode 메서드는 몇 번을 호출해도 항상 같은 값을 반환해야 한다. 단, 애플리케이션이 다시 실행된다면 값이 달라져도 상관없다.
> - equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
> - equals(Object)가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다. 단, 다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아진다.

hashCode 재정의를 잘못했을 때 크게 문제가 되는 조항은 두 번째다.
논리적으로 같은 객체는 같은 해시코드를 반환해야 한다.
[아이템 10](item10.md)에서도 보았듯이 equals는 물리적으로 다른 두 객체를 논리적으로는 같다고 할 수 있다.
하지만 Object의 기본 hashCode 메서드는 이 둘이 다르다고 판단하여 규약과 달리 서로 다른 값을 반환한다.

[아이템 10](item10.md)의 PhoneNumber 클래스의 인스턴스를 HashMap의 원소로 사용한다고 가정해보자.
```java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "제니");
```
이 코드 다음에
```
m.get(new PhoneNumber(707, 867, 5309));
```
을 실행하면 "제니"가 나와야 할 것 같지만 실제로는 null을 반환한다.
두 인스턴스가 논리적으로는 동치이지만 PhoneNumber에 hashCode를 재정의하지 않았기 때문에 서로 다른 해시코드를 반환하여 두 번째 규약을 지키지 못한다.</br>
HashMap은 해시코드가 다른 엔트리끼리는 동치성 검사를 시도조차 하지 않도록 최적화되어 있어 두 인스턴스를 같은 버킷에 담아도 null을 반환한다.
이는 PhoneNumber에 hashCode 메서드를 올바르게 작성하면 해결된다.

```java
@Override public int hashCode() { return 42 };
```
이 코드는 동치인 모든 객체에서 같은 해시코드를 반환하니 적법하다.
하지만 모든 객체에 똑같은 값만 반환하므로 모든 객체가 해시테이블의 한 버킷에 담겨 linked list처럼 동작한다.
이는 해시테이블의 장점인 O(1)의 시간복잡도를 O(n)으로 악화시킨다.

좋은 해시 함수는 서로 다른 인스턴스에 대해 다른 해시코드를 반환한다.
이것이 hashCode의 세 번째 규약이 요구하는 바이며 이상적인 해시함수는 주어진 서로 다른 인스턴스들을 32비트 정수 범위에 균일하게 분배해야 한다.
다음과 같은 요령을 따르면 좋은 hashCode를 작성할 수 있다.
<ol type="1">
  <li>int 변수 result를 선언한 후 값 c로 초기화한다. 이때 c는 해당 객체의 첫번째 핵심 필드를 단계 2.a의 방식으로 계산한 해시코드다. (핵심 필드란 equals 비교에 사용되는 필드이다. [아이템 10](item10.md) 참조)</li>
  <li>해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행한다.
    <ol type="a">
      <li>해당 필드의 해시코드 c를 계산한다.
        <ol type="i">
          <li>기본 타입 필드라면, Type.hashCode(f)를 수행한다. 여기서 Type은 해당 기본 타입의 박싱 클래스다.</li>
          <li>참조 타입 필드면서 이 클래스의 equals 메서드가 이 필드의 equals를 재귀적으로 호출해 비교한다면, 이 필드의 hashCode를 재귀적으로 호출한다. 계산이 더 복잡해진다면 이 필드의 표준형(canonical representation)을 만들어 그 표준형의 hashCode를 호출한다. 필드의 값이 null이면 0을 사용한다(다른 상수도 괜찮지만 전통적으로 0을 사용한다).</li>
          <li>필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로 적용해 각 핵심 원소의 해시코드를 계산한 다음, 단계 2.b 방식으로 갱신한다. 배열에 핵심 원소가 하나도 없다면 단순히 상수(0을 추천한다)를 사용한다. 모든 원소가 핵심 원소라면 Arrays.hashCode를 사용한다.</li>
        </ol>
      </li>
      <li>단계 2.a에서 계산한 해시코드 c로 result를 갱신한다. 코드로는 다음과 같다. </br> result = 31 * result + c;</li>
    </ol>
  </li>
  <li>result를 반환한다.</li>
</ol>

파생 필드는 해시코드 계산에서 제외해도 된다. 또한 equals 비교에 사용되지 않은 필드는 반드시 제외해야 한다.
그렇지 않으면 hashCode 규약 두 번째를 어길 위험이 있다.
단계 2.b의 곱셈 31 * result는 필드를 곱하는 순서에 따라 result 값이 달라지게 한다.
그 결과 클래스에 비슷한 필드가 여러 개일 때 해시 효과를 크게 향상시킨다.

예를 들어 String의 hashCode를 곱셈 없이 구현한다면 모든 아나그램(anagram, 구성하는 철자가 같고 순서만 다른 문자열)의 해시코드가 같아진다.
곱할 숫자를 31로 정한 이유는 홀수이면서 소수(prime)이기 때문이다.
만약 이 수가 짝수이고 오버플로가 발생한다면 정보를 잃게 된다.
2를 곱하는 것은 시프트 연산과 같은 효과를 주기 때문이다.
31을 이용하면 이 곱셈을 시프트 연산과 뺄셈으로 대체해 최적화할 수 있다. (31 * i는 (i << 5) - i와 같다).
요즘 VM들은 이런 최적화를 자동으로 해준다.

```java
@Override public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```
이 PhoneNumber의 hashCode는 핵심 필드 3개만을 사용해 간단한 계산만을 수행한다.
과정에 비결정적(undeterministic) 요소가 없으므로 동치인 PhoneNumber 인스턴스들은 같은 해시코드를 가지는 것이 명확하다.
이러한 방식으로 hashCode를 구현하는것도 충분히 쓸만하지만 해시 충돌을 줄이고 싶다면 구아바의 com.google.common.hash.Hashing을 참고하자.

Object 클래스는 임의의 개수만큼 객체를 받아 해시코드를 계산해주는 static 메서드인 hash를 제공한다.
쉽게 사용할 수 있지만 입력 인수를 담기 위한 배열이 만들어지고, 입력 중 기본 타입이 있다면 박싱과 언박싱을 해야 하기 때문에 속도가 더 느리다.
PhoneNumber의 hashCode 이 hash를 사용하여 재정의할 수 있다.
```java
@Override public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기보다는 캐싱을 고려해야 한다.
이 타입의 객체가 주로 해시의 키로 사용될 것 같다면 인스턴스가 만들어질 때 해시코드를 계산해둬야 한다.
해시의 키로 사용되지 않는 경우라면 hashCode가 처음 불릴 때 계산하는 지연 초기화(lazy initialization)를 고려해보자.
필드를 지연 초기화하려면 그 클래스의 스레드를 안전하게 만들도록 신경은 써야 한다.
```java
private int hashCode; // 자동으로 0으로 초기화된다.

@Override public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```
성능을 높히겠다고 해시코드를 계산할 때 핵심 필드를 생략해서는 안 된다.
속도가 빨라져도 해시 품질이 나빠져 해시테이블의 성능을 심각하게 떨어뜨릴 수 있다.

실제로 java2 전의 String 은 최대 16개의 문자만으로 해시코드를 계산했다.
문자열이 길면 균일하게 나눠 16개 문자만 뽑아내 사용한 것이다.
URL처럼 계층적인 이름을 대량으로 사용한다면 해시 함수의 성능은 최악일 것이다.

hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지는 말자.
그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿔도 문제가 없다.
