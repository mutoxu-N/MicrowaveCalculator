package com.github.mutoxu_n.MicrowaveCalculator

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // load preference
        val pref = getPreferences(Context.MODE_PRIVATE)
        pref?.let {
            findViewById<EditText>(R.id.etBeforeWatt).setText(pref.getInt("beforeWatt", 500).toString())
            findViewById<EditText>(R.id.etBeforeTime).setText(it.getString("beforeTime", "0:00"))
            findViewById<EditText>(R.id.etAfterWatt).setText(it.getInt("afterWatt", 500).toString())
            findViewById<Button>(R.id.btPreset1).text = getString(R.string.wattUnit, it.getInt("preset1", 500))
            findViewById<Button>(R.id.btPreset2).text = getString(R.string.wattUnit, it.getInt("preset2", 600))
            findViewById<Button>(R.id.btPreset3).text = getString(R.string.wattUnit, it.getInt("preset3", 700))
        }

        // set Button click listener
        findViewById<Button>(R.id.bt500W).setOnClickListener(OnWattButtonClick())
        findViewById<Button>(R.id.bt600W).setOnClickListener(OnWattButtonClick())
        findViewById<Button>(R.id.btTimeSelect).setOnClickListener(OnTimeSelect())
        findViewById<Button>(R.id.btPreset1).setOnClickListener(OnWattButtonClick())
        findViewById<Button>(R.id.btPreset1).setOnLongClickListener(OnLongClick())
        findViewById<Button>(R.id.btPreset2).setOnClickListener(OnWattButtonClick())
        findViewById<Button>(R.id.btPreset2).setOnLongClickListener(OnLongClick())
        findViewById<Button>(R.id.btPreset3).setOnClickListener(OnWattButtonClick())
        findViewById<Button>(R.id.btPreset3).setOnLongClickListener(OnLongClick())

        // set EditText change listener
        findViewById<EditText>(R.id.etBeforeTime).addTextChangedListener(OnTimeChanged())
        findViewById<EditText>(R.id.etBeforeWatt).addTextChangedListener(OnWattChanged())
        findViewById<EditText>(R.id.etAfterWatt).addTextChangedListener(OnWattChanged())


        updateDisplay()
    }

    fun updateDisplay() {
        // get data
        val beforeWatt: Int? = findViewById<EditText>(R.id.etBeforeWatt).text.toString().toIntOrNull()
        val afterWatt: Int? = findViewById<EditText>(R.id.etAfterWatt).text.toString().toIntOrNull()
        val beforeTime: String = findViewById<EditText>(R.id.etBeforeTime).text.toString()
        val arr = beforeTime.split(':').reversed()

        // save data
        val editor = getPreferences(Context.MODE_PRIVATE).edit()
        beforeWatt?.let { editor.putInt("beforeWatt", it) }
        afterWatt?.let { editor.putInt("afterWatt", it) }
        editor.putString("beforeTime", beforeTime)
        editor.apply()

        // null skip
        beforeWatt ?: return
        afterWatt ?: return
        if(afterWatt == 0) return

        // calc before seconds
        var seconds = 0
        arr[0].toIntOrNull()?.let { seconds += it }
        arr[1].toIntOrNull()?.let { seconds += it*60 }

        // calc result sec
        val resultSeconds = (beforeWatt.toDouble() * seconds / afterWatt).roundToInt()
        val sec = resultSeconds%60
        val min = (resultSeconds - sec) / 60

        findViewById<TextView>(R.id.tvResultDisplay).text = getString(R.string.result, min, sec)

    }

    private inner class OnWattButtonClick: View.OnClickListener {
        override fun onClick(v: View) {
            var pressed: Int? = null

            when(v.id){
                R.id.bt500W -> {
                    findViewById<EditText>(R.id.etBeforeWatt).setText(500.toString())
                }
                R.id.bt600W -> {
                    findViewById<EditText>(R.id.etBeforeWatt).setText(600.toString())
                }
                R.id.btPreset1 -> pressed = 0
                R.id.btPreset2 -> pressed = 1
                R.id.btPreset3 -> pressed = 2
            }

            // preset
            pressed?.let {
                val pref = getPreferences(Context.MODE_PRIVATE)
                val arr = listOf(
                    pref.getInt("preset1", 500),
                    pref.getInt("preset2", 600),
                    pref.getInt("preset3", 700)
                )

                findViewById<EditText>(R.id.etAfterWatt).setText(arr[it].toString())
            }

            updateDisplay()
        }
    }

    private inner class OnLongClick: View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            // update preset button
            val btn = findViewById<Button>(v.id)
            val watt = findViewById<EditText>(R.id.etAfterWatt).text.toString().toIntOrNull()
            val pressed = when(v.id) {
                R.id.btPreset1 -> 1
                R.id.btPreset2 -> 2
                else -> 3
            }

            watt?.let {
                val editor = getPreferences(Context.MODE_PRIVATE).edit()
                editor.putInt("preset${pressed}", watt).apply()

                btn.text = getString(R.string.wattUnit, watt)

                Toast.makeText(this@MainActivity,
                    getString(R.string.setPreset, pressed, watt), Toast.LENGTH_SHORT).show()
                    return true
            }
            return false
        }
    }

    private inner class OnTimeChanged: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // check == false if we wanna skip format check
            val originalText = s.toString()
            var num: Int? = originalText.replace(":", "").toIntOrNull()
            if(num == null) num = 0

            // calc time
            val sec = num % 100
            val min = (num - sec) / 100

            // output
            val outputText = "${min}:${sec.toString().padStart(2, '0')}"
            val etBeforeTime = findViewById<EditText>(R.id.etBeforeTime)
            if(originalText != outputText) {
                etBeforeTime.setText(outputText)
                etBeforeTime.setSelection(outputText.length)
                updateDisplay()
            }
        }
    }

    private inner class OnWattChanged: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            updateDisplay()
        }
    }

    private inner class OnTimeSelect: View.OnClickListener {
        override fun onClick(v: View?) {
            // get min and sec
            var s = findViewById<EditText>(R.id.etBeforeTime).text.toString().replace(":", "").toIntOrNull()
            if(s == null) s = 0

            val sec = s % 100
            val min = (s - sec) / 100
            val dialog = TimePickDialogFragment(min, sec)
            dialog.show(supportFragmentManager, "TimePickDialog")
        }
    }
}