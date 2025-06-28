package com.example.androidcodexone

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class PicassoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picasso)

        val imageView: ImageView = findViewById(R.id.image_view)
        Picasso.get()
            .load("https://via.placeholder.com/300.png")
            .into(imageView)
    }
}

