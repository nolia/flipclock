package com.nolia.flipclock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var counter = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flipText.setOnClickListener {
            if (counter > 0) {
                counter--
                flipText.text = "$counter"
            }
        }
        flipText.text = "${counter}"
    }
}
