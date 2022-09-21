package com.blackshark.hidperipheral

import java.util.*

object UtilCls {
    /**
     * 延时执行任务
     * @param runnable
     * @param delay
     * @param runonce
     */
    @JvmStatic
    fun DelayTask(runnable: Runnable, delay: Int, runonce: Boolean): TimerTask {
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                runnable.run()
                if (runonce) {
                    cancel()
                }
            }
        }
        timer.schedule(task, delay.toLong())
        return task
    }
}