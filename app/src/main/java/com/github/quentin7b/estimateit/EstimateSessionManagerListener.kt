package com.github.quentin7b.estimateit

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

interface EstimateSessionManagerListener : SessionManagerListener<CastSession> {

    override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
        // dont care
    }

    override fun onSessionSuspended(p0: CastSession?, p1: Int) {
        // dont care
    }

    override fun onSessionStarting(p0: CastSession?) {
        // dont care
    }

    override fun onSessionResuming(p0: CastSession?, p1: String?) {
        // dont care
    }

    override fun onSessionEnding(p0: CastSession?) {
        // dont care
    }

    override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
        // dont care
    }
}