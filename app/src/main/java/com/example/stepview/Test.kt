package com.example.stepview

import kotlinx.coroutines.*

/**
 * name：xiaoyu
 * time: 2020/12/11 16:14
 * desc:
 */



fun main() = runBlocking<Unit> {
    // 启动并发的协程以验证主线程并未阻塞
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }

}


