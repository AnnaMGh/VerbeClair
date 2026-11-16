package com.vamvisions.verbeclair.feature.verblist.presentation

/** One-off happenings on Screen 1 that the screen shows once, outside [VerbListState]. */
sealed interface VerbListEvent {

    /** Adding or removing a favourite failed; the star stays as it was. */
    data object FavouriteUpdateFailed : VerbListEvent
}
