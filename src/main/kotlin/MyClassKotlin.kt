package org.example

/**
 * @JvmStatic
 * This annotation is used to mark a function or property as a static member in the generated Java code.
 *
 * @JvmField
 * This annotation is used to expose a Kotlin Property as a public field in the generated Java code,
 * rather than generating a getter/setter pair.
 *
 * @JvmOverloads
 * This annotation is used to generate multiple overloaded Java methods from a single Kotlin function with default parameter values.
 *
 * @JvmName
 * This annotation allows you to specify a different name for a Kotlin function or property in the generated Java code.
 *
 */


class MyClassKotlin {
    companion object{
        @JvmStatic
        fun myStaticFunction(){
            println("This is a static function.")
        }
    }
    @JvmField
    val myPublicField : Int = 25


    @JvmOverloads
    fun doSomething(a : Int, b : String = "default",c : Boolean = false) { }

    @JvmName("myJavaName")
    fun myKotlinName(){
        println("This is my Kotlin function.")
    }
}