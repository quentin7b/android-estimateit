package com.github.quentin7b.estimateit

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import kotlinx.android.synthetic.main.activity_estimate.*
import java.util.*


class MainActivity : AppCompatActivity() {

    /**
     * Contient notre session actuelle avec l'appareil Cast
     */
    private var castSession: CastSession? = null
    /**
     * Le context, un peu comme sur le sdk permet d'initier les échanges.
     * Il permet d'obtenir une session par la suite
     */
    private val castContext by lazy { CastContext.getSharedInstance(baseContext) }
    /**
     * Notre namespace pour échanger des messages.
     * Sa valeur, comme dans le receiver est urn:x-cast:com.github.quentin7b.estimateit
     */
    private val nameSpace by lazy { baseContext.getString(R.string.namespace) }

    /**
     * Le selector est utilisé par le MediaRouter pour le controle du bouton
     */
    private var mediaSelector: MediaRouteSelector? = null

    /**
     * Le session listener permet d'initialiser notre castSession, il est automatiquement appelé par
     * le castContext quand il ouvre une session et nous permet de stocker sa ref.
     *
     * A la base, l'interface est `SessionManagerListener<CastSession>` mais pour plus de lisibilité,
     * j'ai fait une première impkémentation qui vire toutes les méthodes dont on ne se sert pas.
     * Pour en savoir plus: https://developers.google.com/android/reference/com/google/android/gms/cast/framework/SessionManagerListener
     */
    private val sessionManagerListener = object : EstimateSessionManagerListener {

        override fun onSessionStarted(pCastSession: CastSession?, p1: String?) =
                this@MainActivity.run {
                    castSession = pCastSession
                    invalidateOptionsMenu()
                }

        override fun onSessionResumed(pCastSession: CastSession?, wasSuspended: Boolean) =
                this@MainActivity.run {
                    castSession = pCastSession
                    invalidateOptionsMenu()
                }

        override fun onSessionEnded(pCastSession: CastSession, error: Int) =
                this@MainActivity.run {
                    if (pCastSession == castSession) {
                        cleanup()
                    }
                    invalidateOptionsMenu()
                }
    }

    /**
     * Cette callback ne sert à rien ici si ce n'est permettre la découverte des devices plus tard
     */
    private val mediaRouterCallback = object : MediaRouter.Callback() {
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "onCreate")

        /*
        un layout basique avec 1 gros btn au milieu
         */
        setContentView(R.layout.activity_estimate)

        /*
        Dans l'idée, quand on clique sur le btn, on va envoyer un message à la session active
        Pour cela, on spécifie le namespace et la valeur du message (ici un entier entre 0 et 5)
         */
        btn_estimate_doit.setOnClickListener {
            castSession?.sendMessage(nameSpace, Random().nextInt(5).toString())
        }

        // On créé le mediaSelectore
        mediaSelector = MediaRouteSelector.Builder()
                // On ajoute les intents auxquels on va répondre, ici c'est uniquement systemr
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                // On spécifie notre app id
                .addControlCategory(CastMediaControlIntent.categoryForCast(baseContext.getString(R.string.receiver_id)))
                .build()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        Log.i("MainActivity", "onCreateOptionsMenu")

        /*
        Ce menu contient le cast btn.
        <?xml version="1.0" encoding="utf-8"?>
           <menu xmlns:android="http://schemas.android.com/apk/res/android"
                 xmlns:app="http://schemas.android.com/apk/res-auto">
                <item
                    android:id="@+id/media_route_menu_item"
                    android:title="@string/cast"
                    app:actionProviderClass="androidx.mediarouter.app.MediaRouteActionProvider"
                    app:showAsAction="always" />
            </menu>
         */
        menuInflater.inflate(R.menu.estimate, menu)
        /*
        Ici, on initialise le bouton pour que ça lance la session quand on clique dessus.
        Rien de fou, le SDK fait tout pour nous
         */
        val mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(baseContext, menu, R.id.media_route_menu_item)
        /*
         *On affecte ce menu à notre mediaSelector
         */
        val mediaRouteActionProvider =
                MenuItemCompat.getActionProvider(mediaRouteMenuItem) as MediaRouteActionProvider
        mediaSelector?.also(mediaRouteActionProvider::setRouteSelector)
        /*
        On filtre les devices pour ne faire apparaitre que les Chromecast (pas les Google Home par ex)
         */
        mediaRouteActionProvider.dialogFactory = ChromecastRouteDialogFactory()
        return true
    }

    override fun onStart() {
        /*
        On lance la découverte des appareils
         */
        mediaSelector?.also { selector ->
            MediaRouter.getInstance(baseContext)?.addCallback(selector, mediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "onResume")

        /*
        Ici on demande au context de nous alerter (avec la callback) quand une CastSession est activée
        Ca nous permet de la stocker pour pouvoir jouer avec (envoyer des messages par exemple)
         */
        castContext.sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        if (castSession == null) {
            /*
            Comme on résume l'histoire, c'est possible qu'il y avait une session en cours !
            Si tel est le cas, on tente de la réaffecter
             */
            castSession = castContext.sessionManager.currentCastSession
        }
    }

    override fun onPause() {
        Log.i("MainActivity", "onPause")

        /*
        Classique, on pause alors on se désabonne
         */
        castContext.sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
        super.onPause()
    }

    override fun onStop() {
        cleanup()
        super.onStop()
    }

    private fun cleanup() {
        castSession = null
        MediaRouter.getInstance(baseContext)?.removeCallback(mediaRouterCallback)
    }

}