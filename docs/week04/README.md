# 9. 제네릭스 
자바의 제네릭스와 비슷하다. 
## 9.1 제네릭 타입 파라미터
코틀린 컴파일러는 타입 인자를 추론할 수 있다. 빈 리스트 등 타입을 추론할 수 없을 땐 타입을 명시해줘야한다.

### 타입 파라미터 제약
클래스나 함수에 사용할 수 있는 타입 인자를 제한하는 기능. 예를 들어 sum 함수의 경우 List<Int>나 List<Double>에서는 적용할 수 있지만 List<String>에서는 적용할 수 없다. 
이는 상한 타입 (upper bound type)을 지정함으로서 해결할 수 있다. 
~~~kotlin
fun <T : Number> List<T>.sum() : T
~~~~
타입 파라미터에 여러 제약을 가할려면 다음과 같이 한다
~~~kotlin
fun <T> ensureTrailingPeriod(seq : T) where T: CharSequence, T: Appendable {
    ...
}
~~~~
타입파라미터를 널이 될 수 없는 타입으로 한정하려면 상한 타입을 Any로 사용하면 된다. 
        
## 9.2 실행 시 제네릭스의 동작 : 소거된 타입 파라미터와 실체화된 타입 파라미터
JVM의 제네릭스는 보통 타입 소거를 사용해 구현된다 : 실행 시점에 제네릭 클래스의 인스턴스에 타입 인자 정보가 들어있지 않다. 즉 런타임 시점에서는 해당 객체의 제네릭 타입을 알 수가 없다. 
이를 타입 소거 (type erasure)라고 하는데 이로 인해 한계점이 발생한다. 
우선 타입인자를 따로 저장하지 않기 때문에 실행 시점에 타입 인자를 검사할 수 없다. 다만 이로 인해 저장해야하는 정보가 줄어들어 메모리 사용량이 줄어든다는 장점이 있다. 
~~~kotlin
if(value is List<String>) 
// ERROR: Cannot check for instance of erased type - 컴파일 시 오류 발생
~~~

만약 List라는 것을 체크하고 싶다면 아래처럼 하면 된다. 여기서 *를 스타프로텍션이라고 부른다. 

~~~kotlin
if(value is List<*>) 
~~~

제네릭 타입은 타입체크가 안되므로 잘못된 타입으로 캐스팅도 가능하다! (as)
이런 제약을 피하기 위해서는 inline 함수를 사용한다. inline 함수의 타입 파라미터는 실체화되므로 실행 시점에 인차인 함수의 타입 인자를 알 수 있다.
- inline 함수 : 함수 호출 부분을 함수 본문으로 대체. 람다를 인자로 사용할 경우 인라인을 붙이면 무명클래스를 만들지 않아 성능적 이득이 있음. 
함수를 인라인으로 만들고 타입 파라미터를 reified로 지정하면 value의 타입이 T의 인스턴스인지를 실행 시점에 검사할 수 있다.
인라인 함수의 경우 타입인자를 확정하여 쓸 수 있는 이유는 함수 본문을 호출 부분에 대체하기 때문에 컴파일러는 정확한 타입인자를 알 수 있게 된다. 그래서 가능하다. 함수가 너무 크다면 성능 저하가 일어날 수 도 있다. 
    
~~~kotlin
inline fun <reified T> isA(value : Any) = value is T
~~~
reified : 구체화

~~~kotlin 
val serviceImpl = ServiceLoader.load(Service::class.java)

val serviceImpl = loadService<Service>()
~~~
    

~~~kotlin 
inline fun <reified T> loadService() {
    return ServiceLoader.load(T::class.java)
}
~~~    
    
이런 실체화한 타입 파라미터는 몇가지 제약이 있다. 다음의 경우 실체화된 타입 파라미터를 사용할 수 있다
    - 타입 검사와 캐스팅
    - 코틀린 리플렉션 API
    - 코틀린 타입에 대응하는 java.lang.Class를 얻기
     다른 함수를 호출할 때 타입인자로 사용 
    
다음의 일은 불가능하다
    - 타입 파라미터 클래스의 인스턴스 생성하기 
    - 타입 파라미터 클래스의 동반 객체 메소드 호출하기 
    - 실체화한 타입 파라미터를 요구하는 함수를 호출하면서 실체화하지 않는 타입 파라미터로 받은 타입을 타입 인자로 넘기기
    - 클래스, 프로퍼티, 인라인 함수가 아닌 함수의 타입 파라미터를 reified로 지정하기 

실체화한 타입 파라미터를 인라인 함수에만 사용할 수 있으므로, 실체화한 타임 파라미터를 사용하는 함수는 자신에게 전달되는 모든 람다와 함께 인라이닝된다. 람다 내부에서 타입 파라미터를 사용하는 방식에 따라서는 람다를 인라인할 수 없는 경우가 생기기도 하고, 람다를 인라이닝 하고싶지 않을 수도 있다. 이럴 경우 noinline 변경자를 사용하여 인라이닝을 금지할 수 있다. 
    
    
## 9.3 변성 : 제네릭과 하위 타입
변성 : List<String>과 List<Any>같이 기저 타입이 같고 타입 인자가 다른 여러 타입이 서로 어떤 관계가 있는지 설명하는 개념이다. 

`List<Any>`를 인자로 받는 곳에 `List<String>` 을 인자로 넘긴다면.. 
    - read만 하는 경우는 안전하다 (List)
    - 원소를 추가하거나 변경한다면 타입 불일치가 생길수 있으므로 안전하지 않다 (MutableList)
    
하위 타입 : 하위 클래스와 근본적으로 같다. 널이 될 수 없는 타입은 널이 될 수 있는 타입의 하위타입이다. 
무공변 : 제네릭 타입을 인스턴스화 할때 타입 인자로 서로 다른 타입이 들어가면 인스턴스 타입 사이의 하위 타입관계가 성립하지 않을때 그 제네릭 타입을 무공변이라고 한다. 
공변적 : A가 B의 하위타입이면 `List<A>`는 `List<B>` 의 하위 타입이다. 
    
공변성 : 하위 타입 관계를 유지 
    
A가 B의 하위타입일 때 `Producer<A>`가 `Producer<B>` 의 하위타입이면 Producer는 공변적이다. 코틀린에서 제네릭 클래스가 파라미터에 대해 공변적임을 표시하려면 타입파라미터 이름 앞에 out을 붙여야한다. 
    
~~~kotlin
interface Producer<out T>
    fun produce() : T
~~~~
    
~~~kotlin
open class Animal {
    fun fedd() { ... }
}
    
class Herd<T: Animal>
    val size: Int get() = ...
    operator fun get(i: Int): T { ... }
}
    
fun feedAll(animals: Herd<Animal>) {
    for( i in 0 until animals.size) {
        animals[i].feed()
    }
}
    
class Cat : Animal() {
    fun cleanLitter() { ... }
}
    
fun takeCaredOfCats(cats: Herd<Cat>) {
    for(i in 0 until cats.size) {
        cats[i].cleanLitter()
        feedAll(cats) // error occurs. 변성을 지정하지 않았기 때문에 에러가 발생한다!
    }
}
    
// 아래와 같이 지정해야한다 
    
class Herd<out T : Animal> { ... }

fun takeCareOfCats(cats:Herd<Cat>) {
    .... 
}
~~~
    
반공변성 : 뒤집힌 하위 타입 관계
타입 B가 타입 A의 하위 타입인 경우 `Consumer<A>`가 `Consumer<B>`의 하위타입인 관계가 성립하면 제네릭 클래스 `Consumer<T>`는 타입인자 T에 대해 반공변이다. 
이 경우 in 위치에서만 사용할 수 있다.     
~~~kotlin
interface Comparator<in T> {
    fun compared(e1: T, e2: T) : Int { ... }
~~~
    
위 경우 인터페이스의 메소드는 T 타입 값을 소비하기만 하므로 in 키워드를 붙여야한다.
    
사용 지점 변성 : 타입이 언급되는 지점에서의 변성지정 
    
클래스를 선언하면서 변성을 지정 : 클래스를 사용하는 모든 장소에 변성 지정자가 영향을 끼침. 
~~~kotlin
fun <T> copyData(source: MutableList<out T>, destination: MutableList<T>) {
    for (item in source)
        destination.add(item)
}
~~~    
   
타입 프로젝션이 일어나 strict하게 체크하게 된다.    
    
    
## 과제 설명

    

# 10. 애노테이션과 리플렉션
## 10.1 애노테이션 선언과 적용
기본적으로 자바와 동일하게 사용할 수 있다. 다만 코틀린에는 자바에서 사용하지 않는 좀더 편한 애노테이션들이 존재한다! 

EX)
@Deprecated는 자바와 코틀린 모두 존재한다. 다만 코틀린에는 ReplaceWith field가 있어서 대신할 수 있는 패턴을 좀더 손쉽게 제안할 수 있다. 
이 필드를 보고 IntelliJ에서 IDE 차원에서 제시도 해준다. 

### 자바와 코틀린의 애노테이션 관련 다른점
1. 애노테이션 인자를 지정하는 문법이 다르다, 
    - 클래스를 애노테이션 인자로 지정할때는 ::class를 뒤에 넣어줘야한다 
      @MyAnnotaion(MyClass::class)
      
    - 다른 애노테이션을 인자로 지정할때는 인자로 들어가는 애노테이션의 이름 앞에 @를 넣지 않는다.
      ReplaceWith 는 애노테이션이다. 그러나 Deprecated의 인자로 들어가므로 @를 넣지 않는다
      
    - 배열을 인자로 지정하려면 arrayOf를 사용한다. 대신 자바로 선언된 애노테이션의 경우 자동으로 가변인자로 인식한다. 
    
    애노테이션 인자는 컴파일시점에 타입을 알아야하므로 임의의 프로퍼티는 인자로 지정할 수 없다. const를 붙여 컴파일 시점에 타임을 알 수 있는 프로퍼티만 사용이 가능하다. 


2. 애노테이션 대상을 지정할수 있다.
   
    예를 들어 코틀린 프로퍼티는 기본적으로 자바 필드 & 자바 getter 메소드와 대응하게 되고 var 타입일 경우 setter 메소드와도 
    대응하게 된다. 또한 constructor와 대응될때도 있다. 따라서 코틀린에서 애노테이션을 붙일때 어느것에 대응하여 붙일지 (예를 들어 프로퍼티에 애노테이션을 붙이면
    자바로 컴파일했을 때 getter에 붙이게 되는건지 setter에 붙이게 되는건지)를 정해줘야한다. 
    자바에 선언된 애노테이션을 프로퍼티에 붙이게 되면 기본적으로 프로퍼티 필드에 붙게 되고, 코틀린으로 애노테이션을 선언하면 프로퍼티에 직접 적용할 수 있는 애노테이션을 만들 수 있다. 
    사용 가능한 사용지점 대상 
    ~~~   
    property/field/get/set/receiver/parameter/setparam/delegate/file
    ~~~
    EX) @get:Rule (get : 사용지점대상, Rule : 어노테이션이름)

3. @Retention 애노테이션의 경우 코틀린에서 기본적으로 RUNTIME으로 지정하므로 따로 붙일 필요가 없다. 

### 제이키드     
코틀린에서는 JSON/GSON 모두 지원하지만 제이키드라는 순수 코틀린 라이브러리가 존재한다. 
- @JsonName : 직렬화/역직렬화시 이름을 지정
- @JsonExclude : 직렬화시 포함 여부. 대신 default 값을 설정해줘야함 (그래야 역직렬화가 가능)

### 애노테이션 선언
~~~kotlin
annotation class JsonExclude(val name: String)
~~~

### 메타애노테이션 : 애노테이션을 처리하는 방법을 제어 
~~~kotlin
@Target // 선언이 적용되는 클래스 정의 
~~~
    
### 애노테이션 파라미터로 제네릭 클래스 받기 
스타프로텍션을 이용한다
~~~kotlin
annotation class CustomSerializer(
    val serializerClass: KClass<out ValueSerialilzer<*>>
}
~~~

## 10.2 리플렉션 : 실행 시점에 코틀린 객체 내부 관찰 
코틀린에서는 두가지 리플렉션 API가 존재한다. 하나는 자바의 java.lang.reflect API이고 두번째는 kotlin.reflect를 통해 제공되는 코틀린 리플렉션 APi이다 
이는 자바에 없는 프로퍼티나 널이 될 수 있는 타입 등 코틀린 고유 개켬에 대한 리플렉션을 제공한다. 

### 코틀린 리플렉션 API : KClass, KCallable, KFunction, KProperty
KClass : 클래스 안에 있는 모든 선언을 열거하고 각 선언에 접근하거나 클래스의 상위 클래스를 얻는 등의 작업이 가능해진다.
~~~kotlin
val person = Person("Alice", 29)
val kClass = person.javaClass.kotlin
~~~

KCallable : 함수와 프로퍼티를 아우르는 공통 상위 인터페이스 
~~~kotlin
fun foo(x:Int) = println(x)
val kFunction = ::foo
kFunction.call(42)
~~~
함수를 표현하는 클래스는 KFunction이 있고 프로퍼티를 표현하는 클래스는 KProperty가 있다.
~~~kotlin
val kProperty = ::counter
kProperty.setter.call(21)
println(kProperty.get())
21

val person = Person("Alice", 29)
val memberProperty = Person::age
println(memberProperty.get(person))
29
~~~

여기서 함수를 호출할 때 구체적인 메소드를 사용할 수도 있다. KFunctionN을 이용하면 가능하다. 
자바와 마찬가지로 함수안의 로컬변수에 대한 접근은 리플렉션으로 불가능하다. 

### 리플렉션을 이용한 객체 직렬화와 역직렬화
~~~kotlin
val kClass = obj.javaClass.kotlin
val properties = kClass.memberProperties

properties.joinToStringBuilder(this, prefix = "{", postfix ="}" ) {
    prop -> serializeString(prop.name)
    append(": ")
    serializePropertyValu(prop.get(obj))
}
~~~
위와 같은 과정으로 직렬화를 실현한다. 어노테이션이 붙은 직렬화의 경우 findAnnotation 함수를 이용해서 직렬화를 한다.
책에 나온 deserialize의 예제는 제이키드의 예제이므로...

~~~java
// ClassDeserializer
public ClassDeserializer(@NotNull DeserializationComponents components) {
    Intrinsics.checkNotNullParameter(components, "components");
    super();
    this.components = components;

    final class NamelessClass_1 extends Lambda implements Function1<ClassDeserializer.ClassKey, ClassDescriptor> {
      NamelessClass_1() {
        super(1);
      }

      @Nullable
      public final ClassDescriptor invoke(@NotNull ClassDeserializer.ClassKey key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return ClassDeserializer.this.createClass(key);
      }
    }

    this.classes = (Function1)this.components.getStorageManager().createMemoizedFunctionWithNullableValues((Function1)(new NamelessClass_1()));
  }

  // classId : class name 및 package
  @Nullable
  public final ClassDescriptor deserializeClass(@NotNull ClassId classId, @Nullable ClassData classData) {
    Intrinsics.checkNotNullParameter(classId, "classId");
    return (ClassDescriptor)this.classes.invoke(new ClassDeserializer.ClassKey(classId, classData));
  }
~~~

## 과제 설명
