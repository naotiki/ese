package me.naotiki.ese

import AppContainer
import Terminal
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import initializeComposeCommon
import targetActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Starting... Ese Linux")
        targetActivity=this
        initializeComposeCommon()
        setContent {
            AppContainer {
                Terminal()
            }
        }
    }
}