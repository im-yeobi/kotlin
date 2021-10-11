## 08. 동시성 코드 테스트와 디버깅

동시성 코드는 생각치 못한 에러를 유발할 수 있다
- 예를 들어 a,b,c를 동시에 수행할때 c가 평균적으로 가장 오래걸린다고하자. 여기서 c가 가장 오래걸림을 가정하고 코드를 짠다면 예상치 못한 케이스에서 문제가 될 수 있다.

이런 케이스를 방지하기 위해 테스트를 짜야한다
- 버그 수정을 시나리오를 커버하는 테스트와 함께 수반되어야한다
- 동시성 작업을 위해 모든 값들을 차례로 하는 테스트를 하지 말아야한다

#### 예제 
`test >> week09-test` 의 테스트를 보면 완료가 되기전에 호출을 하면 안된다.
~~~
profession.await()
return User(
    name.getCompleted(),
    age.getCompleted(),
    profession.getCompleted()
)
~~~
만약 profession보다 더 늦게 끝나는 job이 있을 경우 아래와 같은 에러가 뜬다. 

~~~
This job has not completed yet
~~~

모두 await()을 하도록 수정한다. 

~~~
return User(
        name.await(),
        age.await(),
        profession.await()
)
~~~

### 디버깅
~~~
Processing 2 in myPool-3
Processing 1 in myPool-2
Processing 0 in myPool-1
Processing 4 in myPool-3
Processing 3 in myPool-2
Processing 5 in myPool-3
Step two of 2 happening in thread ctx
Step two of 1 happening in thread ctx
Step two of 4 happening in thread ctx
Step two of 3 happening in thread ctx
Finishing 2 in myPool-2
Step two of 5 happening in thread ctx
Finishing 4 in myPool-2
Finishing 1 in myPool-3
Finishing 5 in myPool-3
Finishing 3 in myPool-2
Step two of 0 happening in thread ctx
Finishing 0 in myPool-3
~~~

vm 옵션 항목 추가 
-Dkotlinx.coroutines.debug

~~~
Processing 0 in myPool-1 @coroutine#1
Processing 2 in myPool-3 @coroutine#3
Processing 1 in myPool-2 @coroutine#2
Processing 3 in myPool-3 @coroutine#4
Step two of 2 happening in thread ctx @coroutine#3
Processing 4 in myPool-2 @coroutine#5
Processing 5 in myPool-3 @coroutine#6
Step two of 1 happening in thread ctx @coroutine#2
Step two of 3 happening in thread ctx @coroutine#4
Step two of 4 happening in thread ctx @coroutine#5
Finishing 2 in myPool-2 @coroutine#3
Step two of 5 happening in thread ctx @coroutine#6
Finishing 3 in myPool-2 @coroutine#4
Finishing 1 in myPool-3 @coroutine#2
Finishing 4 in myPool-2 @coroutine#5
Finishing 5 in myPool-3 @coroutine#6
Step two of 0 happening in thread ctx @coroutine#1
Finishing 0 in myPool-2 @coroutine#1
~~~

~~~
val task = GlobalScope.async(pool + CoroutineName("main")) {
~~~
위처럼 이름을 지정해줄수도 있음 

~~~
Processing 1 in myPool-3 @main#2
Processing 2 in myPool-1 @main#3
Processing 0 in myPool-2 @main#1
Step two of 1 happening in thread ctx @inner#2
Processing 4 in myPool-1 @main#5
Processing 3 in myPool-3 @main#4
Processing 5 in myPool-3 @main#6
Step two of 2 happening in thread ctx @inner#3
Step two of 3 happening in thread ctx @inner#4
Step two of 4 happening in thread ctx @inner#5
Finishing 1 in myPool-1 @main#2
Step two of 5 happening in thread ctx @inner#6
Finishing 3 in myPool-1 @main#4
Finishing 2 in myPool-3 @main#3
Finishing 4 in myPool-1 @main#5
Finishing 5 in myPool-3 @main#6
Step two of 0 happening in thread ctx @inner#1
Finishing 0 in myPool-1 @main#1
~~~

마찬가지로 디버깅할때도 Thread.currentThread().name으로 걸수 있다. 

## 과제 설명

## 09. 코틀린의 동시성 내부
Continuation Passing Style(연속체 전달 스타일) : 함수가 값을 반환하지 않는 프로그래밍 스타일이다.

### 연속체
Direct Style
~~~
fun postItem(item: Item) {
    val token = requestToken()
  
    // Continuation
    val post = createPost(token, item);
    processPost(post)
}
~~~
위 함수의 경우 requestToken을 실행하게 되면, 이 부분을 콜스택에 두고 requestToken을 실행한 뒤 그 반환값을 가지고 
다시 createPost를 실행하고, 이때 이는 콜스택에 적재된뒤, createPost실행완료후 이 값으로 processPost를 실행하게 된다.

Continuation Passing Style
~~~
fun postItem(item: Item) {
    requestToken { token ->
        // Continuation
        createPost(token, item) { post ->
            processPost(post)
        }   
    }
}
~~~
이를 콜백형태로 적어보면 위와 같다. 

코루틴에서는 위처럼 direct style로 적어도 아래처럼 바꿔준다. 이는 `Continuation`을 통해 가능하다
~~~
public interface Continuation<in T> {
    public val context : CoroutineContext
    public fun resume(value: T)
    public fun resumeWithException(exception: Throwable)
}
~~~

- context: 실행되는 context의 정보
- resume: 일시 중단을 일으킨 작업의 결과 

#### suspend
주어진 범위의 코드가 연속체를 사용하여 동작하도록 컴파일러에게 지시한다.

~~~
suspend fun getUserSumary(id: Int) : UserSummary {
    println("fetching summary of $id")
    val profile = fetchProfile(id) // suspending fun
    val age = calculateAge(profile.dateOfBirth)
    val terms = validateTerms(profile.country, age) // suspending fun
    return UserSummary(profile, age, terms)
}
~~~

1. 함수 실행
2. 로그 출력
3. fetchProfile 호출. 이때 위 함수는 일시중지됨
4. fetchProfile 종료후 age 계산
5. validateTerms 호출. 이때 위 함수는 중지됨
6. UserSummary 반환

이를 쉽게 표현하면 다음과 같이 구분할 수 있다 

~~~
suspend fun getUserSumary(id: Int) : UserSummary {
    // label 0 
    println("fetching summary of $id")
    val profile = fetchProfile(id) // suspending fun
    // label 1 
    val age = calculateAge(profile.dateOfBirth)
    val terms = validateTerms(profile.country, age) // suspending fun
    // label 2
    return UserSummary(profile, age, terms)
}
~~~

~~~
when(label) {
    0 -> {
        println("fetching summary of $id")
        fetchProfile(id)
        return
    }
    1 -> {
        calculateAge(profile.dateOfBirth)
        validateTerms(profile.country, age) // suspending fun
        return
    }
    2 -> {
        UserSummary(profile, age, terms)
    }
~~~

이제 위 함수를 타기 위한 부분이 있어야한다.
~~~
suspend fun getUserSumary(id: Int) : UserSummary {
    val sm = object : CoroutineImpl {
    override fun doResume(data: Any?, exception: Throwable?) {
        // doResume이 불리면 가지고 있는 label에 따라 어딜 실행할지를 알 수 있다. 
        getUserSummary(id, this)
    }
    
    val state = sm as CoroutineImpl
    when(state.label) {
        0 -> {
            println("fetching summary of $id")
            sm.label = 1 // 라벨 증가
            fetchProfile(id, sm)
            return
        }
        1 -> {
            calculateAge(profile.dateOfBirth)
            sm.label = 2// 라벨 증가
            validateTerms(profile.country, age, sm) // suspending fun
            return
        }
        2 -> {
            UserSummary(profile, age, terms)
        }
    }
}
~~~
아래와 같이 구조 변경 
~~~
private class GetUserSummarySM: CoroutineImpl {
    var value: Any? = null
    var exception: Throwable? = null, 
    var cont: continuation<Any?>? = null, 
    val id: Int? = null
    var profile: Profile? = null, 
    var age: Int? = null,
    var terms: Terms? = null

    override fun doResume(data: Any?, exception: Throwable?) {
        this.value = data, 
        this.exception = exception
        getUserSummary(id, this)
    }
}

fun getUserSummary(id : Long, cont : Continuation<Any?>){
    val sm = cont as? GetUserSummarySm ?: GetUserSummarySm()
    when(state.label) {
        0 -> {
            sm.cont = cont
            println("fetching summary of $id")
            sm.label = 1 // 라벨 증가
            fetchProfile(id, sm)
            return
        }
        1 -> {
            sm.profile = sm.value as Profile
            sm.age = calculateAge(sm.profile!!.dateOfBirth)
            sm.label = 2// 라벨 증가
            validateTerms(sm.profile!!.country, sm.age!!, sm) // suspending fun
            return
        }
        2 -> {
            sm.terms = sm.value as Terms
            //UserSummary(sm.profile!!, sm.age!!, sm.terms!!)
            sm,cont!!.resume(UserSummary(sm.profile!!, sm.age!!, sm.terms!!))
        }
    }
}
~~~


### 컨텍스트 전환
ContinuationInterceptor 



https://www.slipp.net/wiki/pages/viewpage.action?pageId=52527388