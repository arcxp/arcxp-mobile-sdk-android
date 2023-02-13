package com.google.ads.interactivemedia.pal

import com.google.android.gms.tasks.Task

//test double for nonce loader
class NonceLoader() {
    fun loadNonceManager(request: NonceRequest): Task<NonceManager>? { return null}
}


