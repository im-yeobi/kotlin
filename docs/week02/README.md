# 5장. 람다로 프로그래밍
람다식을 통해 method parameter 로 코드 블록을 넘기는 것이 가능해진다.
## 5.1 람다식과 멤버참조 
### 람다식을 사용하지 않은 프로그래밍
~~~kotlin
fun findTheOldest(people : List<Person>){
    for ~~~
}
~~~
### 람다를 사용한 프로그래밍
~~~kotlin
val people = listOf(~~~)
// 정식으로 쓴 람다식
people.maxByOrNull({ p: Person -> p.age })        
// 코틀린에서 제공하는 줄임
people.maxByOrNull { it.age }
~~~
(책에는 ``maxBy`` 로 나와있지만, 이는 1.4부터 deprecated 됨. 대신 `maxByOrNull` 사용)

~~~kotlin
public inline fun <T, R : Comparable<R>> Iterable<T>.maxByOrNull(selector: (T) -> R): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var maxElem = iterator.next()
    if (!iterator.hasNext()) return maxElem
    var maxValue = selector(maxElem)
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxElem = e
            maxValue = v
        }
    } while (iterator.hasNext())
    return maxElem
}
~~~
**inline** : 컴파일된 코드를 실행할 때 inline이 붙은 함수 A 호출시 해당 함수의 구현부로 점프하지 않고, 해당 함수 호출 부분에 함수의 정의를 가지고 와서 컴파일한다. 
(8장에서 더 자세히 나옴)

#### 코틀린에서의 람다식 문법
~~~kotlin
{ (파라미터) -> (본문) }
~~~
ex)
~~~kotlin 
{ x: Int, y : Int -> x+y }
~~~
코틀린에서의 람다식은 항상 **중괄호**로 둘러싸여있다

##### run
인자로 받은 람다를 실행해줌
~~~kotlin
run { printlln(42) } 
~~~

~~~kotlin
people.maxByOrNull({ p: Person -> p.age })
~~~

1. 구분자가 너무 많음  
   함수 호출시 맨 뒤에 있는 인자가 람다식이라면 그 람다를 괄호밖으로 빼낼 수 있다.
   람다가 어떤 함수의 유일한 인자이고 괄호 뒤에 람다를 썼다면 호출시 빈 괄호를 없애도 된다 
    ~~~kotlin
    people.maxByOrNull(){ p: Person -> p.age }
    people.maxByOrNull{ p: Person -> p.age }
    ~~~
    ~~~kotlin
    people.joinToString(separator = " ", transform= { p: Person -> p.name })
    people.joinToString(separator = " ") { p: Person -> p.name }
    people.joinToString(" ") { p: Person -> p.name }
    ~~~

2. 인자 타입은 유추할수 있으므로 적지 않아도 됨
    ~~~kotlin
    people.maxByOrNull{ p: Person -> p.age }
    people.maxByOrNull{ p -> p.age }
    ~~~

3. 인자가 하나일 경우 이름을 붙이지 않아도댐  
   디폴트 파라미터 이름인 it을 사용한다. 다만 람다가 중첩하는 경우 람다의 파라미터를 명시하는 편이 낫다. 
    ~~~kotlin
    people.maxByOrNull{ p -> p.age }
    people.maxByOrNull{ it.age }
    ~~~
    
람다가 변수에 저장될 때는 타입을 추론할 수 없으므로 명시가 필요하다 
 ~~~kotlin
val getAge = { p : Person -> p.age }
people.maxByOrNull(getAge)
~~~

본문이 여러 줄로 이루어진 경우 본문의 맨 마지막에 있는 식이 람다의 결과 값이 된다. 

### 람다에서 변수의 범위
람다를 함수안에서 정의시 함수의 param 뿐만 아니라 람다 정의 앞에 선언된 로컬 변수까지 람다에서 모두 사용할 수 있다

파이널 변수가 아닌 변수에 접근 가능하며 바깥의 변수를 변경할 수 있다 -> 람다 안에서 사용하는 외부 변수 (람다가 포획한 변수)
: 파이널변수의 경우 변수 값을 저장. 파이널이 아닌 경우 변수를 래퍼로 감싼뒤 래퍼를 저장하여 읽을 수 있게 한다
https://blog.fupfin.com/?p=50

**변수 포획** 내부 클래스 중 메서드 안에서 정의되고 생성되는 지역 클래스(Local Class)와 무명 클래스(Anonymous Class)의 경우, 그 객체를 생성할 때에, 지역 클래스와 무명 클래스에서 사용하는 외곽 블럭의 지역 변수와 매개변수를 지역 클래스와 무명 클래스의 멤버 필드로 복사합니다. 이를 변수 포획이라고 부릅니다.

### 멤버 참조 
코틀린에서 자바 8과 마찬가지로 함수를 값으로 바꿀 수 있다. 이때 이중 콜론을 사용한다
~~~kotlin
val getAge = Person::age
~~~
최상위에 선언된 함수나 프로퍼티
~~~kotlin
fun salute() = println("Salute!")
>> run(::salute)
~~~

**바운드 멤버참조**
멤버 참조를 생성할 때 클래스 인스턴스를 따로 저장하므로 수신객체(=해당 메소드가 실행되는 대상)를 따로 지정해주지 않아도 된다. 
~~~
>> val p = Person("A", 34)
>> val personAgeFunction = Person::age
>> println(personAgeFunction(p))
34

>> val dmitryAgeFunction = p::age
>> println(dmitryAgeFunction())
34
~~~
## 5.2 컬렉션 함수형 API
### filter, map, all any, count, find, groupBy
- count vs size : count의 경우 개수만 새고, size의 경우 중간 컬렉션을 만들어서 이에 대한 size를 가지고오기 때문에 기본적으로 count가 더 효율적이다. 

~~~kotlin
val people = listOf(Person("A", 30), ... , Person("Z", 100))
println(people.groupBy { it.age })
~~~
result >> 
~~~json
{ 29 = [Person(name="B", 29) ], 
  28 = [Person(name="C", 28), Person(name="D", 28)]}
~~~

### flatmap, flatten
flatmap은 리스트의 묶음을 풀어서 단일리스트로 변환. 내용이 변환할게 없다면 flatten

## 5.3 wldus rPtks zjffprtus dustks

### Lazy
앞의 함수들은 컬렉션을 즉시 생성한다. 따라서 연산이 많고 컬레션의 크기가 클수록 중간 임시 컬렉션들로 인한 부하가 커진다.

#### sequence 
first collection에 대한 reference와 어떠한 연산이 수행되는지를 저장해둔 sequence object가 반환된다. 최종연산이 불리는 경우 비로서 실행된다. 
sequence는 하나씩 실행을 한다. 


연산의 순서도 매우 중요하다. ``filter 다음의 map vs map 다음의 filter``

#### 람다의 자세한 구현
인라인 되지 않은 모든 람다식은 무명 클래스로 컴파일이 되기

## 5.4 자바 함수형 인터페이스 활용 
코틀린은 함수형 인터페이스를 인자로 취하는 자바 메소드를 호출할때 람다를 넘긴다. 람다 구현부가 함수형 인터페이스의 중심 구현부가 됨. 
~~~kotlin
postponeComputation(int delay, Runnable computation)
>> postponeComputation(1000) { println(42) }

postponeComputation(1000, object : Runnable {
   override fun run() {
      println(42)
   }
}
~~~

## 5.5 수신 객체 지정 람다 : with와 apply 
~~~kotlin
// using with

fun alphabet(): String {
   val result = StringBuilder()
   for ( letter in 'A' .. 'Z')}
      result.append(letter)
   }
   result.append("fin")
   return result.toString()
}

fun alphabet(): String {
   val result = StringBuilder()
   return with(stringBuilder) {
      for ( letter in 'A' .. 'Z')}
         this.append(letter)
      }
      this.append("fin")
      return this.toString()
   }
}

fun alphabet() = with(StringBuilder()) {
   for ( letter in 'A' .. 'Z')}
      append(letter)
   }
   append("fin")
   toString()
}

// using apply - 반환객체가 수신객체와 같은 타입
fun alphabet() = StringBuilder().apply {
   for ( letter in 'A' .. 'Z')}
   append(letter)
   }
   append("fin")
}.toString()

~~~
## 과제 설명
람다식 연습하기 : 람다식을 통해서 성적이 B 이상이고, 나이는 12살 이상 20살 이하인, 학생들을 반별로 묶어서 반환해라. 
이때 반별 학생들은 이름순으로 정렬되어있어야한다. 다만 학생이 많으므로 연산 수행시 오버헤드가 적도록 구현해라 


# 6장. 코틀린 타입 시스템
## 6.1 널 가능성
코틀린에서는 파라미터가 null인지 아닌지를 명확히 구분하도록 되어있다
~~~kotlin
// s는 null이 될수 없다
fun strLen(s: String)  = s.length 
// s는 null이 될 수 있다 
// 아래와 같은 호출은 불가 - nullable type에 직접 메소드를 호출할 수 없다. NPE에 safe함을 표기해줘야함
fun strLenSafe(s: String?) = s.length
// 아래와 같은 호출은 가능 
fun strLenSafe(s: String?) :Int = if(s != null) s.length else 0
// 아래와 같은 호출도 가능. 다만 return은 null이 나오게 된다. 
fun strLenSafe(s: String?) :Int = s?.length
// 만약 s가 null일 경우, 윗윗 예제처럼 0이 반환되었으면 좋겠다.
fun strLenSafe(s: String?) :Int = s?.length ?: 0
~~~

**?:** 엘비스 연산자

### 안전한 캐스트 : as? 
지정한 타입으로 캐스트하는데 캐스트가 불가능할경우 null을 반환한다. 
~~~kotlin
val otherPerson = o as? Person ?: return false
~~~

### 널 아님 단언 : !! 
코틀린에서 널이 될 수 있는 값을 널이 절대 아님으로 강제할 수 있다. 이 경우 만약 null이라면 예외를 던진다. 
예외 확인을 용이하게 하기 위해 !!를 한줄에 쓰는것은 피하자

### let 함수 
안전한 호출 연산자와 함께 사용해 원하는 식을 평가해서 결과가 널인지를 검사한 뒤 결과를 변수에 넣는다 

~~~ kotlin
fun sendEmailTo(email: String) { /*..*/ }
val email: String? = ..
sendEmailTo(email) -> error
if(email != null) sendEmailTo(email)
email?.let { sendEmailTo(it) } // email 이 null일 경우 아무일도 일어나지 않는다. 
~~~

### 나중에 초기화할 프로퍼티 
null과 null이 아닌것에 대한 구분은 필요한상태이며 처음 초기화전에는 null로 변수를 두고 싶을 때가 있다. 
이 경우 lateinit var을 사용한다. 
~~~kotlin
class MyTest {
   private var myService : MyService? = null 
   @Before fun setUp() {
      myService = MyService()
   }
   
   @Test
   fun test(){
      myService!!.performAction()
   }
}

// 아래처럼 변경 가능
class MyTest {
   private lateinit var myService : MyService? = null 
   @Before fun setUp() {
      myService = MyService()
   }
   
   // 메소드가 호출될때 null이 아님을 보장. null일 경우 has not been initalized error occurs
   @Test
   fun test(){
      myService.performAction()
   }
}
~~~

``isNullOrBlank()``나 `isNullOrEmpty()` 는 null이 아님을 보장하지 않아도 된다. 
그 외의 경우는 안전한 호출(?.)를 해주어야한다. 왜냐하면 코틀린은 type check시 <Type>과 <Type>?을 완전 다른 타입으로 취급한다. 
* 확장함수 호출시 not-null에 대해서만 호출가능하도록 안전하게 짜는것이 좋다. 

### 타입 파라미터의 널 가능성
generic으로 쓰이는 type의 경우 Any? 로 추론되기 때문에 null-safe를 보장할 수 있도록 해야한다. 타입파라미터가 널이 아님을 확실히 하려면 
아래처럼 타입 상한을 지정해야한다
~~~kotlin
fun <T : Any> functionName(t : T) 
~~~

### 널 가능성과 자바 
`@Nullabe String` == `String?`
`@NotNull String` == `String`

플랫폼 타입 : 코틀린이 널 관련 정보를 알 수 없는 타입. 이 경우 nullable로 처리해도 되고 그렇게 처리하지 않아도 된다. 대신 not-nullabe로 
처리했는데 null일 경우 NPE가 던져진다. 
이렇게 처리 된 이유는 모든 자바 타입을 nullable로 처리하면 불필요한 null check가 필요하기 때문에 이를 프로그래머에게 위임하는 방식을 선택했다. 


### 상속
코틀린 컴파일러는 null이 아닌 parameter를 갖는 함수를 자손에서 override할 경우 파라미터를 nullable/not-nullable 모두를 허용한다.
따라서 컴파일러가 선언한 모든 파라미털에 대해 널이 아님을 검사하는 단언문을 만들어준다. null값이 들어갈 경우 예외가 발생한다. 

## 6.2 코트린의 원시타입
코틀린은 원시타입과 래퍼타입을 구분하지 않는다. 그럼 전부 래퍼타입으로 관리하는 걸까?
그렇지 않다.
- 원시타입 : Int, Boolean -> 숫자 타입의 경우 실행 시점에 가장 효율적인 방법으로 표현된다.
- 널이 될 수 있는 원시 타입 : Int?, Boolean? -> 이 경우 래퍼타입으로 관리 
- 재네릭 클래스의 경우 래퍼타입으로 사용 

### 숫자 변환 
코틀린은 한타입의 숫자를 다른 타입의 숫자로 변환하지 않는다. 따라서 변환 메소드를 직접 호출해야한다. 숫자를 사용할 경우 리터럴을 붙여줘야한다. 
~~~kotlin
val x = 1
val list = listOf(1L, 2L, 3L)
x in list // false
~~~

### Any, Any? : 최상위 타입
자바의 object와 비슷한 역할. 다만 자바의 경우 object안에 원시타입은 들어가지 않지만, 코틀린은 원시타입까지 포함한다. 
Any에는 `toString`, `equals`, `hashCode` 세가지 함수를 가지고 있다

### Unit 타입 : 코틀린의 void
void와 달리 Unit을 타입인자로 쓸 수 있다. 이럴 경우 void 를 쓴거와 같은 역할을 한다. 명시적으로 반환해주지 않아도 된다. 

### Nothing 타입 
함수의 반환 타입이나 반환 타입으로 쓰일 타입 파라미터로만 쓸 수 있다. 예외를 던지거나 함수가 리턴될 일이 없는 경우 사용
Nothing has no instances. You can use Nothing to represent "a value that never exists": for example, if a function has the return type of Nothing, it means that it never returns (always throws an exception).


## 6.3 컬렉션과 배열 
코틀린에서는 List<Int?> 일경우 null이 포함될 수 있다.
널이 될수 있는 값으로 이루어진 널이 될수 있는 리스트 : List<Int?>?
이런 특성대문에 filterNotNull이라는 함수도 있다. 

### 읽기 전용과 변경 가능한 컬렉션
코틀린에서는 컬렉션안의 데이터에 접근하는 인터페이스와 컬렉션 안의 테이터를 변경하는 인터페이스를 분리했다. 
그렇다고 읽기 전용 인터페이스가 참조하는 컬렉션이 변경불가 컬렉션이라는 말은 아니다! 
여러개의 참조가 하나의 인스턴스를 가르키고 있을 수 있다. -> not thread safe
읽기 전용 : Collection
변경 가능 : MutableCollection

따라서 함수가 컬렉션안의 것을 변경한다면 함수 인자로 MutableCollection 를 받아야한다.

### 컬렉션의 플랫폼 타입 다루기 
컬렉션이 널이 될수 있는지, 원소가 널이 될 수 있는지, 오버라이드하는 메소드가 컬렉션을 변경할수 있는지를 체크한 뒤 변경해야한다. 


## 과제 설명
나이를 나누기 4한 값이 같은 사람들끼리 한 조기 되었다. 사람들을 데리고 조를 만들어라. 그리고 각 조에 대해서 이 사람들이 핸드폰을 가지고 있는지 검사를 해라. 
만약 핸드폰이 없다면 <no-phone>이라고 출력하고 핸드폰이 있다면 번호를 출력하라. 