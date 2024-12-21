# 아이템 8. finalizer와 cleaner 사용을 피하라
자바는 finalizer와 cleaner라는 두 객체 소멸자를 제공한다.
finalizer와 cleaner 모두 예측이 어렵고 상황에 따라서 위험할 수 있어 일반적으로 사용하지 않는다. 
System.gc나 System.runFinalization 메서드 또한 finalizer와 cleaner가 실행될 가능성을 높여줄 수는 있으나 보장해주진 않는다.
추가로 심각한 성능 문제나 보안 문제로 인해 자바에서 제공하는 두 객체 소멸자는 사용하지 않는 것이 합리적이다.

이는 AutoCloseable을 구현해주고, 클라이언트에서 인스턴스를 다 쓰고 나면 close를 호출하는 것으로 대체가 가능하다.
예외가 발생해도 제대로 종료되도록 try-with-resources를 사용해야 한다. ([아이템 9](item9.md)에서 다룬다)
각 인스턴스는 자신이 닫혔는지 추적하는 것이 좋다. close 메서드에서 이 객체는 더 이상 유효하지 않음을 필드에 기록하고, 다른 메서드에서 이 필드를 검사해서 닫힌 경우엔 IllegalStateException을 던지면 된다.

그럼 finalizer와 cleaner는 어디에 사용할까?
자원의 소유자가 close를 호출하지 않는 경우에 늦게라도 자원을 회수하도록 할 수 있다.
그리고 네이티브 피어(native peer)와 연결된 객체에서 사용할 수 있다.
네이티브 피어란 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체를 말한다.
이는 자바 객체가 아니니 가비지 컬렉터에서 관리하지 않으므로 cleaner나 finalizer를 사용하기에 적합하다.
하지만 이 또한 성능 저하가 크지 않고 자원이 중요하지 않는 경우에만 해당하며, 그렇지 않은 경우엔 close를 사용해야 한다.

```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // 청소가 필요한 자원. 절대 Room을 참조해서는 안 된다!
    private static class State implements Runnable {
        int numJunkPiles;
        
        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }
        
        @Override
        public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }
    
    // 방의 상태, cleanable과 공유한다.
    private final State state;
    
    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;
    
    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}

```
cleaner를 사용하는 Room 클래스 예제이다. 
static으로 선언된 중첩 클래스인 State는 cleaner가 방을 청소할 때 수거할 자원들을 담고 있다.
State는 Runnable을 구현하고 run 메서드는 cleanable에 의해 딱 한 번만 호출된다.
이 cleanable 객체는 Room 생성자에서 cleaner에 Room과 State를 등록할 때 얻는다.

run 메서드는 Room이 close를 호출할 때와 close 메서드에서 Cleanable의 clean을 호출할 때 호출된다.
State 인스턴스는 Room 인스턴스를 참조할 경우 순환참조가 생겨 가비지 컬렉터에서 회수하지 못하기 때문에 절대로 하면 안된다.
static이 아닌 클래스는 자동으로 바깥 객체의 참조를 갖게 되기에 State를 static으로 선언한 것이다. 
이와 비슷하게 람다 역시 바깥 객체의 참조를 갖기 쉬우니 사용하지 않는 것이 좋다.
여기서 Room의 cleaner는 단지 안전망으로만 쓰였다.
클라이언트가 모든 Room 생성을 try-with-resources 블록으로 감쌌다면 자동 청소는 필요하지 않다.

```java
public class Adult {
    public static void main(String[] args) {
        try (Room myRoom = new Room(7)) {
            System.out.println("안녕~");
        }
    }
}

```
이 Adult 프로그램은 "안녕~"을 출력한 후 "방 청소"를 출력한다.
```java
public class Teenager {
    public static void main(String[] args) {
        new Room(99);
        System.out.println("아무렴");
    }
}

```
하지만 이 Teenager는 "아무렴"만 출력한다. cleaner가 작동하지 않은 것이고 예측할 수 없다는 것이 이런 상황이다. 
> cleaner의 명세에는 "System.exit을 호출할 때의 cleaner 동작은 구현하기 나름이다. clean이 이뤄질지는 보장하지 않는다." 라고 적혀있다.

### ✔️ 한 줄 요약
cleaner는 안전망 역할이나 중요하지 않은 네이티브 자원 회수에만 사용하자. 물론 이런 경우라도 불확실성과 성능 저하에 주의해야 한다. 
