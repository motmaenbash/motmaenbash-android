package nu.milad.motmaenbash.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.Pages
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme


@Composable
fun InfoListScreen(page: String = Pages.FAQ) {


    val (questions, answers) = when (page) {
        Pages.FAQ -> stringArrayResource(id = R.array.faq_questions) to stringArrayResource(id = R.array.faq_answers)

        Pages.PERMISSION -> stringArrayResource(id = R.array.permission_titles) to stringArrayResource(
            id = R.array.permission_descriptions
        )

        else -> stringArrayResource(id = R.array.faq_questions) to stringArrayResource(id = R.array.faq_answers)
    }

    AppBar(
        title = stringResource(id = R.string.faq_activity_title),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 2.dp),
            contentPadding = innerPadding

        ) {

            items(questions.size, key = { index -> questions[index] }) { index ->
                InfoCard(
                    question = questions[index], answer = answers[index]

                )
            }

        }

    }

}


@Composable
fun InfoCard(question: String, answer: String) {
    AppCard(
        cornerRadius = 12.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, style = typography.headlineSmall)
            Text(
                text = answer,
                style = typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FaqListScreenPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            InfoListScreen(Pages.FAQ)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionListScreenPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            InfoListScreen(Pages.PERMISSION)
        }
    }
}