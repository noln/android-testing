/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.testing.notes.notedetail

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.android.testing.notes.Injection
import com.example.android.testing.notes.R
import com.example.android.testing.notes.util.EspressoIdlingResource

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

/**
 * Main UI for the note detail screen.
 */
class NoteDetailFragment : Fragment(), NoteDetailContract.View {

    private var mActionsListener: NoteDetailContract.UserActionsListener? = null

    private var mDetailTitle: TextView? = null

    private var mDetailDescription: TextView? = null

    private var mDetailImage: ImageView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActionsListener = NoteDetailPresenter(Injection.provideNotesRepository(),
                this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_detail, container, false)
        mDetailTitle = root.findViewById(R.id.note_detail_title) as TextView
        mDetailDescription = root.findViewById(R.id.note_detail_description) as TextView
        mDetailImage = root.findViewById(R.id.note_detail_image) as ImageView
        return root
    }

    override fun onResume() {
        super.onResume()
        val noteId = arguments.getString(ARGUMENT_NOTE_ID)
        mActionsListener!!.openNote(noteId)
    }

    override fun setProgressIndicator(active: Boolean) {
        if (active) {
            mDetailTitle!!.text = ""
            mDetailDescription!!.text = getString(R.string.loading)
        }
    }

    override fun hideDescription() {
        mDetailDescription!!.visibility = View.GONE
    }

    override fun hideTitle() {
        mDetailTitle!!.visibility = View.GONE
    }

    override fun showDescription(description: String) {
        mDetailDescription!!.visibility = View.VISIBLE
        mDetailDescription!!.text = description
    }

    override fun showTitle(title: String) {
        mDetailTitle!!.visibility = View.VISIBLE
        mDetailTitle!!.text = title
    }

    override fun showImage(imageUrl: String) {
        // The image is loaded in a different thread so in order to UI-test this, an idling resource
        // is used to specify when the app is idle.
        EspressoIdlingResource.increment() // App is busy until further notice.

        mDetailImage!!.visibility = View.VISIBLE

        // This app uses Glide for image loading
        Glide.with(this).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(object : GlideDrawableImageViewTarget(mDetailImage) {
            override fun onResourceReady(resource: GlideDrawable,
                                         animation: GlideAnimation<in GlideDrawable>?) {
                super.onResourceReady(resource, animation)
                EspressoIdlingResource.decrement() // App is idle.
            }
        })
    }

    override fun hideImage() {
        mDetailImage!!.setImageDrawable(null)
        mDetailImage!!.visibility = View.GONE
    }

    override fun showMissingNote() {
        mDetailTitle!!.text = ""
        mDetailDescription!!.text = getString(R.string.no_data)
    }

    companion object {

        val ARGUMENT_NOTE_ID = "NOTE_ID"

        fun newInstance(noteId: String): NoteDetailFragment {
            val arguments = Bundle()
            arguments.putString(ARGUMENT_NOTE_ID, noteId)
            val fragment = NoteDetailFragment()
            fragment.arguments = arguments
            return fragment
        }
    }
}
