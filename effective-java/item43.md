# 아이템 43. 람다보다는 메서드 참조를 사용하라
람다의 가장 큰 장점은 간결함이다.
자바에는 람다보다도 더 간결하게 함수 객체를 만드는 방법이 있는데, 바로 메서드 참조(method reference)다.

다음 코드는 임의의 키와 Integer 값의 매핑을 관리하는 프로그램의 일부이다.
값이 키 인스턴스 개수로 멀티셋과 비슷한 기능을 하며, 키가 맵 안에 없다면 키와 1을 매핑하고 이미 있다면 값을 증가시킨다.
```java
map.merge(key, 1, (count, incr) -> count + incr);
```
이 코드에 사용한 Map의 merge 메서드는 key, value, method를 인수로 받으며, 주어진 키가 맵 안에 없다면 주어진 (key, value) 쌍을 그대로 저장한다.
키가 이미 있다면 세 번째 인수로 받은 함수를 현재 값과 주어진 값에 적용한 다음, 그 결과로 현재 값을 덮어쓴다.

깔끔해 보이는 코드지만 매개변수 count와 incr은 크게 하는 일 없이 공간을 꽤 차지한다.
아래와 같이 람다 대신 메서드 참조를 사용하면 더 간결하게 사용할 수 있다.
```java
map.merge(key, 1, Integer::sum);
```
매개변수의 수가 늘어날수록 메서드 참조로 더 많은 코드를 줄일 수 있다.
매개변수의 이름 자체가 코드를 이해하는데 도움을 준다면 람다를 사용해도 좋다.

일반적으로, 람다로 할 수 없는 일이라면 메서드 참조로도 할 수 없다.
메서드 참조를 사용하는 것이 보통은 더 짧고 간결하므로, 람다로 구현했을 때 너무 길거나 복잡하다면 새로운 메서드를 만든 다음 람다 대신 그 메서드 참조를 사용하는 방식도 고려해봐야 한다.

때로는 람다가 메서드 참조보다 간결한 경우도 있다.
예를 들어 다음 코드가 GoshThisClassNameIsHumongous 클래스 안에 있다고 해보자.
```java
service.execute(GoshThisClassNameIsHumongous::action);
```
이를 람다로 대체하면 다음과 같다.
```java
service.execute(() -> action());
```

메서드 참조의 유형은 다섯 가지로, 가장 흔한 유형은 static 메서드를 가리키는 메서드 참조다.

인스턴스 메서드를 참조하는 유형은 bound와 unbound 두 가지가 있다.
bound 인스턴스 메서드 참조는 함수 객체가 받는 인수와 참조되는 메서드가 받는 인수가 같아서 static 메서드 참조와 비슷하다.
unbound 인스턴스 메서드 참조는 함수 객체를 적용하는 시점에 수신 객체를 알려주기 때문에, 수신 객체 전달용 매개변수가 매개변수 목록의 첫 번째에 추가된다.
이는 주로 스트림 파이프라인에서 매핑과 필터 함수에 쓰인다([아이템 45](item45.md)).

마지막으로, 클래스 생성자를 가리키는 메서드 참조와 배열 생성자를 가리키는 메서드 참조가 있다.
생성자 참조는 팩터리 객체로 사용된다.

| Method Ref Type      | Example                     | Lambda Equivalent                   |
|----------------------|-----------------------------|-------------------------------------|
| Static               | `Integer::parseInt`         | `str -> Integer.parseInt(str)`      |
| Bound                | `Instant.now()::isAfter`    | `Instant then = Instant.now();`<br>`t -> then.isAfter(t)` |
| Unbound              | `String::toLowerCase`       | `str -> str.toLowerCase()`          |
| Class Constructor    | `TreeMap<K,V>::new`         | `() -> new TreeMap<K,V>`            |
| Array Constructor    | `int[]::new`                | `len -> new int[len]`               |


> 람다로는 불가능하나 메서드 참조로는 가능한 유일한 예시가 하나 있는데, generic function type 구현이다.
> 함수형 인터페이스의 추상 메서드가 제네릭일 수 있듯이 함수 타입도 제네릭일 수 있다.
> 다음의 인터페이스 계층구조를 생각해보자.
> ```java
> interface G1 {
>     <E extends Exception> Object m() throws E;
> }
> interface G2 {
>     <F extends Exception> String m() throws Exception;
> }
> interface G extends G1, G2 {}
> ```
> 
> 이때 함수형 인터페이스 G를 함수 타입으로 표현하면 다음과 같다.
> ```java
> <F extends Exception> () -> String throws F
> ```
> 이처럼 함수형 인터페이스를 위한 제네릭 함수 타입은 메서드 참조로는 가능하지만, 람다로는 불가능하다.
