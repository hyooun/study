# 아이템 10. equals는 일반 규약을 지켜 재정의하라
equals를 재정의하지 않으면 그 클래스의 인스턴스는 오직 자기 자신과만 같게 된다.
다음과 같은 상황들에서는 equals를 재정의하지 않는 것이 좋다.
1. 각 인스턴스가 본질적으로 고유하다.</br>
   값을 표현하는 게 아니라 동작하는 개체를 표현하는 클래스, Thread가 대표적인 예시로 Object의 equals 메서드는 이러한 클래스에 맞게 구현되었다.
2. 인스턴스의 논리적 동치성(logical equality)을 검사할 일이 없다.</br>
   java.util.regex.Pattern은 equals를 재정의해서 두 Pattern의 인스턴스가 같은 정규표현식을 나타내는지 논리적 동치성을 검사하는 방법이 있다.
   하지만 이 방식이 필요하지 않다고 판단되는 경우는 Object의 equals만으로 해결되므로 재정의가 필요없다.
3. 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.</br>
   대부분의 Set 구현체는 AbstractSet이 구현한 equals를 상속받아 사용하고, List 구현체들은 AbstractList로부터, Map 구현체들은 AbstractMap으로부터 상속받아 그대로 사용한다.
4. 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.</br>
   만약 실수로 equals가 호출되는 것을 막고 싶다면 다음과 같이 구현하면 된다
   
   ```java
   @Override public boolean equals(Object o) {
       throw new AssertionError(); // 호출 금지!
   }
   ```

그렇다면 equals의 재정의가 필요한 경우는 언제일까?
객체 식별성(object identity; 두 객체가 물리적으로 같은가)이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스 equals가 논리적 동치성을 비교하도록 재정의되지 않은 경우이다.
주로 Integer나 String과 같은 값 클래스들이 여기에 해당한다.
두 값 객체를 equals로 비교하는 프로그래머는 객체가 같은지가 아닌, 값이 같은지를 판별하고 싶어 할 것이다.</br>

equals가 논리적 동치성을 확인하도록 재정의해두면, 값을 비교하여 프로그래머가 원하는 동작을 수행하고 Map의 key와 Set의 원소로도 사용이 가능하다.
값 클래스여도 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스([아이템 1](item1.md))라면 equals를 재정의하지 않아도 된다.</br>
Enum([아이템 34](item34.md))도 여기에 해당되며, 이런 클래스에서는 어차피 논리적으로 같은 인스턴스가 2개 이상 만들어지지 않으니 논리적 동치성과 객체 식별성이 사실상 같다.
따라서 Object의 equals가 논리적 동치성도 확인해주니 equals 재정의가 필요없다.

## equals 메서드를 재정의할 때는 반드시 일반 규약을 따라야 한다. 다음은 Object 명세에 적힌 규약이다.
- 반사성(reflexivity) : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true이다.
- 대칭성(symmetry) : null이 아닌 모든 참조 값 x, y에 대해 x.equals(y)가 true이면 y.equals(x)도 true이다.
- 추이성(transitivity) : null이 아닌 모든 참조 값 x, y, z에 대해 x.equals(y)가 true이고 y.equals(z)도 true이면 x.equals(z)도 true이다.
- 일관성(consistency) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출되면 항상 true를 반환하거나 항상 false를 반환한다.
- null-아님 : null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false이다.

```java
public final class CaseInsensitiveString {
    private final String s;
    
    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString) 
            return s.equalsIgnoreCase(
                    ((CaseInsensitiveString) o).s);
        if (o instanceof String)    // 한 방향으로만 작동한다!
            return s.equalsIgnoreCase((String) o);
        return false;
    }
    // 나머지 코드는 생략
}

```
위와 같은 CaseInsensitiveString이 다음과 같이 생성되었을 때
```java
CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
String s = "polish";
```
CaseInsensitiveString의 equals가 대소문자를 구분하지 않도록 구현되어 있어 cis.equals(s)는 true를 반환한다. 
하지만 s.equals(cis)는 false를 반환하기 때문에 대칭성을 위반한다.
이 객체를 다른 객체에 사용했을 때의 동작도 예측할 수 없기 때문에 이러한 equals 재정의는 피해야 한다.
아래와 같이 같은 String과의 비교 없이 CaseInsensitiveString끼리만 비교하는 것이 현명하다.
```java
@Override public boolean equals(Object o) {
    return o instanceof CaseInsensitiveString &&
            ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}
```

```java
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public boolean equals(Object o) {
        if(!(o instanceof Point))
            return false;
        Point p = (Point)o;
        return p.x == x && p.y == y;
    }
    
    // 나머지 코드는 생략
}

```
```java
public class ColorPoint extends Point {
    private final Color color;
    
    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }
    
    // 나머지 코드는 생략
}

```
Point와 이를 상속한 ColorPoint 클래스가 있다. 이 때 ColorPoint에서 equals 메서드를 재정의하지 않으면 Point의 구현이 상속되어 색상 정보는 무시한 채 비교를 수행한다.
equals 규약을 어긴 것은 아니지만, 중요한 정보를 놓치게 되니 사용하기에 적합하지 않다.
```java
@Override public boolean equals(Object o) {
    if (!(o instanceof ColorPoint))
        return false;
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```
이와 같이 재정의하면 Point를 ColorPoint에 비교한 결과와 반대로 ColorPoint를 Point에 비교한 결과가 다를 수 있다.
Point의 equals는 색상을 무시하고 ColorPoint의 equals는 입력 매개변수의 클래스가 ColorPoint가 아니므로 false만 반환할 것이다.
```java
@Override public boolean equals(Object o) {
    if (!(o instanceof Point))
        return false;

    // o가 일반 Point이면 색상을 무시하고 비교한다.
    if (!(o instanceof ColorPoint))
        return o.equals(this);
        
    // o가 ColorPoint면 색상까지 비교한다.
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```
이렇게 변경하여 대칭성을 지킬 수 있지만 추이성을 위배하게 된다.
```
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
```
이렇게 세 Point가 있을 때 p1.equals(p2)와 p2.equals(p3)는 true를 반환하는데, p1.equals(p3)는 false를 반환한다. 
또한 이러한 방식은 무한 재귀에 빠질 위험성도 있다. 
Point의 또 다른 하위클래스 SmellPoint를 만들고 equals를 같은 방식으로 구현했을 때, myColorPoint.equals(mySmellPoint)를 호출하면 StackOverFlowError를 일으킨다.

이 현상은 모든 객체 지향 언어의 동치관계에서 나타나는 근본적인 문제이며, 구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다.
```java
@Override public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass())
        return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
}
```
이 equals는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다. 괜찮아 보일 순 있지만 리스코프 치환 원칙(Liskov substitution principal)을 위배한다.

```java
// 단위 원 안의 모든 점을 포함하도록 unitCircle을 초기화한다.
private static final Set<Point> unitCircle = Set.of(
        new Point( 1, 0), new Point( 0, 1),
        new Point(-1, 0), new Point( 0, -1));
  
public static boolean onUnitCircle(Point p) {
    return unitCircle.contains(p);
}
```
```java
public class CounterPoint extends Point {
    private static final AtomicInteger counter = new AtomicInteger();

    public CounterPoint(int x, int y) {
        super(x, y);
        counter.incrementAndGet();
    }
    public static int numberCreated() { return counter.get(); }
}

```
리스코프 치환 원칙에 따라 어떤 타입에 있어 중요한 속성이라면 그 하위 타입에서도 마찬가지로 중요하다. 따라서 그 타입의 모든 메서드가 하위 타입에서도 동일하게 작동해야 한다.
Point의 하위 클래스는 어디서든 Point로 활용될 수 있어야 한다는 뜻이다.</br>

그런데 위와 같이 CounterPoint의 인스턴스를 onUnitCircle 메서드에 넘기면 문제가 발생한다.
Point 클래스의 equals를 getClass응 사용해 작성했기 때문에 onUnitCircle은 false를 반환할 것이다.</br>
그 이유는 onUnitCircle에서 사용한 Set과 같은 Collection들은 contains에 equals를 사용하는데, CounterPoint의 인스턴스는 어떤 Point와도 같을 수 없기 때문이다.
반면 Point의 equals를 instanceof 기반으로 올바르게 구현했다면 CounterPoint를 건네줘도 onUnitCircle 메서드가 정상적으로 동작할 것이다.

구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만 괜찮은 우회 방법이 있다.
"상속 대신 컴포지션을 사용하라"는 [아이템 18](item18.md)의 조언에 따라 Point를 상속하는 대신 Point를 ColorPoint의 private 필드로 두고 ColorPoint와 같은 위치의 일반 Point를 반환하는 뷰(view)메서드([아이템 6](item6.md))를 public으로 추가하는 것이다.
```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    /**
     * @return ColorPoint의 Point 뷰
     */
    public Point asPoint() {
        return point;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
    // 나머지 코드는 생략
}

```

자바 라이브러리에도 이처럼 구체 클래스를 확장해 값을 추가한 클래스가 있다.
java.sql.Timestamp는 java.util.Date를 확장한 후 nanoseconds 필드를 추가했다.
Timestamp의 equals는 대칭성을 위배하여 Date 객체와 한 클래스에 넣거나 서로 섞어 사용하면 엉뚱하게 동작할 수 있어 API 설명에 Date와 혼용할 때 주의점을 언급하고 있다.
둘을 명확히 분리해 사용하기만 한다면 문제가 없지만 섞이지 않도록 보장해줄 수단이 없어 실수하면 디버깅하기 어려운 오류가 발생할 수 있다.

클래스가 불변이든 가변이든 일관성을 지키기 위해 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다.
java.net.URL의 equals는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교하는데, 호스트 이름을 IP 주소로 바꾸려면 네트워크를 통해야 하므로 그 결과가 항상 같다고 보장할 수 없다.
이는 URL의 equals가 일반 규약을 어긴 것이고 실제로 오류가 많이 발생한다.
이러한 문제를 피하기 위해서는 equals는 항상 메모리에 존재하는 객체만을 사용한 결정적(deterministic) 계산만 수행해야 한다.

null-아님은 이름처럼 모든 객체가 null과 같지 않아야 한다는 것이다. o.equals(null)이 true를 반환해서도 안되고 NullPointerException을 던져서도 안된다.
많은 클래스가 다음 코드처럼 입력이 null인지를 확인한다.
```java
@Override public boolean equals(Object o) {
    if (o == null)
        return false;
    ...
}
```
하지만 이러한 검사는 필요하지 않다. 동치성을 검사하려면 equals는 건네받은 객체를 적절히 형변환한 후 필수 필드들의 값을 알아내야 한다.
그러려면 형변환에 앞서 instanceof로 입력 매개변수가 올바른 타입인지 검사해야 한다.
```java
// 묵시적 null 검사 - 이쪽이 낫다.
@Override public boolean equals(Object o) {
    if (!(o instanceof MyType))
        return false;
    MyType mt = (MyType) o;
    ...
}
```
equals가 타입을 확인하지 않으면 잘못된 타입이 인수로 주어졌을 때 ClassCastException을 던져 일반 규약을 위배하게 된다.
하지만 instanceof는 두 번째 피연산자와 무관하게 첫 번째 피연산자가 null이면 false를 반환한다.
때문에 입력이 null인지 따로 검사하지 않아도 된다.

지금까지 설명한 equals 메서드 구현 방법을 단계별로 정리해보면
1. == 연산자를 사용해 자기 자신이 참조인지 확인한다.
2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
3. 입력을 올바른 타입으로 형변환한다. (2번에서 instanceof를 했기 때문에 무조건 통과한다)
4. 입력 객체와 자신의 대응되는 핵심 필드들이 모두 일치하는지 하나씩 검사한다.

float과 double을 제외한 기본 타입 필드는 == 연산자로 비교하고, 참조 타입 필드는 각각 equals 메서드로, float과 double 필드는 각각 static 메서드인 Float.compare(float, float)와 Double.compare(double, double)로 비교한다.
두 타입은 Float.NaN, -0.0f, 특별한 부동소수 값 등을 다우기 때문에 특별하게 취급한다.
Float.equals나 Double.equals 메서드를 사용할 수도 있지만 오토박싱을 수반할 수 있어 성능상 좋지 않다.
배열 필드는 원소 각각을 앞서의 지침대로 비교한다.
배열의 모든 원소가 핵심 필드라면 Arrays.equals 메서드들 중 하나를 사용하자.

때론 null도 정상 값으로 취급하는 참조 타입 필드도 있다.
이런 필드는 static 메서드인 Object.equals(Object, Object)로 비교해 NullPointerException 발생을 예방하자.

CaseInsensitiveString 예시처럼 비교하기가 아주 복잡한 필드를 가진 클래스도 있다.
이럴 때는 그 필드의 표준형(canonical form)을 저장해둔 후 표준형끼리 비교하면 훨씬 경제적이다.
특히 불변 클래스([아이템 17](item17.md))에서 활용하기 좋고, 가변 객체라면 값이 바뀔 때마다 표준형을 최신 상태로 갱신해주어야 한다.

어떤 필드를 먼저 비교하느냐가 equals의 성능을 좌우하기도 한다.
최선의 성능을 위해서는 다를 가능성이 더 크거나 비교하는 비용이 싼 필드를 먼저 비교하자.
동기화용 lock 필드 같이 객체의 논리적 상태와 관계 없는 필드는 비교하면 안 된다.
핵심 필드로부터 계산해낼 수 있는 파생 필드도 굳이 비교할 필요는 없지만 파생 필드를 비교하는 것이 더 빠른 경우도 있다.

equals를 다 구현했다면 대칭성, 추이성, 일관성에 대해 단위 테스트를 작성해 실행해봐야 한다.
단 equals 메서드를 AutoValue를 이용해 작성했다면 테스트를 하지 않아도 된다.
반사성과 null-아님 또한 만족해야 하지만 둘이 문제가 되는 경우는 많이 없다.
아래는 이를 모두 지킨 PhoneNumber 클래스 예시이다.
```java
public final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "지역코드");
        this.prefix = rangeCheck(prefix, 999, "프리픽스");
        this.lineNum = rangeCheck(lineNum, 9999, "가입자 번호");
    }
    
    private static short rangeCheck(int val, int max, String arg) {
        if (val < 0 || val > max) 
            throw new IllegalArgumentException(arg + ": " + val);
        return (short) val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;
        if (!(o instanceof PhoneNumber)) 
            return false;
        PhoneNumber pn = (PhoneNumber) o;
        return pn.lineNum == lineNum && pn.prefix == prefix
                && pn.areaCode == areaCode;
    }
    ... // 나머지 코드는 생략
}

```

## ❗ 주의사항
- equals를 재정의할 땐 hashCode도 반드시 재정의하자.([아이템 11](item11.md))
- 너무 복잡하게 해결하려 들지 말자. (필드의 동치성만 검사해도 equals 규약을 어렵지 않게 지킬 수 있다.)
- Object 외의 타입을 매개변수로 받는 equals는 선언하지 말자.

```java
// 잘못된 예 - 입력 타입은 반드시 Object여야 한다!
public boolean equals(MyClass o) {
    ...
}
```
위와 같은 코드는 Object.equals를 재정의(Overriding)한 것이 아닌 다중정의(Overloading)한 것이다.
기본 equals에는 영향이 없지만 '타입을 구체적으로 명시한' equals는 오히려 해가 된다.
하위 클래스의 @Override 애너테이션이 긍정 오류(false positive; 거짓 양성)를 내게 하고 보안 측면에서도 좋지 않다.
이번 절 예제 코드들처럼 @Override 애너테이션을 일관되게 사용하면 컴파일 시 에러를 발생시켜 이러한 실수를 예방할 수 있다.

## 💡 Tip
equals와 hashCode를 작성하고 테스트하는 것은 반복적이고 일관적이다.
구글이 만든 오픈소스 프레임워크인 AutoValue를 통해 이를 대체할 수 있다.
애너테이션 하나만 추가하면 메서드를 잘 작성해준다.
