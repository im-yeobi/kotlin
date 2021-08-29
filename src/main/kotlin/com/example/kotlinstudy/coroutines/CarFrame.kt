package com.example.kotlinstudy.coroutines

import kotlin.random.Random

val RANDOM = Random(10)

data class Car(
    val id: Long = RANDOM.nextLong(100),
    val carFrame: CarFrame,
    val carDoor: CarDoor,
    val carTire: CarTire
) {
    fun assemble(employee1: Employee, employee2: Employee, carFrame: CarFrame, carDoor: CarDoor, carTire: CarTire) {
        employee1.active = false
        employee2.active = false
        log("employee1 id:${employee1.id}, employee2 id:${employee2.id}가 프레임 ${carFrame.id}, 문 ${carDoor.id}, 타이어 ${carTire.id}를 이용해 car $id 조립중")
        Thread.sleep(5000L)
        log("employee1 id:${employee1.id}, employee2 id:${employee2.id}가 프레임 ${carFrame.id}, 문 ${carDoor.id}, 타이어 ${carTire.id}를 이용해 car $id 조립완료")
        employee1.active = true
        employee2.active = true
    }
}

data class CarFrame(
    val id: Long = RANDOM.nextLong(100)
) {
    init {
        log("자동차 프레임 $id 배송 중")
        Thread.sleep(1000L)
        log("자동차 프레임 $id 배송 완료")
    }

    fun assemble(employee: Employee) {
        employee.active = false
        log("employee id:${employee.id}가 자동차 프레임 $id 조립 중")
        Thread.sleep(1000L)
        log("employee id:${employee.id}가 자동차 프레임 $id 조립 완료")
        employee.active = true
    }
}

data class CarTire(
    val id: Long = RANDOM.nextLong(100),
    val carFrame: CarFrame
) {
    init {
        log("자동차 타이어 $id 배송 중")
        Thread.sleep(1000L)
        log("자동차 타이어 $id 배송 완료")
    }

    fun assemble(employee: Employee, carFrame: CarFrame) {
        employee.active = false
        log("employee id:${employee.id}가 프레임 ${carFrame.id} 에 자동차 타이어 $id 조립 중")
        Thread.sleep(1000L)
        log("employee id:${employee.id}가 프레임 ${carFrame.id} 에 자동차 타이어 $id 조립 완료")
        employee.active = true
    }
}

class CarDoor(
    val id: Long = RANDOM.nextLong(100),
    val carFrame: CarFrame
) {
    init {
        log("자동차 문 배송 중")
        Thread.sleep(1000L)
        log("자동차 문 배송 완료")
    }

    fun assemble(employee: Employee, carFrame: CarFrame) {
        employee.active = false
        log("employee id:${employee.id}가 프레임 ${carFrame.id} 에 자동차 문 $id 조립 중")
        Thread.sleep(1000L)
        log("employee id:${employee.id}가 프레임 ${carFrame.id} 에 자동차 문 $id 조립 완료")
        employee.active = true
    }
}
