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
