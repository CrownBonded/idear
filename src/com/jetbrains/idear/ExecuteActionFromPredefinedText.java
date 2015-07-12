package com.jetbrains.idear;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.jetbrains.idear.actions.recognition.ActionCallInfo;
import com.jetbrains.idear.actions.recognition.TextToActionConverter;

public class ExecuteActionFromPredefinedText extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

//        String text = "idea extract to field";
        String text = "idea extract to variable name";
//        String text = "idea inline";

        TextToActionConverter provider = new TextToActionConverter();
        ActionCallInfo info = provider.extractAction(text);
        invoke(editor, info);
    }

    private void invoke(Editor editor, ActionCallInfo info) {
        AnAction action = ActionManager.getInstance().getAction(info.actionId);
        String type = info.typeAfter;
        boolean isHitTabAfter = info.hitTabAfter;

        DataManager manager = DataManager.getInstance();
        if (manager != null) {
            DataContext context = manager.getDataContext(editor.getContentComponent());
            action.actionPerformed(new AnActionEvent(null, context, "", action.getTemplatePresentation(), ActionManager.getInstance(), 0));

            if (type != null) {
                typeText(editor, type, context);
            }

            if (isHitTabAfter) {
                hitTab(context);
            }
        }
    }

    private void hitTab(DataContext context) {
        AnAction action = ActionManager.getInstance().getAction("NextTemplateVariable");
        action.actionPerformed(new AnActionEvent(null, context, "", action.getTemplatePresentation(), ActionManager.getInstance(), 0));
    }

    private void typeText(Editor editor, String type, DataContext context) {
        TypedAction typing = EditorActionManager.getInstance().getTypedAction();
        for (char c : type.toCharArray()) {
            typing.actionPerformed(editor, c, context);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (editor != null) {
            Presentation presentation = event.getPresentation();
            presentation.setEnabled(true);
        }
    }
}
