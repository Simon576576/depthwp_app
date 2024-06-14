package com.example.afinal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperWithClockAndImage()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WallpaperWithClockAndImage() {
    val context = LocalContext.current
    var wallpaperBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        // 加载并裁切桌布图片
        val drawable = context.getDrawable(R.drawable.background) // 替换为您的 drawable 资源 ID
        drawable?.let {
            val originalBitmap = it.toBitmap()
            wallpaperBitmap = cropAndScaleBitmapToFillScreen(originalBitmap, context)
        }

        // 加载第二层图片
        val secondDrawable = context.getDrawable(R.drawable.file) // 替换为第二张图片的 drawable 资源 ID
        secondDrawable?.let {
            val originalBitmap = it.toBitmap()
            secondImageBitmap = cropAndScaleBitmapToFillScreen(originalBitmap, context)
        }
    }

    // 使用Box组件叠加图片和时钟
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter // 时钟放置在顶部中央
    ) {
        wallpaperBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Wallpaper",
                modifier = Modifier.fillMaxSize() // 占满全屏
            )
        } ?: run {
            Text("Loading...", modifier = Modifier.padding(16.dp)) // 加载中的提示文字
        }
        ClockScreen(
            offsetX = 0.dp, // 水平偏移
            offsetY = 16.dp // 垂直偏移
        )
        secondImageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Second Image",
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = -20.dp, y = 25.dp)
            )
        }



    }
}

@Composable
fun ClockScreen(
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) {
    var time by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            // 定时更新时间
            val timeZone = TimeZone.getTimeZone("GMT+8")
            val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                this.timeZone = timeZone
            }
            time = simpleDateFormat.format(Date())
            delay(1000)
        }
    }

    // Box用于对齐，Column用于排列文本
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .offset(x = offsetX, y = offsetY) // 设置偏移
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top, // 垂直方向上靠顶部对齐
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = time,
                fontSize = 100.sp, // 字体大小
                fontWeight = FontWeight.Bold // 字体加粗
            )
        }
    }
}

// 将Drawable转为Bitmap的函数
fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

// 裁切并缩放Bitmap以填充屏幕
fun cropAndScaleBitmapToFillScreen(bitmap: Bitmap, context: Context): Bitmap {
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    val originalWidth = bitmap.width
    val originalHeight = bitmap.height

    val targetWidth: Int
    val targetHeight: Int

    val aspectRatioScreen = screenWidth.toFloat() / screenHeight
    val aspectRatioBitmap = originalWidth.toFloat() / originalHeight

    if (aspectRatioBitmap > aspectRatioScreen) {
        // 图片宽度太大，裁切宽度
        targetWidth = (originalHeight * aspectRatioScreen).toInt()
        targetHeight = originalHeight
    } else {
        // 图片高度太大，裁切高度
        targetWidth = originalWidth
        targetHeight = (originalWidth / aspectRatioScreen).toInt()
    }

    val xOffset = (originalWidth - targetWidth) / 2
    val yOffset = (originalHeight - targetHeight) / 2

    val croppedBitmap = Bitmap.createBitmap(
        bitmap,
        xOffset,
        yOffset,
        targetWidth,
        targetHeight
    )

    // 将裁切后的图片放大到屏幕尺寸
    return croppedBitmap.scale(screenWidth, screenHeight)
}
