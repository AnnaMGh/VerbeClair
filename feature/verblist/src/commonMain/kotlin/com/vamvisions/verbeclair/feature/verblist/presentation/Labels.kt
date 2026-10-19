package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.feature.verblist.generated.resources.Res
import com.vamvisions.verbeclair.feature.verblist.generated.resources.active_by_auxiliary
import com.vamvisions.verbeclair.feature.verblist.generated.resources.active_by_group
import com.vamvisions.verbeclair.feature.verblist.generated.resources.active_by_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.chip_non_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.chip_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.group_by_auxiliary
import com.vamvisions.verbeclair.feature.verblist.generated.resources.group_by_group
import com.vamvisions.verbeclair.feature.verblist.generated.resources.group_by_none
import com.vamvisions.verbeclair.feature.verblist.generated.resources.group_by_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_auxiliary_avoir
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_auxiliary_both
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_auxiliary_etre
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_frequency_common
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_frequency_rare
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_frequency_uncommon
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_frequency_very_common
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_group_1
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_group_2
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_group_3
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_non_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.label_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sort_a_to_z
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sort_most_frequent
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sort_z_to_a
import org.jetbrains.compose.resources.StringResource

/** Separator of the meta line parts (L-8). */
const val META_SEPARATOR = " · "

fun VerbGroup.label(): StringResource = when (this) {
    VerbGroup.I -> Res.string.label_group_1
    VerbGroup.II -> Res.string.label_group_2
    VerbGroup.III -> Res.string.label_group_3
}

fun Auxiliary.label(): StringResource = when (this) {
    Auxiliary.AVOIR -> Res.string.label_auxiliary_avoir
    Auxiliary.ETRE -> Res.string.label_auxiliary_etre
    Auxiliary.BOTH -> Res.string.label_auxiliary_both
}

fun reflexiveLabel(reflexive: Boolean): StringResource =
    if (reflexive) Res.string.label_reflexive else Res.string.label_non_reflexive

/** Occurrences per million words: 100 or more very common, 10 or more common, 1 or more uncommon, else rare. */
fun frequencyLabel(frequency: Double): StringResource = when {
    frequency >= 100 -> Res.string.label_frequency_very_common
    frequency >= 10 -> Res.string.label_frequency_common
    frequency >= 1 -> Res.string.label_frequency_uncommon
    else -> Res.string.label_frequency_rare
}

/** Filter chip label in the sheet and the active choices row (Figma 10:519, 10:320). */
fun AuxiliaryFilter.label(): StringResource = when (this) {
    AuxiliaryFilter.AVOIR -> Res.string.label_auxiliary_avoir
    AuxiliaryFilter.ETRE -> Res.string.label_auxiliary_etre
}

fun reflexiveChipLabel(reflexive: Boolean): StringResource =
    if (reflexive) Res.string.chip_reflexive else Res.string.chip_non_reflexive

/** Group by chip label in the sheet. */
fun GroupBy.label(): StringResource = when (this) {
    GroupBy.NONE -> Res.string.group_by_none
    GroupBy.GROUP -> Res.string.group_by_group
    GroupBy.AUXILIARY -> Res.string.group_by_auxiliary
    GroupBy.REFLEXIVE -> Res.string.group_by_reflexive
}

fun SortOrder.label(): StringResource = when (this) {
    SortOrder.A_TO_Z -> Res.string.sort_a_to_z
    SortOrder.Z_TO_A -> Res.string.sort_z_to_a
    SortOrder.MOST_FREQUENT -> Res.string.sort_most_frequent
}

/** Chip label of an active choice (L-6): "être", "Non-reflexive", "By group", "Most frequent". */
fun ActiveChoice.label(): StringResource = when (this) {
    is ActiveChoice.Group -> group.label()
    is ActiveChoice.Auxiliary -> auxiliary.label()
    is ActiveChoice.Reflexive -> reflexiveChipLabel(reflexive)
    is ActiveChoice.Grouping -> when (groupBy) {
        GroupBy.NONE -> Res.string.group_by_none
        GroupBy.GROUP -> Res.string.active_by_group
        GroupBy.AUXILIARY -> Res.string.active_by_auxiliary
        GroupBy.REFLEXIVE -> Res.string.active_by_reflexive
    }
    is ActiveChoice.Sort -> sort.label()
}
