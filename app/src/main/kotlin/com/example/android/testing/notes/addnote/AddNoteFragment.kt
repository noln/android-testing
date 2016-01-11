/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.testing.notes.addnote

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.android.testing.notes.Injection
import com.example.android.testing.notes.R
import com.example.android.testing.notes.util.EspressoIdlingResource

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.io.IOException

import com.google.common.base.Preconditions.checkState

/**
 * Main UI for the add note screen. Users can enter a note title and description. Images can be
 * added to notes by clicking on the options menu.
 */
class AddNoteFragment : Fragment(), AddNoteContract.View {

    private var mActionListener: AddNoteContract.UserActionsListener? = null

    private var mTitle: TextView? = null

    private var mDescription: TextView? = null

    private var mImageThumbnail: ImageView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActionListener = AddNotePresenter(Injection.provideNotesRepository(), this,
                Injection.provideImageFile())

        val fab = activity.findViewById(R.id.fab_add_notes) as FloatingActionButton
        fab.setImageResource(R.drawable.ic_done)
        fab.setOnClickListener {
            mActionListener!!.saveNote(mTitle!!.text.toString(),
                    mDescription!!.text.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_addnote, container, false)
        mTitle = root.findViewById(R.id.add_note_title) as TextView
        mDescription = root.findViewById(R.id.add_note_description) as TextView
        mImageThumbnail = root.findViewById(R.id.add_note_image_thumbnail) as ImageView

        setHasOptionsMenu(true)
        retainInstance = true
        return root
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.take_picture -> {
                try {
                    mActionListener!!.takePicture()
                } catch (ioe: IOException) {
                    if (view != null) {
                        Snackbar.make(view, getString(R.string.take_picture_error),
                                Snackbar.LENGTH_LONG).show()
                    }
                }

                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_addnote_options_menu_actions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun showEmptyNoteError() {
        Snackbar.make(mTitle, getString(R.string.empty_note_message), Snackbar.LENGTH_LONG).show()
    }

    override fun showNotesList() {
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    override fun openCamera(saveTo: String) {
        // Open the camera to take a picture.
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Check if there is a camera app installed to handle our Intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(saveTo))
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE)
        } else {
            Snackbar.make(mTitle, getString(R.string.cannot_connect_to_camera_message),
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showImagePreview(imageUrl: String) {
        checkState(!TextUtils.isEmpty(imageUrl), "imageUrl cannot be null or empty!")
        mImageThumbnail!!.visibility = View.VISIBLE

        // The image is loaded in a different thread so in order to UI-test this, an idling resource
        // is used to specify when the app is idle.
        EspressoIdlingResource.increment() // App is busy until further notice.

        // This app uses Glide for image loading
        Glide.with(this).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(object : GlideDrawableImageViewTarget(mImageThumbnail) {
            override fun onResourceReady(resource: GlideDrawable,
                                         animation: GlideAnimation<in GlideDrawable>?) {
                super.onResourceReady(resource, animation)
                EspressoIdlingResource.decrement() // Set app as idle.
            }
        })
    }

    override fun showImageError() {
        Snackbar.make(mTitle, getString(R.string.cannot_connect_to_camera_message),
                Snackbar.LENGTH_SHORT).show()
    }

    override fun setUserActionListener(listener: AddNoteContract.UserActionsListener) {
        mActionListener = listener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If an image is received, display it on the ImageView.
        if (REQUEST_CODE_IMAGE_CAPTURE == requestCode && Activity.RESULT_OK == resultCode) {
            mActionListener!!.imageAvailable()
        } else {
            mActionListener!!.imageCaptureFailed()
        }
    }

    companion object {

        val REQUEST_CODE_IMAGE_CAPTURE = 4097

        fun newInstance(): AddNoteFragment {
            return AddNoteFragment()
        }
    }
}// Required empty public constructor
