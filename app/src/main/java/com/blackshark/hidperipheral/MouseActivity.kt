package com.blackshark.hidperipheral

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blackshark.hidperipheral.databinding.ActivityMouseBinding

class MouseActivity : AppCompatActivity(),View.OnTouchListener {
    private lateinit var binding: ActivityMouseBinding
    private lateinit var mouseUtils: MouseUtils
    private val TAG = "MouseActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mouseUtils = MouseUtils()
        binding.mousePad.setOnTouchListener(this)
        binding.btnLeft.setOnTouchListener(this)
        binding.btnRight.setOnTouchListener(this)
        binding.middlePad.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        var ret = true
        when (v?.id) {
            R.id.mouse_pad -> ret = mouseUtils.mouseMove(event!!)
            R.id.btn_left -> ret = mouseUtils.mouseLeft(event!!)
            R.id.btn_right -> ret = mouseUtils.mouseRight(event!!)
            R.id.middle_pad -> ret = mouseUtils.mouseMiddle(event!!)
        }
        return ret
    }
}