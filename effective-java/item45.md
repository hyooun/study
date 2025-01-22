# 아이템 45. 스트림은 주의해서 사용하라
스트림 API는 다량의 데이터 처리 작업을 편리하게 하고자 자바 8에 추가되었다.
스트림(stream)은 데이터 원소의 유한 혹은 무한 sequence를 뜻한다.
스트림 파이프라인(stream pipeline)은 이 원소들로 수행하는 연산 단계를 표현하는 개념이다.

스트림의 원소들은 컬렉션, 배열, 파일, regular expression pattern matcher, 난수 생성기, 또 다른 스트림 등에서 올 수 있다.
스트림 안의 데이터 원소들을 객체 참조나 기본 타입(int, long, double) 값이다.

스트림 파이프라인은 source stream에서 하나 이상의 중간 연산(intermediate operation)을 거쳐 종단 연산(terminal operation)으로 끝난다.
각 중간 연산은 스트림을 변환(transform)한다. 
예를 들어, 각 원소에 함수를 적용하거나 특정 조건을 만족하지 못하는 원소를 걸러낼 수 있다.
중간 연산들은 모두 한 스트림을 다른 스트림으로 변환하는데, 변환된 스트림의 원소 타입은 기존과 다를 수 있다.
종단 연산은 마지막 중간 연산을 마치고 원소를 정렬해 컬렉션에 담거나, 특정 원소 하나를 선택하거나, 모든 원소를 출력하는 식의 연산을 수행한다.

스트림 파이프라인은 지연 평가(lazy evaluation)된다.
이는 종단 연산이 호출될 때 이루어지며, 종단 연산에 쓰이지 않는 원소는 계산에 쓰이지 않는다.
때문에 종단 연산이 없는 스트림 파이프라인은 아무 일도 하지 않는 명령어와 같으므로 꼭 포함해야 한다.

스트림 API는 method chaining을 지원하는 fluent API다.
파이프라인 하나를 구성하는 모든 호출을 연결하여 하나의 표현식으로 완성할 수 있고, 파이프라인 여러 개를 연결해 표현식 하나로 만들 수도 있다.

기본적으로 스트림 파이프라인은 순차적으로 실행된다.
병렬로 실행하려면 스트림 중 하나에서 parallel 메서드를 호출하기만 하면 되나, 효과를 볼 수 있는 상황은 적다([아이템 48](item48.md)).

아래의 Anagrams 클래스는 사전 파일에서 단어를 읽어 사용자가 지정한 값보다 원소 수가 많은 anagram 그룹을 출력한다.
anagram은 스펠링을 구성하는 알파벳이 같고 순서만 다른 단어를 의미한다.
```java
public class Anagrams {
    public static void main(String[] args) throws IOException {
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        Map<String, Set<String>> groups = new HashMap<>();
        try (Scanner s = new Scanner(dictionary)) {
            while (s.hasNext()) {
                String word = s.next();
                groups.computeIfAbsent(alphabetize(word),
                    (unused) -> new TreeSet<>()).add(word);
            }
        }

        for (Set<String> group : groups.values())
            if (group.size() >= minGroupSize)
                System.out.println(group.size() + ": " + group);
    }

    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}

```
이 코드에서 map에 각 단어를 삽입할 때 자바 8에 추가된 computeIfAbsent 메서드를 사용했다.
이 메서드는 맵 안에 key가 있는지 찾고, 있으면 단순히 매핑된 값을 반환한다.
없으면 건네진 함수 객체를 key에 적용하여 value를 계산한 다음 key와 value를 매핑하고 계산된 값을 반환한다.

```java
public class Anagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(
                groupingBy(word -> word.chars().sorted()
                            .collect(StringBuilder::new,
                            (sb, c) -> sb.append((char) c),
                            StringBuilder::append).toString()))
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .map(group -> group.size() + ": " + group)
                .forEach(System.out::println);
        }
    }
}
```
이 코드는 이전과 같은 작업을 수행하지만, 스트림을 과하게 사용했다.
확실히 코드가 줄어들긴 했지만 읽기가 어렵다.
이처럼 스트림을 과하게 사용하면 가독성과 유지보수성이 떨어진다.

다음과 같이 스트림을 적당히 사용하면 짧은 코드와 가독성을 모두 챙길 수 있다.
```java
public class Anagrams {
    public static void main(String[] args) throws IOException {
        Path dictionary = Paths.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphabetize(word)))
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .forEach(g -> System.out.println(g.size() + ": " + g));
        }
    }
    // alphabetize method 는 이전과 같다.
}
```
try-with-resources 블록에서 사전 파일을 열고, 파일의 모든 라인으로 구성된 스트림을 얻는다.
이 스트림 파이프라인엔 중간 연산이 없고, 종단 연산에서 모든 단어를 collect해 Map으로 모은다.
그 다음 이 맵의 values()가 반환한 값으로부터 새로운 `Stream<List<String>>` 스트림을 연다.
리스트 중 minGroupSize보다 크기가 작은 것들은 filter로 걸러지고, 종단 연산인 forEach를 통해 출력한다.

alphabetize 메서드도 스트림을 사용해 다르게 구현할 수 있지만, 명확성이 떨어지고 잘못 구현될 가능성이 크다.
자바가 기본 타입인 char에 대한 스트림은 지원하지 않기 때문이다.
```java
"Hello world!".chars().forEach(System.out::print);
```
이런 코드를 작성하면 `Hello world`를 출력하리라 기대했겠지만 `721011081081113211911111410810033`을 출력한다.
`"Hello world!".chars()`가 반환하는 스트림의 원소는 char가 아닌 int 값이기 때문에 정숫값을 출력하는 print 메서드가 호출된 것이다.
의도처럼 `Hello world`를 출력하게 하려면 다음과 같이 형변환을 명시적으로 해주면 된다.
```java
"Hello world!".chars().forEach(x -> System.out.print((char) x));
```
하지만 번거롭고 그다지 효율적인 방법도 아니니 char 값들을 처리할 때는 스트림을 사용하지 않는 편이 낫다.

스트림 파이프라인은 반복적인 계산을 함수 객체(주로 람다나 메서드 참조)로 표현한다.
반복 코드에서는 코드 블록을 사용해 표현한다.
다음과 같은 경우는 코드 블록에서는 가능하지만, 함수 객체로는 할 수 없어 스트림을 사용하기에 부적합하다.
- 코드 블록에서는 범위 안의 지역변수를 읽고 수정할 수 있지만, 람다는 final이거나 사실상 final인 변수만 읽을 수 있고, 지역변수 수정이 불가하다.
- 코드 블록에서는 return 문을 사용해 메서드에서 빠져나가거나, break나 continue 문으로 블록 바깥의 반복문을 종료하거나 건너뛸 수 있다. 메서드 선언에 명시된 검사 예외도 던질 수 있다. 하지만 람다는 이러한 처리가 불가하다.

반대로 다음과 같은 작업은 스트림을 사용하기에 적합하다.
- 원소들의 시퀀스를 일관되게 변환한다.
- 원소들의 시퀀스를 필터링한다.
- 원소들의 시퀀스를 하나의 연산을 사용해 결합한다.
- 원소들의 시퀀스를 컬렉션에 모은다.
- 원소들의 시퀀스에서 특정 조건을 만족하는 원소를 찾는다.

스트림으로 처리하기 어려운 경우도 있다.
예를 들어, 한 데이터가 파이프라인의 여러 단계를 통과할 때 이 데이터의 각 단계에서의 값들에 동시에 접근하기 어려운 경우다.
스트림 파이프라인은 한 값을 다른 값에 매핑하고 나면 원래의 값은 잃기 때문이다.

<p>2<sup>p</sup> - 1의 형태를 가진 수 중에서 소수인 경우를 메르센 소수(Mersenne prime)라고 한다.
다음은 BigIntger의 static 멤버들을 static import 하고 무한 스트림을 반환하는 메서드다.</p>

```java
static Stream<BigInteger> primes() {
    return Stream.iterate(TWO, BigInteger::nextProbablePrime);
}
```
이 코드를 사용해 작성한 처음 20개의 메르센 소수(Mersenne prime)를 출력하는 프로그램은 다음과 같다.
```java
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50))
        .limit(20)
        .forEach(System.out::println);
}
```
소수들을 사용해 메르센 수를 계산하고 결과값이 소수인 경우만 남여 메르센 소수 20개를 걸러 낸다.
만약 각 메르센 소수의 지수 p를 출력하고 싶다고 해보자.
이 값은 초기 스트림에서만 알 수 있어 종단 연산을 하는 시점에서는 알 수 없다.
이 경우엔 첫 번째 중간 연산에서 수행한 매핑을 거꾸로 수행해 계산할 수 있다.
```java
.forEach(mp -> System.out.println(mp.bitLength() + ": " + mp));
```

다음은 enum 타입인 rank와 suit를 가지는 Card라는 불변 값 클래스가 있다고 해보자.
두 집합의 원소들로 만들 수 있는 가능한 모든 조합인 Cartesian product를 구하는 코드이다.
```java
private static List<Card> newDeck() {
    List<Card> result = new ArrayList<>();
    for (Suit suit : Suit.values())
        for (Rank rank : Rank.values())
            result.add(new Card(suit, rank));
    return result;
}
```
이를 스트림을 사용하도록 변경하면 다음과 같다.
```java
private static List<Card> newDeck() {
    return Stream.of(Suit.values())
        .flatMap(suit ->
            Stream.of(Rank.values())
                .map(rank -> new Card(suit, rank)))
        .collect(toList());
}
```
이러한 수준의 차이는 취향에 따라 갈리므로 팀원들의 스트림에 대한 이해도에 따라 결정하면 될 것 같다.
이처럼 직접 코드를 작성해보고 나은 쪽을 선택하면 된다.
