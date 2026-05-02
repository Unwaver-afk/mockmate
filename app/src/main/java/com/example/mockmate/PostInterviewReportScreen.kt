package com.example.mockmate

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.json.JSONArray
import org.json.JSONObject

data class ReportData(
    val roleCompany: String,
    val date: String,
    val score: Int,
    val summary: String,
    val communication: Int,
    val relevance: Int,
    val technical: Int,
    val focus: List<FocusItem>,
    val answers: List<AnswerFeedback>
)

data class FocusItem(
    val title: String,
    val description: String
)

data class AnswerFeedback(
    val question: String,
    val answer: String,
    val idealAnswer: String,
    val score: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostInterviewReportScreen(
    navController: NavController,
    reportJson: String
) {
    val report = remember(reportJson) { parseReport(reportJson) }
    val themeController = LocalThemeController.current
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mockly",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = themeController.toggle) {
                        Icon(
                            imageVector = if (themeController.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ReportHeader(
                    report = report,
                    context = context,
                    onBackToDashboard = {
                        navController.navigate("dashboard") {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
            item { OverallScoreCard(report = report) }
            item { PerformanceBreakdownCard(report = report) }
            item { FocusCard(items = report.focus) }
            item {
                Text(
                    text = "Detailed Question Feedback",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            itemsIndexed(report.answers) { index, answer ->
                QuestionFeedbackCard(index = index + 1, feedback = answer)
            }
        }
    }
}

@Composable
private fun ReportHeader(
    report: ReportData,
    context: Context,
    onBackToDashboard: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = report.roleCompany.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp, letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Post-Interview Report",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = report.date,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                onClick = { shareReport(context, report) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Share Results", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Surface(
                onClick = onBackToDashboard,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dashboard, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Dashboard", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallScoreCard(report: ReportData) {
    ReportCardContainer {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Overall Score",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                ScoreCanvas(score = report.score)
            }
            Text(
                text = "OUT OF 100",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoreCanvas(score: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 10.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = trackColor, startAngle = -90f, sweepAngle = 360f,
            useCenter = false, topLeft = topLeft, size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = primaryColor, startAngle = -90f,
            sweepAngle = 360f * (score / 100f),
            useCenter = false, topLeft = topLeft, size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textColor.hashCode()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 48.sp.toPx()
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        val baseline = center.y - (paint.descent() + paint.ascent()) / 2f
        drawContext.canvas.nativeCanvas.drawText(score.toString(), center.x, baseline, paint)
    }
}

@Composable
private fun PerformanceBreakdownCard(report: ReportData) {
    ReportCardContainer {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Performance Breakdown", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            BreakdownRow("Communication", report.communication)
            BreakdownRow("Relevance", report.relevance)
            BreakdownRow("Technical Accuracy", report.technical)
        }
    }
}

@Composable
private fun BreakdownRow(label: String, score: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text("$score/100", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
private fun FocusCard(items: List<FocusItem>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                }
                Text("Areas to Focus On", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            items.forEachIndexed { index, item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "%02d".format(index + 1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Text(item.description, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionFeedbackCard(index: Int, feedback: AnswerFeedback) {
    var expanded by remember { mutableStateOf(false) }
    val isGood = feedback.score >= 70

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Score + Question label
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    "QUESTION $index",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(if (isGood) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        feedback.score.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isGood) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(feedback.question, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)

            // Your Answer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Your Answer", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(feedback.answer, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Ideal answer toggle
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { expanded = !expanded }.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Coach's Ideal Structure", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Text(
                        feedback.idealAnswer,
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportCardContainer(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp)) { content() }
    }
}

private fun parseReport(reportJson: String): ReportData {
    val json = runCatching { JSONObject(reportJson) }.getOrElse { JSONObject(defaultReportJson) }
    val focusJson = json.optJSONArray("focus") ?: JSONArray()
    val answersJson = json.optJSONArray("answers") ?: JSONArray()
    return ReportData(
        roleCompany = json.optString("roleCompany", "Software Engineer - Front End"),
        date = json.optString("date", "Completed on October 24, 2023"),
        score = json.optInt("score", 85),
        summary = json.optString("summary", "Strong performance! You demonstrated excellent problem-solving skills but could improve conciseness."),
        communication = json.optInt("communication", 92),
        relevance = json.optInt("relevance", 88),
        technical = json.optInt("technical", 75),
        focus = List(focusJson.length()) { index ->
            val item = focusJson.optJSONObject(index) ?: JSONObject()
            FocusItem(
                title = item.optString("title", "Clarify Constraints"),
                description = item.optString("description", "Name scope and tradeoffs before solving.")
            )
        }.ifEmpty { defaultFocus },
        answers = List(answersJson.length()) { index ->
            val item = answersJson.optJSONObject(index) ?: JSONObject()
            AnswerFeedback(
                question = item.optString("question", "Walk me through your approach."),
                answer = item.optString("answer", "I clarified the goal and proposed a structured answer."),
                idealAnswer = item.optString("idealAnswer", "Clarify the goal, structure the answer, and close with measurable impact."),
                score = item.optInt("score", 82)
            )
        }.ifEmpty { defaultAnswers }
    )
}

private val defaultFocus = listOf(
    FocusItem("Algorithm Optimization", "Your initial solutions were brute-force. Practice identifying optimal time complexities faster."),
    FocusItem("STAR Method Structure", "Behavioral answers lacked the 'Result' phase. Ensure you close every story with measurable impact."),
    FocusItem("System Design Depth", "Missed discussing database scaling strategies in the high-level architecture question.")
)

private val defaultAnswers = listOf(
    AnswerFeedback(
        question = "Tell me about a time you had to resolve a conflict with a senior team member.",
        answer = "I once disagreed with a senior dev on using Redux versus Context API. I felt Context was simpler for our use case. I built a quick prototype to show the performance difference, and after reviewing the code together, he agreed we could go with Context.",
        idealAnswer = "Strong use of data (prototype) to prove a point. Next time, explicitly structure this using the STAR method. State the Situation (project deadlines), the Task (choosing state management), the Action (building prototype), and the Result (saved 2 days of dev time, team alignment).",
        score = 90
    ),
    AnswerFeedback(
        question = "Explain how you would design a URL shortener system like Bitly.",
        answer = "I would use a relational database to store the original URL and generate a random 7-character string for the short URL. I'd set up a Node.js server to handle incoming requests, look up the short URL in the DB, and redirect the user.",
        idealAnswer = "You missed crucial capacity estimations and scaling discussions. You should discuss: 1) Read/Write ratio (heavy reads), 2) Data capacity estimations, 3) Base62 encoding strategy, and 4) Database partitioning strategies.",
        score = 50
    )
)

private const val defaultReportJson = """
{
  "roleCompany": "Software Engineer - Front End",
  "date": "Completed on October 24, 2023",
  "score": 85,
  "summary": "Strong performance! You demonstrated excellent problem-solving skills but could improve conciseness.",
  "communication": 92,
  "relevance": 88,
  "technical": 75
}
"""

private fun shareReport(context: Context, report: ReportData) {
    val focusText = report.focus.joinToString("\n") { "  • ${it.title}: ${it.description}" }
    val shareText = buildString {
        appendLine("🎯 Mockly Interview Report")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("📋 ${report.roleCompany}")
        appendLine("📅 ${report.date}")
        appendLine()
        appendLine("🏆 Overall Score: ${report.score}/100")
        appendLine()
        appendLine("📊 Performance Breakdown:")
        appendLine("  💬 Communication: ${report.communication}/100")
        appendLine("  🎯 Relevance: ${report.relevance}/100")
        appendLine("  ⚙️ Technical: ${report.technical}/100")
        appendLine()
        appendLine("📝 Summary: ${report.summary}")
        appendLine()
        appendLine("🔍 Focus Areas:")
        appendLine(focusText)
        appendLine()
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("Powered by Mockly — AI Mock Interview Coach")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Mockly Interview Report — ${report.roleCompany}")
        type = "text/plain"
    }
    val chooser = Intent.createChooser(sendIntent, "Share your report")
    context.startActivity(chooser)
}
