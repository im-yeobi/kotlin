package com.example.kotlinstudy.week03

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

open class PropertyChangeAware {
    protected val changeSupport = PropertyChangeSupport(this)

    fun addPropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    fun removePropertyChangeListener(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }
}

//class Book(
//    val name: String,
//    no: Int,
//    salary: Int
//) : PropertyChangeAware() {
//    var no: Int = no
//        set(value) {
//            val oldValue = field
//            field = value
//            changeSupport.firePropertyChange("no", oldValue, value)
//        }
//    var salary = salary
//        set(value) {
//            val oldValue = field
//            field = value
//            changeSupport.firePropertyChange("salary", oldValue, value)
//        }
//}

//class ObservableProperty(
//    val propName: String,
//    var propValue: Int,
//    val changeSupport: PropertyChangeSupport
//) {
//    fun getValue(): Int = propValue
//    fun setValue(newValue: Int) {
//        val oldValue = propValue
//        propValue = newValue
//        changeSupport.firePropertyChange(propName, oldValue, newValue)
//    }
//}


//class Book(
//    val name: String,
//    no: Int,
//    salary: Int
//) : PropertyChangeAware() {
//    val _no = ObservableProperty("no", no, changeSupport)
//    var no: Int
//        get() = _no.getValue()
//        set(value) {
//            _no.setValue(value)
//        }
//    val _salary = ObservableProperty("salary", salary, changeSupport)
//    var salary
//        get() = _salary.getValue()
//        set(value) {
//            _salary.setValue(value)
//        }
//}


//class ObservableProperty(
//    var propValue: Int,
//    val changeSupport: PropertyChangeSupport
//) {
//    operator fun getValue(b: Book, prop: KProperty<*>): Int = propValue
//    operator fun setValue(b: Book, prop: KProperty<*>, newValue: Int) {
//        val oldValue = propValue
//        propValue = newValue
//        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
//    }
//}
//
//class Book(
//    val name: String,
//    no: Int,
//    salary: Int
//) : PropertyChangeAware() {
//    var no: Int by ObservableProperty(no, changeSupport)
//
//    var salary: Int by ObservableProperty(salary, changeSupport)
//}


class Book(
    val name: String,
    no: Int,
    salary: Int
) : PropertyChangeAware() {
    private val observer = {
        prop: KProperty<*>, oldValue: Int, newValue: Int -> changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }
    var no: Int by Delegates.observable(no, observer)

    var salary: Int by Delegates.observable(salary, observer)
}

fun main() {
    val book = Book("kotlin", 1, 10)
    book.addPropertyChangeListener(PropertyChangeListener { event ->
        println("Property ${event.propertyName} changed from ${event.oldValue} to ${event.newValue}")
    })
    book.no = 2
    book.salary = 100
}