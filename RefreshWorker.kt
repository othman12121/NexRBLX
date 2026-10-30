package com.nexrblx.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RefreshWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val store = Store(applicationContext)
            val l = Api.latest()
            val p = Api.popular()
            if (l.isNotEmpty()) store.saveLatest(l)
            if (p.isNotEmpty()) store.savePopular(p)
            store.lastUpdate = System.currentTimeMillis()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
