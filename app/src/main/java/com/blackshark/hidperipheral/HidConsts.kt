package com.blackshark.hidperipheral

import android.bluetooth.BluetoothHidDevice
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

object HidConsts {
    const val TAG = "u-HidConsts"
    const val NAME = "BS-HID-Peripheral"
    const val DESCRIPTION = "fac"
    const val PROVIDER = "funny"

    @JvmField
    var HidDevice: BluetoothHidDevice? = null

    private var handler: Handler? = null
    private val inputReportQueue: Queue<HidReport> = ConcurrentLinkedQueue()
    var ModifierByte: Byte = 0x00
    var KeyByte: Byte = 0x00
    fun cleanKbd() {
        sendKeyReport(byteArrayOf(0, 0))
        Alted = false
    }

    var Alted = false
    private fun addInputReport(inputReport: HidReport?) {
        if (inputReport != null) {
            inputReportQueue.offer(inputReport)
        }
    }

    var scheperoid: Long = 5
    fun reporters(context: Context) {
        handler = Handler(context.mainLooper)
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val report = inputReportQueue.poll()
                if (report != null) {
                    if (HidUtils.isConnected()) {
                        postReport(report)
                    }
                }
            }
        }, 0, scheperoid)
    }

    private fun postReport(report: HidReport) {
        HidReport.SendState = HidReport.State.Sending
        val ret = HidDevice!!.sendReport(HidUtils.mDevice, report.ReportId.toInt(), report.ReportData)
        if (!ret) {
            HidReport.SendState = HidReport.State.Failded
        } else {
            HidReport.SendState = HidReport.State.Sended
        }
    }

    fun sendMouseReport(reportData: ByteArray?) {
        val report = HidReport(HidReport.DeviceType.Mouse, 0x01.toByte(), reportData!!)
        addInputReport(report)
    }

    private val MouseReport = HidReport(HidReport.DeviceType.Mouse, 0x01.toByte(), byteArrayOf(0, 0, 0, 0))
    fun mouseMove(dx: Int, dy: Int, wheel: Int, leftButton: Boolean, rightButton: Boolean, middleButton: Boolean) {
        var dx = dx
        var dy = dy
        var wheel = wheel
        if (HidReport.SendState == HidReport.State.Sending) {
            return
        }
        if (dx > 127) dx = 127
        if (dx < -127) dx = -127
        if (dy > 127) dy = 127
        if (dy < -127) dy = -127
        if (wheel > 127) wheel = 127
        if (wheel < -127) wheel = -127
        if (leftButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 1
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 1.inv()).toByte()
        }
        if (rightButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 2
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 2.inv()).toByte()
        }
        if (middleButton) {
            MouseReport.ReportData[0] = MouseReport.ReportData[0] or 4
        } else {
            MouseReport.ReportData[0] = (MouseReport.ReportData[0] and 4.inv()).toByte()
        }
        MouseReport.ReportData[1] = dx.toByte()
        MouseReport.ReportData[2] = dy.toByte()
        MouseReport.ReportData[3] = wheel.toByte()
        addInputReport(MouseReport)
    }

    fun leftBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 1
        sendMouseReport(MouseReport.ReportData)
    }

    fun leftBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 1.inv()
        sendMouseReport(MouseReport.ReportData)
    }

    fun leftBtnClick() {
        leftBtnDown()
        UtilCls.DelayTask({ leftBtnUp() }, 20, true)
    }

    fun leftBtnClickAsync(delay: Int): TimerTask {
        return UtilCls.DelayTask({ leftBtnClick() }, delay, true)
    }

    fun rightBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 2
        sendMouseReport(MouseReport.ReportData)
    }

    fun rightBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 2.inv()
        sendMouseReport(MouseReport.ReportData)
    }

    fun midBtnDown() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] or 4
        sendMouseReport(MouseReport.ReportData)
    }

    fun midBtnUp() {
        MouseReport.ReportData[0] = MouseReport.ReportData[0] and 4.inv()
        sendMouseReport(MouseReport.ReportData)
    }

    fun modifierDown(UsageId: Byte): Byte {
        synchronized(HidConsts::class.java) { ModifierByte = ModifierByte or UsageId }
        return ModifierByte
    }

    fun modifierUp(UsageId: Byte): Byte {
        var UsageId = UsageId
        UsageId = UsageId.inv().toByte()
        synchronized(HidConsts::class.java) { ModifierByte = (ModifierByte and UsageId).toByte() }
        return ModifierByte
    }

    fun kbdKeyDown(usageStr: String) {
        var usageStr = usageStr
        if (!TextUtils.isEmpty(usageStr)) {
            if (usageStr.startsWith("M")) {
                usageStr = usageStr.replace("M", "")
                synchronized(HidConsts::class.java) {
                    val mod = modifierDown(usageStr.toInt().toByte())
                    sendKeyReport(byteArrayOf(mod, KeyByte))
                }
            } else {
                val key = usageStr.toInt().toByte()
                synchronized(HidConsts::class.java) {
                    KeyByte = key
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            }
        }
    }

    fun kbdKeyUp(usageStr: String) {
        var usageStr = usageStr
        if (!TextUtils.isEmpty(usageStr)) {
            if (usageStr.startsWith("M")) {
                usageStr = usageStr.replace("M", "")
                synchronized(HidConsts::class.java) {
                    val mod = modifierUp(usageStr.toInt().toByte())
                    sendKeyReport(byteArrayOf(mod, KeyByte))
                }
            } else {
                synchronized(HidConsts::class.java) {
                    KeyByte = 0
                    sendKeyReport(byteArrayOf(ModifierByte, KeyByte))
                }
            }
        }
    }

    private fun sendKeyReport(reportData: ByteArray) {
        val report = HidReport(HidReport.DeviceType.Keyboard, 0x02.toByte(), reportData)
        addInputReport(report)
    }

    @JvmField
    val Descriptor = byteArrayOf(
        0x05.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x02.toByte(),
        0xa1.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x01.toByte(),
        0xa1.toByte(),
        0x00.toByte(),
        0x85.toByte(),
        0x01.toByte(),
        0x05.toByte(),
        0x09.toByte(),
        0x19.toByte(),
        0x01.toByte(),
        0x29.toByte(),
        0x03.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x01.toByte(),
        0x95.toByte(),
        0x03.toByte(),
        0x75.toByte(),
        0x01.toByte(),
        0x81.toByte(),
        0x02.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x05.toByte(),
        0x81.toByte(),
        0x03.toByte(),
        0x05.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x30.toByte(),
        0x09.toByte(),
        0x31.toByte(),
        0x09.toByte(),
        0x38.toByte(),
        0x15.toByte(),
        0x81.toByte(),
        0x25.toByte(),
        0x7f.toByte(),
        0x75.toByte(),
        0x08.toByte(),
        0x95.toByte(),
        0x03.toByte(),
        0x81.toByte(),
        0x06.toByte(),
        0xc0.toByte(),
        0xc0.toByte(),
        0x05.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x06.toByte(),
        0xa1.toByte(),
        0x01.toByte(),
        0x85.toByte(),
        0x02.toByte(),
        0x05.toByte(),
        0x07.toByte(),
        0x19.toByte(),
        0xE0.toByte(),
        0x29.toByte(),
        0xE7.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x01.toByte(),
        0x95.toByte(),
        0x08.toByte(),
        0x81.toByte(),
        0x02.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x08.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x65.toByte(),
        0x19.toByte(),
        0x00.toByte(),
        0x29.toByte(),
        0x65.toByte(),
        0x81.toByte(),
        0x00.toByte(),
        0x05.toByte(),
        0x08.toByte(),
        0x95.toByte(),
        0x05.toByte(),
        0x75.toByte(),
        0x01.toByte(),
        0x19.toByte(),
        0x01.toByte(),
        0x29.toByte(),
        0x05.toByte(),
        0x91.toByte(),
        0x02.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x03.toByte(),
        0x91.toByte(),
        0x03.toByte(),
        0xc0.toByte()
    )
}