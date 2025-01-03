# 아이템 21. 인터페이스는 구현하는 쪽을 생각해 설계하라
자바 8 전에는 기존 구현체를 깨뜨리지 않고 인터페이스에 메서드를 추가할 방법이 없었다.
자바 8부터 기존 인터페이스에 메서드를 추가할 수 있도록 디폴트 메서드가 추가되었지만 추가했을 때 위험이 완전히 사라진 것은 아니다.
디폴트 메서드를 선언하면, 그 인터페이스를 구현한 후 디폴트 메서드를 재정의하지 않은 모든 클래스에서 디폴트 구현이 쓰인다.
이 디폴트 메서드는 구현 클래스에 대한 정보 없이 무작정 '삽입'된다.

자바 8에서는 핵심 컬렉션 인터페이스들에 다수의 디폴트 메서드가 추가되었다. (주로 람다를 사용하기 위해서였다)
자바 라이브러리의 디폴트 메서드는 코드 품질이 높고 범용적이라 대부분의 상황에서 잘 작동한다.
하지만 모든 상황에서 불변식을 해치치 않는 디폴트 메서드를 작성하는 것은 매우 어렵다.

자바 8의 Collection 인터페이스에 추가된 removeIf 메서드를 예로 생각해 보자.
이 메서드는 주어진 boolean 함수(predicate; 프레디키트)가 true를 반환하는 모든 원소를 제거한다. 
```java
    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @implSpec
     * The default implementation traverses all elements of the collection using
     * its {@link #iterator}.  Each matching element is removed using
     * {@link Iterator#remove()}.  If the collection's iterator does not
     * support removal then an {@code UnsupportedOperationException} will be
     * thrown on the first matching element.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *        removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException if the specified filter is null
     * @throws UnsupportedOperationException if elements cannot be removed
     *         from this collection.  Implementations may throw this exception if a
     *         matching element cannot be removed or if, in general, removal is not
     *         supported.
     * @since 1.8
     */
    default boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

```
이 코드보다 범용적으로 구현하기 어렵겠지만, 현존하는 모든 Collection 구현체와 잘 어우러지는 것은 아니다.
대표적인 예시로 org.apache.commons.collections4.collection.SyncronizedCollection이다.
apache commons 라이브러리의 이 클래스는 java.util의 Collection.syncronized Collection 의 static 팩터리 메서드가 반환하는 클래스와 비슷하다.
apache 버전은 컬렉션 대신 클라이언트가 제공한 객체로 락을 거는 기능을 추가로 제공한다.
즉, 모든 메서드에서 주어진 락 객체로 동기화한 후 내부 컬렉션 객체에 기능을 위임하는 래퍼 클래스([아이템 18](item18.md))다.

이 클래스는 지금도 관리되고 있지만, 이 책을 쓰는 시점엔 removeIf를 재정의하지 않고 있다.
이 글을 작성하는 시점엔 아래와 같이 재정의가 되어 있다.
```java
/**
 * @since 4.4
 */
@Override
public boolean removeIf(final Predicate<? super E> filter) {
    synchronized (lock) {
        return decorated().removeIf(filter);
    }
}
```
재정의가 되지 않은 시점에 사용해서 removeIf의 디폴트 구현을 물려받게 된다면, 모든 메서드의 호출을 알아서 동기화해주지 못한다.
removeIf의 구현은 동기화에 관해 아무것도 모르므로 락 객체를 사용할 수 없는 것이다.
따라서 SyncronizedCollection 인스턴스를 여러 스레드가 공유하는 환경에서 한 스레드가 removeIf를 호출하면 ConcurrentModificationException이 발생하거나 다른 예기치 못한 결과로 이어질 수 있다.

자바 플랫폼 라이브러리에서도 이런 문제를 예방하기 위한 조치를 취했다.
구현한 인터페이스의 디폴트 메서드를 재정의하고, 다른 메서드에서는 디폴트 메서드를 호출하기 전에 필요한 작업을 수행하도록 했다.
예를 들어 Collections.syncronizedCollection이 반환하는 package-private 클래스들은 removeIf를 재정의하고, 이를 호출하는 다른 메서드들은 디폴트 구현을 호출하기 전에 동기화를 하도록 했다.

디폴트 메서드는 컴파일에 성공하더라도 기존 구현체에 런타임 에러를 발생시킬 수 있다.
흔한 일은 아니지만 발생하지 않으리라는 보장이 없다.
기존 인터페이스에 디폴트 메서드로 새 메서드를 추가하는 일은 꼭 필요한 경우가 아니라면 피하자.
추가하려는 디폴트 메서드가 기존 구현체들과 충돌하지는 않을지도 고려해야 한다.

새로운 인터페이스를 만드는 경우라면 표준적인 메서드 구현을 제공하는 데 아주 유용하고, 그 인터페이스를 더 쉽게 구현해 활용할 수 있게 해준다([아이템 20](item20.md)).
디폴트 메서드는 인터페이스로부터 메서드를 제거하거나 기존 메서드의 시그니처를 수정하는 용도가 아님을 명심하자.
이런 형태로 인터페이스를 변경하면 기존 클라이언트가 망가진다.

## ✔️ 정리
- 디폴트 메서드가 있더라도 인터페이스를 설계할 때는 세심한 주의가 필요하다.
- 디폴트 메서드로 기존 인터페이스에 새로운 메서드를 추가하면 위험하다.
- 새로운 인터페이스라면 많은 테스트를 통해 릴리스 전에 결함을 찾아내고 수정하는 것이 좋다.
