package com.github.quentin7b.estimateit

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions
                .Builder()
                .setReceiverApplicationId(context.getString(R.string.receiver_id))
                .build()
    }

    override fun getAdditionalSessionProviders(context: Context) = null
}