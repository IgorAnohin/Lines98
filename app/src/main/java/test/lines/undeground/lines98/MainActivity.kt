package test.lines.undeground.lines98

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageView
import org.jetbrains.anko.toast
import java.io.File
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val prefs = this.getSharedPreferences("best_score_file", Context.MODE_PRIVATE)
        val score = prefs.getInt("best_score", 0) //0 is the default value
        val text_score  = findViewById<TextView>(R.id.start_best_score)
        text_score.text = score.toString()

        val retImg = findViewById<ImageView>(R.id.play_button)
        retImg.setOnClickListener {
            retImg.isEnabled = false
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val retImg = findViewById<ImageView>(R.id.play_button)
        retImg.isEnabled = true

        val prefs = this.getSharedPreferences("best_score_file", Context.MODE_PRIVATE)
        val score = prefs.getInt("best_score", 0) //0 is the default value
        val text_score  = findViewById<TextView>(R.id.start_best_score)
        text_score.text = score.toString()
    }
}
