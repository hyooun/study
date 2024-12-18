# 아이템 5. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라
많은 클래스가 하나 이상의 자원에 의존한다. 아래 SpellChecker는 dictionary에 의존하는데 이런 클래스를 static 유틸리티 클래스([아이템 4](item4.md))로 구현한 것을 흔히 볼 수 있다.
```java
public class SpellChecker {
    private static final Lexcion dictionary;

    private SpellChecker() { } // 객체 생성 방지
    
    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}

```
비슷하게 싱글턴([아이템 3](item3.md))으로 구현하는 경우도 흔하다.
```java
public class SpellChecker {
    private static final Lexicon dictionary = ...;

    private SpellChecker() { ... }
    public static SpellChecker INSTANCE = new SpellChecker(...);

    public boolean isValid(String word) { ... }
    public List<String> suggestions(String typo) { ... }
}

```
두 방식 모두 사전을 하나만 사용한다는 점에서 확장성이 떨어져보인다.
SpellChecker가 여러 사전을 사용할 수 있도록 dictionary 필드에서 final을 제거하고 다른 사전으로 교체하는 메서드를 추가할 수 있지만, 오류 발생 가능성이 크고 멀티스레드 환경에서 사용이 불가능하다.
사용하는 자원에 따라서 동작이 달라지는 클래스에서는 static 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.

인스턴스를 생성할 때 생성자에 필요한 자원을 넘겨주는 방식이 이에 적합하다. 이는 의존 객체 주입의 한 형태로 SpellChecker를 생성할 때 의존 객체인 dictionary를 주입해주면 된다.
```java
public class SpellChecker {
    private static final Lexicon dictionary;

    private SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) { ... }
    public List<String> suggestions(String typo) { ... }
}

```
의존 객체 주입은 유연성과 테스트 용이성을 높여주고 불변([아이템 17](item17.md))을 보장하여 같은 자원을 사용하려는 여러 클라이언트가 의존 객체를 안심하고 공유할 수 있다.
의존 객체 주입은 생성자, static 팩터리([아이템 1](item1.md)), 빌더([아이템 2](item2.md))에 똑같이 응용할 수 있다.
생성자에 resource factory를 넘겨주는 방식도 있다. 팩터리란 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체를 말한다. (Factory Method Pattern을 구현한 것) </br>
자바 8에서 소개한 Supplier<T> 인터페이스가 그 예시이며 Supplier<T>를 입력으로 받는 메서드는 일반적으로 한정적 와일드카드 타입(bounded wildcard type, [아이템 31](item31.md))을 사용해 팩터리의 타입 매개변수를 제한해야 한다.
이 방식을 통해 클라이언트는 자신이 명시한 타입의 하위 타입이라면 무엇이든 생성할 수 있는 팩터리를 넘길 수 있다.
```java
Mosaic create(Supplier<? extends Tile> tileFactory) { ... }

```

정리하자면 클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 주는 경우 싱글턴이나 static 유틸리티 클래스는 사용하지 않는 것이 좋다.</br>
이 자원들을 클래스가 직접 만들게 하는 것이 아닌, 필요한 자원이나 그 자원을 만들어주는 팩터리를 생성자에 넘겨주는 의존 객체 주입 기법을 사용하자.
