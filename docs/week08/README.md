## 채널 - 통신을 통한 메모리 공유

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
- capacity 50이면서, capacity가 가득찬 경우 가장 최근에 들어온 데이터를 유실하는 channel 만들어보기
