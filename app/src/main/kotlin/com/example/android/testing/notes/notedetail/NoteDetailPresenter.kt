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

package com.example.android.testing.notes.notedetail

import com.example.android.testing.notes.data.Note
import com.example.android.testing.notes.data.NotesRepository

import com.google.common.base.Preconditions.checkNotNull

/**
 * Listens to user actions from the UI ([NoteDetailFragment]), retrieves the data and updates
 * the UI as required.
 */
class NoteDetailPresenter(notesRepository: NotesRepository,
                          noteDetailView: NoteDetailContract.View) : NoteDetailContract.UserActionsListener {

    private val mNotesRepository: NotesRepository

    private val mNotesDetailView: NoteDetailContract.View

    init {
        mNotesRepository = checkNotNull(notesRepository, "notesRepository cannot be null!")
        mNotesDetailView = checkNotNull(noteDetailView, "noteDetailView cannot be null!")
    }

    override fun openNote(noteId: String?) {
        if (null == noteId || noteId.isEmpty()) {
            mNotesDetailView.showMissingNote()
            return
        }

        mNotesDetailView.setProgressIndicator(true)
        mNotesRepository.getNote(noteId) { note ->
            mNotesDetailView.setProgressIndicator(false)
            if (null == note) {
                mNotesDetailView.showMissingNote()
            } else {
                showNote(note)
            }
        }
    }

    private fun showNote(note: Note) {
        val title = note.title
        val description = note.description
        val imageUrl = note.imageUrl

        if (title != null && title.isEmpty()) {
            mNotesDetailView.hideTitle()
        } else {
            mNotesDetailView.showTitle(title)
        }

        if (description != null && description.isEmpty()) {
            mNotesDetailView.hideDescription()
        } else {
            mNotesDetailView.showDescription(description)
        }

        if (imageUrl != null) {
            mNotesDetailView.showImage(imageUrl)
        } else {
            mNotesDetailView.hideImage()
        }

    }
}
