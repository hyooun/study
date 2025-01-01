# 아이템 18. 상속보다는 컴포지션을 사용하라
상속은 코드를 재사용하는 강력한 수단이지만, 항상 최선은 아니다.
같은 패키지 안에서 상속하는 것은 안전하지만, 패키지 경계를 넘어 다른 패키지의 구체 클래스를 상속하는 것은 위험하다.
여기서 '상속'은 클래스가 다른 클래스를 확장하는 구현 상속을 말한다.
이번 아이템에서 클래스가 인터페이스를 구현하거나 인터페이스가 다른 인터페이스를 확장하는 상속과는 무관하다.

메서드 호출과 달리 상속은 캡슐화를 깨뜨린다.
상위 클래스가 어떻게 구현되느냐에 따라 하위 클래스 동작에 이상이 생길 수 있다.

HashSet을 사용하는 프로그램이 있다고 가정해보자.
성능을 높이려면 이 HashSet은 처음 생성된 이후 원소가 몇 개 더해졌는지 알 수 있어야 한다.
이는 HashSet의 크기와는 다른 개념이다.
현재 크기는 원소가 제거되면 줄어든다. 
```java
public class InstrumentedHashSet<E> extends HashSet<E> {
    // 추가된 원소의 수
    private int addCount = 0;
    
    public InstrumentedHashSet() {
    }
    
    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }
    
    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }
    
    public int getAddCount() {
        return addCount;
    }
}

```
이 클래스는 잘 구현된 것처럼 보이지만 제대로 작동하지 않는다.
이 클래스의 인스턴스에 addAll 메서드로 원소 3개를 더했다고 해보자.
```java
InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
s.addAll(List.of("틱", "탁탁", "펑"));
```
이제 getAddCount를 호출하면 3을 반환할 것이라 기대하겠지만, 실제로는 6을 반환한다.
그 원인은 HashSet의 addAll 메서드가 add 메서드를 사용해 구현하기 때문이다.
이런 내부 구현 방식이 HashSet 문서에는 쓰여 있지 않다.

이 경우 하위 클래스에서 addAll 메서드를 재정의하지 않으면 문제를 고칠 수 있다.
하지만 제대로 HashSet의 addAll이 add 메서드를 이용해 구현했음을 가정한 해법이라는 한계가 있다.
이처럼 자신의 다른 부분을 사용하는 self-use 여부는 해당 클래스의 내부 구현 방식에 해당하며, 다음 릴리스에도 유지될지는 알 수 없다.

addAll 메서드를 주어진 컬렉션을 순회하며 원소 하나당 add 메서드를 한 번만 호출하는 방식으로 재정의할 수 있다.
하지만 여전히 상위 클래스의 메서드 동작을 다시 구현해야 하며, 오류를 내거나 성능을 떨어뜨릴 수 있다는 점은 문제이다.
또한 하위 클래스에서는 접근할 수 없는 private 필드를 써야 하는 상황이라면 이러한 방식은 불가능하다.

다음 릴리스에서 상위 클래스에 새로운 메서드가 추가되는 경우도 문제가 된다.
보안 때문에 컬렉션에 추가된 모든 원소가 특정 조건을 만족해야만 하는 프로그램을 생각해보자.
그 컬렉션을 상속하여 원소를 추가하는 모든 메서드를 재정의해 필요한 조건을 먼저 검사하게끔 하면 된다.
만약 상위 클래스에 새로운 메서드가 추가되면, 하위 클래스에서 재정의하지 못한 '허용되지 않은' 원소가 추가될 수 있다.
실제로 HashTable과 Vector를 컬렉션 프레임워크에 포함시키자 보안 이슈가 발생했다.

이 문제들은 모두 메서드 재정의가 원인이었다.
클래스를 확장하더라도 재정의하는 대신 새로운 메서드를 추가하면 이보다는 안전하지만, 위험이 전혀 없는 것은 아니다.
상위 클래스에 새 메서드가 추가됐는데, 하위 클래스에 추가한 메서드와 시그니처가 같고 반환 타입이 다르다면 컴파일조차 되지 않는다.
반환 타입마저 같다면 새 메서드를 재정의한 꼴이니 앞서의 문제와 같은 상황이 발생한다.

## 💡 해결책 - 컴포지션
기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 private 필드로 기존 클래스의 인스턴스를 참조하게 하자.
기존 클래스가 새로운 클래스의 구성요소로 쓰인다는 뜻에서 이러한 설계를 `컴포지션(composition; 구성)`이라 한다.
새 클래스의 인스턴스 메서드들은 private 필드로 참조하는 기존 클래스에 대응하는 메서드를 호출해 결과를 반환한다.

이 방식을 전달(forwarding)이라 하며, 새 클래스의 메서드들을 전달 메서드(forwarding method)라 부른다.
그 결과 새로운 클래스는 기존 클래스의 내부 구현 방식에 영향을 받지 않으며, 기존 클래스에 새로운 메서드가 추가돼도 영향을 받지 않는다.
다음은 상속 대신 컴포지션을 사용한 InstrumentedSet이다. 
```java
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}

```

```java
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s; }

    public void clear()                { s.clear(); }
    public boolean contains(Object o)  { return s.contains(o); }
    public boolean isEmpty()           { return s.isEmpty(); }
    public int size()                  { return s.size(); }
    public Iterator<E> iterator()      { return s.iterator(); }
    public boolean add(E e)            { return s.add(e); }
    public boolean remove(Object o)    { return s.remove(o); }
    public boolean containsAll(Collection<?> c) 
                                       { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c) 
                                    { return s.addAll(c); }
    public boolean removeAll(Collection<?> c) 
                                    { return s.removeAll(c); }
    public boolean retainAll(Collection<?> c) 
                                    { return s.retainAll(c); }
    public Object[] toArray()          { return s.toArray(); }
    public <T> T[] toArray(T[] a)      { return s.toArray(a); }
    @Override public boolean equals(Object o) 
                                       { return s.equals(o); }

    @Override public int hashCode()    { return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}

```
InstrumentedSet은 HashSet의 모든 가능을 정의한 Set 인터페이스를 활용해 설계되어 견고하고 유연하다.
구체적으로는 Set 인터페이스를 구현했고, Set의 인스턴스를 인수로 받는 생성자를 하나 제공한다. 
임의의 Set에 계측 기능을 더해 새로운 Set을 만드는 것이 이 클래스의 핵심이다.

상속 방식은 구체 클래스 각각을 따로 확장해야 하며, 지원하고 싶은 상위 클래스의 생성자 각각에 대응하는 생성자를 별도로 정의해야 한다.
하지만 컴포지션 방식은 한 번만 구현해두면 어떠한 Set 구현체에서도 사용이 가능하며, 기존 생성자들과도 함께 사용할 수 있다.

```java
Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<E> s = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));
```
InstrumentedSet을 이용하면 대상 Set 인스턴스를 특정 조건에서만 임시로 계측할 수 있다.
```java
static void walk(Set<Dog> dogs) {
    InstrumentedSet<Dog> iDogs = new InstrumentedSet<>(dogs);
    ... // 이 메서드에서는 dogs 대신 iDogs를 사용한다.
}
```
다른 Set 인스턴스를 감싸고(wrap) 있다는 뜻에서 InstrumentedSet 같은 클래스를 `래퍼 클래스(wrapper class)`라고 하며, 다른 Set에 계측 기능을 덧씌운다는 뜻에서 `데코레이터 패턴(Decorator pattern)`이라고 한다.
래퍼 클래스는 단점이 거의 없다. 콜백(callback) 프레임워크와는 어울리지 않는다는 점만 주의하면 된다.
콜백 프레임워크에서는 자기 자신의 참조를 다른 객체에 넘겨서 다음 호출(콜백) 때 사용하도록 한다.
내부 객체는 자신을 감싸고 있는 래퍼의 존재를 모르니 대신 자신(this)의 참조를 넘기고 콜백 때는 래퍼 아닌 내부 객체를 호출하게 된다.

상속은 반드시 하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 쓰여야 한다.
클래스 B가 클래스 A와 is-a 관계일 때만 클래스 A를 상속해야 한다.
클래스 B가 클래스 A를 상속하려고 할 때 "클래스 B가 정말 A인가?"에 대해 확실하지 않다면 B는 A를 상속해서는 안 된다.

이는 컴포지션을 써야 하는 상황이고 이러한 상황에서 상속을 사용하는 건 내부 구현을 불필요하게 노출하는 꼴이다.
그 결과 API가 내부 구현에 묶이고 클래스의 성능이 제한된다. 
또한 클라이언트가 노출된 내부에 직접 접근이 가능해지고 사용자의 혼란을 야기한다.

예를 들어 Properties의 인스턴스인 p가 있을 때, `p.getProperty(key)`와 `p.get(key)`의 결과가 다를 수 있다.
전자가 Properties의 기본 동작인 데 반해, 후자는 Properties의 상위 클래스인 HashTable로부터 물려받은 메서드이기 때문이다.
가장 심각한 문제는 클라이언트에서 상위 클래스를 직접 수정하여 하위 클래스의 불변식을 해칠 수 있다는 것이다. 
Properties는 키와 값으로 문자열만 허용하도록 설계하려 했으나, 상위 클래스인 HashTable의 메서드를 직접 호출하면 이 불변식을 깨버릴 수 있다.
불변식이 깨지고 나면 load와 store같은 Properties API는 사용이 불가능하다.

## ❗ 명심
컴포지션 대신 상속을 사용하기로 결정하기 전에 확장하려는 클래스의 API에 아무런 결함이 없는지, 결함이 있다면 전파돼도 괜찮은지 고려해야 한다.
컴포지션으로는 결함을 숨기는 새로운 API를 설계할 수 있지만, 상속은 상위 클래스 API의 결함까지도 그대로 적용된다.
