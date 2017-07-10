package com.johnlindquist.acejump.keycommands

import com.johnlindquist.acejump.AceFinder
import com.johnlindquist.acejump.AceJumper
import com.johnlindquist.acejump.AceKeyUtil
import com.johnlindquist.acejump.ui.SearchBox
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.event.ChangeListener

class DefaultKeyCommand(val searchBox: SearchBox, val aceFinder: AceFinder, val aceJumper: AceJumper, val textAndOffsetHash: HashMap<String, Int>) : AceKeyCommand() {
    override fun execute(keyEvent: KeyEvent) {
        val keyChar: Char = keyEvent.keyChar

        //fixes the delete bug
        if (keyChar == '\b') return
//        println(searchBox.isSearchEnabled)
        //Find or jump
        if (searchBox.isSearchEnabled) {
            //Find
            aceFinder.addResultsReadyListener(ChangeListener { p0 ->
                eventDispatcher?.getMulticaster()?.stateChanged(p0)
                //                    eventDispatcher?.getMulticaster()?.stateChanged(ChangeEvent(toString()))
            })

            if(searchBox.text == null) {
                searchBox.text = " "
            }
            aceFinder.findText(searchBox.text!!, false)
            searchBox.disableSearch()
        } else {
            //Jump to offset!
            var char = AceKeyUtil.getLowerCaseStringFromChar(keyChar)
            if(char == " ") return

            if(aceFinder.firstChar != ""){
                char = aceFinder.firstChar + char
                aceFinder.firstChar = ""
            }
            val offset = textAndOffsetHash[char]

            if (offset != null) {
                searchBox.popupContainer?.cancel()
              if (keyEvent.isShiftDown && !keyEvent.isMetaDown) {
                    aceJumper.setSelectionFromCaretToOffset(offset)
                    aceJumper.moveCaret(offset)
                } else {
                    aceJumper.moveCaret(offset)
                }

                if (aceFinder.isTargetMode) {
                    aceJumper.selectWordAtCaret()
                }
            }
            else if(textAndOffsetHash.size > 25){
                aceFinder.firstChar = char!!
            }

        }

    }


}