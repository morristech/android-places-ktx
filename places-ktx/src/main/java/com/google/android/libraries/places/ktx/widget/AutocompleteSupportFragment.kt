// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.libraries.places.ktx.widget

import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public sealed class PlaceSelectionResult

public data class PlaceSelectionSuccess(val place: Place) : PlaceSelectionResult()

public data class PlaceSelectionError(val status: Status) : PlaceSelectionResult()

// Since offer() can throw when the channel is closed (channel can close before the
// block within awaitClose), wrap `offer` calls inside `runCatching`.
// See: https://github.com/Kotlin/kotlinx.coroutines/issues/974
private fun <E> SendChannel<E>.offerCatching(element: E): Boolean {
    return runCatching { offer(element) }.getOrDefault(false)
}

@ExperimentalCoroutinesApi
public fun AutocompleteSupportFragment.placeSelectionEvents() : Flow<PlaceSelectionResult> =
    callbackFlow {
        this@placeSelectionEvents.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                offerCatching(PlaceSelectionSuccess(place))
            }

            override fun onError(status: Status) {
                offerCatching(PlaceSelectionError(status))
            }
        })
        awaitClose { this@placeSelectionEvents.setOnPlaceSelectedListener(null) }
    }
