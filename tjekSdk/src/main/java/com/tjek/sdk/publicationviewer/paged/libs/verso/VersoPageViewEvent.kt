package com.tjek.sdk.publicationviewer.paged.libs.verso
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
sealed class VersoPageViewEvent {

    data class Touch(val action: Int, val info: VersoTapInfo) : VersoPageViewEvent()

    data class Tap(val info: VersoTapInfo) : VersoPageViewEvent()
    data class DoubleTap(val info: VersoTapInfo) : VersoPageViewEvent()
    data class LongTap(val info: VersoTapInfo) : VersoPageViewEvent()

    data class ZoomBegin(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class Zoom(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class ZoomEnd(val info: VersoZoomPanInfo) : VersoPageViewEvent()

    data class PanBegin(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class Pan(val info: VersoZoomPanInfo) : VersoPageViewEvent()
    data class PanEnd(val info: VersoZoomPanInfo) : VersoPageViewEvent()
}
