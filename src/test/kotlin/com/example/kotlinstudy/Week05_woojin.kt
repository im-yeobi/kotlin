package com.example.kotlinstudy

import com.example.kotlinstudy.coroutines.Car
import com.example.kotlinstudy.coroutines.CarDoor
import com.example.kotlinstudy.coroutines.CarFrame
import com.example.kotlinstudy.coroutines.CarTire
import com.example.kotlinstudy.coroutines.Employee
import com.example.kotlinstudy.coroutines.EmployeeRepository
import com.example.kotlinstudy.coroutines.log
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Week05_woojin {
    val employeeRepository: EmployeeRepository = EmployeeRepository()
    val random = Random(1)

    @OptIn(ExperimentalTime::class)
    @Test
    fun `5대의 자동차를 가장 빠르게 만들어보자`() {
        val time = measureTime {
            var count = 0
            for (i in 0..4) {
                log("자동차 생산 시작")
                runBlocking {
                    val asyncEmployee = async(Dispatchers.IO) {
                        getEmployee()
                    }

                    val asyncEmployee1 = async(Dispatchers.IO) {
                        getEmployee()
                    }

                    val asyncCarFrame = async(Dispatchers.IO) {
                        CarFrame()
                    }

                    val carFrame = asyncCarFrame.await()
                    val employee = asyncEmployee.await()
                    val employee1 = asyncEmployee1.await()

                    val carDoor = async(Dispatchers.IO) {
                        val carDoor = CarDoor(carFrame = carFrame)
                        carDoor.assemble(employee, carFrame)
                        carDoor
                    }

                    val carTire = async(Dispatchers.IO) {
                        val carTire = CarTire(carFrame = carFrame)
                        carTire.assemble(employee1, carFrame)
                        carTire
                    }


                    launch(Dispatchers.IO) {
                        val car: Car =
                            Car(carFrame = carFrame, carDoor = carDoor.await(), carTire = carTire.await())
                        car.assemble(
                            employee1 = employee, employee2 = employee1,
                            carFrame, carDoor.await(), carTire.await()
                        )
                    }
                }

                log("자동차 생산 완료")
                count++
            }

            count shouldBe 5
        }
        time.shouldBeLessThan(Duration.Companion.seconds(50))
        log("걸린시간 : $time")

    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `5대의 자동차를 가장 빠르게 만들어보자2`() {
        val time = measureTime {
            var count = 0
            for (i in 0..4) {
                log("자동차 생산 시작")
                runBlocking {
                    val asyncEmployee = getAsyncEmployee()
                    val asyncCarFrame = async(Dispatchers.IO) {
                        CarFrame()
                    }
                    val carFrame = asyncCarFrame.await()
                    val employee = asyncEmployee.await()

                    val carDoor = async(Dispatchers.IO) {
                        val carDoor = CarDoor(carFrame = carFrame)
                        carDoor.assemble(employee, carFrame)
                        carDoor
                    }

                    val asyncEmployee1 = getAsyncEmployee()
                    val employee1 = asyncEmployee1.await()
                    val carTire = async(Dispatchers.IO) {
                        val carTire = CarTire(carFrame = carFrame)
                        carTire.assemble(employee1, carFrame)
                        carTire
                    }


                    launch(Dispatchers.IO) {
                        val car: Car =
                            Car(carFrame = carFrame, carDoor = carDoor.await(), carTire = carTire.await())
                        car.assemble(
                            employee1 = employee, employee2 = employee1,
                            carFrame, carDoor.await(), carTire.await()
                        )
                    }
                    releaseEmployee(employee)
                }

                log("자동차 생산 완료")
                count++
            }

            count shouldBe 5
        }
        time.shouldBeLessThan(Duration.Companion.seconds(50))
        log("걸린시간 : $time")

    }

    private fun releaseEmployee(employee: Employee) {
        employee.active = true
    }

    private fun CoroutineScope.getAsyncEmployee(): Deferred<Employee> {
        val asyncEmployee = async(Dispatchers.IO) {
            getEmployee()
        }
        return asyncEmployee
    }

    fun getEmployee(): Employee = employeeRepository.findEmployee(getEmployeeId())
    fun getEmployeeV2(): Employee = employeeRepository.findEmployeeV2(getEmployeeId())
    fun getEmployeeId() = random.nextLong(1, 5)

}
