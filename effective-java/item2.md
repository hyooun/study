# 아이템 2. 생성자에 매개변수가 많다면 빌더를 고려하라
static 팩터리 메서드와 public 생성자 모두 선택적 매개변수가 많은 경우 적절히 대응하기 어렵다. 
</br>
## 점층적 생성자 패턴(telescoping constructor pattern)
기존에는 필수 매개변수만 받는 생성자부터 선택 매개변수를 모두 받는 생성자까지 전부 생성하는 `점층적 생성자 패턴(telescoping constructor pattern)`을 사용했다.
```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;
    
    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}

```
```java
Nutritions cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);
```
위와 같은 방식으로 필요한 매개변수를 포함한 생성자를 호출해서 사용하면 되지만, 불필요한 매개변수를 포함할 수도 있고 매개변수의 개수가 많아질수록 코드를 작성하거나 읽기 어려워져 찾기 어려운 버그나 런타임 에러를 유발할 수 있다.
## 자바빈즈 패턴 (JavaBeans pattern)
매개변수가 없는 생성자로 객체를 만든 후, setter 메서드들을 호출해 원하는 매개변수의 값을 설정해주는 방식이다.
```java
public class NutritionFacts {
    private int servingSize = -1;
    private int servings = -1;
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;
    
    public NutritionFacts() { }
    public void setServingSize(int servingSize) { this.servingSize = servingSize; }
    public void setServings(int servings) { this.servings = servings; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setFat(int fat) { this.fat = fat; }
    public void setSodium(int sodium) { this.sodium = sodium; }
    public void setCarbohydrate(int carbohydrate) { this.carbohydrate = carbohydrate; }
}

```
```java
NutritionFacts cocaCola = new NutritionFacts();
cocaCola.setServingSize(240);
cocaCola.setServings(8);
cocaCola.setCalories(100);
cocaCola.setSodium(35);
cocaCola.setCarbohydrate(27);

```
점층적 생성자 패턴의 단점들이 개선되어 인스턴스를 만들기 쉽고 가독성이 향상되었다.
하지만 자바빈즈 패턴에서는 객체 하나를 만들기 위해서 메서드를 여러 개 호출해야 하고 객체가 완전히 생성되기 전까지는 `일관성(consistency)`이 무너진 상태에 놓이게 된다.
일관성이 깨진 객체가 만들어지면, 디버깅이 어렵고 클래스를 불변으로 만들 수 없으며 스레드 안전성을 얻기 위해서 추가적인 작업도 필요하다. </br>

이러한 단점을 완화하고자 생성이 끝난 객체를 수동으로 `얼리고(freezing)` 얼리기 전에는 사용할 수 없도록 하기도 한다.
하지만 이 방법도 객체 사용 전에 프로그래머가 freeze 메서드를 확실히 호출해줬는지 컴파일러는 보증할 수 없기 때문에 런타임 오류에 취약하다.

## 빌더 패턴(Builder pattern)
점층적 생성자 패턴의 안전성과 자바빈즈 패턴의 가독성을 겸비한 패턴이다. 클라이언트는 필요한 객체를 직접 만드는 대신 필수 매개변수만으로 생성자(혹은 static 팩터리)를 호출해 빌더 객체를 얻는다. 그 후에 빌더 객체가 제공하는 일종의 setter 메서드들을 통해 원하는 선택 매개변수 값을 설정한다.
마지막으로 매개변수가 없는 build 메서드를 호출해 필요한 객체를 얻는다. 빌더는 생성할 클래스 안에 static member 클래스로 만들어두는 것이 일반적이다.
```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;
        
        // 선택 매개변수 - 기본값으로 초기화한다.
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;
        
        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        } 
        
        public Builder calories(int val) { calories = val; return this; }
        public Builder fat(int val) { fat = val; return this; }
        public Builder sodium(int val) { sodium = val; return this; }
        public Builder carbohydrate(int val) { carbohydrate = val; return this; }
        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }
    
    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}

```
NutritionFacts 클래스는 불변이며, setter 메서드들은 자기 자신을 반환하기 때문에 연쇄적인 호출이 가능하다. 이런 방식을 메서드 호출이 물 흐르듯이 연결된다는 뜻으로 `플루언트 API(fluent API)` 혹은 `메서드 연쇄(method chaining)`라 한다.
```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
                .calories(100).sodium(35).carbohydrate(27).build();
```
이처럼 읽고 쓰기 쉬운 빌더 패턴은 named optional parameters와 유사한 방식이다.
또한 잘못된 매개변수를 찾아내기 위해서 빌더의 생성자와 메서드에서 입력 매개변수를 검사하고, build 메서드가 호출되는 생성자에서 여러 매개변수에 걸친 `불변식(invariant)`을 검사한다. 예외 발생 시 어떤 매개변수가 잘못되었는지 메시지를 포함하여 `IllegalArgumentException`을 던지자. </br>
> ✅ 불변(immutable 혹은 immutability)은 어떠한 변경도 허용하지 않는다는 뜻으로, 변경을 허용하는 가변(mutable) 객체와 구분하는 용도로 사용한다. 대표적으로 String 객체는 한번 만들어지면 절대 값을 바꿀 수 없는 불변 객체이다. </br>
> 한편 불변식(invariant)은 프로그램이 실행되는 동안, 혹은 정해진 기간 동안 반드시 만족해야 하는 식을 말한다. 예를 들어 리스트의 크기는 0 이상이어야 하는데 음수가 되면 불변식이 깨진 것이다. 따라서 가변 객체에도 불변식은 존재할 수 있으며, 넓게 보면 불변은 불변식의 극단적인 예라고 할 수 있다.

빌더 패턴은 계층적으로 설계된 클래스와 함께 사용하기 좋다. 각 계층의 클래스에 관련된 빌더를 멤버로 정의하고 추상 클래스는 추상 빌더를, 구체 클래스(concrete class)는 구체 빌더를 갖게 한다. 다음은 피자의 다양한 종류를 표현하는 계층구조의 루트에 놓인 추상 클래스이다.
```java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE };
    final Set<Topping> toppings;
    
    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }
        
        abstract Pizza build();

        // 하위 클래스는 이 메서드를 재정의(overriding)하여
        // "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }
    
    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}

```
Pizza.Builder 클래스는 재귀적 타입 한정을 이용하는 제네릭 타입이다. 여기에 추상 메서드인 self를 더해 하위 클래스에서는 형변환 없이 method chaining을 지원할 수 있다. self 타입이 없는 자바를 위한 이 방법을 `시뮬레이트한 셀프 타입(simulated self-type)` 관용구라 한다.
```java
public class NyPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;
    
    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;
        
        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }
        
        @Override NyPizza build() {
            return new NyPizza(this);
        }

        @Override protected Builder self() { return this; }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}

```
```java
public class Calzone extends Pizza {
    private final boolean sauceInside;
    
    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false;
        
        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }
        
        @Override Calzone build() { return new Calzone(this); }

        @Override protected Builder self() { return this; }
    }
    
    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}

```
이처럼 Pizza의 하위 클래스인 NyPizza와 Calzone이 있다. 각 하위 클래스의 빌더가 정의한 build 메서드는 해당하는 구체 하위 클래스를 반환하도록 선언한다. 
하위 클래스의 메서드가 상위 클래스의 메서드가 정의한 타입이 아닌, 그 하위 타입을 반환하는 기능을 `공변 반환 타이핑(convariant return typing)`이라고 한다.
이를 잘 활용하면 클라이언트가 형변환에 신경쓰지 않고 빌더를 사용할 수 있다.
```java
NyPizza pizza = new NyPizza.Builder(NyPizza.Size.SMALL)
    .addTopping(Pizza.Topping.SAUSAGE).addTopping(Pizza.Topping.ONION).build();
Calzone calzone = new Calzone.Builder()
    .addTopping(Pizza.Topping.HAM).sauceInside().build();

```
빌더를 사용하면 가변인수(varargs) 매개변수를 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하거나 여러 번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수 있다. Pizza 클래스의 addTopping 메소드가 그 예시이다.

🏗️ 빌더 패턴은 상당히 유연하고 특정 필드를 초기화하기도 편리하다. 객체 생성에 앞서 빌더부터 만들어야 하므로 비용이 약간 더 드는 단점은 존재하지만, 생성 비용이 크지도 않고 확장성을 고려했을 때 매개변수가 많다면 빌더 패턴을 고려하는 것이 좋다.
