package org.niikage.planr.features.eventparticipants.query

data class EventParticipantsList(
    val count: Int,
    val participants: List<EventParticipantView>
) {
    companion object {
        fun empty(): EventParticipantsList {
            return EventParticipantsList(
                count = 0,
                participants = emptyList()
            )
        }
    }
}