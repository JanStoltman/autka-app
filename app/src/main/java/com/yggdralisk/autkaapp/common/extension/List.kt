package com.yggdralisk.autkaapp.common.extension

fun <T> ArrayList<T>.removeAndAddAll(l: Collection<T>) {
    this.clear()
    this.addAll(l)
}