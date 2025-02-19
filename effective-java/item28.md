# 아이템 28. 배열보다는 리스트를 사용하라
배열과 제네릭 타입에는 중요한 차이가 두 가지 있다.
배열은 공변(convariant)이다. 예를 들어 Sub가 Super의 하위 타입이라면 배열 Sub[]는 배열 Super[]의 하위 타입이 된다. 반면 제네릭은 불공변(invariant)이다. Type1과 Type2가 있을 때, List<Type1>과 List<Type2>는 서로 별개이다.

다음은 문법상 허용되지만 런타임 에러를 발생시키는 코드이다.
```java
Object[] objectArray = new Long[1];
objectArray[0] = "타입이 달라 넣을 수 없다."; // ArrayStoreException을 던진다.
```
하지만 다음 코드는 문법이 맞지 않아 컴파일되지 않는다.
```java
List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입이다.
ol.add("타입이 달라 넣을 수 없다.");
```
양쪽 다 Long타입 저장소에 String을 넣을 수 없다.
배열에서는 그 실수를 런타임에 알게 되고 리스트는 컴파일에 알 수 있다는 것이 다르다.
컴파일할 때 바로 알고 수정하는 것이 훨씬 좋다.

배열은 실체화(reify)된다.
무슨 뜻이냐면 배열은 런타임에도 자신이 담기로 한 원소의 타입을 인지하고 확인한다.
반면, 제네릭은 타입 정보가 런타임에는 소거(erasure)된다.
이는 제네릭이 지원되기 전의 레거시 코드와 제네릭 타입을 함께 사용할 수 있게 하기 위함이다.

이러한 차이로 인해 배열과 제네릭은 잘 어우러지지 못한다.
배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없다.
`new List<E>[]`, `new List<String>[]`, `new E[]` 식으로 작성하면 컴파일할 때 제네릭 배열 생성 오류를 일으킨다.

이를 허용한다면 컴파일러가 자동으로 생성한 형변환 코드에서 런타임에 `ClassCastException`이 발생할 수 있다.
런타임에 `ClassCastException`이 발생하는 것을 막겠다는 제네릭의 취지에 어긋나는 것이다.
```java
List<String>[] stringLists = new List<String>[1];  // (1)
List<Integer> intList = List.of(42);               // (2)
Object[] objects = stringLists;                    // (3)
objects[0] = intList;                              // (4)
String s = stringLists[0].get(0);                  // (5)
```
제네릭 배열을 생성하는 (1)이 허용된다고 가정해보자.
(2)는 원소가 하나인 List<Integer>를 생성한다.
(3)은 (1)에서 생성한 List<String>의 배열을 Object 배열에 할당한다.
(4)는 (2)에서 생성한 List<Integer>의 인스턴스를 Object 배열의 첫 원소로 저장한다.
제네릭은 소거 방식이기에 타입을 구분하지 않아 이 역시 성공한다.
즉 런타임에는 List<Integer> 인스턴스 타입은 단순히 List가 되고, List<Integer>[] 인스턴스 타입은 단순히 List[]가 된다.
따라서 (4)에서도 `ArrayStoreException`이 발생하지 않는다.

List<String> 인스턴스만 담겠다고 선언한 stringLists 배열에는 List<Integer> 인스턴스가 저장되어 있게 된다.
그리고 (5)에서 이 배열의 첫 리스트에서 첫 원소를 꺼내려고 한다.
컴파일러는 꺼낸 원소를 String으로 형변환하는데, 이 원소는 Integer이므로 런타임에 `ClassCastException`이 발생한다.
이를 방지하기 위해서 (1)에서 제네릭 배열이 생성되지 않도록 컴파일 에러를 발생시키는 것이다.

E, List<E>, List<String> 같은 타입을 실체화 불가 타입(non-reifiable type)이라 한다.
실체화되지 않아서 런타임에는 컴파일타임보다 타입 정보를 적게 가지는 타입이다.
소거 메커니즘 때문에 매개변수화 타입 가운데 실체화될 수 있는 타입은 `List<?>`나 `Map<?, ?>` 같은 비한정적 와일드카드 타입 뿐이다([아이템 26](item26.md)).

배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 경고가 뜨는 경우 대부분 배열인 E[] 대신 컬렉션인 List<E>를 사용하면 해결된다.
코드가 조금 복잡해지고 성능이 저하될 순 있지만, 타입 안정성과 상호운용성이 좋아진다.

다음은 제네릭을 사용하지 않은 Chooser 클래스 예시이다.
```java
public class Chooser {
    private final Object[] choiceArray;

    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    {

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```
이 클래스를 사용하면 choose 메서드를 호출할 때마다 반환된 Object를 원하는 타입으로 형변환해야 한다.
만약 타입이 다른 원소가 들어 있었다면 런타임에 `ClassCaseException`이 발생한다.
```java
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = choices.toArray();
    {

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```
위처럼 제네릭을 사용하려고 수정하고 컴파일하면 다음과 같은 오류 메세지가 출력될 것이다.
```text
Chooser.java:9: error: incompatible types: Object[] cannot be converted to T[]
        choiceArray = choices.toArray();
                                     ^
  where T is type-variable:
    T extends Object declared in class Chooser
```
이는 Object 배열을 T 배열로 형변환하면 해결된다.
```java
        choiceArray = (T[]) choices.toArray();
```
이렇게 수정하면 컴파일은 가능하지만 경고 메세지가 나온다.
```text
Chooser.java:9: warning: [unchecked] unchecked cast
        choiceArray = (T[]) choices.toArray();
                                           ^
  required: T[], found: Object[]
  where T is a type-variable:
T extends Object declared in class Chooser
```
T가 무슨 타입인지 알 수 없으니 컴파일러는 이 형변환이 런타임에도 안전한지 보장할 수 없다는 의미이다.
코드를 작성한 사람이 안전하다고 확신한다면 주석을 남기고 애너테이션을 달아 경고를 숨겨도 된다([아이템 27](item27.md)).
하지만 경고의 원인을 제거하는 것이 훨씬 깔끔하며 배열 대신 리스트를 사용하면 된다.
```java
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    {

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}
```
위와 같이 변경하면 조금 느려지긴 하지만 `ClassCastException`을 발생시키지 않으니 더 안전하다.
