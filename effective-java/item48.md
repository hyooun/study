# 아이템 48. 스트림 병렬화는 주의해서 적용하라
자바 8부터 parallel 메서드만 한 번 호출하면 파이프라인을 병렬 실행할 수 있는 스트림을 지원한다. 
동시성 프로그래밍을 할 때는 안전성과 응답 가능 상태를 유지하는 것을 고려해야 한다.
이는 병렬 스트림 파이프라인에서도 마찬가지이며, [아이템 45](item45.md)에서 다루었던 메르센 소수를 생성하는 프로그램을 다시 살펴보자.
```java
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50))
        .limit(20)
        .forEach(System.out::println);
}
static Stream<BigInteger> primes() {
    return Stream.iterate(TWO, BigInteger::nextProbablePrime);
}
```
이 프로그램의 성능을 향상시키기 위해 스트림 파이프라인의 parallel()을 호출했다고 해보자.
하지만 의도와는 다르게 이 프로그램은 아무것도 출력하지 않고 CPU 점유율만 올라갈 것이다.
그 이유는 스트림 라이브러리가 이 파이프라인을 병렬화하는 방법을 찾아내지 못했기 때문이다.
데이터 소스가 Stream.iterate거나 중간 연산에 limit을 사용하면 파이프라인 병렬화로는 성능이 향상되지 않는다.

대체로 스트림의 소스가 ArrayList, HashMap, HashSet, ConcurrentHashMap의 인스턴스나 배열, int 범위, long 범위일 때 병렬화의 효과가 가장 좋다.
이 자료구조들은 데이터를 원하는 크기로 정확하게 나눌 수 있어서 일을 다수의 스레드에 분배하기 좋다는 특징이 있다.
나누는 작업은 Spliterator가 담당하며, Spliterator 객체는 Stream이나 Iterable의 spliterator 메서드로 얻어올 수 있다.

또한 이 자료구조들은 원소들을 순차적으로 실행할 때의 참조 지역성(locality of reference)이 뛰어나다.
이는 이웃한 원소의 참조들이 메모리에 연속해서 저장되어 있다는 의미이다.
참조들이 가리기는 실제 객체가 메모리에서 서로 떨어져 있는, 참조 지역성이 낮은 경우 스레드는 데이터가 주 메모리에서 캐시 메모리로 전송되어 오는 것을 기다려야 하기 때문에 작업 시간이 늘어진다.

스트림 파이프라인의 종단 연산의 동작 방식도 병렬 수행 효율에 영향을 준다.
종단 연산에서 수행하는 작업량이 파이프라인 전체 작업에서 비중이 높고 순차적인 연산이라면 파이프라인 병렬 수행의 효과는 제한된다.

종단 연산 중 병렬화에 가장 적합한 연산은 축소(reduction)이다.
이는 파이프라인에서 만들어진 모든 원소를 하나로 합치는 작업으로, Stream의 reduce 메서드 중 하나, 혹은 min, max, count, sum과 같이 완성된 형태로 제공되는 메서드 중 하나를 수행한다.

anyMatch, allMatch, noneMatch처럼 조건에 맞으면 바로 반환되는 메서드도 병렬화에 적합하다.
반면 가변 축소(mutable reduction)을 수행하는 Stream의 collect 메서드는 컬렉션들을 합치는 비용이 커서 병렬화에 적합하지 않다. 

직접 구현한 Stream, Iterable, Collection이 병렬화의 이점을 활용하고 싶다면 spliterator 메서드를 재정의하고 성능 테스트를 수행해야 한다.
스트림을 잘못 병렬화하면 성능이 나빠지고 결과 자체가 잘못되거나 예상치 못한 동작이 발생할 수 있다.
때문에 Stream 명세는 이때 사용되는 함수 객체에 관한 규약을 정의해놨다.

Stream의 reduce 연산에 건네지는 accumulator와 combiner 함수는 반드시 결합법칙을 만족하고, 간섭받지 않고(non-interfering) stateless해야 한다.
이를 지키지 못하면 순차적인 실행해서는 문제가 없을 수 있지만, 병렬로 수행하면 문제가 발생할 것이다.
따라서 앞서 병렬화한 메르센 소수 프로그램은 완료되더라도 순서가 올바르지 않을 수 있다.
출력 순서를 순차 버전처럼 정렬하고 싶다면 종단 연산 forEach 대신 forEachOrdered를 사용하면 된다.

스트림 병렬화는 오직 성능 최적화 수단임을 기억해야 한다.
다른 최적화와 마찬가지로 변경 전후의 성능을 테스트하여 사용할 가치가 있는지를 판단해야 한다.
이상적으로는 운영 시스템과 흡사한 환경에서 테스트하는 것이 좋으며, 보통 병렬 스트림 파이프라인도 공동의 fork-join pool에서 수행되므로(같은 스레드 풀을 사용하므로) 잘못된 파이프라인은 시스템 전체 성능에 악영향을 줄 수 있음을 명심하자.

실제로, 스트림 병렬화가 효과를 보는 경우는 그리 많지 않다.
다만 조건을 만족한다면 paralell 메서드 호출 하나로 프로세서 코어 수에 비례하는 성능 향상을 낼 수 있다.

스트림 파이프라인 병렬화를 적용하면 좋은 사례를 알아보자
```java
static long pi(long n) {
    return LongStream.rangeClosed(2, n)
        .mapToObj(BigInteger::valueOf)
        .filter(i -> i.isProbablePrime(50))
        .count();
}
```
이 코드에 paralell 메서드만을 사용해서 병렬화한 코드는 다음과 같다.
```java
static long pi(long n) {
    return LongStream.rangeClosed(2, n)
        .parallel()
        .mapToObj(BigInteger::valueOf)
        .filter(i -> i.isProbablePrime(50))
        .count();
}
```
이 경우 filter 연산이 각 숫자에 대해 독립적으로 실행되고, 판별 결과가 다른 요소의 처리에 영향을 주지 않으므로 병렬 스트림이 효율적이다.

무작위 수들로 이뤄진 스트림을 병렬화하려거든 ThreadLocalRandom보다는 SplittableRandom을 활용하자.
SplittableRandom은 이 상황을 위해 설계된 것이라 병렬화하면 성능이 선형적으로 증가한다.
ThreadLocalRandom은 단일 스레드에서 사용하기 위해 설계되어 병렬 스트림용 데이터 처리에 사용은 가능하지만 SplittableRandom만큼 빠르진 않다.
