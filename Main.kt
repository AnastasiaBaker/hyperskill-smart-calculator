package calculator

import java.math.BigInteger
import java.util.*

object Regex {
    val emptyLine = "".toRegex()
    val exit = "/exit".toRegex()
    val help = "/help".toRegex()
    val command = "^/".toRegex()
    val singleNumber = "[+-]?\\d+".toRegex()
    val pluses = "\\++".toRegex()
    val md = ".*((\\*\\*+)|//+).*".toRegex()
    val setVariable = "([a-zA-Z]+)=([a-zA-Z]+|[+-]?\\d+)".toRegex()
    val getVariable = "[a-zA-Z]+".toRegex()
    val invalidAssignment = "([a-zA-Z]+)=.*".toRegex()
    val invalidIdentifier = ".*=.*".toRegex()
}

object Calculator {
    var run = true
    val variablesMap = mutableMapOf<String, BigInteger>()

    fun work(input: String) {
        when {
            Regex.emptyLine.matches(input) -> return
            Regex.exit.matches(input) -> {
                println("Bye!")
                run = false
            }
            Regex.help.matches(input) -> {
                println("The program supports addition, subtraction, multiplication, integer division, " +
                        "exponentiation, parentheses and variables.\n" +
                        "It also supports both unary and binary minus or plus operators.\n" +
                        "The name of a variable (identifier) can contain only Latin letters.")
            }
            Regex.command.containsMatchIn(input) -> println("Unknown command")
            Regex.singleNumber.matches(input) -> println(input.toBigInteger())
            Regex.md.matches(input) -> println("Invalid expression")
            Regex.setVariable.matches(input) -> createVariable(input)
            Regex.invalidAssignment.matches(input) -> println("Invalid assignment")
            Regex.invalidIdentifier.matches(input) -> println("Invalid identifier")
            Regex.getVariable.matches(input) -> {
                if (variablesMap.contains(input)) println(variablesMap[input]) else println("Unknown variable")
            }
            else -> {
                var expression = input
                var count = 0
                val minuses = mutableMapOf<String, String>()

                if (expression.first() == '+') expression = expression.substring(1)
                if (expression.first() == '-') expression = "0$expression"

                for (i in expression.indices) {
                    if (i != expression.lastIndex && expression[i] == '-') count++
                    else if (count != 0) {
                        if (count % 2 != 0) minuses[expression.substring(i - count until i)] = "-"
                        else minuses[expression.substring(i - count until i)] = "+"

                        count = 0
                    }
                }

                for ((k, v) in minuses) {
                    expression = expression.replace(k, v)
                }

                count(expression)
            }
        }
    }
}

fun count(input: String) {
    val operators = "-+/*^"
    val postfix = mutableListOf<String>()
    val stack = Stack<BigInteger>()

    for (i in input.indices) {
        val idx = operators.indexOf(input[i]).toBigInteger()

        if (idx != "-1".toBigInteger()) {
            if (stack.isEmpty()) stack.push(idx)
            else {
                while (!stack.isEmpty()) {
                    val p2 = stack.peek() / BigInteger.TWO
                    val p1 = idx / BigInteger.TWO
                    if (p2 > p1 || (p2 == p1 && input[i] != '^')) {
                        postfix.add("${operators[stack.pop().toInt()]}")
                    }
                    else break
                }
                stack.push(idx)
            }
        }
        else if (input[i] == '(') {
            stack.push("-2".toBigInteger())
        }
        else if (input[i] == ')') {
            while (stack.isNotEmpty() && stack.peek() != "-2".toBigInteger()) {
                postfix.add("${operators[stack.pop().toInt()]}")
            }

            if (stack.isEmpty()) return println("Invalid expression")

            stack.pop()
        }
        else if (i != 0 && input[i - 1].isDigit()) {
            postfix[postfix.lastIndex] = postfix.last() + input[i]
        } else postfix.add("${input[i]}")
    }

    while (!stack.isEmpty()) {
        if (stack.peek().toInt() in operators.indices) postfix.add("${operators[stack.pop().toInt()]}")
        else return println("Invalid expression")
    }

    for (v in postfix) {
        when {
            v.toBigIntegerOrNull() != null -> stack.push(v.toBigInteger())
            v.first() in operators -> {
                val int1 = stack.pop()
                val int2 = stack.pop()

                stack.push(when (v.first()) {
                    '-' -> int2 - int1
                    '+' -> int2 + int1
                    '/' -> int2 / int1
                    '*' -> int2 * int1
                    else -> int2.pow(int1.toInt())
                })
            }
            Regex.getVariable.matches(v) -> {
                if (v in Calculator.variablesMap) stack.push(Calculator.variablesMap[v]!!)
                else return println("Unknown variable")
            }
        }
    }

    println(stack.pop())
}

fun createVariable(input: String) {
    val values = Regex.setVariable.findAll(input)

    values.forEach { matchResult ->
        if (matchResult.groupValues[2].toBigIntegerOrNull() == null) {
            for (i in Calculator.variablesMap.keys) if (i == matchResult.groupValues[2]) {
                Calculator.variablesMap[matchResult.groupValues[1]] = Calculator.variablesMap[i]!!
            }
        } else {
            Calculator.variablesMap[matchResult.groupValues[1]] = matchResult.groupValues[2].toBigInteger()
        }
        if (!Calculator.variablesMap.contains(matchResult.groupValues[1])) {
            println("Unknown variable")
            return
        }
    }
}

fun main() {
    while (Calculator.run) {
        Calculator.work(readLine()!!.replace(" ", "").replace(Regex.pluses, "+"))
    }
}