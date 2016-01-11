/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.pageflip.stats;

import java.util.List;

public interface PageflipStatsCollector {
    
    /**
     * Start a view event. This method starts one view-event, subsequent invocations of this method will be no-op.
     */
    void startView();

    /**
     * Stop the currently active view-event. All active sub-events in the view-event must also be terminated.
     * If there is no active view-event, then just no-op
     */
    void stopView();

    /**
     * Start a zoom-event. If there is no active view-event to append this zoom event to, then start must be invoked
     */
    void startZoom();

    /**
     * Stop the currently active zoom-event.
     * If there is no active zoom-event, then just no-op
     */
    void stopZoom();

    /**
     * A collect, must ensure that all events have been stopped prior to posting the events.
     */
    void collect();

    /**
     * Get the root event of this collector
     * @return An event
     */
    PageEvent getRootEvent();

    /**
     * Get a list of all events in this collector (recursive)
     * @return A list of events
     */
    List<PageEvent> getEvents();

    /**
     * Reset the current state for the events (and events only)
     */
    void reset();

}
