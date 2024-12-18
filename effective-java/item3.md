# 아이템 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라
싱글턴(singleton)이란 인스턴스를 오직 하나만 생성할 수 있는 클래스를 말한다.
싱글턴의 전형적인 예로는 함수([아이템24](item24.md))와 같은 무상태(stateless) 객체나 설계상 유일해야 하는 시스템 컴포넌트가 있다.</br>
클래스를 싱글턴으로 만들면 이를 사용하는 클라이언트가 테스트하기 어려울 수 있다. 타입을 인터페이스로 정의한 다음 그 인터페이스를 구현해서 만든 싱글턴이 아니라면 인스턴스를 mock 구현으로 대체할 수 없기 때문이다.

## public static final 필드 방식의 싱글턴
```java
public class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    
    public void leaveTheBuilding() { ... }
}

```
private 생성자는 public static final 필드인 Elvis.INSTANCE를 초기화할 때 딱 한번만 호출된다.
public이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다.</br>
권한이 있는 클라이언트는 리플렉션 API([아이템 65](item65.md))인 AccessibleObject.setAccessible을 사용해 private 생성자를 호출할 수 있다. 이러한 공격을 방어하기 위해서는 두번째 객체를 생성하려고 시도했을 때 예외를 던지게 하면 된다.

## 정적 팩터리 방식의 싱글턴
```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    public static Elvis getInstance() { return INSTANCE; }

    public void leaveTheBuilding() { ... }
}

```
Elvis.getInstance는 항상 같은 객체의 참조를 반환하므로 2개 이상의 인스턴스는 만들어지지 않는다. (리플렉션을 통한 예외는 동일하게 적용된다)
첫 번째 방식의 장점은 해당 클래스가 싱글턴임이 API에 드러나고 간결하다는 것이다.</br>
두 번째 방식의 장점은 API를 변경하지 않고 싱글턴이 아니게 변경이 가능하다는 것과 제네릭 싱글턴 팩터리로 변경이 용이하다는 점이다.([아이템 30](item30.md))
또한 정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있다는 점이다. 가령 Elvis::getInstance를 Supplier<Elvis>로 사용할 수 있다. 

둘 중 하나의 방식으로 만든 싱글턴 클래스를 직렬화하려면 단순히 Serializable을 구현한다고 선언하는 것만으로는 부족하다.
모선 인스턴스 필드를 transient로 선언하고 readResolve 메서드를 제공해야 한다.([아이템 89](item89.md)) 
이렇게 하지 않으면 역직렬화할 때마다 새로운 인스턴스가 만들어진다.
```java
private Object readResolve() {
    // '진짜' Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에 맡긴다.
    return INSTANCE;
}
```

## 열거 타입 방식의 싱글턴 - 바람직한 방법
```java
public enum Elvis {
    INSTANCE;
    
    public void leaveTheBuilding() { ... }
}

```
public 필드 방식과 비슷하지만, 더 간결하고 추가 노력 없이 직렬화할 수 있고, 심지어 복잡한 직렬화 상황이나 리플렉션 공격에서도 제2의 인스턴스가 생성되는 것을 완벽하게 막아준다.</br>
보기엔 부자연스러울 수 있지만 대부분의 상황에서는 원소가 하나뿐인 enum이 싱글턴을 만드는 가장 좋은 방법이다.
단, 싱글턴이 Enum 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없다.(열거 타입이 다른 인터페이스를 구현하도록 선언할 수는 있다.)
