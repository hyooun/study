# 아이템 15. 클래스와 멤버의 접근 권한을 최소화하라
잘 설계된 컴포넌트는 클래스 내부 데이터와 내부 구현 정보를 잘 숨기고, 구현과 API를 깔끔하게 분리한다.
오직 API를 통해서만 다른 컴포넌트와 소통하며 서로의 내부 동작 방식에는 전혀 개의치 않는다.
이는 정보 은닉 혹은 캡슐화라고 부르는 소프트웨어 설계의 근간이 되는 원리이다.

## 정보 은닉의 장점
- 여러 컴포넌트를 병렬로 개발할 수 있어 시스템 개발 속도를 높인다.
- 각 컴포넌트를 더 빨리 파악하여 디버깅할 수 있고, 교체 부담도 적어 시스템 관리 비용을 낮춘다.
- 완성된 시스템을 프로파일링해 최적화할 컴포넌트를 정한 다음, 다른 컴포넌트에 영향을 주지 않고 해당 컴포넌트만 최적화할 수 있어 성능 최적화에 도움을 준다.
- 외부에 의존하지 않고 독자적으로 동작할 수 있는 컴포넌트라면 다른 환경에서도 잘 작동하므로 소프트웨어 재사용성이 향상된다.
- 시스템 전체가 완성되지 않은 상태에서도 개별 컴포넌트의 동작을 검증할 수 있어 대규모 시스템 제작 난이도를 낮춘다.

자바는 정보 은닉을 위한 다양한 장치를 제공한다.
그중 접근 제어 메커니즘은 클래스, 인터페이스, 멤버의 접근성(접근 허용 범위)을 명시한다.
각 요소의 접근성은 그 요소가 선언된 위치와 접근 제한자(private, protected, public)로 정해진다.
이 접근 제한자를 제대로 활용하는 것이 정보 은닉의 핵심이다.

기본 원칙은 간단하다.
모든 클래스와 멤버의 접근성을 가능한 한 좁혀야 한다.
소프트웨어가 올바로 동작하는 한 가장 낮은 접근 수준을 부여해야 한다는 의미이다.

가장 바깥의 top-level 클래스와 인터페이스에 부여할 수 있는 접근 수준은 package-private와 public 두 가지다.
top-level 클래스나 인터페이스를 public으로 선언하면 공개 API가 되며, package-private로 선언하면 해당 패키지에서만 사용할 수 있다.
패키지 외부에서 쓸 이유가 없다면 package-private로 선언하자.
그러면 이들은 API가 아닌 내부 구현이 되어 언제든 수정할 수 있다.
즉, 클라이언트에 아무런 영향이 없어 다음 릴리즈에서 수정, 교체, 삭제해도 무방하다. 
반면, public으로 선언한다면 API가 되므로 하위 호환을 위해 지속적인 관리가 필요해진다.

한 클래스에서 사용하는 package-private top-level 클래스나 인터페이스는 이를 사용하는 클래스 안에 private static으로 중첩시켜보자([아이템 24](item24.md)).
top-level로 두면 같은 패키지의 모든 클래스가 접근할 수 있지만, private static으로 중첩시키면 바깥 클래스 하나에서만 접근할 수 있다.

public일 필요가 없는 클래스의 접근 수준을 package-private top-level 클래스로 좁히는 것이 중요하다.
public 클래스는 그 패키지의 API인 반면, package-private top-level 클래스는 내부 구현이기 때문이다.

### 멤버(필드, 메서드, 중첩 클래스, 중첩 인터페이스)에 부여할 수 있는 접근 수준은 다음과 같다.
- private: 멤버를 선언한 top-level 클래스에서만 접근할 수 있다.
- package-private: 멤버가 소속된 패키지 안의 모든 클래스에서 접근할 수 있다. 접근 제한자를 명시하지 않았을 때 적용되는 패키지 접근 수준이다(단, 인터페이스의 멤버는 기본적으로 public이 적용된다).
- protected: package-private의 접근 범위를 포함하며, 이 멤버를 선언한 클래스의 하위 클래스에서도 접근할 수 있다(제약이 조금 존재한다).
- public: 모든 곳에서 접근할 수 있다.

클래스의 공개 API를 설계한 후, 그 외의 모든 멤버는 private로 만들자.
그런 다음 오직 같은 패키지의 다른 클래스가 접근해야 하는 멤버에 한하여 private 접근자를 제거해 package-private로 풀어주자.
권한을 풀어 주는 일을 자주 수행한다면 시스템에서 컴포넌트 분리가 더 필요한지 고민해보아야 한다.

private와 package-private 멤버는 모두 해당 클래스의 구현에 해당하므로 보통은 공개 API에 영향을 주지 않는다. 단 Serializable을 구현한 클래스에서는 그 필드들도 의도치 않게 공개 API가 될 수도 있다([아이템 86](item86.md), [아이템 87](item87.md)).
public 클래스에서는 멤버 접근 수준을 package-private에서 protected로 바꾸는 순간 그 멤버에 접근할 수 있는 대상 범위가 엄청나게 넓어진다.
public 클래스의 protected 멤버는 공개 API이므로 영원히 지원되어야 하고, 내부 동작 방식을 API 문서에 적어 사용자에게 공개해야 할 수도 있다([아이템 19](item19.md)).
따라서 protected 멤버의 수는 적을수록 좋다.

리스코프 치환 원칙(상위 클래스의 인스턴스는 하위 클래스의 인스턴스로 대체해 사용할 수 있다, [아이템 10](item10.md))을 지키기 위해서 상위 클래스의 메서드를 재정의할 때 그 접근 수준을 상위 클래스보다 좁게 설정할 수 없다.
이 규칙을 어기면 하위 클래스에서 컴파일 에러가 발생한다.
클래스가 인터페이스를 구현하는 것도 이 규칙을 적용해야 하고, 클래스는 인터페이스가 정의한 모든 메서드를 public으로 선언해야 한다.

단지 코드를 테스트하기 위해 접근 범위를 넓히는 경우가 있는데, 적당히 넓히는 것은 괜찮다.
public 클래스의 private 멤버를 package-private까지 풀어주는 것은 허용할 수 있지만 그 이상은 안 된다.
즉, 테스트만을 위해 클래스, 인터페이스, 멤버를 공개 API로 만들어서는 안 된다. 
테스트 코드를 테스트 대상과 같은 패키지에 두면 package-private 요소에 접근할 수 있기 때문에 이렇게 할 이유도 딱히 없다.

public 클래스의 인스턴스 필드는 되도록 public이 아니어야 한다([아이템 16](item16.md)).
필드가 가변 객체를 참조하거나, final이 아닌 인스턴스 필드를 public으로 선언하면 그 필드와 관련된 모든 것은 불변식을 보장할 수 없다.
또한, 필드가 수정될 때 락 획득 같은 다른 작업을 할 수 없게 되므로 public 가변 필드를 갖는 클래스는 안전하지 않다.
필드가 final이면서 불변 객체를 참조하더라도 내부 구현을 바꿀 때 해당 public 필드를 없애는 방식으로는 리팩터링이 제한된다.

이러한 문제는 static 필드에서도 마찬가지이나, 해당 클래스가 표현하는 추상 개념을 완성하는 데 꼭 필요한 구성요소로써의 상수라면 public static final 필드로 공개해도 된다.
관례상 이런 상수의 이름은 대문자 알파벳으로 쓰고, 각 단어 사이에 언더바(_)를 넣는다.
이런 필드는 반드시 기본 타입 값이나 불변 객체를 참조해야 한다([아이템 17](item17.md)).
가변 객체를 참조한다면 final이 아닌 필드에 적용되는 모든 불이익이 그대로 적용된다.
다른 객체를 참조하지는 못하지만, 참조된 객체 자체가 수정될 수 있어 오류가 발생할 수 있다.

길이가 0이 아닌 배열은 모두 변경 가능하니 주의하자.
클래스에서 public static final 배열 필드를 두거나 이 필드를 반환하는 접근자 메서드를 제공해서는 안 된다.
이런 필드나 접근자를 제공한다면 클라이언트가 수정할 수 있어 보안 허점이 생긴다.
```java
// 보안 허점이 숨어 있다.
public static final Thing[] VALUES = { ... };
```

이에 대한 해결책은 2가지가 있다.
1. 배열을 private로 만들고 public 불변 리스트를 추가한다.
```java
private static final Thing[] PRIVATE_VALUES = { ... };
public static final List<Thing> VALUES =
    Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
```
2. 배열을 private로 만들고 그 복사본을 반환하는 public 메서드를 추가한다(방어적 복사).
```java
private static final Thing[] PRIVATE_VALUES = { ... };
public static final Thing[] values() {
    return PRIVATE_VALUES.clone();
}
```
클라이언트가 무엇을 원하느냐를 판단해 선택하면 된다.
어느 반환 타입이 더 쓰기 편할지, 성능은 어떤 쪽이 나을지를 고민해보자.

## 📦 모듈(module)
자바 9에서는 모듈 시스템이라는 개념이 도입되면서 두 가지 암묵적 접근 수준이 추가되었다.
패키지가 클래스들의 묶음이듯, 모듈은 패키지들의 묶음이다.
모듈은 자신이 속하는 패키지 중 공개(export)할 것들을 관례상 module-info.java 파일에 선언한다.
protected 혹은 public 멤버라도 해당 패키지를 공개하지 않았다면 모듈 외부에서는 접근할 수 없다.
물론 모듈 안에서는 exports로 선언했는지 여부에 영향을 받지 않는다.
모듈 시스템을 활용하면 클래스를 외부에 공개하지 않으면서도 같은 모듈을 이루는 패키지 사이에서는 자유로운 공유가 가능하다.

이 암묵적 접근 수준들은 각각 public과 protected와 같으나, 그 효과가 모듈 내부로 한정되는 변종인 것이다.
이런 형태로 공유해야 하는 상황은 흔치 않으며, 패키지들 사이에서 클래스들을 재배치하여 해결이 가능하다.

앞서 다룬 4개의 기존 접근 수준과 달리, 모듈에 적용되는 새로운 두 접근 수준은 주의해서 사용해야 한다.
모듈의 JAR 파일을 자신의 모듈 경로가 아닌 애플리케이션의 classpath에 두면 그 모듈 안의 모든 패키지는 마치 모듈이 없는 것 처럼 동작한다.
모듈의 export 여부와 상관 없이 public 클래스가 선언한 모든 public 혹은 protected 멤버를 모듈 밖에서도 접근할 수 있게 된다.
새로 등장한 이 접근 수준을 JDK에서 적극적으로 활용하고 있으며, 자바 라이브러리에서 공개하지 않은 패키지들은 해당 모듈 밖에서는 절대로 접근할 수 없다.

접근 보호 방식 이외에도 모듈은 다방면으로 자바 프로그래밍에 영향을 준다.
모듈의 장점을 잘 활용하기 위해서는 패키지들을 모듈 단위로 묶고, 모듈 선언에 패키지들의 모든 의존성을 명시해야 한다.
그 다음 소스 트리를 재배치하고, 모듈 안으로부터 모듈 시스템을 적용하지 않는 일반 패키지로의 모든 접근에 특별한 조치를 취해야 한다.
그러니 꼭 필요한 경우가 아니라면 당분간은 사용하지 않는 것이 좋을 것 같다.
