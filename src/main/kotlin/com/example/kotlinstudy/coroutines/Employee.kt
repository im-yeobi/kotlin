package com.example.kotlinstudy.coroutines

data class Employee(
    val id: Long,
    var active: Boolean
)


class EmployeeRepository {
    private val employees: Map<Long, Employee> =
        mapOf(
            1L to Employee(1, true),
            2L to Employee(2, true),
            3L to Employee(3, true),
            4L to Employee(4, true),
            5L to Employee(5, true)
        )

    fun findEmployee(id: Long): Employee {
        while (true) {
            val employee = employees[id]
            employee?.run { log("find employee id: ${id}, active: $active") }
            if (employee?.active == true) {
                return employee
            }
            log("해당 일꾼이 일하고 있어서 1초간 대기합니다.")
            Thread.sleep(1000L)
        }
    }
}
