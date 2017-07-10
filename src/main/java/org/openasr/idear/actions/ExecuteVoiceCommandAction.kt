package com.jetbrains.idear.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataKey
import com.jetbrains.idear.actions.recognition.TextToActionConverter
import java.util.logging.Logger

class ExecuteVoiceCommandAction : ExecuteActionByCommandText() {

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val editor = CommonDataKeys.EDITOR.getData(dataContext)!!

        val provider = TextToActionConverter(e.dataContext)
        val info = provider.extractAction(e.getData(KEY)!!)
        if (info != null) {
            invoke(editor, info)
        } else {
            logger.info("Command not recognized")
        }
    }

    companion object {
        private val logger = Logger.getLogger(ExecuteVoiceCommandAction::class.java.simpleName)
        val KEY = DataKey.create<String>("Idear.VoiceCommand.Text")
    }
}
