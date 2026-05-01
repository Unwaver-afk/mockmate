package com.example.mockmate

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.mockmate.ui.theme.MocklyColors
import com.example.mockmate.ui.theme.MocklyTheme
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDark = isDarkTheme,
                    toggle = { isDarkTheme = !isDarkTheme }
                )
            ) {
                MocklyTheme(darkTheme = isDarkTheme) {
                    MocklyApp()
                }
            }
        }
    }
}

@Composable
private fun MocklyApp(
    navController: NavHostController = rememberNavController(),
    interviewViewModel: InterviewViewModel = viewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("dashboard") {
                DashboardScreen(navController = navController)
            }
            composable("leaderboard") {
                LeaderboardScreen(navController = navController)
            }
            composable("setup") {
                SetupInterviewScreen(navController = navController)
            }
            composable(
                route = "interview/{company}/{role}/{difficulty}/{type}",
                arguments = listOf(
                    navArgument("company") { type = NavType.StringType },
                    navArgument("role") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val company = backStackEntry.arguments?.getString("company").orEmpty()
                val role = backStackEntry.arguments?.getString("role").orEmpty()
                val difficulty = backStackEntry.arguments?.getString("difficulty").orEmpty()
                val type = backStackEntry.arguments?.getString("type").orEmpty()
                LiveInterviewScreen(
                    navController = navController,
                    company = company,
                    role = role,
                    difficulty = difficulty,
                    interviewType = type
                )
            }
            composable("report") {
                PostInterviewReportScreen(
                    navController = navController,
                    reportJson = interviewViewModel.reportJson()
                )
            }
            composable(
                route = "post_report/{reportJson}",
                arguments = listOf(navArgument("reportJson") { type = NavType.StringType })
            ) { backStackEntry ->
                PostInterviewReportScreen(
                    navController = navController,
                    backStackEntry = backStackEntry
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    title: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

@Composable
private fun SetupScreen(onBegin: (String, String) -> Unit) {
    var role by remember { mutableStateOf("Product Manager") }
    var interviewType by remember { mutableStateOf("Behavioral") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Shape the session.",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        MocklyInput(value = role, onValueChange = { role = it }, label = "Role")
        MocklyInput(value = interviewType, onValueChange = { interviewType = it }, label = "Interview Type")
        TonalCard {
            CoachBubble("Mockly will adapt follow-up prompts to the role and interview type.")
        }
        GradientButton(text = "Begin", icon = Icons.Filled.Mic, onClick = { onBegin(role, interviewType) })
    }
}

@Composable
private fun InterviewScreen(
    viewModel: InterviewViewModel,
    onFinish: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    val question = viewModel.currentQuestion

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ProgressBlock(current = viewModel.currentIndex + 1, total = viewModel.questions.size)
        AiBubble(question)
        UserAnswerBox(value = answer, onValueChange = { answer = it })
        GradientButton(
            text = if (viewModel.isLastQuestion) "Finish" else "Send Answer",
            icon = Icons.AutoMirrored.Filled.Send,
            onClick = {
                viewModel.submit(answer)
                answer = ""
                if (viewModel.isComplete) onFinish()
            }
        )
    }
}

@Composable
private fun ReportScreen(
    score: Int,
    onPracticeAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(96.dp),
                    ambientColor = MocklyColors.TertiaryFixed,
                    spotColor = MocklyColors.TertiaryFixed
                )
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer,
                strokeWidth = 8.dp
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        TonalCard {
            Text(
                text = "Strong structure. Add sharper metrics in your examples and close with a clearer recommendation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GradientButton(text = "Practice Again", icon = Icons.Filled.CheckCircle, onClick = onPracticeAgain)
    }
}

@Composable
private fun TonalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(32.dp)) {
            content()
        }
    }
}

@Composable
private fun CoachBubble(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MocklyColors.SurfaceBright.copy(alpha = 0.6f),
        tonalElevation = 0.dp,
        modifier = Modifier.width(180.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AiBubble(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UserAnswerBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        MocklyInput(
            value = value,
            onValueChange = onValueChange,
            label = "Your Answer",
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun MocklyInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.2f),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun GradientButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardContent: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(24.dp)) {
            LabelText(title)
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) { cardContent() }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) { cardContent() }
    }
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProgressBlock(current: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelText("Question $current of $total")
        LinearProgressIndicator(
            progress = { current / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

class InterviewViewModel : ViewModel() {
    private val defaultQuestions = listOf(
        "Tell me about yourself and frame your background for this role.",
        "Describe a difficult tradeoff you made and how you measured the outcome.",
        "How would you approach the first 30 days in this role?"
    )

    var questions by mutableStateOf(defaultQuestions)
        private set
    var currentIndex by mutableStateOf(0)
        private set
    var score by mutableStateOf(82)
        private set
    var isComplete by mutableStateOf(false)
        private set
    private var roleContext by mutableStateOf("SDE at Google (Fresher)")
    private var interviewType by mutableStateOf("Technical")
    private val answers = mutableListOf<String>()

    val currentQuestion: String
        get() = questions.getOrElse(currentIndex) { questions.last() }

    val isLastQuestion: Boolean
        get() = currentIndex == questions.lastIndex

    fun start(role: String, type: String) {
        roleContext = role
        interviewType = type
        questions = listOf(
            "Tell me about yourself and frame your background for a $role role.",
            "What is a realistic $type challenge you expect in this role?",
            "Describe a difficult tradeoff you made and how you measured the outcome.",
            "How would you close this interview with confidence?"
        )
        currentIndex = 0
        score = 82
        isComplete = false
        answers.clear()
    }

    fun submit(answer: String) {
        answers.add(answer.ifBlank { "I would clarify the problem, explain my approach, and summarize the tradeoffs." })
        score = (70 + answer.length.coerceAtMost(120) / 5).coerceAtMost(94)
        if (isLastQuestion) {
            isComplete = true
            return
        }
        currentIndex += 1
    }

    fun reportJson(): String {
        val communication = (score + 3).coerceAtMost(96)
        val relevance = (score - 4).coerceAtLeast(58)
        val technical = (score + 1).coerceAtMost(94)
        val questionFeedback = JSONArray()
        questions.forEachIndexed { index, question ->
            questionFeedback.put(
                JSONObject()
                    .put("question", question)
                    .put("answer", answers.getOrElse(index) { "Demo answer captured during the mock interview." })
                    .put("idealAnswer", "A strong answer states the context, clarifies constraints, gives a structured recommendation, and closes with measurable impact.")
                    .put("score", listOf(84, 76, 68, 89).getOrElse(index) { score })
            )
        }
        return JSONObject()
            .put("roleCompany", roleContext)
            .put("date", "May 2, 2026")
            .put("score", score)
            .put("summary", "You showed confident structure and good role alignment. The next lift is adding sharper metrics, clearer tradeoffs, and a tighter final recommendation.")
            .put("communication", communication)
            .put("relevance", relevance)
            .put("technical", technical)
            .put(
                "focus",
                JSONArray()
                    .put(JSONObject().put("title", "Clarify Constraints").put("description", "Open each answer by naming scope, assumptions, and success criteria."))
                    .put(JSONObject().put("title", "Quantify Impact").put("description", "Use numbers or directional metrics so your examples feel concrete."))
                    .put(JSONObject().put("title", "Close Decisively").put("description", "End with a recommendation instead of trailing into explanation."))
            )
            .put("answers", questionFeedback)
            .toString()
    }
}
