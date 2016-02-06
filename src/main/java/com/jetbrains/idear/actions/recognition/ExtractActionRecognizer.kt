package com.jetbrains.idear.actions.recognition

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Pair
import com.intellij.util.containers.ContainerUtil

class ExtractActionRecognizer : ActionRecognizer {

    private val actions = ContainerUtil.newHashSet("extract")

    override fun isMatching(sentence: String): Boolean {
        return actions.filter({ sentence.contains(it)}).firstOrNull().isNullOrEmpty().not()
    }

    override fun getActionInfo(sentence: String, dataContext: DataContext): ActionCallInfo? {
        if (!isMatching(sentence)) return null

        val words = sentence.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val data = getActionId(words) ?: return null

        val info = ActionCallInfo(data.first)
        val index = data.second

        val newName = StringBuilder()
        val newNameStartIndex = if (index + 1 < words.size && words[index + 1] == "to")
            index + 2
        else
            index + 1

        var first = true
        for (i in newNameStartIndex..words.size - 1) {
            val word = if (first) words[i] else words[i].capitalize()
            newName.append(word)
            first = false
        }

        if (newName.length > 0) {
            info.typeAfter = newName.toString()
            info.hitTabAfter = true
        }

        return info
    }

    private fun getActionId(words: Array<String>): Pair<String, Int>? {
        for (i in words.indices) {
            val word = words[i]
            if (word.contains("variable")) {
                return Pair.create("IntroduceVariable", i)
            } else if (word.contains("field")) {
                return Pair.create("IntroduceField", i)
            }
        }
        return null
    }

}
