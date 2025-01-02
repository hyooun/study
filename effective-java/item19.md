# 아이템 19. 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라
[아이템 18](item18.md)에서는 상속을 염두하지 않고 설계했고 상속할 때의 주의점도 문서화하지 않은 외부 클래스를 상속할 때의 위험성에 대해 언급했다.
여기서 외부 클래스는 프로그래머의 통제 밖에 있어서 언제 변경될지 모르는 클래스를 의미한다.

#### 상속을 고려한 설계와 문서화란?
메서드를 재정의하면 어떤 일이 일어나는지를 정확히 정리하여 문서로 남겨야 한다.
다시 말해, 상속용 클래스는 재정의할 수 있는 메서드들을 내부적으로 어떻게 사용하는지(self-use) 문서로 남겨야 한다.
클래스의 API로 공개된 메서드에서 클래스 자신의 또 다른 메서드를 self-use 할 수도 있다. 
이 때 호출되는 메서드가 override가 가능한 메서드라면 그 사실을 호출하는 메서드의 API 설명에 명시해야 한다는 의미이다.
어떤 순서로 호출하는지, 각각의 호출 결과가 이어지는 처리에 어떤 영향을 주는지 상세하게 적어야 한다.
override 가능한 메서드는 public이나 protected 메서드 중 final이 아닌 모든 메서드를 뜻한다.

override 가능 메서드를 호출할 수 있는 모든 상황을 문서로 남겨야 한다. 
예를 들어 백그라운드 스레드나 static 초기화 과정에서도 호출이 일어날 수 있다.

API 문서의 메서드 설명 끝에 "Implementataion Requirements"로 시작하는 절은 그 메서드의 내부 동작 방식에 대한 설명이다.
`@implSpec` 태그를 붙여주면 자바독 도구가 생성해준다.
아래 코드는 java.util.AbstractCollection의 예시이다.
```java
/**
 * {@inheritDoc}
 *
 * @implSpec
 * This implementation iterates over the collection looking for the
 * specified element.  If it finds the element, it removes the element
 * from the collection using the iterator's remove method.
 *
 * <p>Note that this implementation throws an
 * {@code UnsupportedOperationException} if the iterator returned by this
 * collection's iterator method does not implement the {@code remove}
 * method and this collection contains the specified object.
 *
 * @throws UnsupportedOperationException {@inheritDoc}
 * @throws ClassCastException            {@inheritDoc}
 * @throws NullPointerException          {@inheritDoc}
 */
public boolean remove(Object o) {
    Iterator<E> it = iterator();
    if (o==null) {
        while (it.hasNext()) {
            if (it.next()==null) {
                it.remove();
                return true;
            }
        }
    } else {
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                it.remove();
                return true;
            }
        }
    }
    return false;
}

```
iterator 메서드를 재정의하면 remove 메서드의 동작에 영향이 가고, 구현하지 않으면 예외를 던진다는 것을 명시하고 있다.
[아이템 18](item18.md)에서는 add를 재정의했을 때 addAll에 어떤 영향을 주는지 설명이 없던 것과는 대조적이다.

하지만 이러한 방식은 "좋은 API 문서는 '어떻게'가 아닌 '무엇'을 하는지를 설명해야 한다."라는 격언과 거리가 있다.
이는 상속이 캡슐화를 해치기 때문이며, 안전한 상속을 위해선 상속이 아니였다면 기술하지 않아도 되는 내부 구현 방식에 대해 설명해야 한다.

이처럼 내부 매커니즘을 문서로 작성하는 것에 더해 클래스의 내부 동작 과정 중 중간에 끼어들 수 있는 훅(hook)을 잘 선별하여 protected 메서드 형태로 공개해야 효율적인 하위 클래스를 큰 어려움 없이 만들게 할 수 있다.
드물게는 protected 필드로 공개하는 경우도 있다.
java.util.AbstractList의 removeRange 메서드의 예시이다.
```java
/**
 * Removes from this list all of the elements whose index is between
 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
 * Shifts any succeeding elements to the left (reduces their index).
 * This call shortens the list by {@code (toIndex - fromIndex)} elements.
 * (If {@code toIndex==fromIndex}, this operation has no effect.)
 *
 * <p>This method is called by the {@code clear} operation on this list
 * and its subLists.  Overriding this method to take advantage of
 * the internals of the list implementation can <i>substantially</i>
 * improve the performance of the {@code clear} operation on this list
 * and its subLists.
 *
 * @implSpec
 * This implementation gets a list iterator positioned before
 * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
 * followed by {@code ListIterator.remove} until the entire range has
 * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
 * time, this implementation requires quadratic time.</b>
 *
 * @param fromIndex index of first element to be removed
 * @param toIndex index after last element to be removed
 */
protected void removeRange(int fromIndex, int toIndex) {
    ListIterator<E> it = listIterator(fromIndex);
    for (int i=0, n=toIndex-fromIndex; i<n; i++) {
        it.next();
        it.remove();
    }
}

```
List 구현체의 사용자는 removeRange를 알 필요가 없다.
그럼에도 이 메서드를 제공한 이유는 하위 클래스에서 clear 메서드의 성능을 끌어올릴 수 있게 하기 위함이다.
removeRange가 없다면 하위 클래스에서 clear를 호출하면 제거할 원소 수의 제곱에 비례해 성능이 느려지거나 부분 리스트의 매커니즘을 처음부터 구현해야 했을 것이다.

상속용 클래스를 설계할 때 어떤 메서드를 protected로 노출해야 하는지에 대한 명확한 기준은 없다.
많은 상황을 고려하고 실제 하위 클래스를 만들어 테스트해보는 것이 최선이다.
protected 메서드 하나하나가 내부 구현에 해당하므로 가능한 적어야 하고, 그렇다고 너무 적게 노출해서 상속으로 얻는 이점마저 없애서는 안 된다.
상속용 클래스를 테스트하는 방법은 직접 하위 클래스를 만들어보는 것이 유일하니 상속용으로 설계한 클래스는 배포 전에 반드시 하위 클래스를 만들어 검증해봐야 한다.

상속용 클래스의 생성자는 직/간접적으로 재정의 가능 메서드를 호출해서는 안 된다.
상위 클래스의 생성자가 하위 클래스의 생성자보다 먼저 실행되므로 하위 클래스에서 재정의한 메서드가 하위 클래스의 생성자보다 먼저 호출된다.
이때 그 재정의한 메서드가 하위 클래스의 생성자에서 초기화하는 값에 의존한다면 의도대로 동작하지 않을 것이다.
```java
public class Super {
    // 잘못된 예 - 생성자가 재정의 가능 메서드를 호출한다.
    public Super() {
        overrideMe();
    }

    public void overrideMe() {
    }
}
```
```java
public final class Sub extends Super {
    // 초기화되지 않은 final 필드. 생성자에서 초기화한다.
    private final Instant instant;

    Sub() {
        instant = Instant.now();
    }

    // 재정의 가능 메서드. 상위 클래스의 생성자가 호출한다.
    @Override public void overrideMe() {
        System.out.println(instant);
    }

    public static void main(String[] args) {
        Sub sub = new Sub();
        sub.overrideMe();
    }
}

```
이 프로그램이 instant를 두 번 출력하리라 기대했겠지만 첫 번째는 null을 출력한다.
상위 클래스의 생성자는 하위 클래스의 생성자가 인스턴스 필드를 초기화하기 전에 overrideMe를 호출하기 때문이다.
final 필드의 상태가 정상적이라면 단 하나여야 하지만 이 프로그램에서는 두 가지가 된다.

private, final, static 메서드는 재정의가 불가능하니 생성자에서 안심하고 호출할 수 있다.

Cloneable과 Serializable 인터페이스는 상속용 설계의 어려움을 더해준다.
둘 중 하나라도 구현한 클래스를 상속할 수 있게 설계하는 것은 일반적으로 좋지 않은 생각이다.
clone과 readObject 메서드는 새로운 객체를 만들어 생성자와 비슷한 효과를 낸다.

따라서 상속용 클래스에서 Cloneable과 Serializable을 구현한다면, 따르는 제약도 생성자와 비슷하다는 것을 염두해야 한다.
즉, clone과 readObject 모두 직/간접적으로 재정의 가능 메서드를 호출해서는 안 된다.
readObject의 경우 하위 클래스의 상태가 전부 역직렬화되기 전에 재정의한 메서드부터 호출하게 된다.
clone은 하위 클래스의 clone 메서드가 복제본의 상태를 올바른 상태로 수정하기 전에 재정의한 메서드를 호출한다.
두 상황 모두 프로그램 오작동으로 이어질 것이다.

Serializable을 구현한 상속용 클래스가 readResolve나 writeReplace 메서드를 갖는다면 이 메서드들은 private이 아닌 protected로 선언해야 한다.
private로 선언하면 하위 클래스에서 무시되기 때문이다.
이 역시 상속을 허용하기 위해 내부 구현을 API로 공개하는 경우이다.

상속용으로 설계하지 않은, 일반 구체 클래스의 경우엔 상속을 금지하는 것이 현명하다.
상속을 금지하는 방법은 두 가지가 있다.
> 1. 클래스를 final로 선언한다
> 2. 모든 생성자를 private나 package-private로 선언하고 public static 팩터리를 만들어준다.
전자가 더 쉽고 후자는 유연성에 장점이 있다. ([아이템 17](item17.md) 참고)

Set, Map, List처럼 핵심 기능을 정의한 인터페이스가 있고, 클래스가 그 인터페이스를 구현했다면 상속을 금지해도 개발하는 데 문제가 없을 것이다.
[아이템 18](item18.md)에서 설명한 래퍼 클래스 패턴 역시 상속 대신 사용할 수 있는 좋은 대안이다.

구체 클래스가 표준 인터페이스를 구현하지 않았는데 상속을 금지하면 사용하기에 불편해진다.
이런 클래스라도 상속을 허용해야 하는 경우, 클래스 내부에서는 재정의 가능 메서드를 사용하지 않게 만들고 문서화하는 것이 현명한 방법이다.
이렇게 하면 상속해도 그리 위험하지 않은 클래스를 만들 수 있다.
