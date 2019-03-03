package test.lines.undeground.lines98

import android.graphics.Color
import android.graphics.Color.argb
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.jetbrains.anko.textColor

import org.jetbrains.anko.toast

class ButtonManager(val button: Button, var color: Int = Color.WHITE, private var _hinted: Boolean = false) {
    var hinted: Boolean
        get() = _hinted
        set(value) {
            if (value) {
                button.textColor = argb(0xFF, 0xd3, 0xd3, 0xd3)
                button.isClickable = false
            } else {
                button.textColor = argb(0xFF, 0x00, 0x00, 0x00)
                button.isClickable = !button.text.isEmpty()
            }
            _hinted = value
        }
}

class MainActivity : AppCompatActivity() {

    val buttonsManagersList = arrayListOf<ButtonManager>()
    var hintedValuesForNextStep = arrayListOf<Int>()
    private val ITEMS_IN_ROW = 5
    private val CREATED_ITEMS_PER_STEP = 3
    private val SIMILAR_ITEMS_TO_CLEAR = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonsTable = findViewById<TableLayout>(R.id.buttons)

        for (row in 0..(ITEMS_IN_ROW - 1)) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f
                )

            for (column in 0..(ITEMS_IN_ROW - 1)) {
                val button = Button(this)
                button.id = row * ITEMS_IN_ROW + column
                button.setBackgroundResource(R.drawable.empty_shape)
                val buttonManager = ButtonManager(button)


                button.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f
                )

                button.setOnClickListener(object: View.OnClickListener {
                    override fun onClick(v: View?) {
                        val buttonManager = buttonsManagersList[button.id]


                        val background = button.background
                        if (background is GradientDrawable)
                        when (buttonManager.color) {
                            Color.GREEN -> {
                                return_desk_neutral_state(buttonsManagersList)
                            }

                            Color.BLUE -> {
                                val buttonForSwap = findPickedForSwapButton(buttonsManagersList)
                                button.text = buttonForSwap.text
                                buttonForSwap.text = ""
                                return_desk_neutral_state(buttonsManagersList)

                                generateStepsResults(button.id)
                            }

                            else -> {
                                if (!buttonManager.hinted) {
                                    background.setColor(argb(0xAA, 0x00, 0xFF, 0x00))
                                    buttonManager.color = Color.GREEN
                                    for (buttonManager in buttonsManagersList) {
                                        val but = buttonManager.button
                                        if (!but.text.isEmpty() && but.id != button.id && !buttonManager.hinted)
                                            but.isClickable = false
                                    }
                                    setClickableButtons(buttonsManagersList, button.id)
                                }
                            }
                        }
                    }

                    fun setClickableButtons(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int) {
                        fun setButtonHintToClicked(buttonManager: ButtonManager) {
                            val button = buttonManager.button
                            if ((button.text.isEmpty() || buttonManager.hinted) && buttonManager.color != Color.BLUE) {
                                val background = button.background
                                if (background is GradientDrawable)
                                    background.setColor(argb(0xFF, 0x00, 0x00, 0xFF))
                                buttonManager.color = Color.BLUE
                                button.isClickable=true
                                setClickableButtons(buttonsManagers, button.id)
                            }
                        }

                        if (pickedButtonIndex >= ITEMS_IN_ROW) {
                            val buttonManager = buttonsManagers[pickedButtonIndex - ITEMS_IN_ROW]
                            setButtonHintToClicked(buttonManager)
                        }

                        if (pickedButtonIndex <= (ITEMS_IN_ROW * (ITEMS_IN_ROW - 1)) - 1) {
                            val buttonManager = buttonsManagers[pickedButtonIndex + ITEMS_IN_ROW]
                            setButtonHintToClicked(buttonManager)
                        }

                        if ((pickedButtonIndex + 1) % ITEMS_IN_ROW >
                                pickedButtonIndex % ITEMS_IN_ROW && pickedButtonIndex < (ITEMS_IN_ROW * ITEMS_IN_ROW) - 1) {
                            val buttonManager = buttonsManagers[pickedButtonIndex + 1]
                            setButtonHintToClicked(buttonManager)
                        }

                        if (pickedButtonIndex % ITEMS_IN_ROW >
                                (pickedButtonIndex - 1) % ITEMS_IN_ROW && pickedButtonIndex > 0) {
                            val buttonManager = buttonsManagers[pickedButtonIndex - 1]
                            setButtonHintToClicked(buttonManager)
                        }
                    }

                    fun findPickedForSwapButton(buttonsManagers: ArrayList<ButtonManager>): Button {
                        for (manager in buttonsManagers)
                            if (manager.color == Color.GREEN)
                                return manager.button
                        throw Exception("Button not found")
                    }

                    fun return_desk_neutral_state(buttonsManagers: ArrayList<ButtonManager>) {
                        for (manager in buttonsManagers) {
                            val but = manager.button
                            val background = but.background
                            if (background is GradientDrawable)
                                background.setColor(argb(0xFF, 0xFF, 0xFF, 0xFF))
                            manager.color = Color.WHITE
                            but.isClickable = !but.text.isEmpty()
                        }
                    }

                    fun generateStepsResults(pickedButtonIndex: Int) {
                        val extraItems = convertHintedToNormal(hintedValuesForNextStep)
                        extraItems.add(pickedButtonIndex)
                        freeSomeSpaces(buttonsManagersList, extraItems)

                        if (isGameOver(buttonsManagersList))
                            toast("Game is over :\\")
                        else {
                            hintedValuesForNextStep = generateRandomItems(CREATED_ITEMS_PER_STEP)
                        }
                    }

                    fun freeSomeSpaces(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndexes: ArrayList<Int>) {

                        fun getHorizontalRangeForClear(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int): IntProgression {
                            val checkedNumber = buttonsManagers[pickedButtonIndex].button.text
                            val leftIndex = (pickedButtonIndex / ITEMS_IN_ROW) * ITEMS_IN_ROW
                            val rightNotIncludedIndex = ((pickedButtonIndex / ITEMS_IN_ROW) + 1) * ITEMS_IN_ROW

                            var similarItems = 0
                            var clearingHorizontalRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (leftIndex..(rightNotIncludedIndex - 1)))
                                if (buttonsManagers[index].button.text == checkedNumber) {
                                    if (similarItems == 0) {
                                        firstSimilarItem = index
                                    }

                                    similarItems++
                                } else {
                                    if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                        clearingHorizontalRange = (firstSimilarItem..(index-1))
                                    firstSimilarItem = -1
                                    similarItems = 0
                                }
                            if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                clearingHorizontalRange = (firstSimilarItem..(rightNotIncludedIndex-1))

                            return clearingHorizontalRange
                        }

                        fun getVerticalRangeForClear(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int): IntProgression {
                            val checkedNumber = buttonsManagers[pickedButtonIndex].button.text
                            val uppTableIndex = pickedButtonIndex % ITEMS_IN_ROW
                            val downTableIndex = pickedButtonIndex % ITEMS_IN_ROW + (ITEMS_IN_ROW * (ITEMS_IN_ROW - 1))

                            var similarItems = 0
                            var clearingVerticalRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (uppTableIndex..downTableIndex step ITEMS_IN_ROW))
                                if (buttonsManagers[index].button.text == checkedNumber) {
                                    if (similarItems == 0) {
                                        firstSimilarItem = index
                                    }

                                    similarItems++
                                } else {
                                    if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                        clearingVerticalRange = (firstSimilarItem..index step ITEMS_IN_ROW)
                                    firstSimilarItem = -1
                                    similarItems = 0
                                }
                            if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                clearingVerticalRange = (firstSimilarItem..downTableIndex step ITEMS_IN_ROW)

                            return clearingVerticalRange
                        }

                        fun getLeftRightDiagRangeForClear(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int): IntProgression {
                            val checkedNumber = buttonsManagers[pickedButtonIndex].button.text

                            val leftShift = pickedButtonIndex % ITEMS_IN_ROW
                            val upShift = pickedButtonIndex / ITEMS_IN_ROW

                            var leftUpTableIndex = pickedButtonIndex
                            var tempLeftShift = leftShift
                            var tempUpShift = upShift
                            while (tempLeftShift > 0 && tempUpShift > 0) {
                                tempLeftShift--
                                tempUpShift --
                                leftUpTableIndex -= (ITEMS_IN_ROW + 1)
                            }

                            var topDownTableIndex = pickedButtonIndex
                            tempLeftShift = leftShift
                            tempUpShift = upShift
                            while (tempLeftShift < (ITEMS_IN_ROW-1) && tempUpShift < ITEMS_IN_ROW-1) {
                                tempLeftShift++
                                tempUpShift ++
                                topDownTableIndex += (ITEMS_IN_ROW + 1)
                            }

                            var similarItems = 0
                            var clearingLeftRightDiagRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (leftUpTableIndex..topDownTableIndex step ITEMS_IN_ROW+1))
                                if (buttonsManagers[index].button.text == checkedNumber) {
                                    if (similarItems == 0) {
                                        firstSimilarItem = index
                                    }

                                    similarItems++
                                } else {
                                    if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                        clearingLeftRightDiagRange = (firstSimilarItem..index step ITEMS_IN_ROW+1)
                                    firstSimilarItem = -1
                                    similarItems = 0
                                }
                            if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                clearingLeftRightDiagRange = (firstSimilarItem..topDownTableIndex step ITEMS_IN_ROW+1)

                            return clearingLeftRightDiagRange
                        }

                        fun getRightLeftDiagRangeForClear(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int): IntProgression {
                            val checkedNumber = buttonsManagers[pickedButtonIndex].button.text

                            val leftShift = pickedButtonIndex % ITEMS_IN_ROW
                            val upShift = pickedButtonIndex / ITEMS_IN_ROW

                            var rightUpTableIndex = pickedButtonIndex
                            var tempLeftShift = leftShift
                            var tempUpShift = upShift
                            while (tempLeftShift < (ITEMS_IN_ROW-1) && tempUpShift > 0) {
                                tempLeftShift++
                                tempUpShift --
                                rightUpTableIndex -= (ITEMS_IN_ROW - 1)
                            }

                            var leftDownTableIndex = pickedButtonIndex
                            tempLeftShift = leftShift
                            tempUpShift = upShift
                            while (tempLeftShift > 0 && tempUpShift < ITEMS_IN_ROW-1) {
                                tempLeftShift--
                                tempUpShift++
                                leftDownTableIndex += (ITEMS_IN_ROW - 1)
                            }

                            toast((rightUpTableIndex..leftDownTableIndex step ITEMS_IN_ROW-1).toString())

                            var similarItems = 0
                            var clearingRightLeftDiagRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (rightUpTableIndex..leftDownTableIndex step ITEMS_IN_ROW-1))
                                if (buttonsManagers[index].button.text == checkedNumber) {
                                    if (similarItems == 0) {
                                        firstSimilarItem = index
                                    }

                                    similarItems++
                                } else {
                                    if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                        clearingRightLeftDiagRange = (firstSimilarItem..index step ITEMS_IN_ROW-1)
                                    firstSimilarItem = -1
                                    similarItems = 0
                                }
                            if (similarItems >= SIMILAR_ITEMS_TO_CLEAR)
                                clearingRightLeftDiagRange = (firstSimilarItem..leftDownTableIndex step ITEMS_IN_ROW-1)

                            return clearingRightLeftDiagRange
                        }


                        val clearRanges = arrayListOf<IntProgression>()
                        for (pickedIndex in pickedButtonIndexes) {
                            val clearingHorizontalRange = getHorizontalRangeForClear(buttonsManagers, pickedIndex)
                            val clearingVerticalRange = getVerticalRangeForClear(buttonsManagers, pickedIndex)

                            val clearingLeftRightDiagRange = getLeftRightDiagRangeForClear(buttonsManagers, pickedIndex)
                            val clearingRightLeftDiagRange = getRightLeftDiagRangeForClear(buttonsManagers, pickedIndex)
                            if (clearingHorizontalRange.count() != 0) clearRanges.add(clearingHorizontalRange)
                            if (clearingVerticalRange.count() != 0) clearRanges.add(clearingVerticalRange)
                            if (clearingLeftRightDiagRange.count() != 0) clearRanges.add(clearingLeftRightDiagRange)
                            if (clearingRightLeftDiagRange.count() != 0) clearRanges.add(clearingRightLeftDiagRange)

                        }
                        if (clearRanges.isNotEmpty()) {
                            for (range in clearRanges)
                                for (index in range)
                                    buttonsManagers[index].button.text = ""
                        }
                    }

                    fun isGameOver(buttonsManagers: ArrayList<ButtonManager>): Boolean {
                        var freeButtonsCounter = 0
                        for (manager in buttonsManagers) {
                            if (manager.button.text.isEmpty())
                                freeButtonsCounter++
                        }
                        return freeButtonsCounter < CREATED_ITEMS_PER_STEP
                    }
                })
                button.isClickable=false
                tableRow.addView(button)
                buttonsManagersList.add(buttonManager)
            }
            buttonsTable.addView(tableRow)
        }

        val hintedButtonsIds = generateRandomItems(CREATED_ITEMS_PER_STEP)
        convertHintedToNormal(hintedButtonsIds)
        hintedValuesForNextStep = generateRandomItems(CREATED_ITEMS_PER_STEP)
    }

    fun generateRandomItems(itemsToCreate: Int) : ArrayList<Int> {
        val usedIds = arrayListOf<Int>()
        for (item in 1..itemsToCreate) {
            var unique = false
            while (!unique) {
                val randomButtonId = (0..(ITEMS_IN_ROW * ITEMS_IN_ROW - 1)).shuffled().first()
                val button = buttonsManagersList[randomButtonId].button
                if (button.text.isEmpty()) {
                    button.text = item.toString()
                    unique = true
                    buttonsManagersList[randomButtonId].hinted = true
                    usedIds.add(randomButtonId)
                }
            }
        }
        return usedIds
    }

    fun convertHintedToNormal(hintedButtonsIds : ArrayList<Int>) : ArrayList<Int>{
        val items = arrayListOf<Int>()
        var changedButtonsCount = 0
        for (id in hintedButtonsIds)
            if (buttonsManagersList[id].hinted) {
                buttonsManagersList[id].hinted = false
                items.add(id)
                changedButtonsCount++
            }

        if (changedButtonsCount < CREATED_ITEMS_PER_STEP) {
            val extraItems = generateRandomItems(CREATED_ITEMS_PER_STEP - changedButtonsCount)
            for (itemId in extraItems)
                buttonsManagersList[itemId].hinted = false
            extraItems.addAll(items)
            return extraItems
        }
        return items
    }
}
