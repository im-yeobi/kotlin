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


## 09. 
