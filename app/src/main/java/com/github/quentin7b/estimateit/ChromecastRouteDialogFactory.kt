package com.github.quentin7b.estimateit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.mediarouter.app.MediaRouteChooserDialog
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory
import androidx.mediarouter.media.MediaRouter


class ChromecastRouteDialogFactory : MediaRouteDialogFactory() {
    override fun onCreateChooserDialogFragment(): MediaRouteChooserDialogFragment {
        return CustomMediaRouteChooserDialogFragment()
    }
}

class CustomMediaRouteChooserDialogFragment : MediaRouteChooserDialogFragment() {

    override fun onCreateChooserDialog(context: Context?, savedInstanceState: Bundle?): MediaRouteChooserDialog {
        val dialog = CustomMediaRouteChooserDialog(context!!, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        dialog.routeSelector = routeSelector
        return dialog
    }
}

class CustomMediaRouteChooserDialog : MediaRouteChooserDialog {
    constructor(context: Context) : super(context)

    constructor(context: Context, theme: Int) : super(context, theme)

    override fun onFilterRoute(route: MediaRouter.RouteInfo): Boolean {
        // Apply your logic here.
        // Return false to hide the device, true otherwise

        Log.i("MainActivity", "${route.description} - ${route.name}")
        return route.description?.contains("Chromecast") ?: false
    }
}