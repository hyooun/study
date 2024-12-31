# 아이템 17. 변경 가능성을 최소화하라
불변 클래스란 간단히 말해 그 인스턴스의 내부 값을 수정할 수 없는 클래스다.
불변 인스턴스의 정보는 고정되어 객체가 파괴되는 순간까지 절대 달라지지 않는다.
자바 플랫폼 라이브러리에도 다양한 불변 클래스가 있는데, String, 기본 타입의 박싱된 클래스들, BigInteger, BigDecimal 등이 있다.

불변 클래스는 가변 클래스보다 설계하고 구현하고 사용하기 쉬우며, 오류가 생길 여지도 적고 훨씬 안전하다.
클래스를 불변으로 만들려면 다음 다섯 가지 규칙을 따르면 된다.
> - 객체의 상태를 변경하는 메서드(변경자)를 제공하지 않는다.
> - 클래스를 확장할 수 없도록 한다.
>     - 하위 클래스에서 부주의하게 혹은 나쁜 의도로 객체의 상태를 변경하는 것을 막는다. 상속을 막는 대표적인 방법은 클래스를 final로 선언하는 것이다.
> - 모든 필드를 final로 선언한다.
>     - 시스템이 강제하는 수단을 이용해 설계자의 의도를 명확하게 드러내는 방법이다. 새로 생성된 인스턴스를 동기화 없이 다른 스레드로 넘겨도 동일하게 동작하게끔 보장하는 데도 필요하다.
> - 모든 필드를 private로 선언한다.
>     - 필드가 참조하는 가변 객체를 클라이언트에서 직접 접근해 수정하는 것을 막는다. 기술적으로는 기본 타입 필드나 불변 객체를 참조하는 필드를 public final로만 선언해도 불변 객체가 되지만, 이렇게 하면 다음 릴리즈에서 내부 표현을 바꾸지 못하므로 권하지는 않는다. ([아이템 15](item15.md), [아이템 16](item16.md))
> - 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
>     - 클래스에 가변 객체를 참조하는 필드가 하나라도 있다면 클라이언트에서 그 객체의 참조를 얻을 수 없도록 해야 한다. 이런 필드는 절대 클라이언트가 제공한 객체 참조를 가리키레 해서는 안 되며, 접근자 메서드가 그 필드를 그대로 반환해서도 안 된다. 생성자, 접근자, readObject 메서드([아이템 88](item88.md)) 모두에서 방어적 복사를 수행하라.

```java
public final class Complex {
    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart()      { return re; }
    public double imaginaryPart() { return im; }

    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }

    public Complex minus(Complex c) {
        return new Complex(re - c.re, im - c.im);
    }

    public Complex times(Complex c) {
        return new Complex(re * c.re - im * c.im,
                           re * c.im + im * c.re);
    }

    public Complex dividedBy(Complex c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new Complex((re * c.re + im * c.im) / tmp,
                           (im * c.re - re * c.im) / tmp);
    }

    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Complex))
            return false;
        Complex c = (Complex) o;

        // == 대신 compare를 사용하는 이유는 아이템 10 참고
        return Double.compare(re, c.re) == 0
            && Double.compare(im, c.im) == 0;
    }

    @Override public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    @Override public String toString() {
        return "(" + re + " + " + im + "i)";
    }
}

```
이 클래스는 복소수(실수부와 허수부로 구성된 수)를 표현한다.
Object의 메서드 몇 개를 재정의했고, 실수부와 허부수 값을 반환하는 접근자 메서드와 사칙연산 메서드를 정의했다.
이 사칙연산 메서드들은 인스턴스 자신을 수정하는 것이 아닌, 새로운 Complex 인스턴스를 만들어 반환한다.
이처럼 피연산자에 함수를 적용해 그 결과를 반환하지만, 피연산자 자체는 그대로인 프로그래밍 패턴을 함수형 프로그래밍이라 한다.

이와 달리 절차적 혹은 명령형 프로그래밍에서는 메서드에서 피연산자인 자신을 수정해 자신의 형태가 변하게 된다.
또한 메서드 이름으로 add같은 동사 대신 plus같은 전치사를 사용했는데, 이는 해당 메서드가 객체의 값을 변경하지 않는 것을 강조하려는 의도이다.

함수형 프로그래밍에 익숙하지 않다면 부자연스러워 보일 수 있지만, 이 방식으로 프로그래밍하면 코드에서 불변이 되는 영역의 비율이 높아진다.
불변 객체는 생성된 시점의 상태를 파괴될 때까지 유지하기 때문에 단순하다.
모든 생성자가 클래스 불변식(class invariant)을 보장한다면 그 클래스를 사용하는 프로그래머가 다른 노력을 하지 않더라도 영원이 불변으로 남는다.

불변 객체는 근본적으로 스레드-안전(thread-safe)하여 따로 동기화할 필요가 없다.
여러 스레드가 동시에 사용해도 절대 훼손되지 않는다.
불변 객체에 대해서는 그 어떤 스레드도 다른 스레드에 영향을 줄 수 없으니 불변 객체는 안심하고 공유할 수 있다.

따라서 불변 클래스라면 한번 만든 인스턴스를 최대한 재활용하는 것이 좋다.
가장 쉬운 방법은 자주 쓰이는 값들을 상수(public static final)로 제공하는 것이다.
예를 들어, Complex 클래스는 다음과 같은 상수들을 제공할 수 있다.
```java
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE  = new Complex(1, 0);
public static final Complex I    = new Complex(0, 1);
```
이러한 방법으로 불변 클래스는 자주 사용되는 인스턴스를 캐싱하여 중복 생성되지 않도록 static 팩터리 메서드([아이템 1](item1.md))를 제공할 수 있다.
박싱된 기본 타입 클래스 전부와 BigInteger가 이 방법을 사용한다.
이런 static 팩터리를 사용하면 여러 클라이언트가 인스턴스를 공유하여 메모리 사용량과 가비지 컬렉션 비용이 줄어든다.
새로운 클래스를 설계할 때 public 생성자 대신 static 팩터리를 만들어두면, 클라이언트를 수정하지 않고도 필요에 따라 캐싱 기능을 나중에 추가할 수 있다.

불변 객체를 자유롭게 공유할 수 있다는 점은 방어적 복사([아이템 50](item50.md))도 필요 없다는 결론으로 도달한다.
아무리 복사해도 원본과 같으니 복사하는 의미가 없다.
그러니 불변 클래스는 clone 메서드나 복사 생성자([아이템 13](item13.md))를 제공하지 않는 것이 좋다.
String 클래스의 복사 생성자는 이 개념을 모른 채 초창기에 만들어진 것으로 사용하지 않는 것이 좋다.

불변 객체는 자유롭게 공유할 수 있고, 불변 객체끼리는 내부 데이터를 공유할 수 있다.
BigInteger 클래스는 내부에서 값의 부호(sign)와 크기(magnitude)를 따로 표현한다.
부호에는 int 변수를, 크기(절댓값)에는 int 배열을 사용한다.
```java
public class BigInteger extends Number implements Comparable<BigInteger> {
    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero <em>must</em> have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     */
    final int signum;

    /**
     * The magnitude of this BigInteger, in <i>big-endian</i> order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    final int[] mag;
    ...
}
```

한편 negate 메서드는 크기가 같고 부호만 반대인 새로운 BigInteger를 생성하는데, 이때 배열은 비록 가변이지만 복사하지 않고 원본 인스턴스만 공유해도 된다.
그 결과 새로 만든 BigInteger 인스턴스도 원본 인스턴스가 가리키는 내부 배열을 그대로 가리킨다.
```java
public BigInteger negate() {
    return new BigInteger(this.mag, -this.signum);
}
```

객체를 만들 때 다른 불변 객체들을 구성요소로 사용하면 이점이 많다.
값이 바뀌지 않는 구성요소들로 이뤄진 객체라면 그 구조가 복잡하더라도 불변식을 유지하기 훨씬 수월하다.
좋은 예시로, 불변 객체는 Map의 key와 Set의 원소로 사용하기 적합하다.
이 값들이 바뀌면 불변식이 허물어지는데, 불변 객체를 사용하면 그런 걱정을 하지 않아도 된다.

불변 객체는 그 자체로 실패 원자성(failure atomicity; 메서드에서 예외가 발생한 후에도 그 객체는 메서드 호출 전과 똑같은 유효한 상태이다.)을 제공한다.
상태가 절대 변하지 않으니 당연하다.

불변 클래스에도 단점은 존재한다.
값이 다르면 반드시 독립된 객체로 만들어야 한다는 것이다.
값의 가짓수가 많다면 이들을 모두 만들어야 하니 비용이 커진다.
백만 비트짜리 BigInteger에서 비트 하나를 바꿔야 한다고 가정해보자.
```java
BigInteger moby = ...;
moby = moby.flipBit(0);
```
flipBit 메서드는 단지 한 비트 다른 백만 비트짜리 새로운 BigInteger 인스턴스를 생성한다.
연산도 BigInteger 크기에 비례해 시간과 공간 소요가 발생한다.

BitSet도 BigInteger처럼 임의 길이의 비트 순열을 표현하지만 '가변'이다.
BitSet 클래스는 원하는 비트 하나만 상수 시간 안에 바꿔주는 메서드를 제공한다
```java
BitSet moby = ...;
moby.flip(0);
```
원하는 객체를 완성하기까지 단계가 많고, 중간 단계에서 만들어진 객체들이 모두 버려진다면 성능 문제가 발생한다.
두 가지 해결법이 있는데, 첫 번째는 흔히 쓰일 다단계 연산(multistep operation)들을 예측하여 기본 기능으로 제공하는 방법이다.
이를 제공하면 각 단계마다 객체를 생성하지 않아도 된다.
BigInteger는 모듈러 지수 같은 다단계 연산 속도를 높여주는 가변 동반 클래스(companion class)를 package-private로 두고 있다.
앞서 언급한 이유들로 이 클래스를 직접 사용하는 것은 훨씬 어렵지만 BigInteger가 대신 처리해준다.

클라이언트들이 원하는 복잡한 연산들을 정확히 예측할 수 있다면 package-private의 가변 동반 클래스만으로 충분하다.
자바 플랫폼 라이브러리에서 대표적인 예시로 String이 있다.
String의 가변 동반 클래스는 StringBuilder이다.

클래스가 불변함을 보장하려면 자신을 상속하지 못하도록 해야 한다.
가장 쉬운 방법은 final 클래스로 선언하는 것이지만, 더 유연한 방법이 있다.
모든 생성자를 private 혹은 package-private로 만들고 public static 팩터리를 제공하는 방법이다([아이템 1](item1.md)).
```java
public final class Complex {
    private final double re;
    private final double im;

    private Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }
    ... // 나머지 코드는 생략
}

```
이 방식이 최선일 때가 많다.
바깥에서 볼 수 없는 package-private 구현 클래스를 원하는 만큼 만들어 활용할 수 있으니 훨씬 유연하다.
패키지 바깥의 클라이언트에서 바라본 이 불변 객체는 사실상 final이다. public이나 protected 생성자가 없으니 다른 패키지에서는 이 클래스를 확장하는게 불가능하다.
정적 팩터리 방식은 다수의 구현 클래스를 활용한 유연성을 제공하고, 다음 릴리스에서 객체 캐싱 기능을 추가해 성능을 끌어올릴 수도 있다.

BigInteger나 BigDecimal을 설계할 당시엔 불변 객체를 상속하지 못하게 해야 한다는 개념이 잡히지 않았다.
때문에 두 클래스의 메서드들은 재정의가 가능하고 아직까지 고쳐지지 않았다.
신뢰할 수 없는 클라이언트로부터 BigInteger나 BigDecimal 인스턴스를 인수로 받는다면 확인이 필요하다.
이 인수들을 가변이라고 가정하고 방어적 복사하여 사용하자.
```java
public static BigInteger safeInstance(BigInteger val) {
    return val.getClass() == BigInteger.class ?
            val : new BigInteger(val.toByteArray());
}
```

성능을 위해서 "불변 클래스는 모든 필드가 final이고 어떤 메서드도 그 객체를 수정할 수 없어야 한다."를 다음과 같이 완화할 수 있다.
"어떤 메서드도 객체의 상태 중 외부에 비치는 값을 변경할 수 없다."
어떤 불변 클래스는 계산 비용이 큰 값을 나중에 처음 쓰일 때 계산하여 final이 아닌 필드에 캐싱해 놓기도 한다.
똑같은 값을 다시 요철하면 캐싱해둔 값을 반환하며 계산 비용을 줄일 수 있다.
지연 초기화([아이템 83](item83.md))의 예이기도 한 이 기법을 String에서도 사용한다.

## 정리
getter가 있다고 해서 무조건 setter를 만들지는 말자.
클래스는 꼭 필요한 경우가 아니라면 불변이어야 한다.
불변 클래스는 장점이 많으며, 단점은 특정 상황에서의 잠재적 성능 저하 뿐이다.
PhoneNumber나 Complex같은 단순한 값 객체는 무조건 불변으로 만들자.

String과 BigInteger처럼 무거운 값 객체도 불변으로 만들 수 있는지 생각해봐야 한다.
성능 때문에 어쩔 수 없다면 불변 클래스와 쌍을 이루는 가변 동반 클래스를 public 클래스로 제공하도록 하자.

모든 클래스를 불변으로 만들 수는 없다. 
불변으로 만들 수 없는 클래스라도 변경할 수 있는 부분을 최소한으로 줄이자.
객체가 가질 수 있는 상태의 수를 줄이면 그 객체를 예측하기 쉬워지고 오류가 생길 가능성이 줄어든다.
그러니 꼭 변경해야 할 필드를 제외하고는 모두 final로 선언하자.

이번 아이템과 [아이템 15](item15.md)을 종합하면 다음과 같다.
### 다른 합당한 이유가 없다면 모든 필드는 private final이어야 한다.

생성자는 불변식 설정이 모두 완료된, 초기화가 완벽히 끝난 상태의 객체를 생성해야 한다.
확실한 이유가 없다면 생성자와 static 팩터리 외에는 그 어떠한 초기화 메서드도 public으로 제공되어서는 안 된다.
객체를 재활용할 목적으로 상태를 다시 초기화하는 메서드도 복잡성만 커지고 성능 이점은 거의 없다.

java.util.concurrent 패키지의 CountDownLatch 클래스가 이 원칙을 잘 따르고 있다.
비록 가변 클래스지만 가질 수 있는 상태의 수가 많지 않고, 인스턴스를 생성해 한 번 사용하고 끝난다.
```java
package java.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class CountDownLatch {
    /**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *        before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * <p>If the current count is zero then this method returns immediately.
     *
     * <p>If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of two things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the current count is zero then this method returns immediately
     * with the value {@code true}.
     *
     * <p>If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of three things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     *
     * <p>If the count reaches zero then the method returns with the
     * value {@code true}.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the count reached zero and {@code false}
     *         if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Decrements the count of the latch, releasing all waiting threads if
     * the count reaches zero.
     *
     * <p>If the current count is greater than zero then it is decremented.
     * If the new count is zero then all waiting threads are re-enabled for
     * thread scheduling purposes.
     *
     * <p>If the current count equals zero then nothing happens.
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * Returns the current count.
     *
     * <p>This method is typically used for debugging and testing purposes.
     *
     * @return the current count
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * Returns a string identifying this latch, as well as its state.
     * The state, in brackets, includes the String {@code "Count ="}
     * followed by the current count.
     *
     * @return a string identifying this latch, as well as its state
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}

```
