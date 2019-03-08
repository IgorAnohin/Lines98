package test.lines.undeground.lines98

import android.graphics.Color
import android.graphics.Color.argb
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.jetbrains.anko.padding

import org.jetbrains.anko.toast
import android.view.Gravity


class ButtonManager(val button: Button,
                    val imageView: ImageView,
                    var color: Int = Color.WHITE,
                    private var _hinted: Boolean = false) {
    companion object {
        val NO_RESOURCE = 0
        val imagesResourcesIdMap =  mapOf(
                1 to R.drawable.circle_black,
                2 to R.drawable.circle_yellow,
                3 to R.drawable.circle_pink,
                4 to R.drawable.circle_orange)
    }


    private var _resource = NO_RESOURCE

    var resource: Int
        get() = _resource
        set(value) {
            if (value == NO_RESOURCE) {
                button.isClickable = false
                imageView.visibility = ImageView.GONE
                button.text = ""

            } else {
                button.isClickable = true
                imageView.visibility = ImageView.VISIBLE
                val changeImRunnable: Runnable = object : Runnable {
                    override fun run() {
                        val picture = imagesResourcesIdMap[resource]
                        if (picture != null)
                            imageView.setBackgroundResource(picture)
                        imageView.requestLayout()
                    }
                }

                imageView.post(changeImRunnable)

            }

            _resource = value
        }


    var hinted: Boolean
        get() = _hinted
        set(value) {
            button.isClickable = !value
            _hinted = value
        }

    fun makeButtonPicked() {
        val background = button.background
        if (background is GradientDrawable)
            background.setColor(argb(0xAA, 0x00, 0xFF, 0x00))
        this.color = Color.GREEN
    }
}

class MainActivity : AppCompatActivity() {

    val buttonsManagersList = arrayListOf<ButtonManager>()
    var hintedValuesForNextStep = arrayListOf<Int>()
    var hintedImagesPlaces = arrayListOf<ImageView>()
    private val ITEMS_IN_ROW = 8
    private val CREATED_ITEMS_PER_STEP = 3
    private val SIMILAR_ITEMS_TO_CLEAR = 5
    private val RESOURCES_IN_GAME = ButtonManager.imagesResourcesIdMap.size

    // Width always is less, than hight
    private var BUTTON_FRAME_WIDTH = 0
    private var SCREEN_WIDTH = 0
    private var HINT_MARGIN_PERC = 0
    private var NORMAL_MARGIN_PERC = 0

    fun getScreenWidth() : Int {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun setButtonsParameters(screenWidth: Int) {
        BUTTON_FRAME_WIDTH = screenWidth / ITEMS_IN_ROW
        HINT_MARGIN_PERC = (BUTTON_FRAME_WIDTH * 0.25).toInt()
        NORMAL_MARGIN_PERC = (BUTTON_FRAME_WIDTH * 0.1).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SCREEN_WIDTH = getScreenWidth()
        setButtonsParameters(SCREEN_WIDTH)


        val buttonsTable = findViewById<TableLayout>(R.id.buttons)

        for (row in 0..(ITEMS_IN_ROW - 1)) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = LinearLayout.LayoutParams(
                    SCREEN_WIDTH,
                    BUTTON_FRAME_WIDTH
                )
            tableRow.gravity = Gravity.CENTER

            for (column in 0..(ITEMS_IN_ROW - 1)) {
                val framelayout = FrameLayout(this)
                framelayout.layoutParams = TableRow.LayoutParams(
                        BUTTON_FRAME_WIDTH,
                        BUTTON_FRAME_WIDTH
                )
                val id = row * ITEMS_IN_ROW + column
//                framelayout.id = id

                val button = Button(this)
                button.id = id
                button.setBackgroundResource(R.drawable.empty_shape)
                button.layoutParams = FrameLayout.LayoutParams(
                        BUTTON_FRAME_WIDTH,
                        BUTTON_FRAME_WIDTH
                )
                val background = button.background
                if (background is GradientDrawable)
                    background.setColor(argb(0x00, 0xFF, 0xFF, 0xFF))

                val imageView = ImageView(this)
                imageView.setBackgroundResource(R.drawable.circular_black_64dp)
                val layout = FrameLayout.LayoutParams(
                   FrameLayout.LayoutParams.MATCH_PARENT,
                   FrameLayout.LayoutParams.MATCH_PARENT
                )
                layout.setMargins(NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC)
                imageView.layoutParams = layout
                imageView.elevation = 9.0f
                imageView.scaleType = ImageView.ScaleType.CENTER
                imageView.visibility = ImageView.GONE

                framelayout.addView(button)
                framelayout.addView(imageView)
                val buttonManager = ButtonManager(button, imageView)



                button.setOnClickListener(object: View.OnClickListener {
                    override fun onClick(v: View?) {
                        val imBut = findViewById<ImageButton>(R.id.test_button)
                        imBut.padding = 20
                        val buttonManager = buttonsManagersList[id]
                        when (buttonManager.color) {
                            Color.GREEN -> {
                                returnDeskToNeutralState(buttonsManagersList)
                            }

                            Color.BLUE -> {
                                val mangerForSwap = findPickedForSwapButton(buttonsManagersList)
                                buttonManager.resource = mangerForSwap.resource
                                mangerForSwap.resource = ButtonManager.NO_RESOURCE
                                returnDeskToNeutralState(buttonsManagersList)
                                generateStepsResults(id)
                            }

                            else -> {
                                if (!buttonManager.hinted) {
                                    buttonManager.makeButtonPicked()
                                    prepareDeskForPicking(buttonsManagersList, id)
                                }
                            }
                        }
                    }

                    fun prepareDeskForPicking(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int) {
                        makeButtonsUnclickable(buttonsManagers, pickedButtonIndex)
                        setClickableButtons(buttonsManagers, pickedButtonIndex)
                    }

                    fun makeButtonsUnclickable(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int) {
                        for (buttonManager in buttonsManagers) {
                            val but = buttonManager.button
                            if (but.id != button.id)
                                but.isClickable = false
                        }
                    }

                    fun setClickableButtons(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int) {
                        fun setButtonHintToClicked(buttonManager: ButtonManager) {
                            val button = buttonManager.button
                            if ((buttonManager.resource == ButtonManager.NO_RESOURCE || buttonManager.hinted)
                                 && buttonManager.color != Color.BLUE) {
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

                    fun findPickedForSwapButton(buttonsManagers: ArrayList<ButtonManager>): ButtonManager {
                        for (manager in buttonsManagers)
                            if (manager.color == Color.GREEN)
                                return manager
                        throw Exception("Button not found")
                    }

                    fun returnDeskToNeutralState(buttonsManagers: ArrayList<ButtonManager>) {
                        for (manager in buttonsManagers) {
                            val but = manager.button
                            val background = but.background
                            if (background is GradientDrawable)
                                background.setColor(argb(0x00, 0xFF, 0xFF, 0xFF))
                            manager.color = Color.WHITE
                            but.isClickable = manager.resource != ButtonManager.NO_RESOURCE
                        }
                    }

                    fun generateStepsResults(pickedButtonIndex: Int) {
                        for (value in hintedValuesForNextStep) {
                            val buttonManager = buttonsManagersList[value]
                            val imView = buttonManager.imageView
                            val myRunnableLow: Runnable = object : Runnable {
                                override fun run() {
                                    val layout = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                    layout.setMargins(NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC, NORMAL_MARGIN_PERC)
                                    imView.layoutParams = layout

                                    imView.requestLayout()
                                    imView.invalidate()
                                    imView.refreshDrawableState()
                                    imView.forceLayout()
                                    imView.requestLayout()
                                }
                            }

                            imView.post(myRunnableLow)
                        }
                        val extraItems = convertHintedToNormal(hintedValuesForNextStep)
                        extraItems.add(pickedButtonIndex)
                        freeSomeSpaces(buttonsManagersList, extraItems)

                        if (isGameOver(buttonsManagersList))
                            toast("Game is over :\\")
                        else {
                            hintedValuesForNextStep = generateRandomItems(CREATED_ITEMS_PER_STEP)
                            hintedValuesForNextStep.forEachIndexed { index, element ->
                                val buttonManager = buttonsManagersList[element]
                                val imView = buttonManager.imageView
                                val changeMarginsRunnable: Runnable = object : Runnable {
                                    override fun run() {
                                        val layout = FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                                FrameLayout.LayoutParams.MATCH_PARENT
                                        )
                                        layout.setMargins(HINT_MARGIN_PERC, HINT_MARGIN_PERC, HINT_MARGIN_PERC, HINT_MARGIN_PERC)
                                        imView.layoutParams = layout

                                        imView.requestLayout()
                                        imView.invalidate()
                                        imView.refreshDrawableState()
                                        imView.forceLayout()
                                        imView.requestLayout()
                                    }
                                }

                                imView.post(changeMarginsRunnable)
                                val drawableRes = ButtonManager.imagesResourcesIdMap[buttonManager.resource]
                                if (drawableRes != null)
                                    hintedImagesPlaces[index].setBackgroundResource(drawableRes)
                            }
                        }
                    }

                    fun freeSomeSpaces(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndexes: ArrayList<Int>) {

                        fun getHorizontalRangeForClear(buttonsManagers: ArrayList<ButtonManager>, pickedButtonIndex: Int): IntProgression {
                            val checkedNumber = buttonsManagers[pickedButtonIndex].resource
                            val leftIndex = (pickedButtonIndex / ITEMS_IN_ROW) * ITEMS_IN_ROW
                            val rightNotIncludedIndex = ((pickedButtonIndex / ITEMS_IN_ROW) + 1) * ITEMS_IN_ROW

                            var similarItems = 0
                            var clearingHorizontalRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (leftIndex..(rightNotIncludedIndex - 1)))
                                if (buttonsManagers[index].resource == checkedNumber) {
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
                            val checkedNumber = buttonsManagers[pickedButtonIndex].resource
                            val uppTableIndex = pickedButtonIndex % ITEMS_IN_ROW
                            val downTableIndex = pickedButtonIndex % ITEMS_IN_ROW + (ITEMS_IN_ROW * (ITEMS_IN_ROW - 1))

                            var similarItems = 0
                            var clearingVerticalRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (uppTableIndex..downTableIndex step ITEMS_IN_ROW))
                                if (buttonsManagers[index].resource == checkedNumber) {
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
                            val checkedNumber = buttonsManagers[pickedButtonIndex].resource

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
                                if (buttonsManagers[index].resource == checkedNumber) {
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
                            val checkedNumber = buttonsManagers[pickedButtonIndex].resource

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

                            var similarItems = 0
                            var clearingRightLeftDiagRange : IntProgression = IntRange.EMPTY
                            var firstSimilarItem = -1
                            for (index in (rightUpTableIndex..leftDownTableIndex step ITEMS_IN_ROW-1))
                                if (buttonsManagers[index].resource == checkedNumber) {
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
                                    buttonsManagers[index].resource = 0
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
                tableRow.addView(framelayout)
                buttonsManagersList.add(buttonManager)
            }
            buttonsTable.addView(tableRow)
        }


//        <ImageView
//        android:layout_width="match_parent"
//        android:layout_height="match_parent"
//        android:scaleType="center"
//        android:src="@drawable/circular_black_64dp"
//        android:layout_weight="1"/>
        val hintedLayout = findViewById<LinearLayout>(R.id.next_turn_circles)
        // Magic formuls for free spaces
        val freeHintSpaces = (ITEMS_IN_ROW - CREATED_ITEMS_PER_STEP) * BUTTON_FRAME_WIDTH / (CREATED_ITEMS_PER_STEP + 1)
            val startfreeSpace = ImageView(this)
            startfreeSpace.layoutParams = LinearLayout.LayoutParams(
                    freeHintSpaces,
                    BUTTON_FRAME_WIDTH
            )
            hintedLayout.addView(startfreeSpace)
        for (placeForHintedId in 1..CREATED_ITEMS_PER_STEP) {
            val hintedDownImage = ImageView(this)
            val layout = LinearLayout.LayoutParams(
                    BUTTON_FRAME_WIDTH,
                    BUTTON_FRAME_WIDTH
            )
            hintedDownImage.layoutParams = layout
            hintedDownImage.scaleType = ImageView.ScaleType.CENTER
            hintedDownImage.setBackgroundResource(R.drawable.circle_pink)
            hintedLayout.addView(hintedDownImage)
            hintedImagesPlaces.add(hintedDownImage)


            val freeSpace = ImageView(this)
            freeSpace.layoutParams = LinearLayout.LayoutParams(
                    freeHintSpaces,
                    BUTTON_FRAME_WIDTH
            )
            hintedLayout.addView(freeSpace)
        }

        val hintedButtonsIds = generateRandomItems(CREATED_ITEMS_PER_STEP)
        convertHintedToNormal(hintedButtonsIds)
        hintedValuesForNextStep = generateRandomItems(CREATED_ITEMS_PER_STEP)
        hintedValuesForNextStep.forEachIndexed { index, element ->
            val buttonManager = buttonsManagersList[element]
            val imView = buttonManager.imageView
            val changeMarginsRunnable: Runnable = object : Runnable {
                override fun run() {
                    val layout = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    layout.setMargins(HINT_MARGIN_PERC, HINT_MARGIN_PERC, HINT_MARGIN_PERC, HINT_MARGIN_PERC)
                    imView.layoutParams = layout

                    imView.requestLayout()
                    imView.invalidate()
                    imView.refreshDrawableState()
                    imView.forceLayout()
                    imView.requestLayout()
                }
            }

            imView.post(changeMarginsRunnable)
            val drawableRes = ButtonManager.imagesResourcesIdMap[buttonManager.resource]
            if (drawableRes != null)
                hintedImagesPlaces[index].setBackgroundResource(drawableRes)
        }
    }

    fun generateRandomItems(itemsToCreate: Int) : ArrayList<Int> {
        val usedIds = arrayListOf<Int>()
        for (item in 1..itemsToCreate) {
            var unique = false
            while (!unique) {
                val randomId = (0..(ITEMS_IN_ROW * ITEMS_IN_ROW - 1)).shuffled().first()
                val buttonManager = buttonsManagersList[randomId]
                val resource = buttonManager.resource
                if (resource == ButtonManager.NO_RESOURCE) {
                    buttonManager.resource = (1..RESOURCES_IN_GAME).shuffled().first()
                    unique = true
                    buttonsManagersList[randomId].hinted = true
                    usedIds.add(randomId)
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
