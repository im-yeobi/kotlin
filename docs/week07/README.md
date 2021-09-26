## 발표 내용 작성

## Chapter 04. 일시 중단 함수와 코루틴 컨텍스트
- `일시 중단 함수(suspending functions)`를 배워보고 지금까지 사용했던 `비동기 함수`와 비교해본다.
- 코루틴 컨텍스트와 그 사용법도 자세히 다룬다.

### 일시 중단 함수
- 일시 중단 함수 : 함수 실행 중 일시 중단 & 재개될 수 있는 함수를 일시 중단 함수라 한다.
- 일시 중단 함수를 만드려면 시그니처에 `suspend` 제어자만 추가하면 된다.
- 일시 중단 함수에서 `delay()`와 같은 일시 중단 함수를 직접 호출할 수 있다.
```kotlin
suspend fun greetDelayed(delayMillis: Long) {
    delay(delayMillis)  // 일시 중단 함수 호출
    println("Hello, World!")
}
```

- suspend 키워드로 정의한 일시 중단 함수 역시 다른 일시 중단 함수와 동일하게 코루틴 빌더로 감싸야 한다.
```kotlin
runBlocking {
    greetDelayed(1000)  // 일시 중단 함수
}
```

#### 비동기 함수로 구현
- 여기서 비동기 함수란 `launch()`, `async()` 빌더로 감싼 함수를 의미한다.
- 비동기 함수임을 알 수 있도록 네이밍
- 해당 요청이 완료될 때까지 일시정지 해야 하므로 `await()` 사용
```kotlin
interface ProfileServiceClient {
  fun fetchByName(name: String): Deferred<SuspendProfile>
  fun fetchById(id: Long): Deferred<SuspendProfile>
}

class ProfileServiceClientImpl : ProfileServiceClient {
    fun asyncFetchByName(name: String) = GlobalScope.async {    // async 빌더
        Profile(1, name , 28)
    }

    fun asyncFetchById(id: Long) = GlobalScope.async {  // async 빌더
        Profile(id, "Susan" , 28)
    }
}
```

#### 일시 중단 함수로 구현
- `suspend` 키워드를 이용해 일시중단 함수임을 명시
- 반환형 Deferred 제거할 수 있다.
```kotlin
interface ProfileServiceClient {
  suspend fun fetchByName(name: String): SuspendProfile
  suspend fun fetchById(id: Long): SuspendProfile
}

class SuspendProfileServiceClientImpl : ProfileServiceClient {
    suspend fun fetchByName(name: String): SuspendProfile {
        return SuspendProfile(1, name , 28)
    }

    suspend fun fetchById(id: Long): SuspendProfile {
        return SuspendProfile(id, "Susan" , 28)
    }
}
```

#### 비동기 함수와 일시 중단 함수 차이
- 유연함 : 비동기 함수는 구현이 Deferred와 엮이게 됨으로 유연함이 떨어진다. 일시 중단 함수를 쓰면 Future(deferred)를 지원하는 모든 라이브러리를 구현에서 사용 가능.
- 간단함 : 순차적으로 수행하려는 작업에 비동기 함수를 사용하면 항상 `await()`를 호출해야 하는 번거로움이 생기고, 명시적으로 async가 포함된 함수 네이밍을 정의해야 한다.

#### 비동기 함수 대신 일시 중단 함수 사용 가이드라인
- 구현에 Job이 엮이는 것을 피하기 위해 일시 중단 함수 사용
- 인터페이스 정의할 때 항상 일시 중단 함수 사용. 비동기 함수를 사용하면 Job을 반환하기 위한 구현을 해야 한다.
- 추상 메소드를 정의할 때는 항상 일시 중단 함수를 사용한다.

### 코루틴 컨텍스트
- **코루틴은 항상 컨텍스트 안에서 실행된다.**
- 컨텍스트는 코루틴이 어떻게 실행되고 동작해야 하는지를 정의할 수 있게 해주는 요소들의 그룹이다.

### 디스패처
- 대스패처는 코루틴이 실행될 스레드를 결정한다. 시작될 곳, 중단 후 재개될 곳을 모두 결정한다.
- `스레드 간에 코루틴을 분산하는 오케스트레이터`

#### CommonPool
- CPU 바운드 작업을 위해서 프레임워크에 의해 자동으로 생성되는 스레드 풀이다.
- 스레드 풀의 최대 크기는 시스템의 코어 수에서 1을 뺀 값이다.
- 현재는 CommonPool을 직접 사용할 수 없다. `Dispathcers.Default` 가 기본 디스패처이다. (CPU 코어 개수만큼 생성되는 스레드 풀)
```kotlin
GlobalScope.launch(CommonPool) { // 지원 종료
    // TODO: Implement CPU-bound algorithm here
}
```

```kotlin
GlobalScope.launch(Dispatchers.Default) {
    // TODO: Implement CPU-bound algorithm here
}
```

#### Unconfined
- 첫 번째 중단 지점에 도달할 때까지 현재 스레드에 있는 코루틴을 실행한다.
- 중단 후 다음 코루틴이 실행되었던 스레드에서 재개된다.
```kotlin
GlobalScope.launch(Dispatchers.Unconfined) {
    // main 스레드 실행
    println("Starting in ${Thread.currentThread()}")
    delay(500)
    // Default Executor 스레드 실행
    println("Resuming in ${Thread.currentThread()}")
}.join()
```
```text
Starting in Thread[main,5,main]
Resuming in Thread[kotlinx.coroutines.DefaultExecutor,5,main]
```

#### 단일 스레드 컨텍스트
- 항상 코루틴이 특정 스레드 안에서 실행된다는 것을 보장한다.
- `newSingleThreadContext()`를 사용한다.
```kotlin
val dispatcher = newSingleThreadContext("newThread")

GlobalScope.launch(dispatcher) {
    // newThread 스레드 실행
    println("Starting in ${Thread.currentThread()}")
    delay(500)
    // newThread 스레드 실행
    println("Resuming in ${Thread.currentThread()}")
}.join()
```
```text
Starting in Thread[newThread,5,main]
Resuming in Thread[newThread,5,main]
```

#### 스레드 풀
- 스레드 풀을 갖고 있으며 해당 풀에서 가용한 스레드에서 코루틴을 시작하고 재개한다.
- 런타임이 가용한 스레드를 정하고 부하 분산을 위한 방법도 정하기 때문에 따로 할 작업은 없다.
- `newFixedThreadPoolContext()`를 사용한다.
```kotlin
val dispatcher = newFixedThreadPoolContext(4, "myPool")

GlobalScope.launch(dispatcher) {
    // myPool-1 스레드 실행
    println("Starting in ${Thread.currentThread()}")
    delay(500)
    // myPool-2 스레드 실행
    println("Resuming in ${Thread.currentThread()}")
}.join()
```
```text
Starting in Thread[myPool-1,5,main]
Resuming in Thread[myPool-2,5,main]
```

### 에외 처리
- 코루틴 컨텍스트의 또 다른 중요한 용도는 예측이 어려운 예외(uncaught exception)에 대한 동작을 정의하는 것이다.
- `CoroutineExceptionHandler를` 구현해 만들 수 있다.
```kotlin
val handler = CoroutineExceptionHandler { context, throwable ->
    println("Error captured in $context")
    println("Message : ${throwable.message}")
}

GlobalScope.launch(handler) {   // 컨텍스트에 exception handler 지정
    TODO("Not implemented yet")
}
```

```text
Error captured in [chapter04._08_예외처리Kt$main$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@5b2beb70, StandaloneCoroutine{Cancelling}@661a3b36, DefaultDispatcher]
Message : An operation is not implemented: Not implemented yet
```

#### Non-cancellable
- 코루틴의 실행이 취소되면 코루틴 내부에 `CancellationException` 유형의 예외가 발생하고 코루틴이 종료된다.
- 코루틴 내부에서 예외가 발생하기 때문에 try-finally 블록을 사용해 예외를 처리할 수 있다.
```kotlin
val job = launch {
    try {
        while (isActive) {
            delay(500)

            println("still running")
        }
    } finally {
        println("cancelled, will delay finalization now")
        delay(5000) // 실제로 동작하지 않음. 취소 중인 코루틴은 일시 중단될 수 없도록 설계됐다.
        println("delay completed, bye")
    }
}

delay(1200)
job.cancelAndJoin() // 코루틴 실행 취소
```

- 취소 중인 코루틴은 일시 중단될 수 없도록 설계됐다.
- 코루틴이 취소되는 동안 일시 중지가 필요한 경우 `NonCancellable` 컨텍스트를 사용해야 한다.
```kotlin
finally {
    withContext(NonCancellable) {   // 코루틴의 취소 여부와 관계없이 withContext()에 전달된 일시 중단 람다가 일시 중단될 수 있도록 보장한다.
        println("cancelled, will delay finalization now")
        delay(5000)
        println("delay completed, bye")
    }
}
```

### 컨텍스트에 대한 추가 정보
- 컨텍스트는 코루틴이 어떻게 동작할지에 대한 다른 세부사항들을 많이 정의할 수 있다.
- 컨텍스트는 결합된 동작을 정의해 작동하기도 한다.

#### 컨텍스트 조합
- 특정 스레드에서 수행하는 코루틴을 실행하고 동시에 해당 스레드를 위한 예외처리 설정 예제
- 더하기 연산자(+) 이용해 요소 결합
```kotlin
val dispatcher = newSingleThreadContext("myDispatcher")
val handler = CoroutineExceptionHandler { _, throwable ->
    println("Error captured")
    println("Message: ${throwable.message}")
}

GlobalScope.launch(dispatcher + handler) {  // 컨텍스트 조합
    println("Running in ${java.lang.Thread.currentThread().name}")
    TODO("Not implemented")
}.join()
```

#### 컨텍스트 분리
- 결합된 컨텍스트에서 컨텍스트 요소를 제거할 수도 있다.
- 요소를 제거하기 위해서는 제거할 요소의 키에 대한 참조가 있어야 한다.
```kotlin
val context = dispatcher + handler
val tmpContext = context.minusKey(dispatcher.key)   // 컨텍스트에서 디스패처 요소 제거
```

#### withContext를 사용하는 임시 컨텍스트 스위치
- `withContext()`는 코드 블록 실행을 위해 주어진 컨텍스트를 사용할 일시 중단 함수이다.
- withContext는 Job이나 Deferred를 반환하지 않는다. 전달한 람다의 마지막 구문에 해당하는 값을 반환할 것이다.
- `join()`이나 `await()`를 호출할 필요 없이 context가 종료될 때까지 일시 중단된다.
```kotlin
// aysnc
runBlocking {
  val dispatcher = newSingleThreadContext("myThread")
  val name = GlobalScope.async(dispatcher) {
    "Tester"
  }.await() // 결과 반환을 위해 await() 호출

  println("name : $name")
}

// withContext
runBlocking {
    val dispatcher = newSingleThreadContext("myThread")
    val name = withContext(dispatcher) {
        "Tester"
    }

    println("name : $name")
}
```

---

## Chapter 05. 이터레이터, 시퀀스 그리고 프로듀서

5장에서 다루는 내용
- 일시 중단 가능한 시퀀스(Suspendable sequence)
- 일시 중단 가능한 이터레이터(Suspendable iterator)
- 일시 중단 가능한 데이터 소스에서 데이터 산출
- 시퀀스와 이터레이터의 차이점
- 프로듀서(Producer)를 사용한 비동기 데이터 검색
- 프로듀서의 실제 사례

### 이터레이터
- 이터레이터는 요소들의 컬렉션을 순서대로 살펴보는 데 특히 유용하다.
- 코틀린 이터레이터의 특징
    - 인덱스로 요소를 검색할 수 없으므로, 순차 액세스만 가능하다.
    - hasNext() 함수로 더 많은 요소가 있는지 알 수 있다.
    - 요소는 한 방향으로만 검색할 수 있다. 이전 요소 검색 불가.
    - 재설정 할 수 없으므로 한 번만 반복할 수 있다.
- `iterator()` 빌더 사용 (코틀린 1.3부터 iterator 사용)
- 기본적으로 Iterator<T>를 리턴한다.
```kotlin
val iterator = iterator {
    yield(1)  // Iterator<Int>
}
```

#### 모든 요소 살펴보기
- 전체 이터레이터를 반복하기 위해서는 `forEach()`나 `forEachRemaining()` 함수를 사용할 수 있다.
```kotlin
iterator.forEach {
    println(it)
}
```

#### 다음 값 가져오기
- `next()`를 이용해 이터레이터의 요소를 읽을 수 있다.
```kotlin
iterator.next()
```

#### 요소가 더 있는지 검증하기
- `hasNext()`를 이용해 이터레이터에 다음 요소가 있는지 확인 할 수 있다. 요소가 있으면 true, 없으면 false를 반환한다.
```kotlin
iterator.hasNext()
```

#### 요소를 검증하지 않고 next() 호출하기
- `next()`로 이터레이터에서 요소를 가져올 때는 항상 `hasNext()`를 호출하는 것이 안전하다.
- `next()`를 호출했지만 더 이상 가져올 요소가 없으면 `NoSuchElementException`이 발생한다.
```kotlin
val iterator = iterator {
    yield(1)
}
println(iterator.next())
println(iterator.next())  // NoSuchElementException 발생
```

#### hasNext()의 내부 작업에 대한 참고사항
- `hasNext()`가 작동하려면 런타임은 코루틴 실행을 재개한다.
- `hasNext()` 호출로 인해 값이 산출되면 값이 유지되다가 다음에 `next()`를 호출할 때 값이 반환된다.
```kotlin
  val iterator = iterator {
      println("yielding 1")
      yield(1)
      println("yielding 2")  // hasNext() 호출하면 이터레이터가 두 번째 값 생성
      yield(2)
  }

  iterator.next()

  if (iterator.hasNext()) {
      println("iterator has next")
      iterator.next()
  }
```
```text
yielding 1
yielding 2
iterator has next
```

### 시퀀스
- 인덱스로 값을 가져올 수 있다.
- 상태가 저장되지 않으며(stateless) 상호 작용한 후 자동으로 재설정(reset) 된다. => elementAt 등으로 읽어들일 때 항상 처음부터 가져온다. 
- 한 번의 호출로 값 그룹을 가져올 수 있다. (`take`)
- 일시 중단 시퀀스를 만들기 위해 `sequence()` 빌더를 사용한다. (코틀린 1.3부터 sequence 사용)
```kotlin
val sequence = sequence {
    yield(1)
}
```

#### 시퀀스의 모든 요소 읽기
- 시퀀스의 모든 요소를 살펴보기 위해 `forEach()`, `forEachIndexed()`를 사용할 수 있다.
- `forEachIndexed()`는 값과 함께 값의 인덱스를 제공하는 확장 함수다.
```kotlin
sequence.forEach {
    println("$it ")
}

sequence.forEachIndexed { index, value ->
  println("$index is $value ")
}
```

#### 특정 요소 얻기
- `elementAt()`을 이용해 인덱스를 가져와 해당 위치의 요소를 반환한다.
```kotlin
sequence.elementAt(4)
```
- `elementAtOrElse()` 함수는 주어진 인덱스에 요소가 없으면 람다로 실행된다. 람다는 전달된 인덱스를 받는다.
```kotlin
sequence.elementAtOrElse(5) { it * 10 }
```
- `elementAtOrNull` 인덱스를 가져와서 T?를 반환한다. 인덱스에 요소가 없으면 null 반환
```kotlin
sequence.elementAtOrNull(5)
```
- `take()` 요소 그룹 얻기
- take()는 중간 연산이므로 종단연산이 호출되는 시점에 계산돼 Sequence<T>를 반환한다.
- take 한 요수 개수보다 실제 시퀀스의 요소 개수가 적으면, take 한 요소 개수만큼만 반환한다.
```kotlin
val firstFive = sequence.take(5)
println(firstFive.joinToString())
```

#### 시퀀스는 상태가 없다.
- 일시 중단 시퀀스는 상태가 없고(stateless), 사용된 후에 재설정(reset) 된다.
- 이터레이터를 사용하는 것과 달리 시퀀스는 각각의 호출마다 요소의 처움부터 실행된다. (index 0부터 실행)

#### 피보나치
```kotlin
val fibonacci = sequence {
    yield(1)
    var current = 1
    var next = 1

    while(true) {
        yield(next)
        val tmpNext = current + next
        current = next
        next = tmpNext
    }
}

val take = fibonacci.take(10)
println(fibonacci.elementAt(2))
println(take.joinToString())
```

### 프로듀서
- 시퀀스와 이터레이터는 실행 중에 일시 중단할 수 없다는 제한이 있다.
- 프로듀서의 중요한 세부 사항
    - 프로듀서는 값이 생성된 후 일시 중단되며, 새로운 값이 요청될 때 재개된다.
    - 프로듀서는 특정 CoroutineContext로 생성할 수 있다.
    - 전달되는 일시 중단 람다의 본문은 언제든지 일시 중단될 수 있다.
    - 어느 시점에서든 일시 중단할 수 있으므로 프로듀서의 값은 일시 중단 연산에서만 수신할 수 있다.
    - 채널을 사용해 작동하므로 데이터를 스트림처럼 생각할 수 있다. 요소를 수신하면 스트림에서 요소가 제거된다.
- 프로듀서를 만들려면 코루틴 빌더 `produce()`를 호출해야 한다. ReceiveChannel<E>을 리턴한다.
- 프로듀서의 요소를 산출하기 위해 `send(E)` 함수를 사용한다.

```kotlin
val producer = GlobalScope.produce {
    send(1)
}
```

- `launch()`, `async()`와 같은 방식으로 CoroutineContext를 지정할 수 있다.
```kotlin
val context = newSingleThreadContext("myThread")

val producer = GlobalScope.produce(context) {
    send(1)
}
```

#### 프로듀서와 상호작용
- 프로듀서와의 상호작용은 시퀀스와 이터레이터를 사용해 수행되는 방식을 혼합한 것이다.
- 다음장에서 Channel에 대해 자세히 알아보고 이번장에서는 `ReceiveChannel`의 일부 기능에 대해서만 설명.

#### 프로듀서의 모든 요소 읽기
- `consumeEach()` 함수를 이용해 프로듀서의 모든 요소 읽기
```kotlin
val context = newSingleThreadContext("myThread")

val producer = GlobalScope.produce(context) {
    for (i in 0..9) 
        send(i)
}
```

#### 단일 요소 받기
- `receive()` 함수를 이용해 단일 요소 읽기
```kotlin
producer.receive()
```

#### 요소 그룹 가져오기
- `take()` 함수를 이용해 입력한 개수만큼 묶음으로 요소를 가져올 수 있다. (코틀린 1.4에서 deprecated)
- 시퀀스와 동일하게 `take()`는 중간 연산이므로 종단 연산이 발생할 때 요소의 실제 값이 계산된다.
```kotlin
producer.take(3).consumeEach {
    println(it)
}
```

#### 사용 가능한 요소보다 더 많은 요소 사용하기
- 프로듀서는 더 이상 요소가 없으면 중지된다.
- 프로듀서가 실행을 완료하면 채널이 닫히기 때문에 중단이 발생한다. `ClosedReceiveChannelException` 예외가 발생한다.
- isClosedForReceive() 로 검증

---

## 과제 설명
