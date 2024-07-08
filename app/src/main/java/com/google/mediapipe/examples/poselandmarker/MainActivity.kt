package com.google.mediapipe.examples.poselandmarker
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),KneeAngleListener {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var squatCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 透過 ActivityMainBinding.inflate 方法為 ActivityMainBinding 變數分配記憶體
        // layoutInflater 是用來載入佈局的工具
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        // 設定目前 Activity 的內容視圖為 activityMainBinding.root
        // 即將 XML 佈局檔案中的根視圖設定為 Activity 的視圖
        setContentView(activityMainBinding.root)

        // 取得 NavHostFragment，這個 Fragment 是導覽元件的一部分
        // 它的作用是容納導航圖中的所有目的地，並處理導航操作
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

        // 取得 NavController，它用來管理應用程式導航操作
        val navController = navHostFragment.navController

        // 將 BottomNavigationView 與 NavController 綁定
        // 這樣可以確保 BottomNavigationView 的選項與 NavController 管理的目的地同步
        activityMainBinding.navigation.setupWithNavController(navController)

        // 設定 BottomNavigationView 的項目重新選擇監聽器
        // 重新選擇導航項目時什麼都不做（忽略重新選擇）
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }
        val overlayView = findViewById<OverlayView>(R.id.overlay_view)
        overlayView.setKneeAngleListener(this)

        // 初始化 TextView
        squatCountTextView = findViewById(R.id.squat_count_text_view)

        // 使用 ViewBinding 存取 TextView 並設定文字
        activityMainBinding.textView.text = "qqqqqq"
    }

    override fun onBackPressed() {
        finish()
    }
    override fun onKneeAngleBelowThreshold(count: Int) {
        // 更新TextView的文字
        Log.d("MainActivity", "Received squat count update: $count")
        runOnUiThread {
            squatCountTextView.text = "计数: $count"
        }
    }
}