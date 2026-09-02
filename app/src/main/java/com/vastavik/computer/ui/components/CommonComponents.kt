package com.vastavik.computer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vastavik.computer.data.model.*
import com.vastavik.computer.ui.theme.VastavikColors

@Composable
fun VastavikCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Int = 4,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(modifier = modifier.padding(end = 5.dp, bottom = 5.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .clip(shape)
                .background(Color.Black)
        )
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun VastavikButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val containerColor = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
        ButtonVariant.Outlined -> Color.Transparent
        ButtonVariant.Error -> MaterialTheme.colorScheme.error
    }
    val contentColor = when (variant) {
        ButtonVariant.Primary -> Color.White
        ButtonVariant.Secondary -> Color.White
        ButtonVariant.Outlined -> MaterialTheme.colorScheme.primary
        ButtonVariant.Error -> Color.White
    }
    val borderColor = when (variant) {
        ButtonVariant.Outlined -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (variant == ButtonVariant.Outlined) {
            ButtonDefaults.outlinedButtonBorder(enabled = enabled)
        } else null,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

enum class ButtonVariant { Primary, Secondary, Outlined, Error }

@Composable
fun VastavikTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = if (label.isNotEmpty()) {{ Text(label) }} else null,
        placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }} else null,
        leadingIcon = if (leadingIcon != null) {
            { Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            { IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }}
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        isError = isError,
        supportingText = if (supportingText != null) {{ Text(supportingText) }} else null,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "Enter password"
) {
    var passwordVisible by remember { mutableStateOf(false) }
    VastavikTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
        onTrailingIconClick = { passwordVisible = !passwordVisible },
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(VastavikColors.LightPrimary, VastavikColors.LightAccent),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(colors))
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            VastavikButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Outlined.Inbox,
    message: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            VastavikButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int? = null
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            if (badge != null && badge > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(if (badge > 99) "99+" else badge.toString())
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = color)
    }
}

@Composable
fun QuizOptionCard(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCorrect == true -> VastavikColors.LightSuccess.copy(alpha = 0.15f)
        isCorrect == false && isSelected -> VastavikColors.LightError.copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isCorrect == true -> VastavikColors.LightSuccess
        isCorrect == false && isSelected -> VastavikColors.LightError
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val iconTint = when {
        isCorrect == true -> VastavikColors.LightSuccess
        isCorrect == false && isSelected -> VastavikColors.LightError
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).takeIf { isCorrect != null || isSelected }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + index).toString(),
                    color = iconTint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isCorrect != null) {
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.RadioButtonChecked,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: LessonModel,
    index: Int,
    isCompleted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) VastavikColors.LightSuccess.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = VastavikColors.LightSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (lesson.duration.isNotEmpty()) {
                    Text(
                        text = lesson.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CourseCard(
    course: CourseModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(course.color),
                                Color(course.color).copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (course.iconName) {
                        "code" -> Icons.Filled.Code
                        "web" -> Icons.Filled.Language
                        "database" -> Icons.Filled.Storage
                        "mobile" -> Icons.Filled.PhoneAndroid
                        "ai" -> Icons.Filled.SmartToy
                        "security" -> Icons.Filled.Security
                        else -> Icons.Filled.Computer
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (course.description.isNotEmpty()) {
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQueryChange("")
                    onClear?.invoke()
                }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StatsCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumBanner(
    modifier: Modifier = Modifier,
    title: String = "Upgrade to Premium",
    subtitle: String = "Unlock all courses, quizzes, and AI tutor",
    onClick: () -> Unit
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        colors = listOf(
            Color(0xFF6366F1),
            Color(0xFF8B5CF6),
            Color(0xFFA78BFA)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String = ""
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VastavikColors.CodeBackground)
    ) {
        Column {
            if (language.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language,
                        style = MaterialTheme.typography.labelSmall,
                        color = VastavikColors.CodeComment
                    )
                    IconButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = VastavikColors.CodeComment,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Text(
                text = highlightSyntax(code, language),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                color = VastavikColors.CodeText
            )
        }
    }
}

private fun highlightSyntax(code: String, language: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val keywords = setOf(
            "fun", "val", "var", "class", "object", "if", "else", "when", "for", "while",
            "return", "import", "package", "private", "public", "internal", "protected",
            "override", "abstract", "open", "data", "sealed", "enum", "companion", "suspend",
            "fun", "interface", "typealias", "by", "lazy", "init", "constructor", "this", "super",
            "true", "false", "null", "is", "as", "in", "int", "String", "Boolean", "Double", "Float",
            "List", "Map", "Set", "try", "catch", "finally", "throw", "break", "continue",
            "def", "print", "println", "None", "True", "False", "self", "class", "def", "import",
            "from", "elif", "except", "lambda", "with", "as", "pass", "raise", "yield",
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "TABLE",
            "DROP", "ALTER", "JOIN", "ON", "AND", "OR", "NOT", "NULL", "PRIMARY", "KEY",
            "FOREIGN", "REFERENCES", "INDEX", "VIEW", "GROUP", "BY", "ORDER", "HAVING",
            "INTO", "VALUES", "SET", "LIKE", "BETWEEN", "IN", "EXISTS", "UNION", "ALL",
            "html", "head", "body", "div", "span", "p", "a", "h1", "h2", "h3", "h4", "h5", "h6",
            "ul", "ol", "li", "table", "tr", "td", "th", "form", "input", "button", "img",
            "src", "href", "class", "id", "style", "type", "name", "value", "placeholder",
            "console", "document", "window", "function", "const", "let", "async", "await",
            "Promise", "Array", "Object", "JSON", "parse", "stringify", "log", "error",
            "flex", "grid", "block", "inline", "absolute", "relative", "fixed", "sticky",
            "center", "space-between", "space-around", "row", "column", "wrap",
            "rgba", "hex", "rgb", "hsl", "transparent", "solid", "dashed", "dotted",
            "margin", "padding", "border", "background", "color", "font", "display",
            "position", "width", "height", "top", "left", "right", "bottom",
            "float", "clear", "overflow", "z-index", "opacity", "transform",
            "transition", "animation", "keyframes", "media", "query", "import", "from",
            "export", "default", "module", "require", "extends", "implements",
            "try", "catch", "finally", "throw", "new", "delete", "typeof", "instanceof",
            "void", "switch", "case", "default", "do", "for", "in", "of",
            "class", "extends", "super", "new", "static", "get", "set",
            "public", "private", "protected", "readonly", "abstract", "interface",
            "type", "enum", "implements", "declare", "namespace", "module",
            "any", "unknown", "never", "undefined", "null", "true", "false"
        )
        val builtins = setOf(
            "print", "println", "String", "Int", "Float", "Double", "Boolean", "Long",
            "List", "MutableList", "Map", "MutableMap", "Set", "MutableSet",
            "Array", "Pair", "Triple", "Range", "CharSequence", "StringBuilder",
            "ArrayList", "HashMap", "HashSet", "LinkedList", "LinkedHashMap",
            "console", "document", "window", "Math", "JSON", "Date", "RegExp",
            "Error", "TypeError", "RangeError", "SyntaxError",
            "Promise", "Map", "Set", "WeakMap", "WeakSet", "Symbol", "Proxy",
            "Reflect", "Intl", "WebAssembly",
            "None", "True", "False", "self", "cls", "super", "print", "len", "range",
            "enumerate", "zip", "map", "filter", "sorted", "reversed", "list", "dict",
            "set", "tuple", "str", "int", "float", "bool", "type", "isinstance",
            "hasattr", "getattr", "setattr", "property", "staticmethod", "classmethod",
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "TABLE",
            "JOIN", "ON", "AND", "OR", "NOT", "NULL", "PRIMARY", "KEY",
            "INT", "VARCHAR", "TEXT", "BOOLEAN", "FLOAT", "DOUBLE", "DATE", "TIMESTAMP",
            "NOW", "COUNT", "SUM", "AVG", "MIN", "MAX", "GROUP_CONCAT",
            "TRUE", "FALSE", "None", "null", "undefined", "NaN", "Infinity",
            "var", "let", "const", "function", "return", "if", "else", "for", "while",
            "switch", "case", "break", "continue", "try", "catch", "finally", "throw",
            "new", "delete", "typeof", "instanceof", "in", "of", "void", "this", "super",
            "class", "extends", "import", "export", "default", "from", "as",
            "async", "await", "yield", "static", "get", "set",
            "public", "private", "protected", "readonly", "abstract", "interface",
            "type", "enum", "implements", "declare", "namespace", "module"
        )
        val lines = code.split("\n")
        var inMultilineComment = false

        for (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            var i = 0
            while (i < line.length) {
                if (inMultilineComment) {
                    val endIdx = line.indexOf("*/", i)
                    if (endIdx != -1) {
                        append(line.substring(i, endIdx + 2))
                        i = endIdx + 2
                        inMultilineComment = false
                    } else {
                        append(line.substring(i))
                        break
                    }
                    continue
                }

                when {
                    line.startsWith("//", i) -> {
                        withStyle(SpanStyle(color = VastavikColors.CodeComment)) {
                            append(line.substring(i))
                        }
                        break
                    }
                    line.startsWith("/*", i) -> {
                        val endIdx = line.indexOf("*/", i + 2)
                        if (endIdx != -1) {
                            withStyle(SpanStyle(color = VastavikColors.CodeComment)) {
                                append(line.substring(i, endIdx + 2))
                            }
                            i = endIdx + 2
                        } else {
                            withStyle(SpanStyle(color = VastavikColors.CodeComment)) {
                                append(line.substring(i))
                            }
                            inMultilineComment = true
                            break
                        }
                    }
                    line.startsWith("#", i) -> {
                        withStyle(SpanStyle(color = VastavikColors.CodeComment)) {
                            append(line.substring(i))
                        }
                        break
                    }
                    line[i] == '"' || line[i] == '\'' || line[i] == '`' -> {
                        val quote = line[i]
                        var j = i + 1
                        while (j < line.length && line[j] != quote) {
                            if (line[j] == '\\') j++
                            j++
                        }
                        if (j < line.length) j++
                        withStyle(SpanStyle(color = VastavikColors.CodeString)) {
                            append(line.substring(i, j))
                        }
                        i = j
                    }
                    line[i].isDigit() -> {
                        var j = i
                        while (j < line.length && (line[j].isDigit() || line[j] == '.')) j++
                        withStyle(SpanStyle(color = VastavikColors.CodeNumber)) {
                            append(line.substring(i, j))
                        }
                        i = j
                    }
                    line.substring(i).let { sub ->
                        keywords.any { sub.startsWith(it) && (i + it.length >= line.length || !line[i + it.length].isLetterOrDigit()) }
                    } -> {
                        val matched = keywords.first { sub ->
                            line.substring(i).startsWith(sub) && (i + sub.length >= line.length || !line[i + sub.length].isLetterOrDigit())
                        }
                        withStyle(SpanStyle(color = VastavikColors.CodeKeyword, fontWeight = FontWeight.Bold)) {
                            append(matched)
                        }
                        i += matched.length
                    }
                    line.substring(i).let { sub ->
                        builtins.any { sub.startsWith(it) && (i + it.length >= line.length || !line[i + it.length].isLetterOrDigit()) }
                    } -> {
                        val matched = builtins.first { sub ->
                            line.substring(i).startsWith(sub) && (i + sub.length >= line.length || !line[i + sub.length].isLetterOrDigit())
                        }
                        withStyle(SpanStyle(color = VastavikColors.CodeKeyword)) {
                            append(matched)
                        }
                        i += matched.length
                    }
                    line[i].isLetter() || line[i] == '_' -> {
                        var j = i
                        while (j < line.length && (line[j].isLetterOrDigit() || line[j] == '_')) j++
                        append(line.substring(i, j))
                        i = j
                    }
                    else -> {
                        append(line[i])
                        i++
                    }
                }
            }
            if (lineIndex < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(markdown) {
        parseMarkdownToAnnotatedString(markdown)
    }
    Text(
        text = annotatedString,
        modifier = modifier.padding(8.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun parseMarkdownToAnnotatedString(markdown: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.split("\n")
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()

        for (line in lines) {
            when {
                line.trimStart().startsWith("```") -> {
                    if (inCodeBlock) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = VastavikColors.CodeText,
                            background = VastavikColors.CodeBackground
                        )) {
                            append(codeBlockContent.toString().trimEnd())
                        }
                        codeBlockContent = StringBuilder()
                        inCodeBlock = false
                    } else {
                        inCodeBlock = true
                    }
                    append("\n")
                }
                inCodeBlock -> {
                    codeBlockContent.appendLine(line)
                }
                line.trimStart().startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                        append(line.removePrefix("# ").trim())
                    }
                    append("\n")
                }
                line.trimStart().startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                        append(line.removePrefix("## ").trim())
                    }
                    append("\n")
                }
                line.trimStart().startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append(line.removePrefix("### ").trim())
                    }
                    append("\n")
                }
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    append("  \u2022 ")
                    val content = line.trimStart().removePrefix("- ").removePrefix("* ").trim()
                    appendFormattedText(content)
                    append("\n")
                }
                line.trimStart().startsWith("1. ") || line.trimStart().startsWith("2. ") ||
                    line.trimStart().startsWith("3. ") || line.trimStart().startsWith("4. ") ||
                    line.trimStart().startsWith("5. ") || line.trimStart().startsWith("6. ") ||
                    line.trimStart().startsWith("7. ") || line.trimStart().startsWith("8. ") ||
                    line.trimStart().startsWith("9. ") -> {
                    val dotIndex = line.indexOf(". ")
                    if (dotIndex != -1) {
                        append("  ${line.substring(0, dotIndex + 1)} ")
                        val content = line.substring(dotIndex + 2).trim()
                        appendFormattedText(content)
                        append("\n")
                    }
                }
                line.trimStart().startsWith("> ") -> {
                    withStyle(SpanStyle(
                        color = VastavikColors.CodeComment,
                        fontStyle = FontStyle.Italic
                    )) {
                        append("  | ${line.removePrefix("> ").trim()}")
                    }
                    append("\n")
                }
                line.trim() == "---" || line.trim() == "***" -> {
                    append("────────────────────────────────\n")
                }
                else -> {
                    appendFormattedText(line)
                    append("\n")
                }
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendFormattedText(text: String) {
    var remaining = text
    while (remaining.isNotEmpty()) {
        val boldMatch = Regex("\\*\\*(.+?)\\*\\*").find(remaining)
        val italicMatch = Regex("\\*(.+?)\\*").find(remaining)
        val codeMatch = Regex("`(.+?)`").find(remaining)
        val linkMatch = Regex("\\[(.+?)\\]\\((.+?)\\)").find(remaining)

        val firstMatch = listOfNotNull(boldMatch, italicMatch, codeMatch, linkMatch)
            .minByOrNull { it.range.first }

        if (firstMatch == null) {
            append(remaining)
            break
        }

        if (firstMatch.range.first > 0) {
            append(remaining.substring(0, firstMatch.range.first))
        }

        when (firstMatch) {
            boldMatch -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(firstMatch.groupValues[1])
                }
            }
            italicMatch -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(firstMatch.groupValues[1])
                }
            }
            codeMatch -> {
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = VastavikColors.CodeKeyword,
                    background = VastavikColors.CodeBackground.copy(alpha = 0.3f)
                )) {
                    append(firstMatch.groupValues[1])
                }
            }
            linkMatch -> {
                withStyle(SpanStyle(
                    color = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )) {
                    append(firstMatch.groupValues[1])
                }
            }
        }

        remaining = remaining.substring(firstMatch.range.last + 1)
    }
}
