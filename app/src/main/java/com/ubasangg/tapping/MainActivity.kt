package com.ubasangg.tapping

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.ubasangg.tapping.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var binding: ActivityMainBinding

    private var highScore = 0
    private var score = 0
    private val DEFAULT_TIMER = 15
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        this.binding.lBtn.setOnClickListener(this)
        this.binding.rBtn.setOnClickListener(this)
        this.binding.btnRestart.setOnClickListener(this)

        this.sharedPreferences = getSharedPreferences("SP", MODE_PRIVATE)
        highScore = this.sharedPreferences.getInt("SP_HIGHSCORE", 0)
        this.prefEditor = sharedPreferences.edit()

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        reset()
    }

    fun reset() {
        binding.lBtn.setBackgroundColor(getColor(R.color.l_active))
        binding.rBtn.setBackgroundColor(getColor(R.color.r_active))
        this.binding.tvHighscore.text = getString(R.string.high_score, highScore)
        this.binding.tvScore.text = getString(R.string.default_score, score)
        updateTimer(DEFAULT_TIMER)
    }

    private fun updateTimer(seconds: Int) {
        this.binding.tvTimer.text = getString(R.string.timer, seconds.toString().padStart(2, '0'))
    }

    private fun toggleButtons(curBtn: Button, othBtn: Button) {
        if(!isStarted) {
            val timer = object : CountDownTimer((DEFAULT_TIMER * 1000L), 1000) {
                var count = 0

                override fun onTick(millisUntilFinished: Long) {
                    count++
                    updateTimer(DEFAULT_TIMER - count)
                }

                override fun onFinish() {
                    isStarted = false
                    binding.lBtn.isEnabled = false
                    binding.rBtn.isEnabled = false
                    binding.btnRestart.visibility = View.VISIBLE

                    if (highScore < score) {
                        highScore = score
                        prefEditor.putInt("SP_HIGHSCORE", highScore)
                        prefEditor.apply()
                        reset()
                    }

                    binding.lBtn.setBackgroundColor(getColor(R.color.l_inactive))
                    binding.rBtn.setBackgroundColor(getColor(R.color.r_inactive))
                }
            }

            timer.start()
            isStarted = true
        }

        curBtn.isEnabled = !curBtn.isEnabled
        if (score != 1) othBtn.isEnabled = !othBtn.isEnabled

        if (curBtn == findViewById<Button>(R.id.rBtn)) {
            curBtn.setBackgroundColor(getColor(R.color.r_inactive))
            othBtn.setBackgroundColor(getColor(R.color.l_active))
        } else {
            curBtn.setBackgroundColor(getColor(R.color.l_inactive))
            othBtn.setBackgroundColor(getColor(R.color.r_active))
        }

        this.binding.tvScore.text = getString(R.string.default_score, score)
    }

    override fun onClick(v: View?) {
        when (v) {
            this.binding.lBtn -> {
                score++
                toggleButtons(this.binding.lBtn, this.binding.rBtn)
            }

            this.binding.rBtn -> {
                score++
                toggleButtons(this.binding.rBtn, this.binding.lBtn)
            }
            else -> {
                this.binding.lBtn.isEnabled = true
                this.binding.rBtn.isEnabled = true
                this.binding.btnRestart.visibility = View.GONE
                score = 0
                reset()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu) // menu_options is the Android Resource File name

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mi_Reset_Highscore -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage("Are you sure you want to reset your high score of $highScore?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        highScore = 0
                        reset()
                        prefEditor.putInt("SP_HIGHSCORE", highScore)
                        prefEditor.apply()
                    }
                    .setNegativeButton("No") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}