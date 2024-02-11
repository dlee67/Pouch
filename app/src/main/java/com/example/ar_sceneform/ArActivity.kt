package com.example.ar_sceneform

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function


class ArActivity : AppCompatActivity() {
    private val clickNo = 0
    private var arCam: ArFragment? = null
    private var selectedView = R.layout.text_layout;
    lateinit var fabSub1: FloatingActionButton
    lateinit var fabSub2: FloatingActionButton
    lateinit var fabSub3: FloatingActionButton
    lateinit var fabSub4: FloatingActionButton
    var isFabMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        fabSub1 = findViewById<FloatingActionButton>(R.id.fab_sub_1)
        fabSub2 = findViewById<FloatingActionButton>(R.id.fab_sub_2)
        fabSub3 = findViewById<FloatingActionButton>(R.id.fab_sub_3)
        fabSub4 = findViewById<FloatingActionButton>(R.id.fab_sub_4)

        fabSub1.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to basic tutorial",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.text_layout
        }

        fabSub2.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to web",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.layout.image_layout
        }

        fabSub3.setOnClickListener {
            Toast.makeText(
                this,
                "AR object set to model",
                Toast.LENGTH_SHORT
            ).show()
            selectedView = R.raw.tshirt
        }

        fabSub4.setOnClickListener {
            Toast.makeText(
                this,
                "Clearing the AR plane",
                Toast.LENGTH_SHORT
            ).show()
            this.detachAllAnchors(arCam!!)
        }

        if (checkSystemSupport(this)) {
            arCam = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment?
            arCam!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
                val anchor = hitResult.createAnchor()

                if (selectedView != R.raw.tshirt) {
                    ViewRenderable.builder()
                        .setView(this, selectedView) // set glb model
                        .build()
                        .thenAccept { viewRenderable ->
                            addModel(anchor, viewRenderable)
                            afterTapAction()
                        }
                        .exceptionally { throwable ->
                            runOnUiThread {
                                AlertDialog.Builder(this)
                                    .setMessage("Something went wrong: ${throwable.message}")
                                    .show()
                            }
                            null
                        }
                } else {
                    val anchor = hitResult.createAnchor()
                    ModelRenderable.builder()
                        .setSource(this, R.raw.tshirt) // set glb model
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(Consumer<ModelRenderable> { modelRenderable: ModelRenderable? ->
                            addModel(
                                anchor,
                                modelRenderable
                            )
                        })
                        .exceptionally(Function<Throwable, Void?> { throwable: Throwable ->
                            val builder = AlertDialog.Builder(this)
                            builder.setMessage("Something is not right" + throwable.message).show()
                            null
                        })
                }
            }
        }
    }

    //Function to render the model
    private fun addModel(anchor: Anchor, modelRenderable: ViewRenderable) {
        val anchorNode = AnchorNode(anchor)

        anchorNode.parent = arCam!!.arSceneView.scene

        val model = TransformableNode(arCam!!.transformationSystem)
        model.parent = anchorNode
        model.setRenderable(modelRenderable)
        model.select()
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)

        anchorNode.parent = arCam!!.arSceneView.scene

        val model = TransformableNode(arCam!!.transformationSystem)
        model.parent = anchorNode
        model.setRenderable(modelRenderable)
        model.select()
    }

    private fun detachAllAnchors(arFragment: ArFragment) {
        val session = arFragment.arSceneView.session
        val anchors = session?.allAnchors
        if (anchors != null) {
            for (anchor in anchors) {
                anchor.detach()
            }
        }
        // Optionally, you may want to clear any associated visual elements from your scene.
        arFragment.arSceneView.scene.callOnHierarchy { node ->
            if (node is AnchorNode && node.anchor != null) {
                // This ensures that the visual part attached to the anchor is also removed.
                node.setParent(null)
            }
        }
    }

    private fun afterTapAction() {
        when (selectedView) {
            R.layout.text_layout -> {
                Toast.makeText(
                    this,
                    "Basic tutorial view created",
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.layout.image_layout -> {
                Toast.makeText(
                    this,
                    "image layout created",
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.layout.web_layout -> {
                Toast.makeText(
                    this,
                    "model layout created",
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.layout.gif_layout -> {
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