package com.jetbrains.idear;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.jetbrains.idear.recognizer.CustomLiveSpeechRecognizer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ASRServiceImpl implements ASRService {

    public static final double MASTER_GAIN = 0.85;

    private final Thread speechThread = new Thread(new ASRControlLoop(), "ARS Thread");

    private static final String ACOUSTIC_MODEL = "resource:/edu.cmu.sphinx.models.en-us/en-us";
    private static final String DICTIONARY_PATH = "resource:/edu.cmu.sphinx.models.en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH = "resource:/com.jetbrains.idear/grammars";

    private static final Logger logger = Logger.getLogger(ASRServiceImpl.class.getSimpleName());

    private CustomLiveSpeechRecognizer recognizer;
    private ConfigurationManager configurationManager;
    private Robot robot;

    private final AtomicReference<Status> status = new AtomicReference<>(Status.INIT);

    public void init() {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);

        configuration.setGrammarName("dialog");

        try {
            recognizer = new CustomLiveSpeechRecognizer(configuration);
            recognizer.setMasterGain(MASTER_GAIN);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't initialize speech recognizer:", e);
        }

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // Fire up control-loop

        speechThread.start();
    }

    public void dispose() {
        // Deactivate in the first place, therefore actually
        // prevent activation upon the user-input
        deactivate();
        terminate();
    }

    private void terminate() {
        recognizer.stopRecognition();
    }

    private Status setStatus(Status s) {
        return status.getAndSet(s);
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public Status activate() {
        if (getStatus() == Status.ACTIVE)
            return Status.ACTIVE;

        if (getStatus() == Status.INIT) {
            // Cold start prune cache
            recognizer.startRecognition(true);
        }

        return setStatus(Status.ACTIVE);
    }

    @Override
    public Status deactivate() {
        return setStatus(Status.INACTIVE);
    }

    private class ASRControlLoop implements Runnable {

        public static final String FUCK = "fuck";
        public static final String OPEN = "open";
        public static final String SETTINGS = "settings";
        public static final String RECENT = "recent";
        public static final String TERMINAL = "terminal";
        public static final String FOCUS = "focus";
        public static final String EDITOR = "editor";
        public static final String PROJECT = "project";
        public static final String SELECTION = "selection";
        public static final String EXPAND = "expand";
        public static final String SHRINK = "shrink";
        public static final String PRESS = "press";
        public static final String DELETE = "delete";
        public static final String ENTER = "enter";
        public static final String ESCAPE = "escape";
        public static final String TAB = "tab";
        public static final String UNDO = "undo";
        public static final String NEXT = "next";
        public static final String LINE = "line";
        public static final String PAGE = "page";
        public static final String METHOD = "method";
        public static final String PREVIOUS = "previous";
        public static final String INSPECT_CODE = "inspect code";

        @Override
        public void run() {
            while (!isTerminated()) {
                String result = null;

                // This blocks on a recognition result
                if (isActive()) {
                    result = getResultFromRecognizer();
                }

                // This may happen 10-15 seconds later
                if (isActive() && result != null) {
                    logger.log(Level.INFO, "Recognized: " + result);
                    applyAction(result);
                }
            }
        }

        private String getResultFromRecognizer() {
            SpeechResult result = recognizer.getResult();
            
            logger.info("Recognized:    ");
            logger.info("\tTop H:       " + result.getResult() + " / " + result.getResult().getBestToken() + " / " + result.getResult().getBestPronunciationResult());
            logger.info("\tTop 3H:      " + result.getNbest(3));
            
            return result.getHypothesis();
        }

        private void applyAction(String result) {
            if (result.equals(FUCK)) {
                invokeAction(IdeActions.ACTION_UNDO);
            }
            
            else if (result.startsWith("open")) {
                if (result.endsWith("settings")) {
                    invokeAction(IdeActions.ACTION_SHOW_SETTINGS);
                } else if (result.endsWith("recent")) {
                    invokeAction(IdeActions.ACTION_RECENT_FILES);
                } else if (result.endsWith("terminal")) {
                    pressKeystroke(KeyEvent.VK_ALT, KeyEvent.VK_F12);
                }
            }

            else if (result.startsWith("focus")) {
                if (result.endsWith("editor")) {
                    pressKeystroke(KeyEvent.VK_ESCAPE);
                } else if (result.endsWith("project")) {
                    pressKeystroke(KeyEvent.VK_ALT, KeyEvent.VK_1);
                }
            }

            else if (result.endsWith("selection")) {
                if (result.startsWith("expand")) {
                    pressKeystroke(KeyEvent.VK_CONTROL, KeyEvent.VK_W);
                } else if (result.startsWith("shrink")) {
                    pressKeystroke(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_W);
                }
            }

            else if (result.startsWith("press")) {
                if (result.endsWith("delete")) {
                    pressKeystroke(KeyEvent.VK_DELETE);
                } else if (result.endsWith("enter")) {
                    pressKeystroke(KeyEvent.VK_ENTER);
                } else if (result.endsWith("escape")) {
                    pressKeystroke(KeyEvent.VK_ESCAPE);
                } else if (result.endsWith("tab")) {
                    pressKeystroke(KeyEvent.VK_TAB);
                } else if (result.endsWith("undo")) {
                    pressKeystroke(KeyEvent.VK_CONTROL, KeyEvent.VK_Z);
                }
            }

            else if (result.startsWith("next")) {
                if (result.endsWith("line")) {
                    pressKeystroke(KeyEvent.VK_DOWN);
                } else if (result.endsWith("page")) {
                    pressKeystroke(KeyEvent.VK_PAGE_DOWN);
                } else if (result.endsWith("method")) {
                    pressKeystroke(KeyEvent.VK_ALT, KeyEvent.VK_DOWN);
                }
            }

            else if (result.startsWith("previous")) {
                if (result.endsWith("line")) {
                    pressKeystroke(KeyEvent.VK_UP);
                } else if (result.endsWith("page")) {
                    pressKeystroke(KeyEvent.VK_PAGE_UP);
                } else if (result.endsWith("method")) {
                    pressKeystroke(KeyEvent.VK_ALT, KeyEvent.VK_UP);
                }
            }

            else if (result.startsWith("extract this")) {
                if (result.endsWith("method")) {
                    pressKeystroke(KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_M);
                } else if (result.endsWith("parameter")) {
                    pressKeystroke(KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_P);
                }
            }

            else if (result.startsWith("inspect code")) {
                pressKeystroke(KeyEvent.VK_ALT, KeyEvent.VK_SHIFT, KeyEvent.VK_I);
            }

            else if (result.startsWith("speech pause")) {
                pauseSpeech();
            }

            else if (result.startsWith("okay google")) {
                fireGoogleSearch();
            }
        }

        private void fireGoogleSearch() {
            GoogleService gs = ServiceManager.getService(GoogleService.class);
            String searchQuery = gs.getTextForLastUtterance();

            if (searchQuery == null || searchQuery.isEmpty()) {
                return;
            }

            ServiceManager.getService(TTSService.class).say("I think you said " + searchQuery + ", searching Google now");
            gs.searchGoogle(searchQuery);
        }

        private void pauseSpeech() {
            String result;
            while (isActive()) {
                result = getResultFromRecognizer();
                if (result.equals("speech resume")) {
                    break;
                }
            }
        }
    }

    private boolean isTerminated() {
        return getStatus() == Status.TERMINATED;
    }

    private boolean isActive() {
        return getStatus() == Status.ACTIVE;
    }

    private void pressKeystroke(final int... keys) {
        for (int key : keys) {
            robot.keyPress(key);
        }

        for (int key : keys) {
            robot.keyRelease(key);
        }
    }

    private void invokeAction(final String action) {
        try {
            EventQueue.invokeAndWait(() -> {
                AnAction anAction = ActionManager.getInstance().getAction(action);
                anAction.actionPerformed(new AnActionEvent(null,
                        DataManager.getInstance().getDataContext(),
                        ActionPlaces.UNKNOWN, anAction.getTemplatePresentation(),
                    ActionManager.getInstance(), 0));
            });
        } catch (InterruptedException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Could not invoke action:", e);
        }
    }

    // This is for testing purposes solely
    public static void main(String[] args) {
        ASRServiceImpl asrService = new ASRServiceImpl();
        asrService.init();
        asrService.activate();
    }
}
