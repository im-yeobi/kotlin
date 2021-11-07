# Asynchronous Flow

suspending 함수는 asynchronous하게 하나의 값을 return한다. 그럼 계산된 여러개의 값을 asynchronous하게 어떻게 return 할수 있을까?

## Representing multiple value
여러개의 값은 collection으로 표현이 될수 있다. list를 사용하거나 forEach를 사용해 print할 수 있다. 

### sequences
만약 우리가 숫자를 CPU를 사용하는 blocking code를 이용해 계산을 한다면 우리는 sequence를 이용해 숫자를 표현할 수 있다. 
~~~kotlin
fun simple(): Sequence<Int> = sequence { // sequence builder
    for (i in 1..3) {
        Thread.sleep(100) // pretend we are computing it
        yield(i) // yield next value
    }
}

fun main() {
    simple().forEach { value -> println(value) } 
}
~~~
이렇게 되면 각 숫자 출력전 100ms를 기다리게 된다. 

### suspending functions
그러나 이 계산은 코드를 실행하는 main thread를 blocking한다. 만약 이 값들이 asynchronous code에 의해 계산이 된다면 suspend 한정자를 이용해서 main thread를 block 시키지 않고 동작하도록 변경할 수 있다. 

~~~
suspend fun simple(): List<Int> {
    delay(1000) // pretend we are doing something asynchronous here
    return listOf(1, 2, 3)
}

fun main() = runBlocking<Unit> {
    simple().forEach { value -> println(value) } 
}
~~~

### Flows
```List<Int>```의 경우 list에 들어갈 모든 값들이 계산된 뒤 한번에 return하는 것이다. asynchronous하게 계산되는 stream을 표현하기 위해서는 우리는 Flow<Int>라는 것을 사용할 수 있다.
Sequence<Int> 값도 마찬가지로 사용할 수 있다. 
~~~kotlin
fun simple(): Flow<Int> = flow { // flow builder
    for (i in 1..3) {
        delay(100) // pretend we are doing something useful here
        emit(i) // emit next value
    }
}

fun main() = runBlocking<Unit> {
    // Launch a concurrent coroutine to check if the main thread is blocked
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }
    // Collect the flow
    simple().collect { value -> println(value) } 
}
~~~
- flow의 builder function은 flow {} 이다. 
- flow {} 안의 코드는 suspend될 수 있다. 
- flow로 이루어진 함수는 suspend 한정자가 붙지 않아도 된다. 
- 값은 emit를 통해 방출된다
- collect를 통해 값은 모아진다.

만약 simple function의 delay를 Thread.sleep으로 대체하면 main thread가 blocking 되는 것을 볼 수 있다. 


## Flow are cold
Flow는 sequence와 비슷하지만, flow는 collect 되기전까지 실행되지 않는다
~~~
fun simple(): Flow<Int> = flow { 
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    println("Calling simple function...")
    val flow = simple()
    println("Calling collect...")
    flow.collect { value -> println(value) } 
    println("Calling collect again...")
    flow.collect { value -> println(value) } 
}
~~~

~~~
Calling simple function...
Calling collect...
Flow started
1
2
3
Calling collect again...
Flow started
1
2
3
~~~

여기서 simple function은 suspend 한정자를 가지고 있지 않다는 것과 flow는 매번 처음부터 collect 된다는 점에 주목할 수 있다. 

## Flow cancellation basics
Flow는 일반적인 coroutine의 취소와 유사하게 동작한다. withTimeoutOrNull
~~~
fun simple(): Flow<Int> = flow { 
    for (i in 1..3) {
        delay(100)          
        println("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    withTimeoutOrNull(250) { // Timeout after 250ms 
        simple().collect { value -> println(value) } 
    }
    println("Done")
}
~~~

~~~
Emitting 1
1
Emitting 2
2
Done
~~~

## Flow builders
- 앞서 본 flow {} 라는 가장 기본적인 builder가 존재한다
- flowOf는 정해진 set을 가지고 flow를 만든다
- collection을 flow로 변경할땐 .asFlow()를 사용한다. 

~~~
(1..3).asFlow().collect { value -> println(value) }
~~~

## Intermediate flow operators 
Flow도 operator를 이용해 여러 형태로 변환이 가능하다. 
Intermediate operator는 upstream flow를 downstream flow로 반환한다. 이 operator는 flow처럼 cold한다. 이 opreator를 부르는 것 자체는 suspending하게 동작하지 않는다. 
flow의 operator에도 map과 filter가 있다. sequence와 다른점은 operator안의 코드들이 suspending function을 부를 수 있다는 것이다. 


~~~       
suspend fun performRequest(request: Int): String {
    delay(1000) // imitate long-running asynchronous work
    return "response $request"
}

fun main() = runBlocking<Unit> {
    (1..3).asFlow() // a flow of requests
        .map { request -> performRequest(request) }
        .collect { response -> println(response) }
}
~~~

### Transform operator
flow transform operator 중에 가장 많이 쓰이는 것은 transform이다. 이는 map이나 filter같은 간단환 변환을 본떴으며 보다 복잡한 변환을 구현하는데 사용할 수 있다.  

~~~
(1..3).asFlow() // a flow of requests
    .transform { request ->
        emit("Making request $request") 
        emit(performRequest(request)) 
    }
    .collect { response -> println(response) }
~~~

### Size-limiting operators
flow를 정해진 limit만큼만 실행할 수 있다. exception이 발생할 경우 coroutine의 cancellation이 발생하게 된다.  
~~~
fun numbers(): Flow<Int> = flow {
    try {                          
        emit(1)
        emit(2) 
        println("This line will not execute")
        emit(3)    
    } finally {
        println("Finally in numbers")
    }
}

fun main() = runBlocking<Unit> {
    numbers() 
        .take(2) // take only the first two
        .collect { value -> println(value) }
}         
~~~

~~~
1
2
Finally in numbers
Copied!
~~~

## Terminal flow operators
이건 flow를 collect하는 suspending function을 뜻한다. collect가 가장 기본적인 oeprator이다. 
- toList, toSet,
- first, single
- reduce, fold

~~~
val sum = (1..5).asFlow()
    .map { it * it } // squares of numbers from 1 to 5                           
    .reduce { a, b -> a + b } // sum them (terminal operator)

println(sum)
~~~
~~~
55
~~~

## Flows are sequential
여러 flow를 이용하는 operator를 사용하지 않는 한 기본적으로 flow는 순차적으로 동작한다. 
~~~
(1..5).asFlow()
    .filter {
        println("Filter $it")
        it % 2 == 0              
    }              
    .map { 
        println("Map $it")
        "string $it"
    }.collect { 
        println("Collect $it")
    }    
~~~

~~~
Filter 1
Filter 2
Map 2
Collect string 2
Filter 3
Filter 4
Map 4
Collect string 4
Filter 5
~~~

## Flow context
flow의 모음(끝맺음)은 항상 불린 코루틴안에서 발생한다. 
~~~
withContext(context) {
    simple().collect { value ->
        println(value) // run in the specified context
    }
}
~~~
위 코드에서 simple function의 경우 안에가 어떻게 구현이 되어있던 이 코드의 작성자가 지정한 컨텍스트에서 실행된다. 
이를 context preservation이라고 한다. 

~~~
fun simple(): Flow<Int> = flow {
    log("Started simple flow")
    for (i in 1..3) {
        emit(i)
    }
}  

fun main() = runBlocking<Unit> {
    simple().collect { value -> log("Collected $value") } 
}    
~~~
이 경우 아래처럼 불린 context(main) 에서 실행이 된다 
~~~
[main @coroutine#1] Started simple flow
[main @coroutine#1] Collected 1
[main @coroutine#1] Collected 2
[main @coroutine#1] Collected 3
~~~

### Wrong emission withContext
그러나 long-running CPU consuming code의 경우 Dispatcher.Default에서 실행되어야할 필요서잉 있고, UI-updating code는 Dispatcher.Main에서 실행되어야할 필요성이 있을 것이다.
보통 withContext는 코틀린 코루틴의 context를 변경할때 사용한다. 그러나 flow {} 안의 코드는 context preservation이 있기때문에 다른 context에서 emit하는 것이 허용되지 않는다. 

~~~
fun simple(): Flow<Int> = flow {
    // The WRONG way to change context for CPU-consuming code in flow builder
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        for (i in 1..3) {
            Thread.sleep(100) // pretend we are computing it in CPU-consuming way
            emit(i) // emit next value
        }
    }
}

fun main() = runBlocking<Unit> {
    simple().collect { value -> println(value) } 
~~~
이걸 실행하면 아래와 같은 에러가 뜬다

~~~
Exception in thread "main" java.lang.IllegalStateException: Flow invariant is violated:
		Flow was collected in [CoroutineId(1), "coroutine#1":BlockingCoroutine{Active}@5511c7f8, BlockingEventLoop@2eac3323],
		but emission happened in [CoroutineId(1), "coroutine#1":DispatchedCoroutine{Active}@2dae0000, Dispatchers.Default].
		Please refer to 'flow' documentation or use 'flowOn' instead
	at ...
~~~

### flowOn operator
만약 context를 바꾸고 싶다면 flowOn를 사용하면 된다. 
~~~
fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        Thread.sleep(100) // pretend we are computing it in CPU-consuming way
        log("Emitting $i")
        emit(i) // emit next value
    }
}.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

fun main() = runBlocking<Unit> {
    simple().collect { value ->
        log("Collected $value") 
    } 
}     
~~~

## Buffering
flow의 일부를 다른 coroutine에서 실행하는 것은 전체 시간을 감소시키는데 도움을 준다. 
예를 들어 아래처럼 produce하는데 100ms가 걸리고, process에 300ms가 걸리는 flow가 있다고 하자
이 코드는 실행시키는데 1200ms 가량 걸린다. 
~~~
fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100) // pretend we are asynchronously waiting 100 ms
        emit(i) // emit next value
    }
}

fun main() = runBlocking<Unit> { 
    val time = measureTimeMillis {
        simple().collect { value -> 
            delay(300) // pretend we are processing it for 300 ms
            println(value) 
        } 
    }   
    println("Collected in $time ms")
}
~~~
여기서 우리는 buffer operator를 사용할 수 있다. buffer를 사용하면 각 element를 produce할때 100ms를 기다리지 않는다. 그래서 1000ms 정도만 걸린다. 
buffer를 사용하면 emit과 collect가 다른 코루틴에서 동작하게 된다. 
따라서 데이터를 미리 생산해 둘수가 있다. 

### Conflation (합체)
flow의 작업 또는 작업 상태 업데이트의 부분적인 결과를 나타내는 경우 각 값을 처리할 필요가 없고 대신 가장 최근 값만 처리할 수 있다. 이 경우 수집기가 너무 느려서 중간 값을 처리할 수 없을 때 conflate 연산자를 사용하여 중간 값을 건너뛸 수 있다.
1번이 process 되는동안 2,3이 이미 생성이 되었기때문에 2번은 생략이 되고 3번(가장 최근값)만 전달된다.

~~~
val time = measureTimeMillis {
    simple()
        .conflate() // conflate emissions, don't process each one
        .collect { value -> 
            delay(300) // pretend we are processing it for 300 ms
            println(value) 
        } 
}   
println("Collected in $time ms")
~~~

~~~
1
3
Collected in 758 ms
~~~


### Processing the latest value
가장 마지막 값을 가지고 오고 싶을때 
~~~
val time = measureTimeMillis {
    simple()
        .collectLatest { value -> // cancel & restart on the latest value
            println("Collecting $value") 
            delay(300) // pretend we are processing it for 300 ms
            println("Done $value") 
        } 
}   
println("Collected in $time ms")
~~~
~~~
Collecting 1
Collecting 2
Collecting 3
Done 3
Collected in 741 ms
~~~


## Composing multiple flows
여러 flow를 compose하는 operator들

### zip
~~~
val nums = (1..3).asFlow() // numbers 1..3
val strs = flowOf("one", "two", "three") // strings 
nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string
    .collect { println(it) } // collect and print
~~~

~~~
1 -> one
2 -> two
3 -> three
~~~

### combine
~~~
val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
val startTime = System.currentTimeMillis() // remember the start time 
nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string with "zip"
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
~~~

~~~
1 -> one at 442 ms from start
2 -> two at 826 ms from start
3 -> three at 1231 ms from start
~~~
zip의 경우 위와 같은 결과를 얻을 수 있다. 이는 one/two/three가 400ms마다 업데이트가 되기 때문에
400 ms마다 print가 찍히게 된다. 


~~~
val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms          
val startTime = System.currentTimeMillis() // remember the start time
nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
.collect { value -> // collect and print
println("$value at ${System.currentTimeMillis() - startTime} ms from start")
}
~~~

~~~
1 -> one at 481 ms from start
2 -> one at 664 ms from start
2 -> two at 899 ms from start
3 -> two at 966 ms from start
3 -> three at 1303 ms from start
~~~
combine으로 변경한다면 위와 같다. 이건 매번 update(300ms/400ms)마다 print를 하게 된다(업데이트가 일어난다)

## Flattening flows
~~~
fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) // wait 500 ms
    emit("$i: Second")
}

(1..3).asFlow().map { requestFlow(it) }
~~~
flatten을 이용해 Flow<Flow<String>>을 펼칠수가 있다. 

### flatMapConcat
~~~
val startTime = System.currentTimeMillis() // remember the start time 
(1..3).asFlow().onEach { delay(100) } // a number every 100 ms 
    .flatMapConcat { requestFlow(it) }                                                                           
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
~~~

~~~
1: First at 121 ms from start
1: Second at 622 ms from start
2: First at 727 ms from start
2: Second at 1227 ms from start
3: First at 1328 ms from start
3: Second at 1829 ms from start
~~~

### flatMapMerge
flatMapMerge나 flattenMerge로 된다. 즉각적으로 merge를 한다. 

~~~
1: First at 136 ms from start
2: First at 231 ms from start
3: First at 333 ms from start
1: Second at 639 ms from start
2: Second at 732 ms from start
3: Second at 833 ms from start
~~~

flatMapMerge는 sequentially 블락을 실행시키지만, 결과를 동시에 수집한다. 

### flatMapLatest
가장 마지막에 있는 것을 수집함 
~~~
val startTime = System.currentTimeMillis() // remember the start time 
(1..3).asFlow().onEach { delay(100) } // a number every 100 ms 
    .flatMapLatest { requestFlow(it) }                                                                           
    .collect { value -> // collect and print 
        println("$value at ${System.currentTimeMillis() - startTime} ms from start") 
    } 
~~~

~~~
1: First at 142 ms from start
2: First at 322 ms from start
3: First at 425 ms from start
3: Second at 931 ms from start
~~~

## Flow exceptions
~~~
fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i) // emit next value
    }
}

fun main() = runBlocking<Unit> {
    try {
        simple().collect { value ->         
            println(value)
            check(value <= 1) { "Collected $value" }
        }
    } catch (e: Throwable) {
        println("Caught $e")
    } 
}            	
~~~
~~~			    
Emitting 1
1
Emitting 2
2
Caught java.lang.IllegalStateException: Collected 2			    
~~~

			    
~~~
fun simple(): Flow<String> = 
    flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // emit next value
        }
    }
    .map { value ->
        check(value <= 1) { "Crashed on $value" }                 
        "string $value"
    }

fun main() = runBlocking<Unit> {
    try {
        simple().collect { value -> println(value) }
    } catch (e: Throwable) {
        println("Caught $e")
    } 
}            			    
~~~			    
			    
~~~
Emitting 1
string 1
Emitting 2
Caught java.lang.IllegalStateException: Crashed on 2	
~~~	
	
~~~
fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    simple()
        .catch { e -> println("Caught $e") } // does not catch downstream exceptions
        .collect { value ->
            check(value <= 1) { "Collected $value" }                 
            println(value) 
        }
}      	
~~~
~~~			    
simple()
    .onEach { value ->
        check(value <= 1) { "Collected $value" }                 
        println(value) 
    }
    .catch { e -> println("Caught $e") }
    .collect()
~~~
	
	
## Flow completion
~~~
fun simple(): Flow<Int> = (1..3).asFlow()

fun main() = runBlocking<Unit> {
    try {
        simple().collect { value -> println(value) }
    } finally {
        println("Done")
    }
} 	
~~~
~~~	
fun simple(): Flow<Int> = (1..3).asFlow()

fun main() = runBlocking<Unit> {
    try {
        simple().collect { value -> println(value) }
    } finally {
        println("Done")
    }
} 	
~~~
~~~	
fun simple(): Flow<Int> = (1..3).asFlow()

fun main() = runBlocking<Unit> {
    simple()
        .onCompletion { cause -> println("Flow completed with $cause") }
        .collect { value ->
            check(value <= 1) { "Collected $value" }                 
            println(value) 
        }
}	
~~~
~~~
1
Flow completed with java.lang.IllegalStateException: Collected 2
Exception in thread "main" java.lang.IllegalStateException: Collected 2			    
~~~	
downstream flow에도 영향을 받는다 

## Launching flow
~~~
// Imitate a flow of events
fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(100) }

fun main() = runBlocking<Unit> {
    events()
        .onEach { event -> println("Event: $event") }
        .collect() // <--- Collecting the flow waits
    println("Done")
}            
~~~			   
~~~
Event: 1
Event: 2
Event: 3
Done
~~~			   
			   
			   
			   
# Channels
지연된 값은 코루틴 간에 단일 값을 전송하는 편리한 방법을 제공한다. 채널은 값 스트림을 전송하는 방법을 제공한#.

## Channel basics
Channel은 개념적으로 BlockingQueue와 매우 유사하다. 하나 다른점은 blocking의 put대신에, suspending send를 가지고 있다는 것이다. 그리고 blocking take 대신에 suspending recieve를 가지고 있다는 것이다. 

~~~
val channel = Channel<Int>()
launch {
    // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
    for (x in 1..5) channel.send(x * x)
}
// here we print five received integers:
repeat(5) { println(channel.receive()) }
println("Done!")
~~~

~~~
1
4
9
16
25
Done!
Copied!
~~~

## Closing and iteration over channels
Queue와 다르게, channel은 더이상 값이 오지 않는것을 표현하기위해 channel을 닫을 수 있다. 
받는 쪽에서는 for loop등으로 element를 받을수 있기때문에 매우 편리하다.
개념적으로 close는 close를 위한 특별한 token을 보내느 것이다. iteration은 close token을 받으면 멈춘다. 
그래서 close token전에 보내진 element들은 반드시 받음을 보장할 수 있다, 

## Building channel producers
코루틴이 element sequence를 생성하는 것은 일반적이다. producer-consumer pattern은 동시성 코드에서 많이 사용된다.
producer라는 코루틴패턴을 이용해서 코루틴을 만들거나 for 루프를 대체하는 foreach를 사용할 수 있다
~~~kotlin
val squares = produceSquares()
squares.consumeEach { println(it) }
println("Done!")
~~~ 


## Pipelines
파이프라인은 하나의 코루틴이 아마도 무한한 값의 스트림을 생성하는 패턴이다. 예를 들어 아래와 같은 producer가 있다고 하자. 

~~~kotlin 
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) // infinite stream of integers starting from 1
}
~~~

~~~
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}
~~~
이를 받아서 어떤 프로세싱을 거치고 다시 재 produce 하는 스트림도 있다. 위 예제는 스트림을 받아서 제곱을 하고 다시 produce를 하고 있다. 
이 둘을 합치면 

~~~
val numbers = produceNumbers() // produces integers from 1 and on
val squares = square(numbers) // squares integers
repeat(5) {
    println(squares.receive()) // print first five
}
println("Done!") // we are done
coroutineContext.cancelChildren() // cancel children coroutines
~~~


## Prime numbers with pipeline
코루틴의 파이프라인을 이용해 소수를 만드는 예제를 보자 
~~~
fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}
~~~
~~~
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}
~~~
~~~
var cur = numbersFrom(2)
repeat(10) {
    val prime = cur.receive()
    println(prime)
    cur = filter(cur, prime)
}
coroutineContext.cancelChildren() // cancel all children to let main finish
~~~
이중 소수 10개만 출력할려면 그 뒤에 cancelChildren을 통해 그 이후의 작업을 취소할 수 있다. 

물론 이 작업을 기본 코루틴 빌더를 이용해서도 만들 수 있다. producer를 iterator로 변경하고 yield로 send하고 next로 받을 수 있다.
그러나 위와 같이 채널을 사용하는 파이프라인의 이점은 Dispatchers.Default 컨텍스트에서 실행하면 실제로 여러 CPU 코어를 사용할 수 있다는 것이다.
produce를 사용하기 때문에 함수 실행이 끝날때까지 block되는게 아니라 결과값이 만들어질때마다 async하게 channel로 보내지게 되며 consumer 쪽에서 지정된 개수를 받을 띠까지 대기한다. 

어쨌든 이것은 소수를 찾는 매우 비효율적인 방법이다. 
실제로 파이프라인은 다른 외부 서비스로 asynchronous하게 call을 하는 등의 suspend function이 필요한 경우가 많다. 
이러한 파이프라인은 시퀀스/반복자를 사용하여 빌드할 수 없다. 
왜냐하면 완전히 비동기식인 생산과 달리 임의의 일시 중단을 허용하지 않기 때문이다. 

## Fan-out
많은 코루틴들이 하나의 채널에서 값을 컨슘해서 분산적으로 실행하고자 하는 니즈가 있을 수도 있다. 
~~~
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1 // start from 1
    while (true) {
        send(x++) // produce next
        delay(100) // wait 0.1s
    }
}
~~~
(1초에 숫자를 10개 보낸다)
이를 받는 아래와 같은 함수가 있다고 하자(그냥 프린트만함)
~~~
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}
~~~
그리고 아래와 같은 main 함수가 있다고 하자 .
~~~
val producer = produceNumbers()
repeat(5) { launchProcessor(it, producer) }
delay(950)
producer.cancel() // cancel producer coroutine and thus kill them all
~~~
output
~~~
Processor #2 received 1
Processor #4 received 2
Processor #0 received 3
Processor #1 received 4
Processor #3 received 5
Processor #2 received 6
Processor #4 received 7
Processor #0 received 8
Processor #1 received 9
Processor #3 received 10
~~~
결과 해석 : launchProcessor의 경우 channel을 받아와서 channel의 이미지를 for 루프로 찍어낸다. 채널은 1개
그리고 이것이 repeat(5)로 인해 5개의 코루틴이 존재하게 됨. 
하나 프린트하고 100를 쉰다. 
950을 쉬므로 10개의 숫자를 찍을 수 있음. 

또한, launchProcessor 코드에서 팬아웃을 수행하기 위해 for 루프를 사용하여 채널을 명시적으로 반복한다
ConsumerEach와 달리 이 for 루프 패턴은 여러 코루틴에서 사용하기에 완벽하게 safe하다. 
프로세서 코루틴 중 하나가 실패하면 다른 코루틴은 여전히 채널을 처리하는 반면,  consumerEach를 통해 작성된 프로세서는 
정상 또는 비정상 완료 시 항상 기본 채널을 소비(취소)합니다. 


## Fan-in 
여러개의 채널이 하나의 코루틴에 데이터를 보낼때도 있다. 
~~~
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}
~~~

~~~
val channel = Channel<String>()
launch { sendString(channel, "foo", 200L) }
launch { sendString(channel, "BAR!", 500L) }
repeat(6) { // receive first six
    println(channel.receive())
}
coroutineContext.cancelChildren() // cancel all children to let main finish
~~~

~~~
foo
foo
BAR!
foo
foo
BAR!
Copied!
~~~
이럴경우 output 

## Buffered channels
지금까지의 non-buffered 채널은 send가 먼저 호출되면 receive가 호출될 때까지 일시중단되고, receive가 먼저 호출되면 send가 호출될 때까지 일시중단된다. 
buffered channel은 capacity가 있어서 full되면 비워질때까지 suspend된다. 

~~~
val channel = Channel<Int>(4) // create buffered channel
val sender = launch { // launch sender coroutine
    repeat(10) {
        println("Sending $it") // print before sending each element
        channel.send(it) // will suspend when buffer is full
    }
}
// don't receive anything... just wait....
delay(1000)
sender.cancel() // cancel sender coroutine
~~~
~~~
Sending 0
Sending 1
Sending 2
Sending 3
Sending 4
~~~
버퍼 사이즈가 4인 채널에다가 10만큼을 넣었다. recevie가 불리지 않았으므로 5번째를 넣는다고 한뒤에 send가 보내지지 않는다. (receive가 없으므로)


## Channels are fair
보내고 받는 것은 공평하다. 먼저 보내진 것을 먼저 받게 된다. 
~~~
data class Ball(var hits: Int)

fun main() = runBlocking {
    val table = Channel<Ball>() // a shared table
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) // serve the ball
    delay(1000) // delay 1 second
    coroutineContext.cancelChildren() // game over, cancel them
}

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { // receive the ball in a loop
        ball.hits++
        println("$name $ball")
        delay(300) // wait a bit
        table.send(ball) // send the ball back
    }
}
~~~


## Ticker channels
Unit을 return하는 특별한 channel. 쓸모없어보이지만 time-based producer에게는 유의미하다. 
~~~
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking<Unit> {
    val tickerChannel = ticker(delayMillis = 100, initialDelayMillis = 0) // create ticker channel
    var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Initial element is available immediately: $nextElement") // no initial delay

    nextElement = withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
    println("Next element is not ready in 50 ms: $nextElement")

    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 100 ms: $nextElement")

    // Emulate large consumption delays
    println("Consumer pauses for 150ms")
    delay(150)
    // Next element is available immediately
    nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Next element is available immediately after large consumer delay: $nextElement")
    // Note that the pause between `receive` calls is taken into account and next element arrives faster
    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

    tickerChannel.cancel() // indicate that no more elements are needed
}
~~~
~~~
Initial element is available immediately: kotlin.Unit
Next element is not ready in 50 ms: null
Next element is ready in 100 ms: kotlin.Unit
Consumer pauses for 150ms
Next element is available immediately after large consumer delay: kotlin.Unit
Next element is ready in 50ms after consumer pause in 150ms: kotlin.Unit
~~~