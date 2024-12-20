# 아이템 7. 다 쓴 객체 참조를 해제하라
자바는 C, C++과는 다르게 가비지 컬렉터가 메모리를 관리해준다. 하지만 성능을 끌어올리기 위해서는 '메모리 누수'를 줄이기 위한 관리가 필요하다.
```java
import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}

```
위 코드는 스택을 간단히 구현한 것인데 특별한 문제는 없어보인다.
하지만 내부적으로 '메모리 누수'가 발생하고 있어 이 스택을 사용하는 프로그램을 오래 실행하다 보면 가비지 컬렉션의 활동과 메모리 사용량이 점점 늘어나게 되어 점차 성능이 저하될 것이다.
심하면 디스크 페이징이나 OutOfMemoryError가 발생하여 프로그램이 예기치 않게 종료될수도 있다.

이 코드에서는 스택이 커졌다가 줄어들었을 때 스택에서 꺼내진 객체들을 가비지 컬렉터가 회수하지 않는다.
스택이 그 객체들의 `다 쓴 참조(obsolete reference)`를 가지고 있기 때문이다.
elements 배열의 '활성 영역' 밖의 참조들이 여기에 해당하며, 활성 영역은 인덱스가 size보다 작은 원소들이다.
이처럼 가비지 컬렉션을 지원하는 언어에서는 의도치 않게 객체를 살려두는 메모리 누수를 찾기가 까다롭다.
객체 참조 하나를 살려두면 가비지 컬렉터는 그 객체와 그 객체와 연관된 모든 객체를 회수하지 못한다.

이에 대한 해법은 간단한데 해당 참조를 다 사용하고 나서 null 처리(참조 해제)하면 된다.
위의 경우에선 스택에서 꺼내지는 시점인 pop 메서드에서 null 처리를 해준다. 수정한 코드는 아래와 같다.
```java
    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

```
다 쓴 참조를 null 처리했을 때, 만약 null 처리한 참조를 실수로 사용하려고 하면 NullPointerException을 던지기 때문에 디버깅에도 도움이 된다.

> ## ❗ 주의해야할 점
> 모든 객체를 다 사용하자마자 null 처리하는 것은 바람직하지 않다. 객체 참조를 null 처리하는 것은 예외적인 경우여야 한다.
> 다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효 범위(scope) 밖으로 밀어내는 것이다.

그렇다면 이 Stack 클래스에서 메모리 누수가 발생하는 이유가 무엇일까?
스택이 자신의 메모리를 직접 관리하기 때문이다. 이 스택은 객체 자체가 아니라 객체 참조를 담는 elements 배열로 저장소 풀을 만들어 원소들을 관리한다.
배열의 활성 영역과 비활성 영역을 가비지 컬렉터는 구분할 수가 없는 것이 문제이다.
이처럼 자신의 메모리를 직접 관리하는 클래스인 경우에 메모리 누수에 대해 신경써야 한다.

캐시 또한 메모리 누수를 일으키는 주범이다.
객체 참조를 캐시에 넣고 다 쓰고 나서도 캐시를 null처리 하지 않으면 가비지 컬렉터에서 정리하지 않는다.
해법은 캐시 외부에서 key를 참조하는 동안만 entry가 살아있는 캐시인 경우라면 WeakHashMap을 사용해서 만들면 다 쓴 entry가 자동으로 제거된다.

보통은 캐시 entry의 유효 기간을 정확히 정의하기 어려워서 시간이 지날수록 entry의 value를 떨어뜨리는 방식을 흔히 사용하고, 이런 방식에서는 쓰지 않는 entry를 주기적으로 청소해줘야 한다.
Scheduled ThreadPoolExecutor와 같은 백그라운드 스레드를 활용하거나 캐시에 새 entry를 추가할 때 부수 작업으로 수행하는 방법이 있다.</br>
LinkedHashMap은 removeEldestEntry 메서드를 사용하여 후자의 방식으로 처리한다.
이처럼 put을 한 뒤에 afterNodeInsertion 메서드가 실행되어 eldest entry인 경우 삭제하도록 구현되어 있다.
```java
    void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }

```
더 복잡한 캐시를 만들고 싶다면 java.lang.ref 패키지를 직접 활용해야 한다.

listener 혹은 callback 또한 메모리 누수의 주범이다. 클라이언트가 callback을 등록만 하고 해지하지 않는다면 계속해서 쌓인다.
이럴 때 callback을 약한 참조(weak reference)로 저장하면 가비지 컬렉터가 수거해간다. 앞서 언급했듯이 WeakHashMap에 키로 저장하면 된다.
