# 아이템 33. 타입 안전 이종 컨테이너를 고려하라
제네릭은 `Set<E>`, `Map<K, V>` 등의 컬렉션과 `ThreadLocal<T>`, `AtomicReference<T>` 등의 단일원소 컨테이너에도 흔히 쓰인다.
이런 모든 쓰임에서 매개변수화되는 대상은 원소가 아닌 컨테이너 자신이다.
따라서 하나의 컨테이너에서 매개변수화할 수 있는 타입의 수가 제한된다.
Set에는 원소의 타입을 뜻하는 단 하나의 타입 매개변수만 있으면 되며, Map에는 키와 값의 타입을 뜻하는 2개만 필요한 식이다.

하지만 데이터베이스의 경우, 각각의 row에 임의 개수의 column을 가질 수 있는데, 모든 row를 타입 안전하게 만드는 것이 필요하다.
이러한 경우 컨테이너 대신 키를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 키를 함께 제공하면 된다.
이렇게 하면 제네릭 타입 시스템이 값의 타입이 키와 같음을 보장해준다.
이러한 설계 방식을 `타입 안전 이종 컨테이너 패턴(type safe heterogeneous pattern)`이라고 한다.

이에 대한 예시로 타입별로 즐겨 찾는 인스턴스를 저장하고 검색할 수 있는 Favorites 클래스를 생각해보자.
각 타입의 Class 객체를 매개변수화한 키 역할로 사용하면 되는데, 이 방식이 동작하는 이유는 class의 클래스가 제네릭이기 때문이다.
class 리터럴의 타입은 Class가 아닌 `Class<T>`다.
String.class 의 타입은 `Class<String>`이고, Integer.class의 타입은 `Class<Integer>`다.
컴파일타임 타입 정보와 런타임 타입 정보를 알아내기 위해 메서드들이 주고받는 class 리터럴을 타입 토큰(type token)이라 한다.

```java
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance){
        favorites.put(Objects.requireNonNull(type), instance);
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
}
```
이 클래스를 사용하는 예시이다. 즐겨 찾는 String, Integer, Class 인스턴스를 저장, 검색, 출력하고 있다.
```java
public static void main(String[] args) {
    Favorites f = new Favorites();

    f.putFavorites(String.class, "Java");
    f.putFavorites(Integer.class, 0Xcafebabe);
    f.putFavorites(Class.class, Favorites.class);

    String favoriteString = f.getFavorite(String.class);
    int favoriteInteger = f.getFavorite(Integer.class);
    Class<?> favoriteClass = f.getFavorite(Class.class);

    System.out.printf("%s %x %s%n, favoriteString,
        favoriteInteger, favoriteClass.getName());
}
```
이 프로그램은 Java cafebabe Favorites를 출력한다.
Favorites는 타입 안전하고, 모든 키의 타입이 제각각이라 일반적인 맵과는 달리 여러가지 타입의 원소를 담을 수 있다.
따라서 Favorites를 타입 안전 이종(heterogeneous) 컨테이너라고 할 수 있다.

Favorites가 사용하는 private Map 변수인 favorites의 타입은 `Map<Class<?>, Object>`이다.
비한정적 와일드카드 타입이라 아무것도 넣을 수 없다고 생각할 수 있지만, 맵 자체가 와일드카드가 아닌 key가 와일드카드 값이라 값을 넣을 수 있고 key에 다양한 타입을 지원한다.
그리고 이 Map의 value 타입은 Object인데, 이는 Map에서 key와 value 사이의 관계를 보증하지 않음을 의미한다.

putObject에서는 주어진 Class 객체와 즐겨찾기 인스턴스를 favorites에 추가해 관계를 짓는다.
key와 value 사이의 타입 링크(type link) 정보는 버려진다.
즉, 그 value가 key 타입의 인스턴스라는 정보가 사라진다.

getFavorite에서 주어진 Class 객체에 해당하는 값을 꺼내고, 이는 Object 타입이니 T로 바꾸어 반환한다.
Class의 cast 메서드를 이용해 이 객체 참조를 Class 객체가 가리키는 타입으로 동적 형변환한다.

cast 메서드는 형변환 연산자의 동적 버전이다.
주어지 인수가 Class 객체가 알려주는 타입의 인스턴스인지 검사한 다음, 맞다면 그 인수를 그대로 반환하고, 아니면 ClassCastException을 던진다.
```java
    /**
     * Casts an object to the class or interface represented
     * by this {@code Class} object.
     *
     * @param obj the object to be cast
     * @return the object after casting, or null if obj is null
     *
     * @throws ClassCastException if the object is not
     * null and is not assignable to the type T.
     *
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    @IntrinsicCandidate
    public T cast(Object obj) {
        if (obj != null && !isInstance(obj))
            throw new ClassCastException(cannotCastMsg(obj));
        return (T) obj;
    }
```

작성한 Favorites 클래스에는 2가지 제약이 존재한다.
첫 번째, 악의적인 클라이언트가 Class 객체를 제네릭이 아닌 raw 타입([아이템 26](item26.md))으로 넘기면 Favoeites 인스턴스의 타입 안전성이 쉽게 깨진다.
이렇게 작성한 코드에서는 컴파일 시 비검사 경고를 보여준다.
HashSet과 HashMap 등의 일반 컬렉션 구현체에도 같은 문제가 있다.
HashSet의 raw 타입을 사용하면 `HashSet<Integer>`에 String을 넣을 수 있다.

Favorites가 타입 불변식을 어기는 일이 없도록 보장하려면 putFavorite 메서드에서 인수로 주어진 instance의 타입이 type으로 명시한 타입과 같은지 확인하면 된다.
다음과 같이 동적 형변환을 사용하자.
```java
public <T> void putFavorite(Class<T> type, T instance) {
    favorites.put(Objects.requireNonNull(type), type.cast(instance));
}
```
java.util.Collections에 checkedSet, checkedList, checkedMap 같은 메서드가 이와 같은 방식을 사용하고 있다.
이 static 팩터리들은 컬렉션(혹은 맵)과 함께 1개(혹은 2개)의 Class 객체를 받는다.
이 메서드들은 모두 제네릭이라 Class 객체와 컬렉션의 컴파일타임 타입이 같음을 보장한다.

두 번째 제약은 실체화 불가 타입([아이템 28](item28.md))에는 사용할 수 없다는 것이다.
String이나 String[]은 저장할 수 있어도 `List<String>`은 저장할 수 없다.
`List<String>`용 Class 객체는 존재하지 않기 때문이다.
> 이 제약을 슈퍼 타입 토큰(super type token)으로 해결하려는 시도도 있다.
> 스프링 프레임워크에서 ParameterizedTypeReference라는 클래스로 구현이 되어 있으며, Favorites에 슈퍼 타입 토큰을 적용하면 제네릭 타입도 저장이 가능하다.
> ```java
>     Favorites f = new Favorites();
>
>     List<String> pets = Arrays.asList("개", "고양이", "앵무");
>
>     f.putFavorite(new TypeRef<List<String>>(){}, pets);
>     List<String> listofStrings = f.getFavorite(new TypeRef<List<String>>(){});
> ```
> 슈퍼 타입 토큰도 완벽하진 않으니 조심해서 사용해야 한다. </br>
> 참고 : [Super Type Tokens](https://gafter.blogspot.com/2006/12/super-type-tokens.html)

Favorites가 사용하는 타입 토큰은 비한정적이다.
이를 제한하고 싶으면 한정적 타입 토큰을 활용하면 된다.
이는 한정적 타입 매개변수([아이템 29](item29.md))나 한정적 와일드카드([아이템 31](item31.md))를 사용하여 표현 가능한 타입을 제한하는 타입 토큰을 말한다.

애너테이션 API([아이템 39](item39.md))는 한정적 타입 토큰을 적극적으로 사용한다.
다음은 AnnotatedElement 인터페이스에 선언된 메서드로, 대상 요소에 달려 있는 애너테이션을 런타임에 읽어 오는 기능을 한다.
이 메서드는 리플렉션의 대상이 되는 타입들, 즉 클래스(java.class.Class<T>), 메서드(java.lang.reflect.Method), 필드(java.lang.reflect.Field)같이 프로그램 요소를 표현하는 타입들에서 구현한다.
```java
    /**
     * Returns this element's annotation for the specified type if
     * such an annotation is <em>present</em>, else null.
     *
     * @param <T> the type of the annotation to query for and return if present
     * @param annotationClass the Class object corresponding to the
     *        annotation type
     * @return this element's annotation for the specified annotation type if
     *     present on this element, else null
     * @throws NullPointerException if the given annotation class is null
     * @since 1.5
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);
```
여기서 annotationClass 인수는 애너테이션 타입을 뜻하는 한정적 타입 토큰이다.
이 메서드는 토큰으로 명시한 타입의 애너테이션이 대상 요소에 달려있다면 그 애너테이션을 반환하고, 없다면 null을 반환한다.
즉, 애너테이션된 요소는 그 키가 애너테이션 타입인, 타입 안전 이종 컨테이너라고 할 수 있다.

`Class<T>` 타입의 객체가 있고, getAnnotation 처럼 한정적 토큰을 받는 메서드에 넘기려면 어떻게 해야 할까?
객체를 `Class<? extends Annotation>`으로 형변환할 수도 있지만, 이 형변환은 비검사이므로 컴파일 경고가 표시된다([아이템 27](item27.md)).
Class 클래스가 이런 형변환을 안전하게 할 수 있도록 인스턴스 메서드를 제공한다.
asSubclass라는 메서드로, 호출된 인스턴스 자신의 Class 객체를 인수가 명시한 클래스로 형변환한다.
```java
    /**
     * Casts this {@code Class} object to represent a subclass of the class
     * represented by the specified class object.  Checks that the cast
     * is valid, and throws a {@code ClassCastException} if it is not.  If
     * this method succeeds, it always returns a reference to this {@code Class} object.
     *
     * <p>This method is useful when a client needs to "narrow" the type of
     * a {@code Class} object to pass it to an API that restricts the
     * {@code Class} objects that it is willing to accept.  A cast would
     * generate a compile-time warning, as the correctness of the cast
     * could not be checked at runtime (because generic types are implemented
     * by erasure).
     *
     * @param <U> the type to cast this {@code Class} object to
     * @param clazz the class of the type to cast this {@code Class} object to
     * @return this {@code Class} object, cast to represent a subclass of
     *    the specified class object.
     * @throws ClassCastException if this {@code Class} object does not
     *    represent a subclass of the specified class (here "subclass" includes
     *    the class itself).
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }
```
다음은 컴파일 시점에 타입을 알 수 없는 애너테이션을 asSubclass 메서드를 사용해 런타임에 읽어내는 예이다.
```java
static Annotation getAnnotation(AnnotatedElement element,
                                String annotationTypeName) {
    Class<T> annotationType = null; // 비한정적 타입 토큰
    try {
        annotationType = Class.forName(annotationTypeName);
    } catch (Exception ex) {
        throw new IllegalArgumentException(ex);
    }
    return element.getAnnotation(
        annotationType.asSubclass(Annotation.class));
}
```
