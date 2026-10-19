package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewModel
import com.example.data.Course
import com.example.data.NoteItem
import com.example.data.RealDateTimeUtils
import com.example.ui.theme.*

enum class NotePageMode {
    VIEWER,
    EDITOR
}

@Composable
fun NoteEditorViewerScreen(
    note: NoteItem?,
    courses: List<Course>,
    viewModel: AppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val accent = LocalAccentColor.current
    var mode by remember { mutableStateOf(if (note == null) NotePageMode.EDITOR else NotePageMode.VIEWER) }
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var courseCode by remember(note) { mutableStateOf(note?.courseCode ?: courses.firstOrNull()?.code ?: "") }
    var tagsInput by remember(note) { mutableStateOf(note?.tags?.joinToString(", ") ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val wordCount = remember(content) {
        content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val readTimeMin = remember(wordCount) {
        (wordCount / 180).coerceAtLeast(1)
    }

    val palette = LocalThemePalette.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("note_full_screen_page")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-End Minimalist Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(38.dp)
                            .liquidGlass(shape = CircleShape, backgroundColor = Color(0x18FFFFFF))
                            .testTag("note_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Notes / $courseCode",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Sleek Segmented Switcher & Single Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(18.dp)),
                        color = Color(0x16FFFFFF)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (mode == NotePageMode.VIEWER) accent else Color.Transparent)
                                    .clickable { mode = NotePageMode.VIEWER }
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Read",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mode == NotePageMode.VIEWER) GlassObsidianDeep else TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (mode == NotePageMode.EDITOR) accent else Color.Transparent)
                                    .clickable { mode = NotePageMode.EDITOR }
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mode == NotePageMode.EDITOR) GlassObsidianDeep else TextSecondary
                                )
                            }
                        }
                    }

                    if (note != null) {
                        IosDeleteButton(
                            onClick = { showDeleteConfirm = true },
                            contentDescription = "Delete Note"
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x14FFFFFF))

            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "note_mode_switch"
            ) { currentMode ->
                if (currentMode == NotePageMode.VIEWER) {
                    FancyMobbinNoteViewer(
                        title = title,
                        content = content,
                        courseCode = courseCode,
                        tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        wordCount = wordCount,
                        readTimeMin = readTimeMin,
                        accent = accent,
                        onEdit = { mode = NotePageMode.EDITOR },
                        onConvertToReviewer = {
                            val tempNote = note ?: NoteItem(
                                id = "temp",
                                title = title,
                                content = content,
                                courseCode = courseCode,
                                tags = tagsInput.split(","),
                                lastEdited = "Now"
                            )
                            viewModel.convertNoteToReviewer(tempNote)
                            onClose()
                        },
                        onToggleCheckbox = { lineIndex, currentLine ->
                            val lines = content.lines().toMutableList()
                            if (lineIndex in lines.indices) {
                                val toggled = if (currentLine.contains("- [ ]")) {
                                    currentLine.replace("- [ ]", "- [x]")
                                } else {
                                    currentLine.replace("- [x]", "- [ ]")
                                }
                                lines[lineIndex] = toggled
                                content = lines.joinToString("\n")
                                if (note != null) {
                                    viewModel.saveNote(
                                        title = title,
                                        content = content,
                                        courseCode = courseCode,
                                        tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    )
                                }
                            }
                        }
                    )
                } else {
                    FancyMobbinNoteEditor(
                        title = title,
                        onTitleChange = { title = it },
                        content = content,
                        onContentChange = { content = it },
                        courseCode = courseCode,
                        onCourseChange = { courseCode = it },
                        tagsInput = tagsInput,
                        onTagsChange = { tagsInput = it },
                        courses = courses,
                        accent = accent,
                        wordCount = wordCount,
                        readTimeMin = readTimeMin,
                        onSave = {
                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.saveNote(
                                title = title.ifBlank { "Untitled Note" },
                                content = content,
                                courseCode = courseCode,
                                tags = tagsList
                            )
                            Toast.makeText(context, "Note Saved", Toast.LENGTH_SHORT).show()
                            mode = NotePageMode.VIEWER
                        }
                    )
                }
            }
        }

        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete Note",
                message = "Permanently remove '${title.ifBlank { "Untitled" }}'? This cannot be undone.",
                onConfirm = {
                    if (note != null) {
                        viewModel.deleteNote(note.id)
                    }
                    showDeleteConfirm = false
                    onClose()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

// ---------------- Mobbin-Grade Fancy Viewer ----------------

@Composable
private fun FancyMobbinNoteViewer(
    title: String,
    content: String,
    courseCode: String,
    tags: List<String>,
    wordCount: Int,
    readTimeMin: Int,
    accent: Color,
    onEdit: () -> Unit,
    onConvertToReviewer: () -> Unit,
    onToggleCheckbox: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fancy_note_viewer_page"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Document Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CourseBadge(code = courseCode)
                    Text(
                        text = "$wordCount words • $readTimeMin min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accent.copy(alpha = 0.14f))
                                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action CTA Row (ONE authoritative pair)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    onClick = onEdit,
                    text = "Edit Note",
                    icon = Icons.Default.Edit,
                    accentColor = Color(0x30FFFFFF),
                    modifier = Modifier.weight(1f)
                )
                LiquidGlassButton(
                    onClick = onConvertToReviewer,
                    text = "Generate Flashcards",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = accent,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        // Rendered Markdown Content Body
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x0EFFFFFF),
                        borderBrush = LiquidGlassBorderBrush
                    )
                    .padding(20.dp)
            ) {
                RenderMarkdownContent(
                    markdown = content,
                    accent = accent,
                    onToggleCheckbox = onToggleCheckbox
                )
            }
        }
    }
}

// ---------------- Mobbin-Grade Fancy Editor ----------------

@Composable
private fun FancyMobbinNoteEditor(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    courseCode: String,
    onCourseChange: (String) -> Unit,
    tagsInput: String,
    onTagsChange: (String) -> Unit,
    courses: List<Course>,
    accent: Color,
    wordCount: Int,
    readTimeMin: Int,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("note_editor_workspace")
    ) {
        // Course Selector Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            val list = if (courses.isEmpty()) listOf(Course("GEN100", "General Studies")) else courses
            items(list) { course ->
                val isSelected = courseCode == course.code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accent.copy(alpha = 0.25f) else Color(0x12FFFFFF))
                        .border(1.dp, if (isSelected) accent else Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { onCourseChange(course.code) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${course.code} • ${course.name}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accent else TextSecondary
                    )
                }
            }
        }

        // Title Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(16.dp), backgroundColor = Color(0x12FFFFFF))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_editor_input"),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Title your study document...",
                            style = TextStyle(color = TextMuted, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sticky Keyboard Formatting Toolbar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(14.dp), backgroundColor = Color(0x18FFFFFF))
                .padding(6.dp)
        ) {
            item {
                ToolbarPill(label = "B", fontStyle = FontStyle.Normal) {
                    onContentChange("$content **bold text**")
                }
            }
            item {
                ToolbarPill(label = "I", fontStyle = FontStyle.Italic) {
                    onContentChange("$content *italic text*")
                }
            }
            item {
                ToolbarPill(label = "H1") {
                    onContentChange("$content\n# Heading 1\n")
                }
            }
            item {
                ToolbarPill(label = "H2") {
                    onContentChange("$content\n## Heading 2\n")
                }
            }
            item {
                ToolbarPill(label = "==Highlight==") {
                    onContentChange("$content ==highlighted key concept==")
                }
            }
            item {
                ToolbarPill(label = "☑ Task") {
                    onContentChange("$content\n- [ ] ")
                }
            }
            item {
                ToolbarPill(label = "• Bullet") {
                    onContentChange("$content\n- ")
                }
            }
            item {
                ToolbarPill(label = "💡 Callout") {
                    onContentChange("$content\n> 💡 **Core Principle**: ")
                }
            }
            item {
                ToolbarPill(label = "{ } Code") {
                    onContentChange("$content\n```cpp\n// write code here\n```\n")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Body Input Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .liquidGlass(shape = RoundedCornerShape(18.dp), backgroundColor = Color(0x0CFFFFFF))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                cursorBrush = SolidColor(accent),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("note_body_editor_input"),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Start writing lecture notes or paste syllabus outlines...\nUse # Headings, ==highlight==, - [ ] Checklist, > 💡 Callout",
                            style = TextStyle(color = TextMuted, fontSize = 15.sp, lineHeight = 22.sp)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Authoritative Save Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$wordCount words • ~$readTimeMin min read",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            LiquidGlassButton(
                onClick = onSave,
                text = "Save Note",
                icon = Icons.Default.Check,
                accentColor = accent,
                modifier = Modifier.testTag("save_note_editor_btn")
            )
        }
    }
}

@Composable
private fun ToolbarPill(
    label: String,
    fontStyle: FontStyle = FontStyle.Normal,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x18FFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontStyle = fontStyle,
            color = TextPrimary
        )
    }
}

// ---------------- Fancy Markdown Content Parser ----------------

@Composable
private fun RenderMarkdownContent(
    markdown: String,
    accent: Color,
    onToggleCheckbox: (Int, String) -> Unit
) {
    val lines = markdown.lines()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        var insideCodeBlock = false
        val codeBuffer = mutableListOf<String>()

        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()

            if (line.startsWith("```")) {
                if (insideCodeBlock) {
                    RenderCodeBlock(code = codeBuffer.joinToString("\n"))
                    codeBuffer.clear()
                    insideCodeBlock = false
                } else {
                    insideCodeBlock = true
                }
                return@forEachIndexed
            }

            if (insideCodeBlock) {
                codeBuffer.add(rawLine)
                return@forEachIndexed
            }

            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.startsWith("> ") -> {
                    // Refractive Callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = Color(0x18FFFFFF),
                                borderBrush = SolidColor(accent.copy(0.45f))
                            )
                            .padding(12.dp)
                    ) {
                        RenderInlineHighlightedText(text = line.removePrefix("> "), accent = accent)
                    }
                }
                line.startsWith("- [ ]") || line.startsWith("- [x]") -> {
                    val isChecked = line.startsWith("- [x]")
                    val textAfter = line.substring(5).trim()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCheckbox(index, line) }
                            .padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleCheckbox(index, line) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = accent,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = GlassObsidianDeep
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RenderInlineHighlightedText(text = textAfter, accent = accent)
                    }
                }
                line.startsWith("- ") || line.startsWith("• ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp)) {
                        Text("•", color = accent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        RenderInlineHighlightedText(text = line.drop(2), accent = accent)
                    }
                }
                line == "---" -> {
                    Divider(color = Color(0x20FFFFFF), modifier = Modifier.padding(vertical = 6.dp))
                }
                line.isNotBlank() -> {
                    RenderInlineHighlightedText(text = line, accent = accent)
                }
            }
        }

        if (insideCodeBlock && codeBuffer.isNotEmpty()) {
            RenderCodeBlock(code = codeBuffer.joinToString("\n"))
        }
    }
}

@Composable
private fun RenderCodeBlock(code: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = Color(0xFF060912),
                borderBrush = SolidColor(Color(0x28FFFFFF))
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Code Block", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = LiquidMint
                )
            }
        }
    }
}

@Composable
private fun RenderInlineHighlightedText(text: String, accent: Color) {
    if (text.contains("==")) {
        val builder = AnnotatedString.Builder()
        val parts = text.split("==")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                builder.pushStyle(
                    androidx.compose.ui.text.SpanStyle(
                        background = accent.copy(alpha = 0.35f),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                builder.append(parts[i])
                builder.pop()
            } else {
                builder.append(parts[i].replace("**", "").replace("*", ""))
            }
        }
        Text(builder.toAnnotatedString(), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    } else {
        Text(text.replace("**", "").replace("*", ""), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
