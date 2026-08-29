package ru.javser.aquarium

import android.graphics.Bitmap
import android.graphics.Color

object WhiteBackgroundRemover {

    fun removeWhite(src: Bitmap): Bitmap {
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
        
        // ИСПРАВЛЕНО: добавили '0' для координаты Y
        // Сигнатура: (pixels, offset, stride, x, y, width, height)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)

        val isWhite = BooleanArray(w * h) { i ->
            val c = pixels[i]
            minOf(Color.red(c), Color.green(c), Color.blue(c)) > 215
        }
        val visited = BooleanArray(w * h)
        val queue = IntArray(w * h)
        var head = 0; var tail = 0

        fun seed(x: Int, y: Int) {
            val i = y * w + x
            if (isWhite[i] && !visited[i]) { visited[i] = true; queue[tail++] = i }
        }
        for (x in 0 until w) { seed(x, 0); seed(x, h - 1) }
        for (y in 0 until h) { seed(0, y); seed(w - 1, y) }

        while (head < tail) {
            val i = queue[head++]
            val x = i % w; val y = i / w
            pixels[i] = Color.TRANSPARENT
            if (x > 0)     { val j = i - 1; if (isWhite[j] && !visited[j]) { visited[j] = true; queue[tail++] = j } }
            if (x < w - 1) { val j = i + 1; if (isWhite[j] && !visited[j]) { visited[j] = true; queue[tail++] = j } }
            if (y > 0)     { val j = i - w; if (isWhite[j] && !visited[j]) { visited[j] = true; queue[tail++] = j } }
            if (y < h - 1) { val j = i + w; if (isWhite[j] && !visited[j]) { visited[j] = true; queue[tail++] = j } }
        }

        // ИСПРАВЛЕНО: добавили '0' для координаты Y
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }
}
