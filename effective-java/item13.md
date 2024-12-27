# 아이템 13. clone 재정의는 주의해서 진행하라
Cloneable은 복제해도 되는 클래스임을 명시하는 용도의 mixin interface([아이템 20](item20.md))지만, 아쉽게도 의도한 목적을 제대로 이루지 못했다.
가장 큰 문제는 clone 메서드가 선언된 곳이 Cloneable이 아닌 Object이고, 그마저도 protected라는 데 있다.</br>
그래서 Cloneable을 구현하는 것만으로는 외부 객체에서 clone 메서드를 호출할 수 없다.
리플렉션([아이템 65](item65.md))을 사용하면 가능하지만, 해당 객체가 접근이 허용된 clone 메서드를 제공한다는 보장이 없기에 100%는 아니다.

하지만 이를 포함한 여러 문제점에도 불구하고 Cloneable 방식은 널리 쓰이고 있어서 잘 알아두는 것이 좋다.
이번 아이템에서는 clone 메서드를 잘 동작하게끔 해주는 구현 방법과 언제 그렇게 해야 하는지를 알려주고, 가능한 다른 선택지를 제시한다.

Cloneable 인터페이스는 Object의 protected 메서드인 clone의 동작 방식을 결정한다.
Cloneable을 구현한 클래스의 인스턴스에서 clone을 호출하면 그 객체의 필드들을 하나하나 복사한 객체를 반환하며, 그렇지 않은 클래스의 인스턴스에서 호출하면 CloneNotSupportException을 던진다.
이는 인터페이스를 상당히 이례적으로 사용한 예이니 따라 하지는 말자. </br>
인터페이스를 구현한다는 것은 일반적으로 해당 클래스가 그 인터페이스에서 정의한 기능을 제공한다고 선언하는 행위이다.
그런데 Cloneable의 경우는 상위 클래스에 정의된 protected 메서드의 동작 방식을 변경한다.

명세에서 다루진 않지만 실무에서 Cloneable을 구현한 클래스는 clone 메서드를 public으로 제공하며, 사용자는 당연히 복제가 제대로 이뤄지리라 기대한다.
이 기대를 만족시키려면 그 클래스와 모든 상위 클래스는 복잡하고, 강제할 수 없고, 허술하게 기술된 프로토콜을 지켜야만 하는데, 그 결과로 깨지기 쉽고 위험하고, 모순적인 매커니즘이 탄생한다.
생성자를 호출하지 않고도 객체를 생성할 수 있게 되는 것이다.
clone 메서드의 일반 규약은 허술하다. Object 명세엔 다음과 같이 적혀 있다.
> 이 객체의 복사본을 생성해 반환한다. '복사'의 정확한 뜻은 그 객체를 구현한 클래스에 따라 다를 수 있다.
> 일반적인 의도는 다음과 같다. 어떤 객체 x에 대해 다음 식은 참이다.
>
> `x.clone() != x`
>
> 또한 다음 식도 참이다.
>
> `x.clone().getClass() == x.getClass()`
> 하지만 이상의 요구를 반드시 만족해야 하는 것은 아니다.
> 한편 다음 식도 일반적으로 참이지만, 역시 필수는 아니다.
>
> `x.clone().equals(x)`
>
> 관례상, 이 메서드가 반환하는 객체는 super.clone을 호출해 얻어야 한다. 이 클래스와 (Object를 제외한) 모든 상위 클래스가 이 관례를 따른다면 다음 식은 참이다.
>
> `x.clone().getClass() == x.getClass()`
>
> 관례상, 반환된 객체와 원본 객체는 독립적이어야 한다. 이를 만족하려면 super.getClone으로 얻은 객체의 필드 중 하나 시앙을 반환 전에 수정해야 할 수도 있다.

강제성이 없다는 점만 빼면 생성자 연쇄(constructor chaining)와 살짝 비슷한 매커니즘이다.
즉 clone 메서드가 super.clone이 아닌, 생성자를 호출해 얻은 인스턴스를 반환해도 컴파일러는 알지 못한다.
하지만 이 클래스의 하위 클래스에서 super.clone을 호출한다면 잘못된 클래스의 객체가 만들어져, 결국 하위 클래스의 clone 메서드가 제대로 동작하지 않게 된다.</br>
clone을 재정의한 클래스가 final이라면 걱정해야 할 하위 클래스가 없으니 이 관례는 무시해도 안전하다. 
하지만 final 클래스의 clone 메서드가 super.clone을 호출하지 않는다면 Cloneable을 구현할 이유도 없다.
Object의 clone 구현의 동작 방식에 기댈 필요가 없기 때문이다. 

제대로 동작하는 clone 메서드를 가진 상위 클래스를 상속해 Cloneable을 구현하고 싶다고 해보자.
먼저 super.clone을 호출한다. 그렇게 얻은 객체는 원본 객체의 완벽한 복사본일 것이다.
클래스에 정의된 모든 필드는 원본 필드와 똑같은 값을 갖는다. </br>
모든 필드가 기본 타입이거나 불변 객체를 참조한다면 이 객체는 완벽히 우리가 원하는 상태라 더 손볼 것이 없다.
[아이템 10](item10.md)에서의 PhoneNumber 클래스가 여기 해당된다.
그런데 쓸데없는 복사를 지양한다는 관점에서 보면 불변 클래스는 굳이 clone 메서드를 제공하지 않는 게 좋다.
이 점을 고려해 PhoneNumber의 clone 메서드는 다음처럼 구현할 수 있다.
```java
@Override public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportException e) {
        throw new AssertionError();  // 일어날 수 없는 일이다.
    }
}
```
이 메서드가 동작하게 하려면 PhoneNumber 클래스 선언에 Cloneable을 구현한다고 추가해야 한다.
Object의 clone 메서드는 Object를 반환하지만 PhoneNumber의 clone 메서드는 PhoneNumber를 반환하게 했다.
자바가 공변 반환 타이핑(covariant return typing)을 지원하니 이렇게 하는 것이 가능하고 권장하는 방식이기도 하다.

달리 말해서, 재정의한 메서드의 반환 타입은 상위 클래스의 메서드가 반환하는 타입의 하위 타입일 수 있다.
이 방식으로 클라이언트가 형변환하지 않을 수 있도록 해주자.
이를 위해 앞 코드에서는 super.clone에서 얻은 객체를 반환하기 전에 PhoneNumber로 형변환하였다(절대 실패하지 않는다).

super.clone 호출을 try-catch 블록으로 감싼 이유는 Object의 clone 메서드가 검사 예외(checked exception)인 CloneNotSupportedException을 던지도록 선언되었기 때문이다. 
PhoneNumber가 Cloneable을 구현하기 때문에 super.clone은 항상 성공한다.
이 코드는 CloneNotSupportedException이 사실은 비검사 예외(unchecked exception)여야 했다는 것을 의미한다.

위와 같은 구현은 클래스가 가변 객체를 참조하는 순간 재앙으로 돌변한다.
[아이템 7](item7.md)에서 구현한 Stack 클래스를 예로 들어보자.
```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = element[--size];
        element[size] = null; // 다 쓴 참조 해제
        return result;
    }

    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}

```
이 클래스를 복제할 수 있도록 만들어보자.
clone 메서드가 단순히 super.clone의 결과를 그대로 반환한다면 어떻게 될까?
반환된 Stack 인스턴스의 size 필드는 올바른 값을 갖겠지만, element 필드는 원본 Stack 인스턴스와 똑같은 배열을 참조할 것이다.</br>
원본이나 복제본 중 하나를 수정하면 다른 하나도 수정되어 불변식을 해치게 된다.
따라서 프로그램이 이상하게 동작하거나 NullPointerException이 발생할 것이다.


Stack 클래스의 하나뿐인 생성자를 호출해서 사용하면 이러한 상황은 절대 발생하지 않는다.
clone 메서드는 사실상 생성자와 같은 효과를 내므로 clone은 원본 객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야 한다.
Stack의 clone이 제대로 동작하기 위해서는 스택 내부 정보를 복사해야 하는데, 가장 쉬운 방법은 elements 배열의 clone을 재귀적으로 호출하는 것이다.
```java
@Override public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone();
        return result;
    } catch (CloneNotSupportException e) {
        throw new AssertionError();
    }
}
```
elements.clone의 결과를 Object로 형변환 할 필요는 없다.
배열의 clone은 런타임과 컴파일타입 타입 모두가 원본 배열과 똑같은 배열을 반환한다.
따라서 배열을 복제할 때는 배열의 clone 메서드를 사용하라고 권장한다.
사실 배열은 clone 기능을 제대로 사용하고 있는 유일한 예라고 할 수 있다.

element 필드가 final이었다면 앞서의 방식은 작동하지 않는다.
final 필드에서는 새로운 값을 할당할 수 없기 때문이다.
이는 직렬화처럼 근본적인 문제로 Cloneable 아키텍처는 '가변 객체를 참조하는 필드는 final로 선언하라'는 일반 용법과 충돌한다.
그래서 복제할 수 있는 클래스를 만들기 위해 일부 필드에서 final을 제거해야 할 수도 있다.

clone을 재귀적으로 호출해도 해결되지 않는 문제도 있다.
해시테이블용 clone을 한번 생각해보자.
해시테이블 내부는 버킷들의 배열이고, 각 버킷은 키-값 쌍을 담는 연결 리스트의 첫 번째 엔트리를 참조한다.
그리고 성능을 위해 java.util.LinkedList 대신 직접 구현한 경량 연결 리스트를 사용하자.
```java
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    
    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    // 나머지 코드는 생략
}
```
그리고 Stack에서처럼 단순히 버킷 배열의 clone을 재귀적으로 호출해보자.
```java
@Override public HashTable clone() {
    try {
        HashTable result = (HashTable) super.clone();
        result.buckets = buckets.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

복제본은 자신만의 배열 버킷을 갖지만, 이 배열은 원본과 같은 연결 리스트를 참조하여 원본과 복제본 모두 예상치 못한 동작이 발생할 수 있다.
이를 해결하려면 각 버킷을 구성하는 연결 리스트를 복사해야 한다.
```java
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Entry deepCopy() {
            return new Entry(key, value,
                    next == null ? null : next.deepCopy());
        }
    }
    
    @Override public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = new Entry[buckets.length];
            for (int i = 0; i < buckets.length; i++) {
                if (buckets[i] != null) {
                    result.buckets[i] = buckets[i].deepCopy();
                }
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    // 나머지 코드는 생략
}

```
private 클래스인 HashTable.Entry는 deep copy를 지원하도록 보강되었다.
HashTable의 clone 메서드는 원본과 같은 크기의 새로운 버킷 배열을 할당하고 원래 배킷 배열을 순회하며 null이 아닌 각 버킷에 대해 deep copy를 수행한다.
이때 Entry의 deepCopy 메서드는 자신이 가리키는 연결 리스트 전체를 복사하기 위해 자신을 재귀적으로 호출한다.

이 방법은 간단하고 버킷이 길지 않다면 잘 작동한다.
하지만 재귀 호출 때문에 리스트의 원소 수만큼 스택 프레임을 소비하여 리스트가 긴 경우 스택 오버플로우를 발생시킬 수 있어 연결 리스트를 복제하는 방법으로는 적합하지 않다.
deepCopy를 재귀 호출 대신 반복자를 써서 순회하는 방향으로 수정하면 된다.
```java
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for (Entry p = result; p.next != null; p = p.next)
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    return result;
}
```

복잡한 가변 객체를 복제하는 마지막 방법은, 먼저 super.clone을 호출하여 얻은 객체의 모든 필드를 초기 상태로 설정한다.
다음에 원본 객체의 상태를 다시 생성하는 고수준 메서드들을 호출한다.
HashTable이라면, bucket의 필드를 새로운 버킷 배열로 초기화한 다음 원본 테이블에 담긴 모든 키-값 쌍 각각에 대해 복제본 테이블의 put(key, value) 메서드를 호출해 둘의 내용을 똑같게 해주면 된다.</br>
이처럼 고수준 API를 활용해 복제하면 보통은 간단해지지만 아무래도 저수준에서 바로 처리하는 것보다는 느리다.
또한 Cloneable 아키텍처의 기초가 되는 필드 단위 복사가 아니기 때문에 전체 Cloneable 아키텍처와는 어울리지 않기도 하다.

생성자에서는 재정의될 수 있는 메서드를 호출하지 않아야 하는데([아이템 19](item19.md)) clone 메서드도 마찬가지다.
만약 clone이 하위 클래스에서 재정의한 메서드를 호출하면, 하위 클래스는 복제 과정에서 원본과 복제본의 상태가 달라질 가능성이 크다.
따라서 앞 문단에서 언급한 put(key, value) 메서드는 final이거나 private이어야 한다.

Object의 clone 메서드는 CloneNotSupportException을 던진다고 선언했지만 재정의한 메서드는 그렇지 않다.
public인 clone 메서드에서는 throws 절을 없애야 한다.
검사 예외를 던지지 않아야 그 메서드를 사용하기 편하기 때문이다([아이템 71](item71.md)).

상속해서 쓰기 위한 클래스 설계 방식 두 가지([아이템 19](item19.md)) 중 어느 쪽에서든, 상속용 클래스에서는 Cloneable을 구현해서는 안 된다.
또한 Cloneable을 구현한 스레드 안전 클래스를 작성할 때는 clone 메서드 역시 적절히 동기화해줘야 한다([아이템 78](item78.md)).
Object의 clone 메서드는 동기화를 고려하지 않았다.
그러니 super.clone 호출 외에 다른 작업이 없더라도 clone을 재정의하고 동기화해줘야 한다.

정리하자면 Cloneable을 구현하는 모든 클래스는 clone을 재정의해야 하고, 접근 제어자는 public으로, 반환 타입은 클래스 자신으로 변경해야 한다.
이 메서드는 super.clone을 호출한 후 필요한 필드를 전부 적절히 수정한다.
객체 내부에 있는 모든 가변 객체를 복사하고, 복제본이 가진 객체 참조가 복사된 객체를 가리키게 해야 한다.

Cloneable/clone과 비슷하게 복사 생성자와 복사 팩터리라는 더 나은 객체 복사 방식도 존재한다.
복자 생성자는 단순히 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자를 말한다.
```java
public Yum(Yum yum) { ... };
```
복사 팩터리 생성자는 복사 생성자를 모방한 static 팩터리([아이템 1](item1.md))이다.
```java
public static Yum newInstance(Yum yum) { ... };
```
이 방식은 생성자를 사용하지 않는 위험한 항식이 아니기 때문에 Clonable/clone 방식보다 나은 면이 많다.
복사 생성자와 복사 팩터리는 해당 클래스가 구현한 '인터페이스' 타입의 인스턴스를 인수로 받을 수 있다.
이들의 더 정확한 이름은 `변환 생성자(conversion constructor)`와 `변환 팩터리(conversion factory)`이다.


이들을 이용하면 클라이언트는 원본의 구현 타입에 얽매이지 않고 복제본의 타입을 직접 선택할 수 있다.
예를 들어 HashSet의 객체 s를 TreeSet 타입으로 복제가 가능하다.
clone으로는 불가능한 이 기능을 변환 생성자로는 간단하게 new TreeSet<>(s)로 처리할 수 있다.
