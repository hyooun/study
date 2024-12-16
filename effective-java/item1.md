# 아이템 1. 생성자 대신 정적 팩터리 메서드를 고려하라

클라이언트가 클래스의 인스턴스를 얻는 전통적인 수단은 public 생성자이지만 생성자와 별개로 정적 팩터리 메서드(static factory method)를 사용하면 여러 장점이 있다. 
하지만 단점도 있기 때문에 잘 고려해서 사용해야 한다.
```java
public static Boolean valueOf(boolean b) {
    return b ? Boolean.TRUE : Boolean.FALSE;
}
```
### 장점 1 : 이름을 가질 수 있다.
- 생성자에 넘기는 매개변수와 생성자 자체만으로는 반환될 객체의 특성을 제대로 설명하지 못하는 반면, 정적 팩터리 메서드는 이름만 잘 지으면 객체의 특성을 쉽게 묘사할 수 있다.
- BigInteger(int, int, Random)과 정적 팩터리 메서드인 BigInteger.probablePrime 중 '값이 소수인 BigInteger를 반환한다'는 의미를 잘 전달하는 쪽은 후자이다.
- 같은 생성자에서 매개변수의 순서를 바꾸어 다양하게 사용할 수는 있지만 혼동을 야기할 수 있다.

### 장점 2 : 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.
- 불변 클래스(immutable calss; [아이템 17](item17.md))는 이와 같은 방법으로 인스턴스를 미리 만들어 놓거나 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.
- Boolean.valueOf(boolean b) 메서드는 객체를 아예 생성하지 않는다.

  ```java
  @jdk.internal.ValueBased
  public final class Boolean implements java.io.Serializable,
                                        Comparable<Boolean>, Constable
  {
      public static final Boolean TRUE = new Boolean(true);
  
      public static final Boolean FALSE = new Boolean(false);
  
      @SuppressWarnings("unchecked")
      public static final Class<Boolean> TYPE = (Class<Boolean>) Class.getPrimitiveClass("boolean");
  
      private final boolean value;
  
      @java.io.Serial
      private static final long serialVersionUID = -3665804199014368530L;
  }
  ```
- 플라이웨이트 패턴(Flyweight pattern)도 이와 비슷한 기법이라고 할 수 있다.
- 이런 클래스를 인스턴스 통제(instance-controlled)클래스라고 하며 싱글턴(singleton; [아이템 3](item3.md)), 인스턴스화 불가(noninstantiable; [아이템 4](item4.md))로 만들 수 있다.
- 불변 클래스(immutable calss; [아이템 17](item17.md))에서는 동치인 인스턴스가 단 한개임을 보장할 수 있다. (a == b일 때만 a.equals(b)가 성립)

### 장점 3 : 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다. 
- java.util.Collections는 인스턴스화 불가 클래스이고 정적 팩터리 메서드를 통해 하위 타입의 인스턴스를 반환한다.

  ```java
  public class Collections {
      // Suppresses default constructor, ensuring non-instantiability.
      private Collections() {
      }
  
      public static <T extends Comparable<? super T>> void sort(List<T> list) {
          list.sort(null);
      }
  
      public static <T>
      int binarySearch(List<? extends Comparable<? super T>> list, T key) {
          if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
              return Collections.indexedBinarySearch(list, key);
          else
              return Collections.iteratorBinarySearch(list, key);
      }
  }
  ```
- 컬렉션 프레임워크는 45개의 유틸리티 구현체를 제공하는데 전부 non-public으로 API의 외견을 줄였으며, 프로그래머가 이를 사용하기 위해 익혀야 할 개념의 수와 난이도를 낮췄다.
- 정적 팩터리 메서드를 사용하는 경우 그 구현체가 아닌 인터페이스 타입만을 다루게 되어 편하다.
- 자바 8부터 인터페이스가 static 메서드를 가질 수 있지만 public만 허용된다.

### 장점 4 : 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.
- 반환 타입이 하위 타입이기만 하면 어떤 클래스의 객체를 반환하든 상관없다.
- EnumSet 클래스는 public 생성자 없이 오직 정적 팩터리 메서드만을 제공하는데, OpenJDK에서는 원소에 수에 따라서 다른 구현체의 인스턴스를 반환한다.

  ```java
  public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        Enum<?>[] universe = getUniverse(elementType);
        if (universe == null)
            throw new ClassCastException(elementType + " not an enum");

        if (universe.length <= 64)
            return new RegularEnumSet<>(elementType, universe);
        else
            return new JumboEnumSet<>(elementType, universe);
    }
  ```

### 장점 5 : 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.
- 이런 유연함은 서비스 제공자 프레임워크(service provider framework)를 만드는 근간이 되며, 대표적인 예시로 JDBC(Java Database Connectivity)가 있다.
- 서비스 제공자 프레임워크는 구현체의 동작을 정의하는 서비스 인터페이스, 제공자가 구현체를 등록할 때 사용하는 제공자 등록 API, 클라이언트가 서비스의 인스턴스를 얻을 때 사용하는 서비스 접근 API 총 3개의 핵심 컴포넌트로 구성된다. 서비스 인터페이스의 인스턴스를 제공하는 서비스 제공자 인터페이스를 만들 수도 있는데, 없는 경우에는 리플랙션을 사용해서 구현체를 만들어 준다.
- JDBC에서는 Connection이 서비스 인터페이스 역할을, DriverManager.registerDriver가 제공자 등록 API 역할을, DiverManager.getConnection이 서비스 접근 API 역할을, Driver가 서비스 제공자 인터페이스 역할을 수행한다.

### 단점 1 : 상속을 하려면 public이나 protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.
- 따라서 Collection 프레임워크에서 제공하는 구현체들은 상속할 수 없다. 오히려 이 제약은 상속보다 컴포지션 사용([아이템 18](item18.md))하도록 유도하고 불변 타입([아이템 17](item17.md))으로 만들려면 이 제약을 지켜야 한다는 점에서 장점으로 받아들일 수 있다.

### 단점 2 : 정적 팩터리 메서드는 프로그래머가 찾기 어렵다.
- 생성자처럼 API 설명에 명확히 드러나지 않아 사용자는 정적 팩터리 메서드 방식 클래스를 인스턴스화할 방법을 알아내야 한다.
- API 문서를 잘 써놓고 메서드 이름도 널리 알려진 규약을 따라 짓는 식으로 보완할 수 있다.

### 정적 팩터리 메서드에서 흔히 쓰는 이름들
- from : 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드
```java
Date d = Date.from(instant);
```
- of : 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드
```java
Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
```
- valueOf : from과 of의 더 자세한 버전
```java
BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
```
- instance 혹은 getInstance : (매개변수를 받는다면) 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.
```java
StackWalker luke = StackWalker.getInstance(options);
```
- create 혹은 newInstance : instance 혹은 getInstance와 같지만, 매번 새로운 인스턴스를 생성해 반환함을 보장한다.
```java
Object Array = Array.newInstance(classObject, arrayLen);
```
- getType : getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. `Type`은 팩터리 메서드가 반환할 객체의 타입이다.
```java
FileStore fs = Files.getFileStore(path);
```
- newType : newInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 쓴다. `Type`은 팩터리 메서드가 반환할 객체의 타입이다.
```java
BufferedReader br = Files.newBufferedReader(path);
```
- type : getType과 newType의 간결한 버전
```java
List<Complaint> litany = Collections.list(legacyLitany);
```
