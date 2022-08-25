package com.tjek.sdk.publicationviewer.paged.views
/*
 * Copyright (C) 2022 Tjek
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
import android.content.Context
import android.widget.FrameLayout
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageView
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoPageViewListener

abstract class OutroView(
    context: Context,
    override val page: Int,
    val publication: PublicationV2?
) : FrameLayout(context), VersoPageView {

    override fun onZoom(scale: Float): Boolean = false

    override fun setOnLoadCompleteListener(listener: VersoPageViewListener.OnLoadCompleteListener) { }

    override fun onVisible() { }

    override fun onInvisible() { }

}