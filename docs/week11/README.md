# Composing suspending functions

## Sequential by default

```kotlin
suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

fun main() = runBlocking {
    val time = measureTimeMillis {
        log("one before")
        val one = doSomethingUsefulOne()
        log("one complete")
        val two = doSomethingUsefulTwo()
        log("two complete")
        log("The answer is ${one + two}")
    }
    log("Completed in $time ms")
}
/*
The answer is 42
Completed in 2017 ms
 */
```
- coroutine에서 runBlocking은 순차적으로 동작하므로 suspend를 하더라도 병렬로 동작되지 않는다.

## Concurrent using async
```kotlin
val time = measureTimeMillis {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    println("The answer is ${one.await() + two.await()}")
}
println("Completed in $time ms")
```
- async: `Deferred`를 return하고, 결과 값 non-blocking 방식을 이용해 가져온다.
- launch: `Job`을 return 하고, 결과 값을 가져오지 않는다.

## Lazily started async
```kotlin
fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) {
            doSomethingUsefulOne()
        }
        val two = async(start = CoroutineStart.LAZY) {
            doSomethingUsefulTwo()
        }
//        one.start()
//        two.start()
        log("The answer is ${one.await() + two.await()}")
    }
    log("Completed in $time ms")
}
/*
===== start 없이 await만 사용
13:26:28.791:Thread[main,5,main]: The answer is 42
13:26:28.816:Thread[main,5,main]: Completed in 2071 ms
===== start 사용
13:27:34.291:Thread[main,5,main]: The answer is 42
13:27:34.318:Thread[main,5,main]: Completed in 1064 ms
 */
```
- `LAZY`는 프로그래머에 의해서 순서를 조절할 수 있음
- `LAZY`로 사용하고, `await` 전에 `start` 없이 사용하게 되면 해당 coroutine은 순차적으로 동작하게 된다.

## Async-style functions

```kotlin
// The result type of somethingUsefulOneAsync is Deferred<Int>
@OptIn(DelicateCoroutinesApi::class)
fun somethingUsefulOneAsync() = GlobalScope.async {
        log("somethingUsefulOneAsync")
        doSomethingUsefulOne()
    }

// The result type of somethingUsefulTwoAsync is Deferred<Int>
@OptIn(DelicateCoroutinesApi::class)
fun somethingUsefulTwoAsync() = GlobalScope.async {
    log("somethingUsefulTwoAsync")
    doSomethingUsefulTwo()
}

// note that we don't have `runBlocking` to the right of `main` in this example
fun main() {
    log("main")
    val time = measureTimeMillis {
        log("measureTimeMillis")
        // we can initiate async actions outside of a coroutine
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        runBlocking {
            log("The answer is ${one.await() + two.await()}")
        }
    }
    log("Completed in $time ms")
}

/*
13:45:19.935:Thread[main,5,main]: main
13:45:19.963:Thread[main,5,main]: measureTimeMillis
13:45:20.010:Thread[DefaultDispatcher-worker-2,5,main]: somethingUsefulTwoAsync
13:45:20.010:Thread[DefaultDispatcher-worker-1,5,main]: somethingUsefulOneAsync
13:45:21.029:Thread[main,5,main]: The answer is 42
13:45:21.035:Thread[main,5,main]: Completed in 1066 ms
 */
```
- `GlobalScope`를 이용하게 되면 해당 corouine이 async thread에서 실행되게 된다.
- 해당 async coroutine의 결과를 알고 싶다면, suspend block이나, runBlockin block안에서 확인 해야 한다.
- 결과를 알 필요가 없는 경우에는, 굳이 block을 할 필요 없이 놔두면 비동기 쓰레드에서 실행이 종료된다.

## Structured concurrency with async
```kotlin
fun main() = runBlocking<Unit> {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}
```
- `coroutineScope`를 통해서도 async builder를 사용 가능
- scope로 범위를 나눴을 때 해당 scope에서 에러가 발생하면, 범위 내에 있는 coroutine은 전부 cancel이 된다

```kotlin
fun main() = runBlocking<Unit> {
    try {
        failedConcurrentSum()
    } catch(e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async<Int> { 
        try {
            delay(Long.MAX_VALUE) // Emulates very long computation
            42
        } finally {
            println("First child was cancelled")
        }
    }
    val two = async<Int> { 
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}
/*
Second child throws an exception
First child was cancelled
Computation failed with ArithmeticException
 */
```
- delay 처럼 suspend 상태의 coroutine만 멈출 뿐 sleep 상태의 coroutine은 cancel 되는데 시간이 걸린다.

# Coroutine context and dispatchers

## Dispatchers and threads
- coroutine context는 어떤 쓰레드에서 실행시킬지를 결정하는 `coroutine dispatcher`를 포함하고 있다.
- coroutine dispatcher는 특정 쓰레드, 쓰레드 풀에서 실행될지를 결정한다.

```kotlin
fun main() = runBlocking<Unit> {
    launch { // context of the parent, main runBlocking coroutine
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher 
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}
```
- launch, async 뒤에 Dispatchers를 전달해서 CoroutineDispatcher를 결정할 수 있다.
- launch 뒤에 아무 것도 안줄 경우 상위 scope에 CoroutineDispatcher를 상속한다.
- Dispatchers.Default는 범위가 명시적으로 지정되지 않았을 경우 사용되며, 해당 쓰레드 풀은 공유된다.
- newSingleThreadContext, newFixedThreadPoolContext의 경우 비싼 리소스를 사용하는 전용 스레드 방식
- 해당 방식의 경우 더 이상 사용하지 않는 경우 close를 반드시 해줘야하며, 여러번 만들지 말고 전체 응용프로그램에서 재사용할 수 있게 개발해야 한다.

## Unconfined vs confined dispatcher
- unconfied : 실행하는 쓰레드를 사용하지만 suspend 후 resume 될 때는 resume 시킨 쓰레드의 scope로 실행된다.
- CPU를 사용하거나, 특정 쓰레드에 공유된 자원을 사용하지 않는 경우에 사용하기 적합하다.

```kotlin
launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
    println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
    delay(500)
    println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
}
launch { // context of the parent, main runBlocking coroutine
    println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
    delay(1000)
    println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
}
/*
Unconfined      : I'm working in thread main @coroutine#2
main runBlocking: I'm working in thread main @coroutine#3
Unconfined      : After delay in thread kotlinx.coroutines.DefaultExecutor @coroutine#2
main runBlocking: After delay in thread main @coroutine#3
 */
```

## Debugging coroutines and threads
- intellij에서 coroutine에 대해서 디버깅 툴을 제공
- JVM option에 `-Dkotlinx.coroutines.debug` 해당 옵션을 추가하면 현재 실행 중인 Thread에 coroutine 번호가 로그에 같이 나옴

```kotlin
fun main() = runBlocking<Unit> {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")
}

/*
18:35:03.710:Thread[main @coroutine#2,5,main]: I'm computing a piece of the answer
18:35:03.742:Thread[main @coroutine#3,5,main]: I'm computing another piece of the answer
18:35:03.747:Thread[main @coroutine#1,5,main]: The answer is 42
 */
```

```kotlin
fun main() {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}

```
- kotlin에서 closable을 사용하기 위해서는 `use`를 사용해서 finally쪽에 close가 될 수 있도록 작성

## Naming coroutines for debugging
```kotlin
fun main() = runBlocking(CoroutineName("main")) {
    log("Started main coroutine")
    // run two background value computations
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
}
/*
20:57:34.574:Thread[main @main#1,5,main]: Started main coroutine
20:57:35.115:Thread[main @v1coroutine#2,5,main]: Computing v1
20:57:35.612:Thread[main @v2coroutine#3,5,main]: Computing v2
20:57:35.619:Thread[main @main#1,5,main]: The answer for v1 / v2 = 42
 */
```
- coroutine에 name을 설정해서 디버깅에 유용하게 할 수 있음
- 단 `-Dkotlinx.coroutines.debug` 해당 옵션을 줘야 코루틴의 name이 콘솔에 나오게 된다.
- 기존 CoroutineContext에 plus 오버로딩을 통해서 CoroutineName을 전달해서 수정 가능

## Job in the context
```kotlin
println("My job is ${coroutineContext[Job]}")
/*
My job is "coroutine#1":BlockingCoroutine{Active}@32a1bec0
 */
```
- CoroutineContext에 Job을 넘기게 되면, 해당 Job의 상태와, 실행 중인 코루을 알 수 있음
- 해당 Job의 상태를 알기 위해서는 isActive를 통해서 확인을 해야 하는데, `coroutineContext[Job]?.isActive == true.` 해당 방식으로도 확인 가능
- `isCompleted`, `isCancelled`, `isActive`에 대해서도 확인 가능
- `coroutineContext.job.isActive` 해당 방식을 이용하면 null 검사를 생략할 수 있음

## Children of a coroutine

```kotlin
fun main() = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        // it spawns two other jobs
        launch(Job()) {
            log("job1: I run in my own Job and execute independently!")
            delay(1000)
            log("job1: I am not affected by cancellation of the request")
        }

        GlobalScope.launch() {
            log("job async: I run in my own Job and execute independently!")
            delay(1000)
            log("job async: I am not affected by cancellation of the request")
        }
        // and the other inherits the parent context
        launch {
            delay(100)
            log("job2: I am a child of the request coroutine")
            delay(1000)
            log("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel() // cancel processing of the request
    delay(1000) // delay a second to see what happens
    log("main: Who has survived request cancellation?")
}

/*
19:04:51.987:Thread[main,5,main]: job1: I run in my own Job and execute independently!
19:04:51.987:Thread[DefaultDispatcher-worker-1,5,main]: job1: I run in my own Job and execute independently!
19:04:52.121:Thread[main,5,main]: job2: I am a child of the request coroutine
19:04:53.023:Thread[main,5,main]: job1: I am not affected by cancellation of the request
19:04:53.024:Thread[DefaultDispatcher-worker-1,5,main]: job1: I am not affected by cancellation of the request
19:04:53.460:Thread[main,5,main]: main: Who has survived request cancellation?
 */
```
- 코루틴은 부모, 자식 관계를 가질 수 있는데, 부모의 쓰레드가 취소된 경우 자식의 쓰레드도 취소 된다.
- GlobalScope이거나, 다른 Scope인 경우는 부모, 자식 관계가 아니라 영향을 미치지 않음
- 상위에 Job을 하위 Job에게 CoroutineContext로 전달하게 되면, 부모, 자식의 관계를 끊을 수 있다.

## Parental responsibilities

```kotlin
fun main() = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        repeat(3) { i -> // launch a few children jobs
            launch  {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                log("Coroutine $i is done")
            }
        }
        log("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join() // wait for completion of the request, including all its children
    log("Now processing of the request is complete")
}

/*
20:54:33.243:Thread[main,5,main]: request: I'm done and I don't explicitly join my children that are still active
20:54:33.482:Thread[main,5,main]: Coroutine 0 is done
20:54:33.671:Thread[main,5,main]: Coroutine 1 is done
20:54:33.873:Thread[main,5,main]: Coroutine 2 is done
20:54:33.874:Thread[main,5,main]: Now processing of the request is complete
 */
```
- 코루틴은 자식 코루틴이 종료될 때 까지 부모 코루틴이 종료되지 않는다.

## Thread-local data

```kotlin
fun main() = runBlocking<Unit> {
    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}
```
- ThreadLocal에 있는 데이터를 coroutineScope에서 사용할 수 있게 `ThreadLocal.asContextElement`를 지원한다.
- 해당 값은 coroutineScope내에서 contextSwitch 시에만 저장되며, 해당 scope가 끝나면 다시 기존에 ThreadLocal 값으로 사용 된다.

```kotlin
internal actual inline fun <T> withContinuationContext(continuation: Continuation<*>, countOrElement: Any?, block: () -> T): T {
    val context = continuation.context
    val oldValue = updateThreadContext(context, countOrElement)
    val undispatchedCompletion = if (oldValue !== NO_THREAD_ELEMENTS) {
        // Only if some values were replaced we'll go to the slow path of figuring out where/how to restore them
        continuation.updateUndispatchedCompletion(context, oldValue)
    } else {
        null // fast path -- don't even try to find undispatchedCompletion as there's nothing to restore in the context
    }
    try {
        return block()
    } finally {
        if (undispatchedCompletion == null || undispatchedCompletion.clearThreadContext()) {
            restoreThreadContext(context, oldValue)
        }
    }
}
```

```kotlin
internal class ThreadLocalElement<T>(
    private val value: T,
    private val threadLocal: ThreadLocal<T>
) : ThreadContextElement<T> {
    override val key: CoroutineContext.Key<*> = ThreadLocalKey(threadLocal)

    override fun updateThreadContext(context: CoroutineContext): T {
        val oldState = threadLocal.get()
        threadLocal.set(value)
        return oldState
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: T) {
        threadLocal.set(oldState)
    }
    ...
}
```

- coroutine이 resume 됬을 때 updateThreadContext를 통해서 ThreadLocal로 부터 값을 셋팅
- 해당 block()이 종료된 이후에는 restoreThreadContext를 통해서 기존 oldValue를 ThreadLocal에 다시 셋팅

# 과제
- `ThreadLocal`에 값을 할당해서, coroutine 내부에서 값이 잘 나오는지, coroutine 종료 후 원래 값이 잘 나오는지 확인
- case1: runBlocking에 launch, async
- case2: runBlocking에 suspend를 사용한 withContext를 통해서 다른 CoroutineContext에서 잘 나오는지 확인
- case3: coroutineScope를 활용해서 같은 Dispatchers에서도 잘 나오는지 확인
