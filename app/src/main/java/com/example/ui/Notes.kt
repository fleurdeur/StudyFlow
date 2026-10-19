package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewModel
import com.example.StudyFlowUiState
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun NotesScreen(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val courseFilters = listOf("All") + state.courses.map { it.code }
    var noteToDelete by remember { mutableStateOf<NoteItem?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val palette = LocalThemePalette.current
    val accent = palette.accent
    val isSelectMode = state.isSelectionMode && state.activeSelectionModule == SortModule.NOTES
    val sortFilter = state.sortFilters[SortModule.NOTES] ?: UnifiedSortFilterState(SortModule.NOTES)

    val filteredNotes = remember(state.notes, sortFilter, state.noteSearchQuery) {
        val sorted = UnifiedSortEngine.filterAndSortNotes(state.notes, sortFilter)
        if (state.noteSearchQuery.isBlank()) sorted
        else sorted.filter {
            it.title.contains(state.noteSearchQuery, ignoreCase = true) ||
            it.content.contains(state.noteSearchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(state.noteSearchQuery, ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("notes_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Sort & Select Mode (Plus button repositioned to bottom right)
            item {
                IosLargeTitleHeader(
                    title = "Notes",
                    subtitle = "${filteredNotes.size} study notes in vault",
                    onSortClick = { viewModel.openSortFilterSheet(SortModule.NOTES) },
                    onSelectClick = { viewModel.toggleSelectionMode(SortModule.NOTES) },
                    isSelectActive = isSelectMode
                )
            }

            // Multi-Selection Action Bar
            if (isSelectMode) {
                item {
                    IosSelectionBar(
                        selectedCount = state.selectedItemIds.size,
                        totalCount = filteredNotes.size,
                        onSelectAllToggle = {
                            viewModel.toggleSelectAll(filteredNotes.map { it.id })
                        },
                        onDeleteSelected = { showBatchDeleteConfirm = true },
                        onExitSelectMode = { viewModel.clearSelection() }
                    )
                }
            }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = state.noteSearchQuery,
                            onValueChange = { viewModel.setNoteSearchQuery(it) },
                            placeholder = { Text("Search notes, topics, formulas...", color = palette.textMuted) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = palette.textPrimary,
                                unfocusedTextColor = palette.textPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("notes_search_input"),
                            singleLine = true
                        )
                        if (state.noteSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setNoteSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Course filter chips
            if (courseFilters.size > 1) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(courseFilters) { course ->
                            val isSelected = course == sortFilter.selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accent.copy(alpha = 0.22f) else palette.cardBg)
                                    .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.applySortFilter(
                                            SortModule.NOTES,
                                            sortFilter.copy(selectedCategory = course)
                                        )
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("note_filter_$course")
                            ) {
                                Text(
                                    text = course,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) accent else palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                item {
                    EmptyPlaceholder(
                        icon = Icons.Default.Description,
                        title = "No Notes Found",
                        message = "Tap the + button in the bottom right corner to start authoring a new note."
                    )
                }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    val isChecked = state.selectedItemIds.contains(note.id)

                    IosCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_card_${note.id}"),
                        onClick = {
                            if (isSelectMode) {
                                viewModel.toggleItemSelection(note.id)
                            } else {
                                viewModel.openNotePage(note)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelectMode) {
                                    IosSelectionCheckbox(
                                        isSelected = isChecked,
                                        onToggle = { viewModel.toggleItemSelection(note.id) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                CourseBadge(code = note.courseCode)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = note.lastEdited,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (!isSelectMode) {
                                    IosDeleteButton(
                                        onClick = { noteToDelete = note },
                                        contentDescription = "Delete Note"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.content.replace("#", "").replace(">", "").replace("==", "").trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (note.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(note.tags) { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(palette.hairline)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = palette.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Repositioned Floating Action Button in the Bottom Right
        IosFloatingAddButton(
            onClick = { viewModel.openNotePage(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
            contentDescription = "Create New Note"
        )

        // Confirm Single Delete
        noteToDelete?.let { note ->
            ConfirmDeleteDialog(
                title = "Delete Note",
                message = "Are you sure you want to delete '${note.title}'? This action cannot be undone.",
                onConfirm = {
                    viewModel.deleteNote(note.id)
                    noteToDelete = null
                },
                onDismiss = { noteToDelete = null }
            )
        }

        // Confirm Batch Delete
        if (showBatchDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete Selected Notes",
                message = "Are you sure you want to delete ${state.selectedItemIds.size} selected notes? This cannot be undone.",
                onConfirm = {
                    viewModel.deleteSelectedItems()
                    showBatchDeleteConfirm = false
                },
                onDismiss = { showBatchDeleteConfirm = false }
            )
        }
    }
}
