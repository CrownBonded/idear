package org.openasr.idear.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_TEMPLATE_VARIABLE
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import org.openasr.idear.actions.recognition.ActionCallInfo
import org.openasr.idear.actions.recognition.TextToActionConverter
import org.openasr.idear.ide.IDEService
import org.openasr.idear.ide.dataContext

abstract class ExecuteActionByCommandText : IdearAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = IDEService.getEditor(e.dataContext)!!

        //        String text = "idea extract to field";
        //        String text = "idea extract to variable name";
        //        String text = "idea inline";

        val text = "surround with not null check"

        val provider = TextToActionConverter(e.dataContext)
        val info = provider.extractAction(text)
        if (null != info) runInEditor(editor, info)
    }

    protected open fun runInEditor(editor: Editor, info: ActionCallInfo) =
            editor.dataContext.let { context ->
                ActionManager.getInstance().getAction(info.actionId).run {
                    actionPerformed(buildActionEvent(info, this, context))
                }

                info.typeAfter?.let { typeText(editor, it, context) }

                if (info.hitTabAfter) hitTab(context)
            }

    private fun hitTab(context: DataContext) {
        val action = ActionManager.getInstance().getAction(ACTION_EDITOR_NEXT_TEMPLATE_VARIABLE)
        action.actionPerformed(AnActionEvent(null, context, "", action.templatePresentation, ActionManager.getInstance(), 0))
    }

    private fun typeText(editor: Editor, type: String, context: DataContext) {
        val typedAction = EditorActionManager.getInstance().typedAction
        for (c in type.toCharArray()) typedAction.actionPerformed(editor, c, context)
    }

    override fun update(event: AnActionEvent?) =
            IDEService.getEditor(event?.dataContext)
                    ?.run { event?.presentation?.isEnabled = true } ?: Unit

    private fun buildActionEvent(info: ActionCallInfo, action: AnAction, context: DataContext): AnActionEvent =
            if (info.actionEvent != null) info.actionEvent as AnActionEvent
            else AnActionEvent(null, context, "", action.templatePresentation, ActionManager.getInstance(), 0)
}
