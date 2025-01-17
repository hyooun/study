# 아이템 40. @Override 애너테이션을 일관되게 사용하라
`@Override`는 메서드 선언에만 달 수 있으며, 이 애너테이션이 달렸다는 것은 상위 타입 메서드를 재정의했음을 뜻한다.
이를 잘 사용하면 여러 버그들을 예방할 수 있다.
```java
public class Bigram {
    private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Bigram b) {
        return b.first == first && b.second == second;
    }

    public int hashCode() {
        return 31 * first + second;
    }

    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++)
            for (char ch = 'a'; ch <= 'z'; ch++)
                s.add(new Bigram(ch, ch));
        System.out.println(s.size());
    }
}

```
main에서 같은 소문자 2개로 구성된 Bigram 26개를 10번 반복해 set에 추가하고 그 크기를 출력한다.
작성자의 의도는 set은 중복을 허용하지 않으므로 26을 예상했겠지만, 260이 출력된다.

equals 메서드와 hashCode를 재정의한 것처럼 보이지만, 재정의(overriding)한 게 아니라 다중정의(overloading)한 것이다.
그래서 Object 메서드에서 상속한 equals와는 별개인 equals를 새로 정의한 꼴이고, Object의 equals는 기존과 동일하게 객체의 identity만 확인하기 때문에 260개의 Bigram이 서로 다른 객체로 인식된 것이다.
`@Override` 애너테이션을 메서드에 추가하면 메서드를 재정의한다는 의도를 명시한 것이고, 컴파일러가 오류를 찾아준다.
```java
@Override public boolean equals(Bigram b) {
    return b.first == first && b.second == second;
}
```
이처럼 명시해주면 컴파일 시 에러가 발생한다.
```text
Bigram.java:10: method does not override or implement a method
from a supertype
    @Override public boolean equals(Bigram b) {
    ^
```
에러 메시지를 보고 메서드를 수정하면 된다.
```java
@Override public boolean equals(Object o) {
    if (!(o instanceof Bigram))
        return false;
    Bigram b = (Bigram) o;
    return b.first == first && b.second == second;
}
```

상위 클래스의 메서드를 재정의하는 모든 메서드에 `@Override` 애너테이션을 달자.
구체 클래스에서 상위 클래스의 추상 메서드를 재정의할 때는 굳이 달지 않아도 된다.
구체 클래스인데 구현하지 않은 추상 메서드가 있다면 컴파일러가 알려준다.
