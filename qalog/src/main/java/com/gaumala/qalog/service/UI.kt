package com.gaumala.qalog.service

import android.content.Context
import android.view.WindowManager

internal class UI(ctx: Context) {
    private val button = ShareButton(ctx)
    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    init {
        button.attach(ctx, wm)
    }

    fun setOnClickListener(listener: () -> Unit) {
        button.clickListener = listener
    }

    fun clear() {
        button.detach(wm)
    }
}