package org.example


import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun simple() : Flow<Int> = flow {
    println("Flow started")
    for(i in 1..3){
        delay(100)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {

}