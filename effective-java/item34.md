# 아이템 34. int 상수 대신 열거 타입을 사용하라
열거 타입은 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입이다.
```java
public static final int APPLE_FUJI         = 0;
public static final int APPLE_PIPPIN       = 1;
public static final int APPLE_GRANNY_SMITH = 2;

public static final int ORANGE_NAVEL  = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD  = 2;
```
이렇게 정수를 열거해서 사용하면 타입 안전을 보장할 방법이 없으며 표현력도 좋지 않다.

이러한 단점을 보완하기 위해 자바에서는 열거 타입(enum type)을 지원한다.
```java
public enum Apple  { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```
열거 타입 자페는 클래스이며, 상수 하나당 자신의 인스턴스를 하나씩 만들어 public static final 필드로 공개한다.
열거 타입은 밖에서 접근할 수 있는 생성자를 제공하지 않기 때문에 클라이언트가 직접 생성하거나 확장할 수 없어 열거 타입 선언으로 만들어진 인스턴스들은 딱 하나씩만 존재함이 보장된다.

또한 열거 타입은 컴파일타임 타입 안정성을 제공한다.
위 코드에서 Apple 열거 타입을 매개변수로 받는 메서드를 선언했다면, 건네받은 참조는 null이 아니라면 Apple의 세 가지 값 중 하나임이 확실하다.
다른 타입의 값을 넘기려 하면 컴파일 오류가 난다.
타입이 다른 열거 타입 변수를 할당하려 하거나 다른 열거 타입의 값끼리 == 연산자로 비교하려는 꼴이기 때문이다.

열거 타입에는 임의의 메서드나 필드를 추가할 수 있고, 임의의 인터페이스를 구현하게 할 수도 있다.
Object 메서드들을 높은 품질로 구현해놨고, Comparable([아이템 14](item14.md))과 Serializable([아이템 12](item12.md))을 구현해놨으며, 직렬화 형태도 웬만큼 변형을 가해도 문제없이 동작하게끔 구현해놨다.

다음은 열거 타입에 메서드나 필드를 추가하는 예시이다.
```java
public enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS  (4.869e+24, 6.052e6),
    EARTH  (5.975e+24, 6.378e6),
    MARS   (6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN (5.685e+26, 6.027e7),
    URANUS (8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);

    private final double mass;           // 질량(단위: 킬로그램)
    private final double radius;         // 반지름(단위: 미터)
    private final double surfaceGravity; // 표면중력(단위: m / s^2)

    // 중력상수(단위: m^3 / kg s^2)
    private static final double G = 6.67300E-11;

    // 생성자
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        this.surfaceGravity = G * mass / (radius * radius);
    }

    public double mass()           { return mass; }
    public double radius()         { return radius; }
    public double surfaceGravity() { return surfaceGravity; }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity; // F = ma
    }
}

```
열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다.
열거 타입은 근본적으로 불변이라 모든 필드는 final이어야 한다([아이템 17](item17.md)).
필드를 public으로 선언해도 되지만, private으로 두고 별도의 public 접근자 메서드를 두는 게 낫다([아이템 16](item16.md)).

Planet 열거 타입을 통해 어떤 객체의 지구에서의 무게를 입력받아 여덟 행성에서의 무게를 출력하는 일을 다음과 같이 짧게 작성할 수 있다.
```java
public static void main(String[] args) {
    double earthWeight = Double.parseDouble(args[0]);
    double mass = earthWeight / Planet.EARTH.surfaceGravity();
    for (Planet p : Planet.values()) {
        System.out.printf("%s에서의 무게는 %f이다.%n",
                          p, p.surfaceWeight(mass));
    }
}
```
열거 타입은 자신 안에 정의된 상수들의 값을 배열에 담아 반환하는 static 메서드인 values를 제공한다.
값들은 선언된 순서로 저장된다.

열거 타입에서 상수를 제거하면 제거한 상수를 참조하지 않는 클라이언트에는 아무 영향이 없다.
제거된 상수를 참조하는 클라이언트는 프로그램을 다시 컴파일하면 컴파일 오류가 발생한다.
컴파일하지 않으면 런타임에 오류가 발생해 수정하도록 알려준다.

열거 타입을 선언한 클래스 혹은 그 패키지에서만 유용한 기능은 private나 package-private 메서드로 만들자.
클라이언트에 노출할 합당한 이유가 없다면 private로, 필요하다면 package-private로 선언하라([아이템 15](item15.md)).

널리 쓰이는 열거 타입은 톱레벨 클래스로 만들고, 특정 톱레벨 클래스에서만 쓰인다면 해당 클래스의 멤버 클래스([아이템 24](item24.md))로 만든다.
예를 들어 소수 자릿수의 반올림 모드를 뜻하는 열거 타입인 java.math.RoundingMode는 BigDecimal이 사용한다.
그런데 RoungindMode는 BigDecimal 말고도 유용한 개념이라 톱레벨에 위치해 있다.

다음은 상수마다 다르게 동작하는 enum 타입의 예시이다.
```java
public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    // 상수가 뜻하는 연산을 수행한다.
    public double apply(double x, double y) {
        switch(this) {
            case PLUS:   return x + y;
            case MINUS:  return x - y;
            case TIMES:  return x * y;
            case DIVIDE: return x / y;
        }
        throw new AssertionError("알 수 없는 연산: " + this);
    }
}
```
동작은 하지만 코드가 아름답진 않다.
새로운 상수를 추가하면 해당 case 문도 추가해줘야 하고, 만약 깜빡한다면 런타임 에러가 발생할 수 있다.
열거 타입에 apply라는 추상 메서드를 선언하고 각 상수별 클래스 몸체(constant-specific class body)에서 자신에 맞게 재정의하는 방법이다.
이를 상수별 메서드 구현(constant-specific method implementation)이라 한다.

```java
public enum Operation {
    PLUS  {public double apply(double x, double y){return x + y;}},
    MINUS {public double apply(double x, double y){return x - y;}},
    TIMES {public double apply(double x, double y){return x * y;}},
    DIVIDE{public double apply(double x, double y){return x / y;}};

    public abstract double apply(double x, double y);
}
```
apply 메서드가 상수 선언 바로 옆에 붙어 있으니 새로운 상수를 추가할 때 apply 메서드를 재정의해야 한다는 것을 기억하기 좋다.
또한 apply가 abstract 메서드이므로 재정의하지 않으면 컴파일 에러가 발생한다.

상수별 메서드 구현을 상수별 데이터와 결합할 수도 있다.
다음은 Operation의 toString을 재정의해 해당 연산을 뜻하는 기호를 반환하도록 했다.
```java
public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    @Override public String toString() { return symbol; }
    public abstract double apply(double x, double y);
}

```
다음과 같은 코드를 실행하고 2와 4를 입력하면
```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    for (Operation op : Operation.values()) 
        System.out.printf("%f %s %f = %f%n",
                          x, op, y, op.apply(x, y));
}
```
다음과 같은 결과를 볼 수 있다.
```text
2.000000 + 4.000000 = 6.000000
2.000000 - 4.000000 = -2.000000
2.000000 * 4.000000 = 8.000000
2.000000 / 4.000000 = 0.500000
```

열거 타입에는 상수 이름을 입력받아 그 이름에 해당하는 상수를 반환해주는 valueOf(String) 메서드가 자동 생성된다.
열거 타입의 toString 메서드를 재정의할 때는 toString이 반환하는 문자열을 해당 열거 타입 상수로 변환해주는 fromString 메서드도 함께 제공하는 걸 고려해보자.
아래 코드는 열거 타입에서 사용할 수 있도록 구현한 fromString이다.
```java
private static final Map<String, Operation> stringToEnum =
        Stream.of(values()).collect(
            toMap(Object::toString, e -> e));

// 지정한 문자열에 해당하는 Operation을 (존재한다면) 반환한다.
public static Optional<Operation> fromString(String symbol) {
    return Optional.ofNullable(stringToEnum.get(symbol));
}
```

Operation 상수가 stringToEnum 맵에 추가되는 시점은 열거 타입 상수 생성 후 static 필드가 초기화될 때다.
앞의 코드는 values 메서드가 반환하는 배열 대신 stream을 사용했다.

상수별 메서드 구현에는 열거 타입 상수끼리 코드를 공유하기 어렵다는 단점이 있다.
```java
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,
    SATURDAY, SUNDAY;

    private static final int MINS_PER_SHIFT = 8 * 60;

    int pay(int minutessWorked, int payRate) {
        int basePay = hoursWorked * payRate;

        int overtimePay;
        switch (this) {
            case SATURDAY: case SUNDAY: // 주말
                overtimePay = basePay / 2;
                break;
            default: // 주중
                overtimePay = hoursWorked <= HOURS_PER_SHIFT ?
                  0 : (minutesWorked - MINS_PER_SHIFT) * payRate * 2;
        }
        return basePay + overtimePay;
    }
}

```
이 열거 타입은 직원의 시간당 기본 임금과 그날 일한 시간이 주어지면 일당을 계산해주는 메서드를 갖고 있다.
주중에 오버타임이 발생하면 잔업수당이 주어지고, 주말에는 무조건 잔업수당이 주어진다.
간결하긴 하지만, 관리 관점에서는 위험한 코드이다.
휴가와 같은 새로운 값을 열거 타입에 추가하려면 그 값을 처리하는 case 문도 같이 추가해줘야 한다.

상수별 메서드 구현으로 급여를 정확히 계산하는 방법은 두 가지다.
잔업수당을 계산하는 코드를 모든 상수에 중복해서 넣거나, 계산 코드를 평일용과 주말용으로 나눠 각각을 도우미 메서드로 작성한 다음 각 상수가 자신에게 필요한 메서드를 적절히 호출하면 된다.
두 방식 모두 코드가 길어져 가독성이 떨어지고 오류 발생 가능성이 높아진다.

가장 깔끔한 방법은 새로운 상수를 추가할 때 잔업수당 '전략'을 선택하도록 하는 것이다.
잔업수당 계산을 private 중첩 열거 타입(아래 코드의 PayType)으로 옮기고 PayrollDay 열거 타입의 생성자에서 이 중 적당한 것을 선택한다.
그러면 PayrollDay 열거 타입은 잔업수당 계산을 그 전략 열거 타입에 위임하여, switch 문이나 상수별 메서드 구현이 필요 없어진다.
이 패턴은 switch 문보다 복잡하지만 더 안전하고 유연하다.
```java
public enum PayrollDay {
    MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WEDNESDAY(WEEKDAY),
    THURSDAY(WEEKDAY), FRIDAY(WEEKDAY),
    SATURDAY(WEEKEND), SUNDAY(WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) { this.payType = payType; }

    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }

    // 전략 열거 타입
    enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked <= MINS_PER_SHIFT ? 0 :
                       (minsWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };

        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;

        int pay(int minsWorked, int payRate) {
            int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }
}

```

switch 문은 열거 타입의 상수별 동작을 구현하는 데 적합하지 않다.
하지만 기존 열거 타입에 상수별 동작을 혼합해 넣을 때는 좋은 선택이 될 수 있다.
Operation 열거 타입에서 각 연산의 반대 연산을 반환하는 메서드가 필요하다고 해보자.
```java
public static Operation inverse(Operation op) {
    switch(op) {
        case PLUS:   return Operation.MINUS;
        case MINUS:  return Operation.PLUS;
        case TIMES:  return Operation.DIVIDE;
        case DIVIDE: return Operation.TIMES;

        default: throw new AssertionError("알 수 없는 연산: " + op);
    }
}
```
추가하려는 메서드가 의미상 열거 타입에 속하지 않는다면 직접 만든 열거 타입이라도 이 방식을 적용하는 것이 좋다.

필요한 원소를 컴파일타입에 다 알 수 있는 상수 집합이라면 항상 열거 타입을 사용하자.
열거 타입에 정의된 상수 개수가 영원히 고정 불변일 필요는 없다.
