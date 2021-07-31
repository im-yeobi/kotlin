# 2장

## 변수
```kotlin
val answer: Int
val answer = 42
```
- 타입추론에 의해서 컴파일러가 타입을 지정해준다
- 단, 초기 값을 주지 않은 경우 타입을 반드시 명시해야 한다.
- `val` : 불변(immutable) 값을 저장하는 변수, 자바의 final
- `var` : 가변(mutable) 값을 저장하는 변수

### 문자열 템플릿
```kotlin
val name: String = "Kotlin"
println("Hello, $name!")
```
- 스크립트 언어와 비슷하게 코틀린에서도 변수를 문자열 안에서 사용할 수 있다.
- `$` 키워드를 사용해서 간단하게 사용가능하며, 변수에 function을 사용하는 경우 `${}` 형태로 사용해야 한다.
- `$`를 문자열에서 표시하기 위해서는 `\$` 형태로 사용해야 한다.
- **한글처럼 유니코드를 사용하는 경우에는 변수 인식이 되지 않아 중괄호를 사용해서 해결해야 한다.**

## 함수

```kotlin
fun max(a: Int, b: Int): Int {
    return if (a > b) a else b
}

fun max(a: Int, b:Int): Int = if (a > b ) a else b

fun max(a: Int, b: Int) = if (a > b) a else b
```
- `fun` 키워드 사용, 반환 타입은 `:`로 마지막에 작성
- `변수명:타입`으로 파라메터를 지정
- 반환값을 생략하더라도 컴파일러가 타입을 분석해 타입을 정해주는 타입추론을 지원

## 클래스
- 코틀린의 기본 접근제한자는 public이라서 생략 가능
```kotlin
class Person(
    val name: String,
    var isMarried: Boolean
)

// decompile
public final class Person {
    @NotNull
    private final String name;
    private boolean isMarried;

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final boolean isMarried() {
        return this.isMarried;
    }

    public final void setMarried(boolean var1) {
        this.isMarried = var1;
    }

    public Person(@NotNull String name, boolean isMarried) {
        Intrinsics.checkNotNullParameter(name, "name");
        super();
        this.name = name;
        this.isMarried = isMarried;
    }
}
```
- val : 읽기 전용 프로퍼티, getter만 생성
- var : 수정가능 프로퍼티, getter, setter 생성

### 프로퍼티
```kotlin
println(person.name)
```
- 코틀린에서는 프로퍼티로 호출해도 실제로는 getter를 호출하고 있다.

#### 커스텀 접근자
```kotlin
class Rect(val height: Int, val width: Int) {
    val isSquare: Boolean
    get() {
        return height == width
    }
}
```
- 함수로 하는거랑 커스텀 게터로 정의하는 방식은 취향차이


## enum
```kotlin
enum class Color(
    val r: Int, val g: Int, b: Int
) {
    RED(255, 0, 0), ORANGE(255, 165, 0), YELLOW(255, 255, 0);
    
    fun rgb() = (r * 256 + g) * 256 + b
}
```

- fun 작성이 필요한 경우, 반드시 마지막 enum 값 뒤에 `;`를 추가해야 한다.


## when
```kotlin
when(color) {
    RED -> "warm"
    GREEN -> "neutral"
    BLUE -> "cold"
}

when(setOf(c1, c2)) {
    setOf(RED, YELLOW) -> ORANGE
    setOf(YELLOW, BLUE) -> GREEN
    setOf(BLUE, VIOLET) -> INDIGO
    else -> throw Exception("Dirty color")
}
```
- java에 switch 비슷하지만, 다양하게 활용 가능
- when 값에 복수개의 값도 허용 가능

## if
- 코틀린에서는 3항연산자를 지원하지 않아, `if`를 사용
- 자바에서는 `if`가 반환값이 없었는데, kotlin에서는 `if`도 반환값을 가짐

## 이터레이션
```kotlin
val oneToTen = 1..10

for (i in 100 downTo 1 step 2) {
    
}
for (x in 0 until size) {
    
}
```
- 코틀린에서는 range를 사용하여 반복문의 범위를 지정
- range로 설정하면 기본적으로 시작값과 끝값을 포함 된다.
- `downTo`: reverse해서 역순으로 값이 들어온다
- `step`: i의 증가값을 step {value} 값 만큼 증가시킨다
- `until` : 마지막 끝값을 포함하지 않게 범위를 조절한다.

```kotlin
// index를 포함
for ((index, element) in list.withIndex()) {
    
}
// map에서 사용
for ((key,value) in map) {
    
}
```
- collection에서 사용

```kotlin
fun isLetter(c:Char) = c in 'a'..'z' || c in 'A'..'Z'
fun recognize(c: Char) = when(c) {
    in '0'..'9' -> ""
    !in '0'..'9' -> ""
    else -> ""
}
```
- 알파벳 범위로 지정하면 알파벳 범위이내에 존재하는지 확인하게 되고, collection으로 하게 되면 collection 내부에 존재하는지로 비교하게 된다.

## 스마트 캐스트
```kotlin
var sum: Int = 0
if (e is Int) {
    sum += e
}
else if (e is String){
    println("String is $e")
}
```
- 컴파일러가 캐스팅 해주는 방식
- 타입변환을 한 후에 다시 해당 타입으로 캐스팅하지 않아도 마치 처음부터 그 변수가 원하는 타입으로 선언된 것처럼 사용할 수 있음
- `is`의 경우 프로퍼티는 반드시 val이여야 하고, 커스텀 접근자를 사용한 경우 사용 불가
- 이럴때는 `is` 대신 `as`를 사용하여 캐스팅 후 사용


## 예외처리
- 코틀린에서는 unchecked exception, checked exception을 구분하지 않는다.
- 따라서 `throws`를 사용하지 않아도, cache에 checked exception을 명시하지 않아도 된다.
- `try`도, `if`, `when`과 마찬가지로 expression이다.

## 과제 설명
- when에 인자가 복수개로 전달되는 경우에 해당 조건에 맞게 테스트 코드 작성하기

# 3장

## 컬렋션
- java의 컬렉션을 그대로 사용하고, 추가 기능을 제공한다.
- 그대로 사용하는 이유는 자바 코드와 상호작용하기 위해서

### 디폴트 파라미터

```kotlin
fun <T> joinToString(
    collection: Collection<T>,
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
)
```
- 디폴트 파라미터를 이용하면, 함수 오버로딩을 줄일 수 있다.
- `@JvmOverloads`를 추가하면 코틀린 컴파일러가 자동으로 모든 오버라딩 함수를 만들어 준다.
- Java에서는 디폴트 파라미터를 지원하지 않기때문에 `@JvmOverloads`를 추가하지 않으면 에러 발생

```kotlin
@JvmOverloads
   public static final void joinToString(@NotNull Collection collection, @NotNull String separator, @NotNull String prefix, @NotNull String postfix) {
      Intrinsics.checkNotNullParameter(collection, "collection");
      Intrinsics.checkNotNullParameter(separator, "separator");
      Intrinsics.checkNotNullParameter(prefix, "prefix");
      Intrinsics.checkNotNullParameter(postfix, "postfix");
   }

   // $FF: synthetic method
   public static void joinToString$default(Collection var0, String var1, String var2, String var3, int var4, Object var5) {
      if ((var4 & 2) != 0) {
         var1 = ", ";
      }

      if ((var4 & 4) != 0) {
         var2 = "";
      }

      if ((var4 & 8) != 0) {
         var3 = "";
      }

      joinToString(var0, var1, var2, var3);
   }

   @JvmOverloads
   public static final void joinToString(@NotNull Collection collection, @NotNull String separator, @NotNull String prefix) {
      joinToString$default(collection, separator, prefix, (String)null, 8, (Object)null);
   }

   @JvmOverloads
   public static final void joinToString(@NotNull Collection collection, @NotNull String separator) {
      joinToString$default(collection, separator, (String)null, (String)null, 12, (Object)null);
   }

   @JvmOverloads
   public static final void joinToString(@NotNull Collection collection) {
      joinToString$default(collection, (String)null, (String)null, (String)null, 14, (Object)null);
   }
```

### 최상위 함수
```java
import static com.example.kotlinstudy.week01.strings.JoinKt.joinToString;

public class JavaClazz {
    public static void test() {
        joinToString(List.of());
    }
}
```
- 코틀린에서는 class를 생성하지 않고도 함수를 정의해서 사용 가능
- Java에서 코틀린 최상위 함수를 사용하려고 하면, {코틀린 파일}Kt.{method}로 사용해야 한다 
- `@JvmName`을 사용하면 자동으로 생성되는 Kt 파일의 이름을 변경 가능하다.

### 확장 함수

```kotlin
fun String.lastChar(): Char = this[this.length - 1]
fun String.lastChar(): Char = get(length -1)
```
- `String`을 수신 객체 타입이라 부르고, `this`를 호출되는 대상을 수신 객체라고 한다.

```java
char c = StringUtilKt.lastChar("Java")
```  
- java에서도 사용 가능하나, 코틀린이 생성해준 class에 static method를 호출해서 사용해야 한다.

```kotlin
open class View {
}

class Button: View() {

}

fun View.showOff() = println("I'm a View!")
fun Button.showOff() = println("I'm a Button!")

fun main() {
    val view: View = Button()
    view.showOff()
}
```
```java
public static final void showOff(@NotNull View $this$showOff) {
      Intrinsics.checkNotNullParameter($this$showOff, "$this$showOff");
      String var1 = "I'm a View!";
      boolean var2 = false;
      System.out.println(var1);
   }

   public static final void showOff(@NotNull Button $this$showOff) {
      Intrinsics.checkNotNullParameter($this$showOff, "$this$showOff");
      String var1 = "I'm a Button!";
      boolean var2 = false;
      System.out.println(var1);
   }

   public static final void main() {
      View view = (View)(new Button());
      showOff(view);
   }
```
- 확장함수의 경우 오버라이딩 되지 않으니 주의가 필요
- 확장함수는 컴파일되면 정적 메소드로 변경되고 정적 메소드의 파라미터의 값에 따라서 값이 결정되기 때문
- 해당 class에 확장함수와 멤버 함수가 존재한다면 멤버 함수가 확장 함수보다 호출 우선순위가 높다.

### 가변인자
```kotlin
fun varargTest(vararg nums: Int): Int {
    return nums.sum()
}

fun main() {
    println(varargTest(1,2,3,4,5))

    val args = arrayOf<String>("1", "2", "3")
    val list = listOf("args: ", *args)
    println(list)
}
```
- Java에서는 `...`을 사용했지만 코틀린에서는 `vararg`를 사용한다.
- array로 된 값을 vararg에 전달하기 위해서는 `*`을 이용해서 전달해야 한다.

### 중위 호출
```kotlin
infix fun Any.to(other:Any) = Pair(this, other)
```
- 인자가 하나뿐인 일반 메소드나 인자가 하나뿐인 확장 함수에서 사용 가능

### 3중 따옴표
```kotlin
val regex = """(.+)/(.+)\.(.+)""".toRegex()
val kotlinKogo = """|  //
                    | //
                    |/ \"""
```
- 정규표현식에서 문자 이스케이프 할 필요없이 작성 가능
- 줄바꿈을 표현하느 문자열에서 이스케이프 할 필요 없음
## 과제
- String의 마지막 char 값을 uppercase 해주는 `lastUpperCase()` 구현하기
