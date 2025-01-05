# 아이템 25. 톱레벨 클래스는 한 파일에 하나만 담으라
소스 파일 하나에 톱레벨 클래스를 여러 개 선언해도 컴파일 에러가 발생하진 않는다.
하지만 이렇게 하면 한 클래스를 여러 가지로 정의할 수 있으며, 그중 어느 것을 사용할지는 어느 소스 파일을 먼저 컴파일하느냐에 따라 달라져 위험하다.

다음 소스 파일은 Main 클래스 하나를 담고 있고, Main 클래스는 다른 톱레벨 클래스 2개(Utensil과 Dessert)를 참조한다.
```java
public class Main {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}
```
Utensil과 Dessert 클래스가 `Utensil.java`라는 한 파일에 정의되어 있다고 해보자.
```java
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```
Main을 실행하면 pancake를 출력한다.
우연히 똑같은 두 클래스를 담은 `Dessert.java`라는 파일을 만들었다고 가정하자.
```java
class Utensil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
```
`javac Main.java Dessert.java` 명령으로 컴파일한다면 컴파일 오류가 나고 Utensil과 Dessert를 중복 정의했다고 알려줄 것이다.
컴파일러는 가장 먼저 `Main.java`를 컴파일하고 그 안에서 Dessert 참조보다 먼저 나오는 Utensil 참조를 만나면 `Utensil.java` 파일을 살펴 Utensil과 Dessert를 모두 찾아낼 것이다.
그 다음 컴파일러가 두 번째로 넘어온 `Dessert.java`를 처리하려고 할 때 같은 클래스의 정의가 이미 있음을 알게 된다.

만약 `javac Main.java`나 `javac Main.java Utensil.java` 명령으로 컴파일하면 `Dessert.java`파일을 작성하기 전처럼 pancake를 출력한다.
그러나 `javac Dessert.java Main.java` 명령으로 컴파일하면 potpie를 출력한다.
이처럼 컴파일러에 어느 소스 파일을 먼저 주느냐에 따라 동작이 달라지므로 해결해야 한다.

단순히 톱레벨 클래스들인 Utensil과 Dessert를 서로 다른 소스 파일로 분리하면 해결된다.
굳이 여러 톱레벨 클래스를 한 파일에 담고 싶다면 static 멤버 클래스([아이템 24](item24.md))를 고려해볼 수 있다.
다른 클래스에 딸린 부가적인 클래스라면 static 멤버 클래스로 만드는 것이 읽기 졸고, private로 선언하여 접근 범위도 최소로 관리할 수 있다.
```java
public class Test {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }

    private static class Utensil {
        static final String NAME = "pan";
    }

    private static class Dessert {
        static final String NAME = "cake";
    }
}
```
