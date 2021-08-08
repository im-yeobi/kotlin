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
