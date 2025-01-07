# 아이템 29. 이왕이면 제네릭 타입으로 만들라
제네릭 타입을 새로 만드는 건 조금 어렵지만 그만한 값어치를 한다.
다음은 [아이템 7](item7.md)에서 다룬 단순한 스택 코드이다.
```java
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
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}

```
이 클래스는 제네릭 타입이어야 마땅하다.
또한 이 클래스는 제네릭으로 바꾼다고 해도 사용하는 클라이언트에는 아무런 해가 없다.
오히려 지금 코드에서 클라이언트가 스택에서 꺼낸 객체를 형변환해야 하는데, 이때 런타임 오류가 발생할 위험이 있다.

일반 클래스를 제네릭 클래스로 만드는 첫 단계는 클래스 선언에 타입 매개 변수를 추가하는 일이다.
이때 타입 이름으로는 보통 E를 사용한다([아이템 68](item68.md)).
```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public E pop() {
        if (size == 0)
            throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    ... // isEmpty와 ensureCapacity는 같다.
}
```
이렇게 변경한 코드는 다음과 같은 컴파일 에러가 발생한다.
```text
Stack.java:8: generic array creation
        elements = new E[DEFAULT_INITIAL_CAPACITY];
                   ^
```
[아이템 28](item28.md)에서 설명한 것처럼 E와 같은 실체화 불가 타입으로는 배열을 만들 수 없다.
해결책은 두 가지이다.

첫 번째는 제네릭 배열 생성을 금지하는 제약을 대놓고 우회하는 방법이다.
Object 배열을 생성한 다음 제네릭 배열로 형변환해보자.
컴파일은 가능하지만 컴파일러가 경고를 내보낸다.
```text
Stack.java:8: warning: [unchecked] unchecked cast
found: Object[], required: E[]
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
                       ^
```
배열 elements는 private 필드에 저장되고, 클라이언트로 반환되거나 다른 메서드에 전달되는 일이 없다.
push 메서드를 통해 배열에 저장되는 원소의 타입은 항상 E다.
따라서 이 비검사 형변환은 안전하다고 할 수 있다.

비검사 형변환이 안전하다고 판단된다면 범위를 최소로 좁혀 `@SuppressWarnings` 애너테이션으로 해당 경고를 숨긴다([아이템 27](item27.md)).
이 예에서는 생성자가 비검사 배열 생성 말고는 하는 일이 없으니 생성자 전체에 적용해도 된다.
```java
// 배열 elements는 push(E)로 넘어온 E 인스턴스만 남는다.
// 따라서 타입 안전성을 보장하지만, 이 배열의 런타임 타입은 E[]가 아닌 Object[]다.
@SuppressWarnings("unchecked")
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}
```

제네릭 배열 생성 오류를 해결하는 두 번째 방법은 elements 필드의 타입을 E[]에서 Object[]로 바꾸는 것이다.
이렇게 하면 첫 번째와는 다른 오류가 발생한다.
```text
Stack.java:19: incompatible types
found: Object, required: E
        E result = elements[--size];
                           ^
```
배열이 반환한 원소를 E로 형변환하면 오류 대신 경고가 뜬다.
```text
Stack.java:19: warning: [unchecked] unchecked cast
found: Object, required: E
        E result = (E) elements[--size];
                               ^
```
E는 실체화 불가 타입이므로 컴파일러는 런타임에 이뤄지는 형변환이 안전한지 알 수 없다.
이번에도 마찬가지로 직접 증명하고 경고를 숨길 수 있다.
pop 메서드 전체에서 경고를 숨기지 말고, [아이템 27](item27.md)의 조언에 따라 비검사 형변환을 수행하는 할당문에서만 숨겨보자.
```java
// 비검사 경고를 적당히 숨긴다.
public E pop() {
    if (size == 0)
        throw new EmptyStackException();

    // push에서 E 타입만 허용하므로 이 형변환은 안전하다.
    @SuppressWarnings("unchecked") E result = (E) elements[--size];

    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```
첫 번째 방법은 가독성이 더 좋다.
배열의 타입을 E[]로 선언하여 오직 E 타입 인스턴스만 받음이 명확하다.
또한 첫 번째 방식에서는 형변환을 배열 생성 시 한 번만 해주면 되지만, 두 번째 방식에서는 배열에서 원소를 읽을 때마다 해줘야 한다.
따라서 현업에서는 첫 번째 방식을 더 선호한다.

하지만 E가 Object가 아닌 이상 배열의 런타임 타입이 컴파일타임 타입과 달라서 힙 오염(heap pollution; [아이템 32](item32.md))을 일으킨다.
힙 오염을 고려하는 프로그래머는 두 번째 방식을 고수하기도 한다.

지금까지 설명한 Stack의 예시는 [아이템 28](item28.md)의 "배열보다는 리스트를 우선하라"는 조언과 모순돼 보인다.
사실 제네릭 타입 안에서 리스트를 사용하는게 항상 가능하지도, 더 좋은 것도 아니다.
자바가 리스트를 기본 타입으로 제공하지 않으므로 ArrayList 같은 제네릭 타입도 결국 기본 타입인 배열을 사용해 구현해야 한다.
또한 HashMap 같은 제네릭 타입은 성능을 높일 목적으로 배열을 사용하기도 한다.

Stack의 예처럼 대다수 제네릭 타입은 타입 매개변수에 아무런 제약을 두지 않는다.
Stack<Object>, Stack<int[]>, Stack<List<String>>, Stack 등 어떤 참조 타입으로도 Stack을 만들 수 있다.
단 기본 타입은 사용할 수 없다.
Stack<int>나 Stack<double>을 사용하면 오류가 난다.
이는 자바 제네릭 타입 시스템의 근본적인 문제이나, 박싱된 기본 타입([아이템 61](item61.md))을 사용해 우회할 수 있다.

타입 매개변수에 제약을 두는 제네릭도 있다.
java.util.concurrent.DelayQueue는 다음처럼 선언되어 있다.
```java
public class DelayQueue<E extends Delayed> extends AbstractQueue<E>
    implements BlockingQueue<E>
```
타입 매개변수 목록인 `<E extends Delayed>`는 java.util.concurrent.Delayed의 하위 타입만 받는다는 뜻이다.
이렇게 하여 DelayQueue 자신과 DelayQueue를 사용하는 클라이언트는 DelayQueue의 원소에서 형변환 없이 Delayed 클래스의 메서드를 호출할 수 있다.
이러한 타입 매개변수 E를 한정적 타입 매개변수(bounded type parameter)라고 한다.
