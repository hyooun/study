# 아이템 20. 추상 클래스보다는 인터페이스를 우선하라
자바가 제공하는 다중 구현 매커니즘은 인터페이스와 추상 클래스, 두 가지가 있다.
자바 8부터 인터페이스도 디폴트 메서드(default method)를 제공할 수 있게 되어 두 메커니즘 모두 인스턴스 메서드를 구현 형태로 제공할 수 있다.

둘의 가장 큰 차이는 추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 클래스가 되어야 한다는 점이다.
자바는 단일 상속만 지원하므로 추상 클래스 방식은 새로운 타입을 정의하는데 큰 제약이 되는 것이다.
반면 인터페이스가 선언한 메서드를 모두 정의하고 그 일반 규약을 잘 지킨 클래스라면 다른 어떤 클래스를 상속해도 같은 타입으로 취급된다.

기존 클래스에도 손쉽게 새로운 인터페이스를 구현해넣을 수 있다.
인터페이스가 요구하는 메서드를 추가하고, 클래스 선언에 implements 구문만 추가하면 된다.
반면 기존 클래스 상위에 추상 클래스를 끼워넣기는 일반적으로 어렵다.

또한 인터페이스는 믹스인(mixin) 정의에 적합하다.
예를 들어 Comparable은 자신을 구현한 클래스의 인스턴스들끼리는 순서를 정할 수 있다고 선언하는 믹스인 인터페이스이다.
이처럼 대상 타입의 주된 기능에 선택적 기능을 혼합(mixed in)한다고 해서 믹스인이라고 부른다.
추상 클래스는 기존 클래스에 덧씌울 수 없기 때문에 믹스인을 정의할 수 없다.
클래스는 두 부모를 섬길 수 없고, 클래스 계층구조에는 믹스인을 삽입하기에 합리적인 위치가 없다.

인터페이스로는 계층구조가 없는 타입 프레임워크를 만들 수 있다.
타입을 계층적으로 정의하면 많은 개념을 구조적으로 잘 표현할 수 있지만, 현실에는 계층을 엄격히 구분하기 어려운 개념도 있다.
Singer 인터페이스와 SongWriter 인터페이스가 있다고 해보자.
```java
public interface Singer {
    AudioClip sing(Song s);
}

public interface Songwriter {
    Song compose(int chartPosition);
}
```
현실엔 싱어송라이터도 존재한다.
이 코드의 타입을 인터페이스로 정의하면 Singer와 Songwriter를 모두 정의해도 문제가 없다.
```java
public interface SingerSongwriter extends Singer, Songwriter {
    AudioClip strum();
    void actSensitive();
}
```
이와 같은 구조를 클래스로 만드려면 가능한 조합을 각각의 클래스로 정의한 계층구조가 만들어 질 것이다.
속성이 n개라면 지원해야 할 조합의 수는 2<sup>n</sup>개나 된다.
공통 기능을 정의해놓은 타입이 없어 매우 비효율적인 구조가 된다.

래퍼 클래스 관용구([아이템 18](item18.md))와 함께 사용하면 인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.
타입을 추상 클래스로 정의해두면 그 타입에 기능을 추가하는 방법은 상속뿐이다.
상속해서 만든 클래스는 래퍼 클래스보다 활용도가 더 떨어지고 깨지기도 쉽다.

인터페이스의 메서드 중에서 구현 방법이 명백한 것이 있다면, 그 구현을 디폴트 메서드로 제공해 프로그래머들에게 편리함을 줄 수 있다.
디폴트 메서드를 제공할 때는 상속하려는 사람을 위한 설명을 `@implSpec` 자바독 태그를 붙여 문서화해야 한다([아이템 19](item19.md)).
주의사항은 equals와 hashCode 같은 Object 메서드는 디폴트 메서드로 제공해서는 안 된다.
인터페이스에서는 구현의 세부 사항을 알지 못하기 때문에 많이 쓰인다고 해서 무조건 디폴트 메서드로 제공하는 것은 피해야 한다.
또한 인터페이스는 인스턴스 필드를 가질 수 없고 public 이 아닌 static 멤버도 가질 수 없다(private static 메서드는 예외다).

## 템플릿 메서드 패턴
인터페이스와 추상 골격 구현(skeletal implementation) 클래스를 함께 제공하는 식으로 인터페이스와 추상 클래스의 장점을 모두 취하는 방법도 있다.
인터페이스로는 타입을 정의하고, 필요하면 디폴트 메서드 몇 개도 제공한다.
그리고 골격 구현 클래스에서 나머지 메서드들까지 구현한다.
이렇게 하면 단순히 골격 구현을 확장하는 것 만으로도 인터페이스를 구현하는 데 필요한 일들이 대부분 완료된다.
이 패턴을 `템플릿 메서드 패턴`이라고 한다.

관례상 인터페이스의 이름이 Interface라면 골격 구현 클래스 이름은 AbstractInterface로 짓는다.
좋은 예시로 컬렉션 프레임워크의 AbstractCollection, AbstractList, AbstractMap 각각이 핵심 컬렉션 인터페이스의 골격 구현이다.
다음 코드는 완벽히 동작하는 List 구현체를 반환하는 static 팩터리 메서드로, AbstractList 골격 구현을 활용했다.
```java
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);
    
    return new AbstractList<>() {
        @Override public Integer get(int i) {
            return a[i];    // 오토박싱
        }

        @Override public Integer set(int i, Integer val) {
            int oldVal = a[i];
            a[i] = val;     // 오토박싱
            return oldVal;  // 오토언박싱
        }

        @Override public int size() {
            return a.length;
        }
    };
}

```
이 예시는 int 배열을 받아 Integer 인스턴스 리스트 형태로 보여주는 어탭터(Adapter)이기도 하다.
int 값과 Integer 인스턴스 사이의 변환(박싱과 언박싱) 때문에 성능은 그리 좋지 않다.
또한 이 구현에서 익명 클래스([아이템 24](item24.md)) 형태를 사용했음에 주목하자.

골격 구현 클래스는 추상 클래스처럼 구현을 도와주고, 추상 클래스로 타입을 정의할 때 따라오는 제약에서는 자유롭다.
인터페이스를 구현한 클래스에서 해당 골격 구현을 확장한 private 내부 클래스를 정의하고 각 메서드 호출을 내부 클래스의 인스턴스에 전달하면 골격 구현 클래스를 우회적으로 이용할 수 있다.
[아이템 18](item18.md)에서 다룬 래퍼 클래스와 비슷한 이 방식을 simulated multiple inheritance라고 하며, 다중 상속의 많은 장점을 제공하는 동시에 단점은 보완해준다.

골격 구현을 작성하는 방법은 다음과 같다.
1. 인터페이스에서 메서드들의 구현에 사용되는 기반 메서드들을 선정한다.(골격 구현에서 abstract 메서드가 된다)
2. 기반 메서드들을 사용해 직접 구현할 수 있는 메서드를 모두 디폴트 메서드로 제공한다.
3. 기반 메서드나 디폴트 메서드로 만들지 못한 메서드가 남았다면, 이 인터페이스를 구현하는 골격 구현 클래스를 만들어 남은 메서드들을 작성해 넣는다.

Map.Entry 인터페이스를 보면 getKey와 getValue는 기반 메서드이고, 선택적으로 setValue를 포함할 수 있다.
equals와 hashCode의 동작 방식도 정의해놨다.
```java
interface Entry<K, V> {
        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        K getKey();

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * {@code remove} operation), the results of this call are undefined.
         *
         * @return the value corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        V getValue();

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation).  (Writes through to the map.)  The
         * behavior of this call is undefined if the mapping has already been
         * removed from the map (by the iterator's {@code remove} operation).
         *
         * @param value new value to be stored in this entry
         * @return old value corresponding to the entry
         * @throws UnsupportedOperationException if the {@code put} operation
         *         is not supported by the backing map
         * @throws ClassCastException if the class of the specified value
         *         prevents it from being stored in the backing map
         * @throws NullPointerException if the backing map does not permit
         *         null values, and the specified value is null
         * @throws IllegalArgumentException if some property of this value
         *         prevents it from being stored in the backing map
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        V setValue(V value);

        /**
         * Compares the specified object with this entry for equality.
         * Returns {@code true} if the given object is also a map entry and
         * the two entries represent the same mapping.  More formally, two
         * entries {@code e1} and {@code e2} represent the same mapping
         * if<pre>
         *     (e1.getKey()==null ?
         *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &amp;&amp;
         *     (e1.getValue()==null ?
         *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre>
         * This ensures that the {@code equals} method works properly across
         * different implementations of the {@code Map.Entry} interface.
         *
         * @param o object to be compared for equality with this map entry
         * @return {@code true} if the specified object is equal to this map
         *         entry
         */
        boolean equals(Object o);

        /**
         * Returns the hash code value for this map entry.  The hash code
         * of a map entry {@code e} is defined to be: <pre>
         *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *     (e.getValue()==null ? 0 : e.getValue().hashCode())
         * </pre>
         * This ensures that {@code e1.equals(e2)} implies that
         * {@code e1.hashCode()==e2.hashCode()} for any two Entries
         * {@code e1} and {@code e2}, as required by the general
         * contract of {@code Object.hashCode}.
         *
         * @return the hash code value for this map entry
         * @see Object#hashCode()
         * @see Object#equals(Object)
         * @see #equals(Object)
         */
        int hashCode();
// 나머지 코드 생략
}
```
```java
public abstract class AbstractMapEntry<K, V> 
        implements Map.Entry<K, V> {

    // 변경 가능한 엔트리는 이 메서드를 반드시 재정의해야 한다
    @Override public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    // Map.Entry.equals의 일반 규약을 구현한다.
    @Override public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> e = (Map.Entry) o;
        return Objects.equals(e.getKey(), getKey())
                && Objects.equals(e.getValue(), getValue());
    }

    // Map.Entry.hashCode의 일반 규약을 구현한다.
    @Override public int hashCode() {
        return Objects.hashCode(getKey())
                ^ Objects.hashCode(getValue());
    }

    @Override public String toString() {
        return getKey() + "=" + getValue();
    }
}

```
골격 구현은 기본적으로 상속해서 사용하는 것을 가정하므로 [아이템 19](item19.md)에서 이야기한 설계 및 문서화 지침을 모두 따라야 한다.

단순 구현(simple implementation)은 골격 구현의 변종으로, AbstractMap.SimpleEntry가 좋은 예시이다.
단순 구현도 골격 구현과 같이 상속을 위해 인터페이스를 구현한 것이지만, 추상 클래스가 아니라는 점이 다르다.
```java

    /**
     * An Entry maintaining a key and a value.  The value may be
     * changed using the {@code setValue} method. Instances of
     * this class are not associated with any map nor with any
     * map's entry-set view.
     *
     * @apiNote
     * This class facilitates the process of building custom map
     * implementations. For example, it may be convenient to return
     * arrays of {@code SimpleEntry} instances in method
     * {@code Map.entrySet().toArray}.
     *
     * @param <K> the type of key
     * @param <V> the type of the value
     *
     * @since 1.6
     */
    public static class SimpleEntry<K,V>
        implements Entry<K,V>, java.io.Serializable
    {
        @java.io.Serial
        private static final long serialVersionUID = -8499721149061103585L;

        @SuppressWarnings("serial") // Conditionally serializable
        private final K key;
        @SuppressWarnings("serial") // Conditionally serializable
        private V value;

        /**
         * Creates an entry representing a mapping from the specified
         * key to the specified value.
         *
         * @param key the key represented by this entry
         * @param value the value represented by this entry
         */
        public SimpleEntry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        /**
         * Creates an entry representing the same mapping as the
         * specified entry.
         *
         * @param entry the entry to copy
         */
        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            this.key   = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry
         */
        public V getValue() {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value.
         *
         * @param value new value to be stored in this entry
         * @return the old value corresponding to the entry
         */
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        /**
         * Compares the specified object with this entry for equality.
         * Returns {@code true} if the given object is also a map entry and
         * the two entries represent the same mapping.  More formally, two
         * entries {@code e1} and {@code e2} represent the same mapping
         * if<pre>
         *   (e1.getKey()==null ?
         *    e2.getKey()==null :
         *    e1.getKey().equals(e2.getKey()))
         *   &amp;&amp;
         *   (e1.getValue()==null ?
         *    e2.getValue()==null :
         *    e1.getValue().equals(e2.getValue()))</pre>
         * This ensures that the {@code equals} method works properly across
         * different implementations of the {@code Map.Entry} interface.
         *
         * @param o object to be compared for equality with this map entry
         * @return {@code true} if the specified object is equal to this map
         *         entry
         * @see    #hashCode
         */
        public boolean equals(Object o) {
            return o instanceof Map.Entry<?, ?> e
                    && eq(key, e.getKey())
                    && eq(value, e.getValue());
        }

        /**
         * Returns the hash code value for this map entry.  The hash code
         * of a map entry {@code e} is defined to be: <pre>
         *   (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *   (e.getValue()==null ? 0 : e.getValue().hashCode())</pre>
         * This ensures that {@code e1.equals(e2)} implies that
         * {@code e1.hashCode()==e2.hashCode()} for any two Entries
         * {@code e1} and {@code e2}, as required by the general
         * contract of {@link Object#hashCode}.
         *
         * @return the hash code value for this map entry
         * @see    #equals
         */
        public int hashCode() {
            return (key   == null ? 0 :   key.hashCode()) ^
                   (value == null ? 0 : value.hashCode());
        }

        /**
         * Returns a String representation of this map entry.  This
         * implementation returns the string representation of this
         * entry's key followed by the equals character ("{@code =}")
         * followed by the string representation of this entry's value.
         *
         * @return a String representation of this map entry
         */
        public String toString() {
            return key + "=" + value;
        }

    }
```
