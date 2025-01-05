# 아이템 23. 태그 달린 클래스보다는 클래스 계층구조를 활용하라
두 가지 의미를 표현할 수 있으며, 그중 현재 표현하는 의미를 태그 값으로 알려주는 클래스가 존재한다.
아래는 원과 사각형을 표현할 수 있는 클래스이다.
```java
class Figure {
    enum Shape { RECTANGLE, CIRCLE }

    // 태그 필드 - 현재 모양을 나타낸다.
    final Shape shape;

    // 다음 필드들은 모양이 사각형(RECTANGLE)일 때만 쓰인다.
    double length;
    double width;

    // 다음 필드는 모양이 원(CIRCLE)일 때만 쓰인다.
    double radius;

    // 원용 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // 사각형용 생성자
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    public double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}

```
이와 같은 태그 달린 클래스는 단점이 많다. 열거 타입 선언, 태그 필드, switch 문 등 쓸데없는 코드가 많다.
또한 메모리 사용도 더 많고, 필드들을 final로 선언하려면 사용하지 않는 필드들도 생성자에서 초기화해야 한다.
태그 필드를 설정하고 해당 의미에 쓰이는 데이터 필드를 초기화하는 데 switch문을 사용하다 보니 오류를 내기 쉽고 비효율적이다.

자바와 같은 객체 지향 언어는 타입 하나로 다양한 의미의 객체를 표현하는 서브타이핑(subtyping)을 제공한다.
위와 같은 코드를 클래스 계층구조로 변경하는 방법은 다음과 같다.
> 1. 루트(root)가 될 추상 클래스를 정의한다.
> 2. 태그 값에 따라 동작이 달라지는 메서드들을 루트 클래스의 추상 메서드로 선언한다.
> 3. 태그 값에 상관없이 동작이 일정한 메서드들을 루트 클래스에 일반 메서드로 추가한다.
> 4. 모든 하위 클래스에서 공통으로 사용하는 데이터 필드들도 루트 클래스로 올린다.
> 5. 루트 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의한다.
> 6. 루트 클래스가 정의한 추상 메서드 각자의 의미에 맞게 구현한다.

```java
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;

    Circle(double radius) {this.radius = radius; }

    @Override double area() { return Math.PI * (radius * radius); }
}

class Rectangle extends Figure {
    final double length;
    final double width;

    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override double area() { return length * width; }
}

```
개선된 코드는 간결하고 명확하며, 불필요한 코드가 제거되어 가독성도 향상되었다.
쓸데없는 데이터 필드는 모두 제거하였고 남은 필드들은 모두 final이다.
때문에 각 클래스의 생성자가 모든 필드를 초기화하고 추상 메서드를 모두 구현했는지 컴파일러가 확인해주어 오류가 발생할 가능성이 낮아진다.
