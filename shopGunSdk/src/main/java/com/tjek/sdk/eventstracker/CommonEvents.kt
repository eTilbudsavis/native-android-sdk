package com.tjek.sdk.eventstracker

import com.tjek.sdk.api.Id
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
    The dummy event used for testing purposes.
    - timestamp: when the event occurred. Defaults to now.
 */
fun dummy(timestamp: Long = timestamp()): Event {
    return Event(timestamp = timestamp, type = EventType.Dummy.code)
}

/**
    The event when a paged publication has been "opened" by a user.
    In general, "opening" a paged publication means any action that results in the paged publication being presented for browsing,
    which would also result in a paged publication page event.
    - publicationId: The uuid of the publication.
    - timestamp: when the event occurred. Defaults to now.
 */
fun pagedPublicationOpened(
    publicationId: Id,
    timestamp: Long = timestamp(),
): Event {
    val payload: PayloadType = mapOf(Pair("pp.id", publicationId))
    return Event(
        timestamp =  timestamp,
        type = EventType.PagedPublicationOpened.code,
        payloadType = payload
    ).apply { addViewToken(generateViewToken(publicationId.toByteArray(Charsets.UTF_8))) }
}

/**
    The event when a paged publication page has been "presented" to the user.
    "presented" in this context means any action that results in the paged publication page being drawn to the screen.
    - publicationId: The uuid of the publication.
    - pageNumber: The (1-indexed) number of the opened page.
    - timestamp: when the event occurred. Defaults to now.
 */
fun pagedPublicationPageOpened(
    publicationId: Id,
    pageNumber: Int,
    timestamp: Long = timestamp()
): Event {
    val payloadType: PayloadType = mapOf(
        Pair("pp.id", publicationId),
        Pair("ppp.n", pageNumber)
    )

    // Concatenate the bytes of the publicationId and the pageNumber to generate the viewToken
    val b = ByteBuffer.allocate(4)
    b.order(ByteOrder.BIG_ENDIAN)
    b.putInt(pageNumber)
    val pageBytes = b.array()
    val ppIdBytes = publicationId.toByteArray(Charsets.UTF_8)
    val viewTokenContent = ByteArray(ppIdBytes.size + pageBytes.size)
    System.arraycopy(ppIdBytes, 0, viewTokenContent, 0, ppIdBytes.size)
    System.arraycopy(pageBytes, 0, viewTokenContent, ppIdBytes.size, pageBytes.size)

    return Event(
        timestamp = timestamp,
        type = EventType.PagedPublicationPageOpened.code,
        payloadType = payloadType
    ).apply { addViewToken(generateViewToken(viewTokenContent)) }
}

/**
    The event when an incito publication has been "opened" by a user. In general, "opening" an incito publication means any action that results in the incito's contents being presented for browsing.
    - incitoId: The uuid of the incito.
    - timestamp: when the event occurred. Defaults to now.
 */
fun incitoPublicationOpened(
    incitoId: Id,
    timestamp: Long = timestamp()
): Event {
    val payload: PayloadType = mapOf(Pair("ip.id", incitoId))
    return Event(
        timestamp =  timestamp,
        type = EventType.IncitoPublicationOpenedV2.code,
        payloadType = payload
    ).apply { addViewToken(generateViewToken(incitoId.toByteArray(Charsets.UTF_8))) }
}