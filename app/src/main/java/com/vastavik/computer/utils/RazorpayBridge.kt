package com.vastavik.computer.utils

object RazorpayBridge {
    var onSuccess: ((String) -> Unit)? = null
    var onError: ((Int, String?) -> Unit)? = null
    var pendingOrderId: String = ""
}
