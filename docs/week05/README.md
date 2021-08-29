# 코루틴과 Async/Await

## 코루틴이란?

- 코틀린 1.3부터 코루틴이 표준 라이브러리에 정식 포함
- [https://speakerdeck.com/taehwandev/kotlin-coroutines-and-flow?slide=2](https://speakerdeck.com/taehwandev/kotlin-coroutines-and-flow?slide=2)
- [https://speakerdeck.com/taehwandev/kotlin-coroutines?slide=2](https://speakerdeck.com/taehwandev/kotlin-coroutines?slide=2)
- 비선점형 멀티태스킹을 수행하는 일반화한 서브루틴 (non-blocking, async 방식)
- ![coroutine](/docs/img/coroutine.png)

## 코틀린의 코루틴 지원: 일반적인 코루틴

```groovy
plugins {
    kotlin("jvm") version "1.5.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}
```

- gradle에서 coroutines 셋팅을 위해서 해당 설정을 추가해야 한다.

### 여러 가지 코루틴

- 코루틴 빌더 : 코루틴 빌더에 원하는 동작을 람다로 넘겨서 코루틴을 만들어 실행하는 방식으로 코루틴을 활용

### kotlinx.coroutines.CoroutineScope.launch

- launch : 코루틴을 Job으로 반환하며, 만들어진 코루틴을 즉시 실행하거나, Job을 cancel을 호출해 중단할 수 잇다.

```kotlin
fun now() = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.MILLIS)

fun log(msg: String) = println("${now()}:${Thread.currentThread()}: ${msg}")

fun launchInGlobalScope() {
    GlobalScope.launch {
        log("coroutine started.")
    }
}

fun main() {
    log("main() started")
    launchInGlobalScope()
    log("launchInGlobalScope() executed")
    Thread.sleep(5000L) // 슬립을 제거하면 코루틴이 실행되지 않고 종료됨
    log("main() terminated")
}

/*
17:15:59.370:Thread[main,5,main]: main() started
17:15:59.455:Thread[main,5,main]: launchInGlobalScope() executed
17:15:59.458:Thread[DefaultDispatcher-worker-1,5,main]: coroutine started.
17:16:04.459:Thread[main,5,main]: main() terminated
 */
```

- `GlobalScope`: `CorotuineScope`가 없는 상태라면 GlobalScope를 사용해서 코루틴 사용가능
- 하지만, main thread가 종료되면서 코루틴 쓰레드도 같이 종료되기에 정상적으로 동작하지 않는다.

```kotlin
fun runBlockingExample() {
    runBlocking {
        launch {
            log("GlobalScope.launch started.")
        }
    }
}
/*
17:22:52.941:Thread[main,5,main]: main() started
17:22:53.007:Thread[main,5,main]: GlobalScope.launch started.
17:22:53.007:Thread[main,5,main]: runBlockingExample() executed
17:22:53.007:Thread[main,5,main]: main() terminated
 */
```

- `runBlocking` : 해당 코루틴이 끝날때까지 기다려준다.
- main thread에서 실행되었다.

```kotlin
fun yieldExample() {
    runBlocking {
        launch {
            log("1")
            yield()
            log("3")
            yield()
            log("5")
        }
        log("after first launch")
        launch {
            log("2")
            delay(1000L)
            log("4")
            delay(1000L)
            log("6")
        }
        log("after second launch")
        launch {
            log("7")
            yield()
            log("8")
            yield()
            log("9")
        }
        log("after third launch")
    }
}

log("main() started")
yieldExample()
log("yieldExample() executed")
log("main() terminated")

/*
17:33:11.224:Thread[main,5,main]: main() started
17:33:11.284:Thread[main,5,main]: after first launch
17:33:11.287:Thread[main,5,main]: after second launch
17:33:11.287:Thread[main,5,main]: after third launch
17:33:11.288:Thread[main,5,main]: 1
17:33:11.289:Thread[main,5,main]: 2
17:33:11.293:Thread[main,5,main]: 7
17:33:11.293:Thread[main,5,main]: 3
17:33:11.293:Thread[main,5,main]: 8
17:33:11.293:Thread[main,5,main]: 5
17:33:11.293:Thread[main,5,main]: 9
17:33:12.294:Thread[main,5,main]: 4
17:33:13.297:Thread[main,5,main]: 6
17:33:13.298:Thread[main,5,main]: yieldExample() executed
17:33:13.298:Thread[main,5,main]: main() terminated
 */
```

- `launch`는 즉시 반환만하고 실행은 나중에 된다.
- `runBlocking`은 내부 코루틴이 모두 끝난 다음에 반환
- `yield()`: 해당 launch 동작을 멈추고 다른 launch가 실행될 수 있게 양보한다.
- `delay()`: 해당 시간이 지날 때까지 다른 코루틴에게 실행을 양보한다.

### kotlinx.coroutines.CoroutineScope.async

- `async`: `launch`와 같은 일으하지만, `launch`는 `Job`을 return하고 async는 `Deffered`를 return 한다.
- `Deffered`는 `Job`을 상속 받고 있어서 `launch` 대신 `async`를 사용해도 항상 아무 문제가 없다.

```kotlin
public fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T // Deferred는 값을 반환
): Deferred<T> {
    val newContext = newCoroutineContext(context)
    val coroutine = if (start.isLazy)
        LazyDeferredCoroutine(newContext, block) else  // 
        DeferredCoroutine<T>(newContext, active = true)//
    coroutine.start(start, coroutine, block)
    return coroutine
}

public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit // 값을 반환하지 않음
): Job {
    val newContext = newCoroutineContext(context)
    val coroutine = if (start.isLazy)
        LazyStandaloneCoroutine(newContext, block) else //
        StandaloneCoroutine(newContext, active = true)  //
    coroutine.start(start, coroutine, block)
    return coroutine
}
```

- `Deffered`는 await를 통해서 코루틴이 결과 값을 내놓을때까지 기다렸다가 결과값을 얻을 수 있음

```kotlin
fun sumAll() {
    runBlocking {
        val d1 = async {
            log("execute d1")
            delay(1000L)
            log("executed d1")
            1
        }
        log("after async(d1)")

        val d3 = async {
            log("execute d3")
            delay(1000L)
            log("executed d3")
            3
        }
        log("after async(d3)")

        val d2 = async {
            log("execute d2")
            delay(1000L)
            log("executed d2")
            2
        }
        log("after async(d2)")

        log("1+2+3 = ${d1.await() + d2.await() + d3.await()}")
        log("after wait all & add")
    }
}

/*
17:49:53.777:Thread[main,5,main]: main() started
17:49:53.840:Thread[main,5,main]: after async(d1)
17:49:53.845:Thread[main,5,main]: after async(d3)
17:49:53.845:Thread[main,5,main]: after async(d2)
17:49:53.850:Thread[main,5,main]: execute d1
17:49:53.851:Thread[main,5,main]: execute d3
17:49:53.851:Thread[main,5,main]: execute d2
17:49:54.852:Thread[main,5,main]: executed d1
17:49:54.854:Thread[main,5,main]: executed d3
17:49:54.854:Thread[main,5,main]: executed d2
17:49:54.860:Thread[main,5,main]: 1+2+3 = 6
17:49:54.860:Thread[main,5,main]: after wait all & add
17:49:54.860:Thread[main,5,main]: sumAll() executed
17:49:54.860:Thread[main,5,main]: main() terminated
 */

// await를 안 한 경우
/*
17:52:55.303:Thread[main,5,main]: main() started
17:52:55.369:Thread[main,5,main]: after async(d1)
17:52:55.372:Thread[main,5,main]: after async(d3)
17:52:55.372:Thread[main,5,main]: after async(d2)
17:52:55.372:Thread[main,5,main]: after wait all & add
17:52:55.373:Thread[main,5,main]: execute d1
17:52:55.378:Thread[main,5,main]: execute d3
17:52:55.378:Thread[main,5,main]: execute d2
17:52:56.379:Thread[main,5,main]: executed d1
17:52:56.380:Thread[main,5,main]: executed d3
17:52:56.380:Thread[main,5,main]: executed d2
17:52:56.381:Thread[main,5,main]: sumAll() executed
17:52:56.381:Thread[main,5,main]: main() terminated
 */
```

- `async` 부분은 바로 실행되지 않고 `await`를 호출하거나, 해당 block이 종료될 때 실행된다.
- 3초가 걸렸어야 하지만, 가장 오래걸리는 로직 시간만큼 block되고 완료되는 것을 알 수 있음
- 보통 멀티 쓰레드로 병렬 처리는 하는 방면 코루틴에서는 하나의 쓰레드만으로도 병렬 처리의 성능을 가져올 수 있음
- 실행하려는 작업이 시간이 얼마 걸리지 않거나 I/O에 의한 대기 시간이 크고, CPU 코어 수가 작아 동시에 실행할 수 있는 스레드 개수가 한정된 경우에는 특히 코루틴과 일반 스레드를 사용한 비동기 처리 사이에
  차이가 커진다.

## 코루틴 컨텍스트와 디스패처

```kotlin
public interface CoroutineScope {
    public val coroutineContext: CoroutineContext
}
```

- `launch`, `async` 등은 모두 `CoroutineScope`의 확장 함수
- `CoroutineContext`가 코루틴 실행 중인 여러 작업(Job)과 디스패처를 저장하는 일종의 맵

```kotlin
fun printContext() {
    runBlocking {
        launch { // 부모 컨텍스트를 사용
            log("main runBlocking: I'm working in thread")
        }
        log("after launch")
        launch(Dispatchers.Unconfined) { // 특정 스레드에 종속되지 않음 ? 
            log("Unconfined: I'm working in thread ")
        }
        log("after Unconfined")
        launch(Dispatchers.Default) { // 기본 디스패처를 사용
            log("Default: I'm working in thread ")
        }
        log("after Default")
        launch(Dispatchers.IO) { // 기본 디스패처를 사용
            log("IO: I'm working in thread ")
        }
        log("after IO")
        launch(newSingleThreadContext("MyOwnThread")) {
            log("newSingleThreadContext: I'm working in thread ")
        }
        log("after newSingleThreadContext")
    }
}
/*
18:09:07.948:Thread[main,5,main]: main() started
18:09:08.020:Thread[main,5,main]: after launch
18:09:08.023:Thread[main,5,main]: Unconfined: I'm working in thread 
18:09:08.024:Thread[main,5,main]: after Unconfined
18:09:08.035:Thread[main,5,main]: after Default
18:09:08.037:Thread[DefaultDispatcher-worker-1,5,main]: Default: I'm working in thread 
18:09:08.037:Thread[main,5,main]: after IO
18:09:08.037:Thread[DefaultDispatcher-worker-1,5,main]: IO: I'm working in thread 
18:09:08.044:Thread[main,5,main]: after newSingleThreadContext
18:09:08.044:Thread[MyOwnThread,5,main]: newSingleThreadContext: I'm working in thread 
18:09:08.044:Thread[main,5,main]: main runBlocking: I'm working in thread
18:09:08.045:Thread[main,5,main]: main() terminated
 */
```

- 같은 `launch`를 사용하더라도 전달하는 컨텍스트에 따라서 서로 다른 스레드상에서 코루틴이 실행 됨

### 코루틴 빌더와 일시 중단 함수

- `launch`, `async`, `runBlocking`은 모두 코루틴 빌더라고 불린다.
- `produce`: 정해진 채널로 데이터를 스트림으로 보내는 코루틴을 만든다.
- `actor`: 정해진 채널로 메시지를 받아 처리하는 액터를 코루틴으로 만든다.
- `delay`, `yield`: 일시 중단 함수로 쓰인다.
- `withContext`: 다른 컨텍스트로 코루틴을 전환한다.
- `withTimeout`: 코루틴이 정해진 시간 안에 실행되지 않으면 예외를 발생시킨다.
- `withTimeoutOrNull`: 코루틴이 정해진 시간 안에 실행되지 않으면 null을 결과로 돌려준다.
- `awaitAll`: 모든 작업의 성공을 기다린다. 작업 중 어느 하나가 예외로 실패하면 `awaitAll`도 그 예외로 실패한다.
- `joinAll`: 모든 작업이 끝날 때까지 현재 작업을 일시 중단시킨다.

## suspend 키워드와 코틀린의 일시 중단 함수 컴파일 방법

- 일시 중단 함수는 코루틴이나 suspend function에서만 사용가능하다.
- fun 앞에 suspend를 추가해서 일시 중단 함수를 만들 수 있다.

```kotlin
suspend fun yieldThreadTimes() {
    log("1")
    delay(1000L)
    yield()
    log("2")
    delay(1000L)
    yield()
    log("3")
    delay(1000L)
    yield()
    log("4")
}

fun suspendExample() {
    log("suspend started")
    GlobalScope.launch { yieldThreadTimes() }
    log("suspend executed")
}

fun main() {
    log("main started")
    suspendExample()
    Thread.sleep(5000L)
    log("main ended")
}
/*
18:46:42.837:Thread[main,5,main]: main started
18:46:42.869:Thread[main,5,main]: suspend started
18:46:42.913:Thread[main,5,main]: suspend executed
18:46:42.917:Thread[DefaultDispatcher-worker-1,5,main]: 1
18:46:43.931:Thread[DefaultDispatcher-worker-1,5,main]: 2
18:46:44.935:Thread[DefaultDispatcher-worker-1,5,main]: 3
18:46:45.940:Thread[DefaultDispatcher-worker-1,5,main]: 4
18:46:47.918:Thread[main,5,main]: main ended
 */
```

- 일시 중단을 할 때 코루틴이 동작 중이었던 상태를 저장해야 다음에 돌아왔을 때 이어서 할 수 있기에 상태 저장은 컴파일러에 의해서 생성 된다. 이때 컨티뉴에이션 패싱 스타일(CPS, continuation
  passing style) 변환과 상태 기계를 활용해 코드를 생성 해냄
- 프로그램의 실행 중 특정 시점 이후에 진행해야 하는 내용을 별도의 함수로 뽑고(컨티뉴에이션), 그 함수에게 현재 시점까지 실행한 결과를 넘겨서 처리하게 만드는 소스코드 변환 기술

```kotlin
public static final void suspendExample() {
    Sample1Kt.log("suspend started");
    BuildersKt.launch$default((CoroutineScope)GlobalScope.INSTANCE, (CoroutineContext)null, (CoroutineStart)null, (Function2)(new Function2((Continuation)null) {
    int label;

    @Nullable
    public final Object invokeSuspend (@NotNull Object $result) {
    Object var2 = IntrinsicsKt . getCOROUTINE_SUSPENDED ();
    switch(this.label) {
        case 0:
        ResultKt.throwOnFailure($result);
        this.label = 1;
        if (SuspendKt.example(5, this) == var2) {
            return var2;
        }
        break;
        case 1:
        ResultKt.throwOnFailure($result);
        break;
        default:
        throw new IllegalStateException ("call to 'resume' before 'invoke' with coroutine");
    }

    return Unit.INSTANCE;
}

    @NotNull
    public final Continuation create (@Nullable Object value, @NotNull Continuation completion) {
    Intrinsics.checkNotNullParameter(completion, "completion");
    Function2 var3 = new < anonymous constructor > (completion);
    return var3;
}

    public final Object invoke (Object var1, Object var2) {
    return ((<undefinedtype>)this.create(var1, (Continuation) var2)).invokeSuspend(Unit.INSTANCE);
}
}), 3, (Object)null);
    Sample1Kt.log("suspend executed");
}

@Nullable
public static final Object example(int v, @NotNull Continuation $completion) {
    return Boxing.boxInt(v * 2);
}
```

- suspend 함수를 디컴파일하면 `Continuation` 인자가 마지막에 추가 된다.
- 동작 방식은 함수가 호출할때는 함수 호출이 끝난 후 수행해야 할 작업을 var에 Continuation으로 전달하고, 함수 내부에서는 필요한 모든 일을 수행한 다음에 결과를 var에 넘기는 코드를 추가

## 과제 설명

- 코루틴을 적용해 자동차 조립을 가장 빠르게 해보기

# 1장

## 프로세스, 스레드, 코루틴

### 프로세스

- 실행 중인 애플리케이션의 인스턴스
- 상태를 갖고 있다 ex) 리소스를 여는 핸들, 프로세스 ID, 데이터, 네트워크 연결 등

### 스레드

- 실행 스레드는 프로세스가 실행할 일련의 명령을 포함
- 프로세스는 최소한 하나의 스레드를 포함하며, 이 스레드는 애플리케이션의 진입점을 실행하기 위해 생성
- 스레드 안에서 명령은 한 번에 하나씩 실행돼 스레드가 블록되면 블록이 끝날때까지 같은 스레드에서 다른 명령을 실행할 수 없다.
- 스레드 로컬 스토리지라는 자체 저장소도 갖고 있다

### 코루틴

- 코틀린 문서에서는 경량 스레드라고 부른다.
- 스레드와 비슷한 라이프 사이클을 갖고 있으며, 스레드와 마찬가지로 프로세서가 실행할 명령어 집함의 실행을 정의한다.
- 스레드 하나에 많은 코루틴이 있을 수 있지만, 주어진 시간에 하나의 스레드에서 하나의 명령만이 실행 될 수 있다.
- 스레드와 코루틴의 가장 큰 차이점은 코루틴이 빠르고 적은 비용으로 생성할 수 있다.

```kotlin
fun createCoroutines(amount: Long) {
    runBlocking {
        log("${Thread.activeCount()} threads active at the start")

        val jobs = arrayListOf<Job>()
        for (i in 1..amount) {
//            jobs += launch {
//            jobs += launch(Dispatchers.IO) {
//            jobs += launch(Dispatchers.Unconfined) {
            jobs += launch(Dispatchers.Default) {
                log("launch started $i")
                delay(1000)
                log("launch ended $i")
            }
        }
        jobs.forEach {
            it.join()
        }
        log("${Thread.activeCount()} threads active at the end")
    }

}

fun main() {
    val time = measureTimeMillis {
        createCoroutines(4)
    }
    log("Took $time ms")
}
/*
// 기본
20:51:31.967:Thread[main,5,main]: 2 threads active at the start
20:51:32.006:Thread[main,5,main]: launch started 1
20:51:32.008:Thread[main,5,main]: launch started 2
20:51:33.009:Thread[main,5,main]: launch ended 1
20:51:33.011:Thread[main,5,main]: launch ended 2
20:51:33.011:Thread[main,5,main]: 2 threads active at the end
20:51:33.015:Thread[main,5,main]: Took 1130 ms

// IO
20:51:48.869:Thread[main,5,main]: 2 threads active at the start
20:51:48.912:Thread[DefaultDispatcher-worker-1,5,main]: launch started 1
20:51:48.912:Thread[DefaultDispatcher-worker-3,5,main]: launch started 2
20:51:49.920:Thread[DefaultDispatcher-worker-1,5,main]: launch ended 1
20:51:49.921:Thread[DefaultDispatcher-worker-3,5,main]: launch ended 2
20:51:49.923:Thread[main,5,main]: 7 threads active at the end
20:51:49.927:Thread[main,5,main]: Took 1125 ms

// Unconfined
20:52:10.452:Thread[main,5,main]: 2 threads active at the start
20:52:10.480:Thread[main,5,main]: launch started 1
20:52:10.491:Thread[main,5,main]: launch started 2
20:52:11.489:Thread[kotlinx.coroutines.DefaultExecutor,5,main]: launch ended 1
20:52:11.492:Thread[kotlinx.coroutines.DefaultExecutor,5,main]: launch ended 2
20:52:11.492:Thread[main,5,main]: 3 threads active at the end
20:52:11.497:Thread[main,5,main]: Took 1112 ms

// Default
20:52:29.762:Thread[main,5,main]: 2 threads active at the start
20:52:29.803:Thread[DefaultDispatcher-worker-2,5,main]: launch started 2
20:52:29.803:Thread[DefaultDispatcher-worker-1,5,main]: launch started 1
20:52:30.812:Thread[DefaultDispatcher-worker-2,5,main]: launch ended 2
20:52:30.812:Thread[DefaultDispatcher-worker-1,5,main]: launch ended 1
20:52:30.814:Thread[main,5,main]: 5 threads active at the end
20:52:30.818:Thread[main,5,main]: Took 1134 ms
 */
```

- 스레드는 한 번에 하나의 코루틴만 실행할 수 있기 때문에 프레임워크가 필요에 따라 코루틴을 스레드들 사이에 옮기는 역할을 한다.

## 동시성에 대해

- 동시성과 병렬성의 차이
- 동시성은 하나의 코어에서 여러개의 스레드를 동작시킬 때 교차적으로 실행시켜서 동시에 실행 되는 것 처럼 보인다.
- 병렬성은 하나의 코어에서 하나의 스레드를 동작시키고, 동시에 두 개 이상의 코어에서 스레드가 실행되기에 동시성을 의미하지만, 동시성은 병렬성이 없어도 발생할 수 있다

## CPU 바운드와 I/O 바운드

### CPU 바운드

- CPU 성능에 의존적인 영역
- 단순 알고리즘을 실행하는 경우 CPU의 성능이 좋을 수록 더 빠르게 실행 된다.

### I/O 바운드

- 입출력 장치에 의존하는 알고리즘
- 파일에서 데이터를 읽거나, 네트워크 통신의 경우 I/O 작업이다.
- 최적화를 하기 위해서는 외부 시스템이나 장치에 의존해야 한다.

### CPU 바운드 알고리즘에서의 동시성과 병렬성

- 다중 코어에서 병렬성을 활용하면 성능을 향상 시킬 수 있지만 단일 코어에서 동시성을 구현하면 성능이 저하될 수 있다.

#### 단일 코어에서 실행

- 단일 코어에서 3개의 스레드의 영역을 실행시키면 교차 실행이 되며, 컨텍스트 스위칭이 발생한다.
- 컨텍스트 스위칭은 현재 스레드의 상태를 저장한 후 다음 스레드의 상태를 적재해야 하는 작업이 들어가기에, 전체 프로세스에 오버헤드가 발생
- 순차적 구현에 비해서 단일 코어에 멀티 스레드를 동작시키는 것은 오버헤드가 커질 수 있음

#### 병렬 실행

- 병렬 실행의 경우 각 스레드를 하나의 코어에서 실행하기에 컨텍스트 스위칭이 발생하지 않음
- CPU 바운드 알고리즘의 경우 코어수를 기준으로 적절한 스레드 수를 생성하도록 고려해야 함
- CPU 바운드 알고리즘을 실행하기 위해 생성된 스레드 풀인 CommonPool을 활용할 수 있음
- Common pool size = core size - 1

### I/O 바운드 알고리즘에서의 동시성 대 병렬성

- I/O 작업은 순차적인 알고리즘보다 동시성 구현에서 항상 더 나은 성능을 발휘할 것으로 예상

### 동시성이 어려운 이유

```kotlin
fun main() {
    // getUserInfo가 1000 ms 이상 걸린다면 Main 스레드가 종료되어서 해당 유저의 값이 표시 되지 않음
    val user = asyncGetUserInfo()
    delay(1000)
    println(user)
}
```

- 레이스 컨디션: 동시성 코드 일부가 제대로 작동하기 위해 일정한 순서로 와나료돼야 할때 발생

```kotlin
val count = 0
fun main() {
    val workA = asyncInc(2000)
    val workB = asyncInc(100)
  
  workA.await()
  workB.await()
  print("counter [$counter]")
}
```
- 원자성 위반: 데이터의 간섭이 발생하여 원하는 값이 아닌 다른 값이 생 되는 경우 

```kotlin
fun main() = runBlocking {

    jobA = GlobalScope.launch {
        delay(1000)
        jobB.join()
    }
    jobB = GlobalScope.launch {
        jobA.join()
    }
    jobA.join()
    println("Finished")
}
```
- 교착상태: 순환적 의존성(circular dependencies)로 인해서 전체 애플리케이션의 실행이 중단 되는 상황
- 라이브락: 어플리케이션이 올바르게 실행을 계속할 수 없을 때 발생하는 교착상태와 유사
- 교착상태에서 벗어나는 방법은 알고있지만, 회복하려는 시점에 그들의 진행을 방해하는 상태

## 코틀린에서의 동시성
### 넌 블로킹
- 채널(Channels), 액터(Actors), 상호배제(mutal exclusions)와 같은 훌륭한 기본형도 제공해 스레드를 블록하지 않고 동시성 코드를 
효과적으로 통신하고 동기화하는 메커니즘을 제공한다.
### 명시적인 선언
- 연산이 동시에 실행돼야 하는 시점을 명시적으로 만드는 것이 중요
#### 가독성
- 동시성 코드는 디버깅이랑 코드를 읽기가 어렵지만, 코루틴은 순차적 코드만큼 읽기가 쉽다.
#### 기본형 활용
- 동시성 코드를 쉽게 구현할 수 있는 고급함수와 기본형을 제공
- newSingleThreadContext()
- newFixedThreadPoolContext()
- CommonPool
#### 유연성
- 채널(Channel): 코루틴 간에 데이터를 안전하게 보내고 받는데 사용할 수 있는 파이프
- 작업자 풀(Worker pools): 많은 스레드에서 연산 집합의 처리를 나눌 수 있는 코루틴의 풀
- 액터(Actors): 채널과 코루틴을 사용하는 상태를 감싼 래퍼로 여러 스레드에서 상태를 안전하게 수정하는 메커니즘을 제공
- 뮤텍스(Mutexes): 크리티컬 존 영역을 정의해 한 번에 하나의 스레드만 실행할 수 있도록 하는 동기화 메커니즘
- 스레드 한정(Thread confinement): 코루틴의 실행을 제한해서 지정된 스레드에서만 실행하도록 하는 기능
- 생성자(반복자 및 시퀀스): 필요에 따라 정보를 생성할 수 있고 새로운 정보가 필요하지 않을 때 일시 중단될 수 있는 데이터 소스

### 코틀린 동시성 관련 개념과 용어
#### 일시중단 연산
- 일시중단연산(suspending computations)
- 해당 스레드를 차단하지 않고 실행을 일지중지할 수 있는 연산
- 일시중단을 해둔 스레드에서 다시 시작할때까지 다른 연산에 사용할 수 있음
#### 일시중단 함수
- 함수에 `suspend` 키워드를 사용해서 사용, `delay()`
- 해당 스레드에 연산을 일시 중지하고, 일시 중지된 동안 다른 연산에 수행에 사용될 수 있다.
#### 코루틴 디스패처
- 코루틴을 시작하거나 재개할 스레드를 결정하기 위해 코루틴 디스패처가 사용된다.
- `CoroutineDispatcher` 인터페이스를 구현해야 한다.
- DefaultDispatcher: CommonPool과 같다.
- CommonPool: 공유된 백그라운드 스레드 풀에서 코루틴을 실행하고 다시 시작한다.
- Unconfined: 현재 스레드(코루틴이 호출된 스레드)에서 코루틴을 시작하지만 어떤 스레드에서도 코루틴이 다시 재개될 수 있다.
#### 코루틴 빌더
- 일시 중단 람다를 받아 그것을 실행시키는 코루틴을 생성하는 함수
- async(): 결과가 예상되는 코루틴을 시작하는데 사용, 결과 또는 예외를 포함하는 `Deferred<T>`를 반환
- launch(): 결과를 반환하지 않는 코루틴을 시작, 실행을 취소하기 위해 사용할 수 있는 `Job`을 반환
- runBlocking(): 블로킹 코드를 일시 중지 가능한 코드로 연결하기 위해 작성, 코루틴의 실행이 끝날 때까지 현재 스레드를 차단
