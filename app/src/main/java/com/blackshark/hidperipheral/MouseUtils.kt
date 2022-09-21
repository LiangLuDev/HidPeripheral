package com.blackshark.hidperipheral

import android.view.MotionEvent
import java.util.*
import kotlin.math.max

class MouseUtils {
    private var Xpad = 0f
    private var Ypad = 0f
    private val Xmus = 0f
    private var Ymus = 0f
    private var maxPointerCount = 0
    private var actionDownTime_Pad: Long = 0
    var rate = 1f
    var leftbtnUped = true //左键是否抬起
    var leftUped = true //pad双击模拟左键是否抬起
    var rightbtnUped = true //右键是否抬起
    var midbtnUped = true //中键是否抬起
    var virtureClickTask: TimerTask? = null
    private val mParam1: String? = null
    private val mParam2: String? = null
    fun mouseLeft(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            HidConsts.leftBtnUp()
            leftbtnUped = true
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            HidConsts.leftBtnDown()
            leftbtnUped = false
        }
        return true
    }

    fun mouseMove(event: MotionEvent): Boolean {
        val now:Long
        val dis:Long
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                now = Date().time
                dis = now - actionDownTime_Pad
                if (dis in 50..150 && leftbtnUped) {
                    if (virtureClickTask != null) {
                        virtureClickTask!!.cancel()
                    }
                    HidConsts.leftBtnDown()
                    leftUped = false
                }
//                HidConsts.clickDown(event.rawX.toInt(),event.rawY.toInt())
                actionDownTime_Pad = now
                maxPointerCount = event.pointerCount
                Xpad = event.x
                Ypad = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                maxPointerCount = max(maxPointerCount, event.pointerCount)
                if (HidUtils.isConnected()) {
                    val deltaX = ((event.x - Xpad) * rate).toInt()
                    val deltay = ((event.y - Ypad) * rate).toInt()
                    HidConsts.mouseMove(deltaX, deltay, 0, !leftbtnUped || !leftUped, !rightbtnUped, !midbtnUped)
                }
                Xpad = event.x
                Ypad = event.y
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                Xpad = event.x
                Ypad = event.y
                now = Date().time
                dis = now - actionDownTime_Pad
                actionDownTime_Pad = now
//                HidConsts.clickUp()
                if (HidUtils.isConnected()) {
                    if (maxPointerCount == 1) {
                        if (dis in 50..150 && leftUped) {
                            virtureClickTask = HidConsts.leftBtnClickAsync(150)
                        } else if (dis in 50..150 && !leftUped) {
                            HidConsts.leftBtnUp()
                            leftUped = true //模拟左键抬起
                            HidConsts.leftBtnClickAsync(20)
                        } else {
                            HidConsts.leftBtnUp()
                            leftUped = true //模拟左键抬起
                            virtureClickTask = null
                        }
                    }
                }
//                Log.d(TAG, "mouseMove: $dis");
                return true
            }
        }
        return false
    }

    fun mouseRight(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            HidConsts.rightBtnUp()
            rightbtnUped = true
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            HidConsts.rightBtnDown()
            rightbtnUped = false
        }
        return true
    }

    fun keyboardDown(usageStr: String){
        HidConsts.kbdKeyDown(usageStr)
    }

    fun keyboardUp(usageStr: String){
        HidConsts.kbdKeyUp(usageStr)
    }

    fun mouseMiddle(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                HidConsts.midBtnDown()
                midbtnUped = false
                maxPointerCount = event.pointerCount
                Ymus = event.y
                midbtnUped = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                maxPointerCount = Math.max(maxPointerCount, event.pointerCount)
                if (HidUtils.isConnected()) {
                    if (!midbtnUped) {
                        HidConsts.midBtnUp()
                        midbtnUped = true
                    }
                    val deltay = -(event.y - Ymus).toInt()
                    HidConsts.mouseMove(0, 0, deltay, !leftbtnUped, !rightbtnUped, !midbtnUped)
                }
                Ymus = event.y
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (!midbtnUped) {
                    HidConsts.midBtnUp()
                    midbtnUped = true
                }
                return true
            }
        }
        return false
    }

    companion object {
        const val TAG = "u-FragmentMousePad"
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }
}