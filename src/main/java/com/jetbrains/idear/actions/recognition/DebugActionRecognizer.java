package com.jetbrains.idear.actions.recognition;

import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;

//only selected configuration
public class DebugActionRecognizer implements ActionRecognizer {

    @Override
    public boolean isMatching(@NotNull String sentence) {
        return sentence.contains("debug");
    }

    @Override
    public ActionCallInfo getActionInfo(@NotNull String sentence, DataContext dataContext) {
        return new ActionCallInfo("Debug");
    }
}
