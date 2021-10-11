# 06. 채널 - 통신을 통한 메모리 공유

# 채널의 이해

- 실행 중인 스레드에 상관없이 서로 다른 코루틴 간에 메시지를 안전하게 보내고 받기 위한 파이프라인

# 채널 유형과 배압

- send() : 데이터를 보내는 용도로 사용되며, 일시 중단 함수
- receive() : 보낸 데이터를 받는 용도로 사용 됨

## 언버퍼드 채널

- 버퍼가 없는 채널을 언버퍼드 채널
- 언버퍼드 채널의 유일한 구현은 RendezvousChannel 뿐, 책과 다르게 `RendezvousChannel`를 바로 만들순 없다
- capacity의 종류는 `RENDEZVOUS`, `CONFLATED`, `UNLIMITED`, `BUFFERED`, custom으로 구성

```kotlin
public fun <E> Channel(
    capacity: Int = RENDEZVOUS,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    onUndeliveredElement: ((E) -> Unit)? = null
): Channel<E> =
    when (capacity) {
        RENDEZVOUS -> {
            if (onBufferOverflow == BufferOverflow.SUSPEND)
                RendezvousChannel(onUndeliveredElement) // an efficient implementation of rendezvous channel
            else
                ArrayChannel(1, onBufferOverflow, onUndeliveredElement) // support buffer overflow with buffered channel
        }
        CONFLATED -> {
            require(onBufferOverflow == BufferOverflow.SUSPEND) {
                "CONFLATED capacity cannot be used with non-default onBufferOverflow"
            }
            ConflatedChannel(onUndeliveredElement)
        }
        UNLIMITED -> LinkedListChannel(onUndeliveredElement) // ignores onBufferOverflow: it has buffer, but it never overflows
        BUFFERED -> ArrayChannel( // uses default capacity with SUSPEND
            if (onBufferOverflow == BufferOverflow.SUSPEND) CHANNEL_DEFAULT_CAPACITY else 1,
            onBufferOverflow, onUndeliveredElement
        )
        else -> {
            if (capacity == 1 && onBufferOverflow == BufferOverflow.DROP_OLDEST)
                ConflatedChannel(onUndeliveredElement) // conflated implementation is more efficient but appears to work in the same way
            else
                ArrayChannel(capacity, onBufferOverflow, onUndeliveredElement)
        }
    }
```

```kotlin
fun main() {
    runBlocking {
        val time = measureTimeMillis {
            val channel = Channel<Int>()
            val sender = launch {
                repeat(10) {
                    channel.send(it)
                    log("Sent $it")
                }
            }
            channel.receive()
            channel.receive()
        }
        log("Took ${time}ms")
    }
}

/*
09:24:08.212:Thread[main,5,main]: Sent 0
09:24:08.242:Thread[main,5,main]: Took 66ms
09:24:08.243:Thread[main,5,main]: Sent 1
 */
```

- 프로그램이 종료되지 않고 기다림

```kotlin
fun main() = runBlocking(Dispatchers.IO) {
    val time = measureTimeMillis {
        val channel = Channel<Int>()
        val sender = launch {
            repeat(10) {
                log("Sent $it")
                channel.send(it)
            }
        }
        repeat(10) {
            val data = channel.receive()
            log("received $data")
        }
    }
    log("Took ${time}ms")
}
/*
09:36:59.958:Thread[DefaultDispatcher-worker-3,5,main]: Sent 0
09:36:59.978:Thread[DefaultDispatcher-worker-3,5,main]: Sent 1
09:36:59.979:Thread[DefaultDispatcher-worker-1,5,main]: received 0
09:36:59.979:Thread[DefaultDispatcher-worker-1,5,main]: received 1
09:36:59.979:Thread[DefaultDispatcher-worker-1,5,main]: Sent 2
09:36:59.979:Thread[DefaultDispatcher-worker-1,5,main]: Sent 3
09:36:59.979:Thread[DefaultDispatcher-worker-1,5,main]: received 2
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: received 3
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: Sent 4
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: Sent 5
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: received 4
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: received 5
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: Sent 6
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: Sent 7
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: received 6
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: received 7
09:36:59.980:Thread[DefaultDispatcher-worker-1,5,main]: Sent 8
09:36:59.981:Thread[DefaultDispatcher-worker-1,5,main]: Sent 9
09:36:59.981:Thread[DefaultDispatcher-worker-1,5,main]: received 8
09:36:59.981:Thread[DefaultDispatcher-worker-1,5,main]: received 9
09:36:59.986:Thread[DefaultDispatcher-worker-1,5,main]: Took 58ms
 */
```

- 버퍼에 있는 데이터가 다 사라질때까지 종료되지 않음
- 추가적으로 send보다 더 많은 receive를 하려고 해도 종료되지 않음

## 버퍼드 채널

- 버퍼를 가지는 채널
- 채널 내 요소의 수가 버퍼의 크기와 같을 때마다 송신자의 실행이 중지

### LinkedListChannel

- 중단 없이 무한의 요소를 전송할 수 있는 채널
- LinkedListChannel은 메모리를 너무 많이 소모할 수 있기 때문에 사용시 주의 필요
- 해당 채널보다는 버퍼 크기를 갖는 버퍼드 채널을 사용하는 것을 권장

```kotlin
fun main() = runBlocking(Dispatchers.IO) {
    val time = measureTimeMillis {
        // public const val UNLIMITED: Int = Int.MAX_VALUE
        val channel = Channel<Int>(Channel.UNLIMITED)
        val sender = launch {
            repeat(5) {
                log("Sent $it")
                channel.send(it)
            }
        }
        delay(500)
    }
    log("Took ${time}ms")
}

/*
09:43:25.882:Thread[DefaultDispatcher-worker-3,5,main]: Sent 0
09:43:25.902:Thread[DefaultDispatcher-worker-3,5,main]: Sent 1
09:43:25.902:Thread[DefaultDispatcher-worker-3,5,main]: Sent 2
09:43:25.902:Thread[DefaultDispatcher-worker-3,5,main]: Sent 3
09:43:25.902:Thread[DefaultDispatcher-worker-3,5,main]: Sent 4
09:43:26.372:Thread[DefaultDispatcher-worker-3,5,main]: Took 518ms
 */
```

### ArrayChannel

- 버퍼 크기를 0부터 최대 `int.MAX_VALUE - 1`까지 가질 수 있고, 버퍼 크기에 이르면 송신자를 일시 중단
- channel에 `take()`는 현재 존재하지 않음
- `capacity`로 전달된 값이 0~4로 5개의 공간, 즉 `capacity + 1`의 공간을 갖고 있음

```kotlin
fun main() = runBlocking(Dispatchers.IO) {
    val time = measureTimeMillis {
        val channel = Channel<Int>(4)
        val sender = launch {
            repeat(10) {
                log("Sent $it")
                channel.send(it)
            }
        }
        delay(500)
        log("Taking two")
        repeat(2) {
            val data = channel.receive()
            log("received $data")
        }
        delay(500)
    }
    log("Took ${time}ms")
}
/*
09:53:10.580:Thread[DefaultDispatcher-worker-3,5,main]: Sent 0
09:53:10.598:Thread[DefaultDispatcher-worker-3,5,main]: Sent 1
09:53:10.599:Thread[DefaultDispatcher-worker-3,5,main]: Sent 2
09:53:10.599:Thread[DefaultDispatcher-worker-3,5,main]: Sent 3
09:53:10.599:Thread[DefaultDispatcher-worker-3,5,main]: Sent 4
09:53:11.059:Thread[DefaultDispatcher-worker-3,5,main]: Taking two
09:53:11.060:Thread[DefaultDispatcher-worker-1,5,main]: Sent 5
09:53:11.060:Thread[DefaultDispatcher-worker-3,5,main]: received 0
09:53:11.060:Thread[DefaultDispatcher-worker-3,5,main]: received 1
09:53:11.060:Thread[DefaultDispatcher-worker-3,5,main]: Sent 6
09:53:11.571:Thread[DefaultDispatcher-worker-3,5,main]: Took 1029ms
 */
```

- 버퍼에 있는 데이터가 다 사라질때까지 종료되지 않음

### ConflatedChannel

- 유실이 될 수 있는 버퍼, 송신자가 일시중지 되지 않음
- 버퍼를 갖고 있지만, 새로운 요소가 보내질 때 버퍼가 꽉 차있다면 가장 마지막에 등록되어 있는 데이터를 삭제한다

```kotlin
fun main() = runBlocking(Dispatchers.IO) {
    val time = measureTimeMillis {
        // public const val CONFLATED: Int = -1
        val channel = Channel<Int>(Channel.CONFLATED)
        val sender = launch {
            repeat(5) {
                log("Sent $it")
                channel.send(it)
            }
        }
        delay(500)
        repeat(1) {
            val data = channel.receive()
            log("received $data")
        }
    }
    log("Took ${time}ms")
}
/*
10:53:37.406:Thread[DefaultDispatcher-worker-3,5,main]: Sent 0
10:53:37.430:Thread[DefaultDispatcher-worker-3,5,main]: Sent 1
10:53:37.430:Thread[DefaultDispatcher-worker-3,5,main]: Sent 2
10:53:37.430:Thread[DefaultDispatcher-worker-3,5,main]: Sent 3
10:53:37.430:Thread[DefaultDispatcher-worker-3,5,main]: Sent 4
10:53:37.887:Thread[DefaultDispatcher-worker-3,5,main]: received 4
10:53:37.894:Thread[DefaultDispatcher-worker-3,5,main]: Took 521ms
 */
```

# 채널과 상호작용

- `Channel<T>`의 동작은 `SendChannel<T>`와 `ReceiveChannel<T>`의 두 개의 인터페이스로 이뤄져 있음

## SendChannel

- 채널을 통해 요소를 본개ㅣ 위한 몇 개의 함수와 무언가를 보낼 수 있는지 검증하기 위한 다른 함수들을 정의

### 보내기 전 검증

- `isClosedForSend` : 채널이 닫혀 있는지 확인하는 필드

```kotlin
fun main() {
    val channel = Channel<Int>()
    log("${channel.isClosedForSend}") // false
    channel.close()
    log("${channel.isClosedForSend}") // true
}
```

- `isFull` : 없음
- `isEmpty`: 해당 채널에 데이터가 있는지 확인하는 필드

```kotlin
private suspend fun isEmpty(method: String) {
    val channel = Channel<Int>(1)
    log("$method : ${channel.isEmpty}") // true
    channel.send(1)
    log("$method : ${channel.isEmpty}") // false
}
```

### 요소 전송

- 채널을 통해 데이터를 전송하려면 send() 함수롤 사용해야 한다.
- 버퍼드 채널에서는 버퍼가 가득차면 송신자를 일시 중단하며, RendezvousChannel 이면 receive가 호출될 때까지 일시 중단한다

```kotlin
private suspend fun closedChannelException(method: String) {
    val channel = Channel<Int>(1)
    channel.close()
    channel.offer(10) // channel.trySend(10)
}
```

- `offer()`는 deperecated 되었고, 에러가 발생하지만 `trySend()`를 사용하면 에러가 발생하지 않는다.

```kotlin
val channel = Channel<Int>(1)
log("$method : ${channel.trySend(2)}")
log("$method : ${channel.receive()}")
```

## ReceiveChannel

### isClosedForReceive

```kotlin
fun main() = runBlocking {
    isClosedForReceive("isClosedForSend")
    closedReceiveChannelException("closedReceiveChannelException")
}

private fun isClosedForReceive(method: String) {
    val channel = Channel<Int>()
    log("$method : ${channel.isClosedForReceive}") // false
    channel.close()
    log("$method : ${channel.isClosedForReceive}") // true
}

private suspend fun closedReceiveChannelException(method: String) {
    val channel = Channel<Int>(1)
    channel.close()
    channel.receive()
}

/*
11:25:26.939:Thread[main,5,main]: isClosedForSend : false
11:25:26.957:Thread[main,5,main]: isClosedForSend : true
Exception in thread "main" kotlinx.coroutines.channels.ClosedReceiveChannelException: Channel was closed
 */
```

## 과제 설명

- capacity 50이면서, capacity가 가득찬 경우 가장 최근에 들어온 데이터를 유실하는 channel 만들어보기 가

# 07. 스레드 한정, 액터 그리고 뮤텍스

## 원자성 위반

- 동시성 애플리케이션에서 서로 다른 스레드가 공유하는 데이터를 변경할 때 변경사항이 정상적으로 적용되지 않는 현상

```kotlin
fun main() = runBlocking {
    val workerA = asyncIncrement(2000)
    val workerB = asyncIncrement(2000)
    workerA.await()
    workerB.await()

    log("counter [$counter]")
}

var counter = 0

fun asyncIncrement(number: Int) =
    GlobalScope.async(Dispatchers.IO) {
        for (i in 0 until number) {
            counter++
        }
    }

/*
21:25:59.209:Thread[main,5,main]: counter [2590]
 */
```

# 해결 방법

## 코루틴을 단일 스레드로 한정

- singleThread에서 동작하게 하면 동시성은 떨어지지만 원자성을 보장할 수 있음

```kotlin
val context = newSingleThreadContext("counter")
fun incrementAsyncBySingleThread(number: Int) =
    GlobalScope.async(context) {
        for (i in 0 until number) {
            counter++
        }
    }
/*
21:29:02.380:Thread[main,5,main]: counter [4000]
 */
```

## 액터

```kotlin
val c = actor {
    // initialize actor's state
    for (msg in channel) {
        // process message here
    }
}
// send messages to the actor
c.send(...)
...
// stop the actor when it is no longer needed
c.close()
```

- actor를 만들고 channel로 부터 msg를 받을 수 있게 셋팅
- actor에 send()를 이용해 데이터를 보내면 msg에 데이터가 들어오게 되고 해당 msg를 기반으로 데이터를 처리
- actor를 멈춰야 하는 경우 close를 호출해서 멈춘다
- channel에 SendChannel, ReceiveChannel을 활용하는 케이스인데, actor에 capacity queue가 있어 원자성을 보장 해준다.

```kotlin
private var counterByActor = 0
private val contextActor = newSingleThreadContext("counterActor")

val actorCounter = GlobalScope.actor<Void?>(contextActor) {
    for (msg in channel) {
        log("counter 증가 ${counterByActor++}")
    }
}

fun main() = runBlocking {
    val workerA = incrementAsyncByActor(2000, "workerA")
    val workerB = incrementAsyncByActor(2000, "workerB")
    workerA.await()
    workerB.await()

    log("counter [$counterByActor]")
}

fun incrementAsyncByActor(number: Int, worker: String) =
    GlobalScope.async(Dispatchers.IO) {
        for (i in 0 until number) {
            actorCounter.send(null)
            log("$worker send $i")
        }
    }
```

## 액터를 사용한 기능 확장

```kotlin
enum class Action {
    INCREASE,
    DECREASE
}

val actorCounter = GlobalScope.actor<Action>(contextActor) {
    for (msg in channel) {
        when (msg) {
            Action.INCREASE -> counterByActor++
            Action.DECREASE -> counterByActor--
        }
    }
}

fun incrementAsyncByActor(number: Int, worker: String) =
    GlobalScope.async(Dispatchers.IO) {
        for (i in 0 until number) {
            actorCounter.send(Action.INCREASE)
            log("$worker send $i")
        }
    }
```

- send시에 `Action`을 전달하여 msg에서 해당 값을 보고 분기 처리가 가능

## 액터 상호 작용에 대한 추가 정보

### 버퍼드 액터

```kotlin
fun main() {
    runBlocking {
        val bufferedPrinter = actor<String>(capacity = 10) {
            for (msg in channel) {
                log(msg)
            }
        }

        bufferedPrinter.send("hello")
        bufferedPrinter.send("world")
        bufferedPrinter.close()
    }
}
```

- capacity의 값을 설정해서 Channel을 ArrayChannel로 생성한다.
- 버퍼가 가득찬 경우에 send쪽을 block 한다

### CoroutineContext를 갖는 액터

```kotlin
fun main() {
    runBlocking {
        val bufferedPrinter = actor<String>(newFixedThreadPoolContext(3, "pool")) {
            for (msg in channel) {
                log(msg)
            }
        }

        bufferedPrinter.send("hello")
        bufferedPrinter.send("world")
        bufferedPrinter.close()
    }
}
/*
23:37:11.479:Thread[pool-1,5,main]: hello
23:37:11.508:Thread[pool-1,5,main]: world
*/
```

- dispatcher에 쓰레드 풀을 전달하여 특정 쓰레드 풀에서 처리되게 할 수 있음

### CorouitineStart

```kotlin
DEFAULT-- immediately schedules coroutine for execution according to its context;
LAZY-- starts coroutine lazily, only when it is needed;
ATOMIC-- atomically ( in a non -cancellable way) schedules coroutine for execution according to its context;
UNDISPATCHED-- immediately executes coroutine until its first suspension point in the current thread.
```

```kotlin
fun main() {
    runBlocking {
        createActor(CoroutineStart.LAZY)
        createActor(CoroutineStart.DEFAULT)
        createActor(CoroutineStart.ATOMIC)
        createActor(CoroutineStart.UNDISPATCHED)
    }
}

private suspend fun CoroutineScope.createActor(start: CoroutineStart) {
    val defaultActor = actor<String>(start = start) {
        for (msg in channel) {
            log(msg)
        }
    }
    log("started ${defaultActor.toString()}")
    log(defaultActor.toString())
    defaultActor.send("hello")
    log(defaultActor.toString())
    defaultActor.send("world")
    defaultActor.close()
    log("closed ${defaultActor.toString()}")
}
/*
23:47:36.781:Thread[main,5,main]: started LazyActorCoroutine{New}@3e57cd70
23:47:36.799:Thread[main,5,main]: LazyActorCoroutine{New}@3e57cd70
23:47:36.804:Thread[main,5,main]: hello
23:47:36.805:Thread[main,5,main]: LazyActorCoroutine{Active}@3e57cd70
23:47:36.806:Thread[main,5,main]: closed LazyActorCoroutine{Active}@3e57cd70
23:47:36.807:Thread[main,5,main]: started ActorCoroutine{Active}@1d7acb34
23:47:36.807:Thread[main,5,main]: ActorCoroutine{Active}@1d7acb34
23:47:36.807:Thread[main,5,main]: world
23:47:36.807:Thread[main,5,main]: hello
23:47:36.808:Thread[main,5,main]: ActorCoroutine{Active}@1d7acb34
23:47:36.808:Thread[main,5,main]: closed ActorCoroutine{Active}@1d7acb34

 */
```

- lazy의 경우 만들때 상태값은 New 이며, send 후 Active 상태로 변경된다
- 그 외에는 만들때부터 Active 상태이다.

# 상호배제

- Actor 방식은 단일 스레드에서 처리가 될 수 있게하여 원자성 위반을 회피
- 한 번에 하나의 코루틴만 코드 블록을 실행할 수 있도록 하는 동기화 메커니즘도 제공
- 코틀린 뮤텍스(mutex)의 가장 중요한 특징은 블록되지 않는다는 점
- 실행 대기 중인 코루틴은 잠금을 획득하고 코드 블록을 실행할 수 있을 때까지 일시 중단된다.
- 코루틴은 일시 중단되지만 일시 중단 함수를 사용하지 않고 뮤텍스를 잠글 수 있다.
- 뮤텍스를 자바에 비유하면 넌 블로킹, synchronized

## 뮤텍스 생성

```kotlin
fun main() {
    var mutex = Mutex()

    runBlocking {
        fun asyncIncrement(by: Int) = async {
            for (i in 0 until by) {
                mutex.withLock {
                    counter++
                }
            }
        }
        asyncIncrement(2000).await()
        asyncIncrement(2000).await()

        log("counter $counter")
    }
}
/*
23:59:49.585:Thread[DefaultDispatcher-worker-3,5,main]: thread
23:59:49.585:Thread[DefaultDispatcher-worker-1,5,main]: thread
23:59:49.617:Thread[DefaultDispatcher-worker-3,5,main]: before
23:59:49.617:Thread[DefaultDispatcher-worker-1,5,main]: before
23:59:49.617:Thread[DefaultDispatcher-worker-3,5,main]: counter
23:59:49.623:Thread[DefaultDispatcher-worker-3,5,main]: after
23:59:49.623:Thread[DefaultDispatcher-worker-3,5,main]: before
23:59:49.624:Thread[DefaultDispatcher-worker-1,5,main]: counter
23:59:49.624:Thread[DefaultDispatcher-worker-1,5,main]: after
23:59:49.624:Thread[DefaultDispatcher-worker-1,5,main]: before
 */
```

- 한 번에 하나의 코루틴만 잠금을 보유하고, 잠금을 시도하는 다른 코루틴을 일시 중단 함으로써 카운터에 대한 모든 증분이 동기화 됨

## 상호 배제와 상호 작용

- lock() : 잠금을 할 때 사용
- unlock() : 잠금을 해제할 때 사용
- withLock() : lock, action, unlock으로 구성

```kotlin
@OptIn(ExperimentalContracts::class)
public suspend inline fun <T> Mutex.withLock(owner: Any? = null, action: () -> T): T {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }

    lock(owner)
    try {
        return action()
    } finally {
        unlock(owner)
    }
}
```

- tryLock() : lock 획득이 가능한지를 확인

# 휘발성 변수

- thread-safe 문제를 해결하지 못하지만, 스레드 간에 정보를 공유해야 할 때 간단한 솔루션으로 사용
- `@Volatile` : JVM환경에서만 사용가능, 해당 변수의 경우 변경 즉시 다른 스레드가 값을 확인할 수 있음
- thread-safe를 해결하지 못하는 이유
- 값이 바뀐것을 모든 스레드가 알지라도, 스레드들이 변경하는 시점이 같다면, 변경된 부분은 한개만 적용되니 유실 발생

### Bad case

```kotlin
class Volatile {
    @Volatile
    private var type = 0
    private var title = ""

    fun setTitle(newTitle: String) {
        when (type) {
            0 -> title = newTitle
            else -> throw Exception("invalid state")
        }
    }
}
```

- `type`을 보고 `title` 값을 수정하고 있지만, title을 바꾸려는 순간과 type을 바꾸려는 순간이 겹치면 문제가 발생

### Good case

```kotlin
class DataProcessor {
    @Volatile
    private var shutdownRequested = false

    fun shutdown() {
        shutdownRequested = true
    }

    fun process() {
        while (shutdownRequested) {
            // process away
        }
    }
}
```

- `shutdownRequested` 를 바꿨을 때 다른 부분에 영향이 가지 않고, 모든 스레드들이 같은 동작을 할 수 있으므로 문제가 되지 않음

# 원자적 데이터 구조

- AtomicXXX 를 사용해서 원자성을 확보 하는 방법

```kotlin
runBlocking {
    fun asyncIncrement(by: Int) = async {
        for (i in 0 until by) {
            counter.incrementAndGet()
        }
    }

    val workerA = asyncIncrement(2000)
    val workerB = asyncIncrement(2000)
    workerA.await()
    workerB.await()

    log("counter: $counter")
}
```
