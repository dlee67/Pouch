package com.example.ar_sceneform

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.Objects

class ArActivity : AppCompatActivity() {
    private val clickNo = 0
    private var arCam: ArFragment? = null
    private var selectedView = 0;
    lateinit var fabMain: FloatingActionButton
    lateinit var fabSub1: FloatingActionButton
    lateinit var fabSub2: FloatingActionButton
    lateinit var fabSub3: FloatingActionButton
    lateinit var fabSub4: FloatingActionButton
    var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        fabMain = findViewById<FloatingActionButton>(R.id.fab_main)
        fabSub1 = findViewById<FloatingActionButton>(R.id.fab_sub_1)
        fabSub2 = findViewById<FloatingActionButton>(R.id.fab_sub_2)
        fabSub3 = findViewById<FloatingActionButton>(R.id.fab_sub_3)
        fabSub4 = findViewById<FloatingActionButton>(R.id.fab_sub_4)

        fabMain.setOnClickListener {
            if (!isFabMenuOpen) {
                // Show sub FABs
                showIn(fabSub1)
                showIn(fabSub2)
                showIn(fabSub3)
                showIn(fabSub4)
                isFabMenuOpen = true
            } else {
                // Hide sub FABs
                showOut(fabSub1)
                showOut(fabSub2)
                showOut(fabSub3)
                showOut(fabSub4)
                isFabMenuOpen = false
            }
        }

        fabSub1.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to video",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.video_layout
            //videoView.start()
        }

        fabSub2.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to web",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.web_layout
        }

        fabSub3.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to chart",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.chart_layout
        }

        fabSub4.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to pdf",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.pdf_layout
        }

        if (checkSystemSupport(this)) {
            //Connecting to the UI
            // ArFragment is linked up with its respective id used in the activity_main.xml
            arCam = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment?
            arCam!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
                //fixing the coordinates in the detected plane
//                val inflater = LayoutInflater.from(this)
//                val view = inflater.inflate(R.layout.selectedView, null)
                val anchor = hitResult.createAnchor()
                ViewRenderable.builder()
                    .setView(this, selectedView) // set glb model
                    .build()
                    .thenAccept { viewRenderable ->
                        // This lambda is invoked with the completed ViewRenderable
                        addModel(anchor, viewRenderable)
                        afterTapAction()
                    }
                    .exceptionally { throwable ->
                        // This is where you handle any exceptions
                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setMessage("Something went wrong: ${throwable.message}")
                                .show()
                        }
                        null // Return null to signify the exception was handled
                    }
            }
        }
    }

    //Function to render the model
    private fun addModel(anchor: Anchor, modelRenderable: ViewRenderable) {
        // Creating a AnchorNode with a specific anchor
        val anchorNode = AnchorNode(anchor)

        // attaching the anchorNode with the ArFragment
        anchorNode.parent = arCam!!.arSceneView.scene

        // attaching the anchorNode with the TransformableNode
        val model = TransformableNode(arCam!!.transformationSystem)
        model.parent = anchorNode
        // attaching the 3d model with the TransformableNode
        // that is already attached with the node
        model.setRenderable(modelRenderable)
        model.select()
    }

    // Function to show a sub FAB with animation
    private fun showIn(fab: FloatingActionButton) {
        fab.visibility = View.VISIBLE
        fab.alpha = 0f
        fab.translationY = fab.height.toFloat()
        fab.animate()
            .setDuration(200)
            .translationY(0f)
            .setListener(null)
            .alpha(1f)
            .start()
    }

    // Function to hide a sub FAB with animation
    private fun showOut(fab: FloatingActionButton) {
        fab.animate()
            .setDuration(200)
            .translationY(fab.height.toFloat())
            .setListener(null)
            .alpha(0f)
            .withEndAction {
                fab.visibility = View.INVISIBLE
            }
            .start()
    }

        private fun afterTapAction() {
            when (selectedView) {
                R.layout.video_layout -> {
                    val videoView = findViewById<VideoView>(R.id.videoView)
                    val videoPath = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    val uri = Uri.parse(videoPath)
                    videoView.setVideoURI(uri)
                    // videoView.start()
                }
                R.layout.pdf_layout -> {
                    Toast.makeText(
                        this,
                        "PDF layout created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.layout.web_layout -> {
                    Toast.makeText(
                        this,
                        "Web layout created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.layout.chart_layout -> {
                    Toast.makeText(
                        this,
                        "Chart layout created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    companion object {
        fun checkSystemSupport(activity: Activity): Boolean {
            // checking whether the API version of the running Android >= 24
            // that means Android Nougat 7.0
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val openGlVersion =
                    (Objects.requireNonNull(activity.getSystemService(ACTIVITY_SERVICE)) as ActivityManager).deviceConfigurationInfo.glEsVersion
                // checking whether the OpenGL version >= 3.0
                if (openGlVersion.toDouble() >= 3.0) {
                    true
                } else {
                    Toast.makeText(
                        activity,
                        "App needs OpenGl Version 3.0 or later",
                        Toast.LENGTH_SHORT
                    ).show()
                    activity.finish()
                    false
                }
            } else {
                Toast.makeText(
                    activity,
                    "App does not support required Build Version",
                    Toast.LENGTH_SHORT
                )
                    .show()
                activity.finish()
                false
            }
        }
    }
}