# 7장

# 산술 연산자 오버로딩

## 이항 산술 연산 오버로딩

```kotlin
data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }
}

fun main() {
    val p1 = Point(10, 20)
    val p2 = Point(30, 40)
    println(p1 + p2)
}
```

- operator 키워드를 붙이고 관례에 사용하는 키워드를 사용하면 연산자 오버로딩처럼 사용할 수 있음
- [operator koltin docs](https://kotlinlang.org/docs/operator-overloading.html#increments-and-decrements)
- operator로 정의하더라도 연산자 우선순위는 알고 있던 연산자 우선순위와 동일하다
- 교환 법칙을 지원하지 않음 ex) `p*1.5` => ok, `1.5*p` => no

```kotlin
operator fun Point.times(scale: Double): Point {
    return Point((x * scale).toInt(), (y * scale).toInt())
}

operator fun Double.times(p: Point): Point {}
```  

- operator 함수도 파라메터가 다르다면 오버로딩이 가능하다.

## 복합 대입 연산자 오버로딩
- +=, -= 등의 연산자를 의미, `plusAssign`, `minusAssign`으로 메소드를 정의

```kotlin
val list = arrayListOf(1, 2)
list += 3
// [1,2,3]
val newList = list + listOf(4, 5)
// [1,2,3,4,5]
```

## 단한 연산자 오버로딩

```kotlin
operator fun unaryPlus(): Point {
    return Point(+x, +y)
}

operator fun unaryMinus(): Point {
    return Point(-x, -y)
}

operator fun not(): Point {
    return Point(-x, -y)
}

operator fun inc(): Point {
    return Point(x + 1, y + 1)
}

operator fun dec(): Point {
    return Point(x - 1, y - 1)
}
```

- `unaryMinus`, `unaryPlus`, `not`, `inc`, `dec`으로 사용
- `inc`, `dec`는 후위, 전위 연산자로 사용 가능

# 비교 연산자 오버로딩

## 동등성 연산자: equals

- 상위에 operator로 정의된 메소드를 하위에 상속받아서 override 하게 되면 하위에 operator를 추가하지 않아도 자동으로 상위 클래스의 operator 지정이 적용된다.
- `!=`는 `==`를 사용하고 해당 값에 `!`(not)을 적용한거와 같다.

## 순서 연산자: compareTo

- java에서는 래퍼클래스에 대해서는 `<`,`>`로 짧게 표현 가능하지 않지만 코틀린에서는 가능하다.
- `Comparable` 인터페이스를 사용해서 `<`,`>`,`<=`,`>=`를 짧게 사용 가능

```kotlin
a >= b -> a.compareTo(b) >= 0
```

# 컬렉션과 범위에 대해 쓸 수 있는 관례

## 인덱스로 원소에 접근: get과 set

```kotlin
operator fun Point.get(index: Int): Int {
    return when (index) {
        0 -> x
        1 -> y
        else -> throw java.lang.IndexOutOfBoundsException()
    }
}
val p = Point(10, 20)
println(p[1])
// 20

operator fun Point.set(index: Int, value: Int): Int {
    return when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw java.lang.IndexOutOfBoundsException()
    }
}
val p = Point(10, 20)
p[1] = 42
println(p[1])
// 42
```

- 보통 배열에 대해서 인덱스로 해당 값을 가져오게 설정할 수 있는데, 코틀린에서는 객체에 필드를 인덱스를 통해서 가져오거나 셋팅할 수 있다.

## in 관례

```kotlin
data class Line(val first: Int, val last: Int) {
    operator fun contains(value: Int): Boolean {
        return value in first..last
    }
}
println(5 in Line(1, 10)) // true
println(11 in Line(1, 10)) // false
println(0 in Line(1, 10))  // false
``` 

- in에 대해서는 contains를 이용해서 연산자 오버로딩을 사용할 수 있다.

## rangeTo

```kotlin
val now = LocalDate.now()
val vacation = now..now.plusDays(10)
println(now.plusWeeks(1) in vacation) // true
```
- `rangeTo`를 사용해서 범위 지정에 사용할 수 있다

```kotlin
public operator fun <T : Comparable<T>> T.rangeTo(that: T): ClosedRange<T> = ComparableRange(this, that)
```

## for 루프를 위한 iterator 관례
```kotlin
operator fun ClosedRange<LocalDate>.iterator() : Iterator<LocalDate> = object : Iterator<LocalDate> {
    var current = start
    override fun hasNext() = current <= endInclusive


    override fun next() = current.apply {
        current = plusDays(1)
    }
}

fun main() {
    val newYear = LocalDate.ofYearDay(2017, 1)
    val daysOff = newYear.minusDays(1)..newYear
    for (dayOff in daysOff) {
        println(dayOff)
    }
}
```
- rangeTo는 ClosedRange의 인스턴스를 반환해서 범위를 지정할 수 있고, iterator를 상속받아서 
for 루프에서 사용할 수 있게 된다.
  
## 구조 분해 선언과 component 함수
```kotlin
val p = Point(10,20)
val (x,y) = p
println(x) // 10
println(y) // 20
```

```kotlin
fun splitFilename(fullName: String) {
    val (name, extension) = fullName.split('.', limit=2)
}
```
- data class로 구현하거나, componentN을 구현한 class에서는 필드의 N번째 원소를 가져올 수 있다.
- Pair, Triple을 사용하는 것 보다 component를 이용해서 필드를 가져오게되면 가독성더 좋아 짐
- 단, collection의 경우는 앞에서 5개까지만 가져올 수 있음
- class에서도 name을 가지고 값을 맵핑하는게 아니라 필드의 순서를 보고 맵핑하기에 필드 순서가 바뀌면 영향이 갈 수 있음
- map에서 주로 사용 ex : (key, value)

# 프로퍼티 접근자 로직 재활용: 위임 프로퍼티
## 위임 프로퍼티 소개
```kotlin
class Foo {
    var p: Type by Delegate()
}

class Foo {
    private val delegate = Delegate()
    var p: Type
        set(value: Type) = delegate.setValue(..., value)
        get() = delegate.getValue(...)
}
class Delegate() {
    operator fun getValue(...) {...}
    operator fun setValue(..., value: Type) {...}
}
```
- Delegate를 이용해서 해당 프로퍼티의 동작방식을 위임할 수 있음
- p가 호출될 때 delegate의 get, set이 호출되는 방식

## 위임 프로퍼티 사용: by lazy()를 사용한 프로퍼티 초기화 지연
- 초기화 과정에서 자원을 많이 사용하거나 객체를 사용할 때 꼭 초기화하지 않아도 되는 프로퍼티에 대해서 사용
- lazy 함수는 기본적으로 스레드 안전하다.

## 프로퍼티 값을 맵에 저장
```kotlin
class Person {
    private val _attributes = hashMapOf<String, String>()
    fun setAttribute(attrName: String, value: String) {
        _attributes[attrName] = value
    }
    val name: String by _attributes
}
```
- map을 이용해서 해당 값을 가져올 때 delegate가 가능하다.
- map, mutableMap에서는 `getValue`, `setValue`의 확장함수를 제공


## 과제 설명
- 해당 User에 성적이 높고, 이름이 빠른 순으로 정렬하는 compareTo를 작성해라
- lazy 패턴을 적용해서 캐시가 될 수 있게 적용해보기
- delegate를 적용해서 email 값을 검증

# 8장
## 고차 함수 정의
```kotlin
list.filter{ x > 0 }
```
- 다른 함수를 인자로 받거나 함수를 반환하는 함수
### 함수 타입
```kotlin
val sum = { x: Int, y: Int -> x+y }
// => val sum: (Int, Int) -> Int = { x, y -> x+y }
val action = { println(42) }
// => val action: () -> Unit = { println(42) }
var canReturnNull: (Int, Int) -> Int? = {x, y -> null}
var funOrNull: ((Int, Int) -> Int)? = null
```
- 함수타입을 선언 할 때는 반환 타입을 반드시 명시해야 하므로 Unit을 빼먹어서는 안된다.
- 파라미터 타입은 유추할 수 있으나 컴파일에러시에 적어줘야 한다.
- 함수타입이 널이 될 수 있는 경우에는 함수타입을 괄호로 감싸고 그 뒤에 물음표를 붙여야 한다.

```kotlin
fun default(value: Long, operator: (a: Long) -> String = { it.toString()}) {
    println(operator(value))
}

fun default2(value: Long, operator: ((a: Long) -> String)? = null ) {
    println(operator?.invoke(value) ?: value.toString())
}
```
- 고차함수를 정의할 때 default action을 지정해둘 수 있다.
- 함수 타입이 `invoke` 메소드를 구현하는 인터페이스라서 null 가능성이 있는 경우 `?.`을 이용해서 null check가 가능하다.

### 함수를 함수에서 반환
```kotlin
fun printNumber(): (Int) -> Unit {
    return { num ->
        if (num % 2 == 0) println("짝수") else {
            println("홀수")
        }
    }
}
fun main() {
    val message = printNumber()
    message(5)
}
```
- 함수에서 함수를 반환하는 것도 가능

### 람다를 활용한 중복 제거
```kotlin
// 특정 OS에 대해서 duration 평균 구하기
fun List<SiteVisit>.averageDurationFor(os: String) = filter { it.os == os }.map { it.duration }.average()
// 특정 os N개에 대해서 duration 평균 구하기
fun List<SiteVisit>.averageDurationFor(osSet: Set<String>) = filter { it.os in osSet }.map { it.duration }.average()
// 특정 조건에 대해서 duration 평균 구하기
fun List<SiteVisit>.averageDurationFor(predicate: (SiteVisit) -> Boolean) =
    filter(predicate).map { it.duration }.average()
```
- 최종 목표가 같지만 중간 과정이 매번 달라지는 경우 고차함수를 이용해서 분리시키면 매번 새롭게 코드를 만들지 않고도
기존에 코드를 재사용할 수 있는 효과를 가져올 수 있다.
- **코드의 일부분을 복사해 붙여넣고 싶은 경우**가 있다면 그 코드를 람다로 만들면 중복을 제거할 수 있다.
- strategy pattern 방식과 비슷

## 인라인 함수: 람다의 부가 비용 없애기
- kotlin에서는 inline 키워드를 함수에 붙이면 컴파일러는 그 함수를 호출하는 모든 문장을 함수 본문에 해당하는
바이트코드로 바꿔치기 해준다.
### 인라이닝이 작동하는 방식
```kotlin
println("before")
synchronized("1234", { println("action") })
println("after")

==>

println("before")
l.lock()
try {
    println("action")
} finally {
    l.unlock()
}
println("after")

==>

println("before")
l.lock()
try {
    // body: () -> Unit
    body()
} finally {
    l.unlock()
}
println("after")
```
- inline으로 함수를 작성하게 되면 컴파일 시점에 해당 코드가 해당 메소드에 합쳐지게 된다.
- 단, 함수 타입의 변수를 넘기게 되면, 본문만 인라인 되지 않는다.
### 인라인 함수의 한계
```kotlin
fun <T, R> Sequence<T>.map(transform: (T) -> R): Sequence<R> {
    return TransformingSequence(this, transform)
}
// transform을 함수 인터페이스를 구현하는 무명 클래스 인스턴스로 만들어야 함!
```
- inline 함수에 전달된 람다식의 경우 일반적으로 컴파일 시점에 바이트코드로 변환해서 본문에 들어간다.
- 만약에 람다식을 다른 변수에 저장하고 나중에 사용해야 하는경우 본문에 포함될 수 없다.
```kotlin
inline fun foo(inlined: () -> Unit, noinline notInlined: () -> Unit) {
    
}
```
- inline 함수에 둘 이상의 람다를 인자로 받는 함수에서 일부 람다만 인라이닝하고 싶은 경우 `noinline` 키워드를 사용

### 컬렉션 연산 인라이닝
```kotlin
println(people.filter { it.age<30 })

println(people.asSequence().filter { it.age > 30})

public inline fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T> {
    return filterTo(ArrayList<T>(), predicate)
}

public fun <T> Sequence<T>.filter(predicate: (T) -> Boolean): Sequence<T> {
    return FilteringSequence(this, true, predicate)
}
```
- 컬렉션에서는 filter, map의 경우 inline 함수로 구현되어 중간 결과물을 만드는 방식을 사용
- sequence에서는 중간 결과물을 만들지 않는 대신, inline 함수를 사용하지 못 한다.
- 시퀀스를 통해 성능을 향상 시킬 수 있는 경우는 컬렉션 크기가 큰 경우만 해당
- 그 외에는 일반 컬렉션 연산이 성능이 더 나을 수 있다.
### 함수를 인라인으로 선언해야 하는 경우
- inline 키워드를 모든 곳에 붙여도 성능이 좋아질 가능성이 적고, 람다를 사용한 곳에서만 좋아질 가능성이 있다.
- 람다를 인자로 받는 함수를 인라이닝하면 좋은점 
1. 함수 호출 비용을 줄일 수 있을 뿐 아니라 람다를 표현하는 클래스와 람다 인스턴스에 해당하는 객체를 만들 필요도 없어진다.
2. 현재의 JVM은 함수 호출과 람다를 인라이닝해 줄 정도로 똑똑하지 못하다.
3. non-local 반환
- 단점
- 인라이닝하는 함수의 코드가 큰 경우 해당 함수를 호출하는 본문에 해당 바이트코드를 복사 해 넣어야 한다.

### 자원 관리를 위해 인라인된 람다 사용
```java
static String readFirstLineFromFile(String path) throws IOException {
    try(BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine()
        }
}
```
```kotlin
@Throws(IOException::class)
fun readFirstLineFromFile(path: String?): String? {
    BufferedReader(FileReader(path)).use { br -> return br.readLine() }
}
```
- kotlin에서는 try-with-resource를 `use`로 대신사용할 수 있다. 

## 고차 함수 안에서 흐름 제어
### 람다 안의 Return문 : 람다를 둘러싼 함수로부터 반환
```kotlin
fun lookForAlice(people: List<Person>) {
    for (person in people) {
        if(person.name == "Alice") {
            println("Found!")
            return // non-local return
        }
    }
    println("Alice is not found")
}
```
- `non-local return`: 자신을 둘러싸고 있는 블록보다 더 바깥에 있는 다른 블록을 반환하게 만드는 return문
- 인라인 함수로 되어 있는 람다 본문의 경우 non-local return이 가능하지만, 인라인 함수로 되어 있지 않은 람다의 경우는 non-local return이 불가능하다.

### 람다로부터 반환: 레이블을 사용한 return
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach label@ {
        if (it.name == "Alice") return@label // custom label
    }
    println("Alice might be somewhere")
}

fun lookForAlice(people: List<Person>) {
    people.forEach {
        if (it.name == "Alice") return@forEach // default label
    }
    println("Alice might be somewhere")
}
```
- local return : 해당 람다식의 실행을 해당 부분까지만 실행하겠다는 의미 `break`와 동일
- 람다식에는 label을 한개만 줄 수 있고, 설정을 했다면 default label로 호출할 수 없다.
### 무명 함수: 기본적으로 로컬 Return
```kotlin
fun lookForAlice(people: List<Person>) {
    people.forEach(fun (person) {
        if (person.name === "Alice") return
        println("${person.name} is not Alice")
    })
}
```
- 무명함수와 일반함수의 차이 : 함수 이름이나 파라미터 타입을 생략할 수 있다

# 과제
- 고차함수를 이용해 반 별 평균, 남녀 평균을 구하는 고차함수를 작성