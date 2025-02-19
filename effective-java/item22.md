# 아이템 22. 인터페이스는 타입을 정의하는 용도로만 사용하라
인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입의 역할을 한다.
클래스가 어떤 인터페이스를 구현한다는 것은 자신의 인스턴스로 무엇을 할 수 있는지를 클라이언트에 얘기해주는 것이다.
인터페이스는 오직 이 용도로만 사용해야 한다.

메서드 없이 static final 필드로만 구성된 상수 인터페이스라는 것이 있다.
이 상수들을 사용하려는 클래스에서 정규화된 이름(qualified name)을 쓰는 걸 피하고자 그 인터페이스를 구현하곤 한다.
```java
public interface PhysicalContants {
    // 아보가드로 수 (1/몰)
    static final double AVOGADROS_NUMBER    = 6.022_140_857e23;

    // 볼츠만 상수 (J/K)
    static final double BOLTZMANN_CONSTANT  = 1.380_648_52e-23;

    // 전자 질량 (kg)
    static final double ELECTRON_MASS       = 9.109_383_56e-31;
}
```
이는 인터페이스를 잘못 사용한 예시이다.
클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 해당한다.
따라서 상수 인터페이스를 구현하는 것은 이 내부 구현을 클래스의 API로 노출하는 행위다.

특정 클래스나 인터페이스에 강하게 연관된 상수라서 공개할 목적이라면 그 클래스나 인터페이스 자체에 추가해야 한다.
모든 숫자 기본 타입의 박싱 클래스가 대표적으로, Integer와 Double에 선언된 MIN_VALUE와 MAX_VALUE 상수가 이런 예다.
열거 타입으로 나타내기 적합한 상수라면 열거 타입으로 만들어 공개하면 된다([아이템 34](item34.md)).
혹은 인스턴스화할 수 없는 유틸리티 클래스([아이템 4](item4.md))에 담아 공개하자.

다음 코드는 PhysicalConstants의 유틸리티 클래스 버전이다.
```java
public class PhysicalConstants {
    private PhysicalConstants() { } // 인스턴스화 방지

    // 아보가드로 수 (1/몰)
    static final double AVOGADROS_NUMBER    = 6.022_140_857e23;
    // 볼츠만 상수 (J/K)
    static final double BOLTZMANN_CONSTANT  = 1.380_648_52e-23;
    // 전자 질량 (kg)
    static final double ELECTRON_MASS       = 9.109_383_56e-31;
}

```
유틸리티 클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스 이름까지 함께 명시해야 한다.
`PhysicalConstants.AVOGADROS_NUMBER`처럼 사용한다.
유틸리티 클래스의 상수를 자주 사용한다면 static import를 하여 클래스 이름을 생략할 수 있다.
```java
import static effectivejava.chapter4.item22.constantutilityclass.PhysicalConstant.*;

public class Test {
    double atoms(double mols) {
        return AVOGADROS_NUMBER * mols;
    }
    ...
    // PhysicalConstants를 빈번히 사용한다면 static import가 값어치를 한다.
}

```
