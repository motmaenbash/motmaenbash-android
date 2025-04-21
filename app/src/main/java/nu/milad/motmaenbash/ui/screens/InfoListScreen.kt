package nu.milad.motmaenbash.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.Pages
import nu.milad.motmaenbash.models.InfoItem
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

    val title = when (page) {
        Pages.FAQ -> stringResource(id = R.string.faq_screen_title)
        Pages.PERMISSION -> stringResource(id = R.string.permissions_explanation_screen_title)
        else -> stringResource(id = R.string.faq_screen_title)
    }

    AppBar(
        title = title,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp, 2.dp),
            contentPadding = innerPadding

        ) {

            items(questions.size, key = { index -> questions[index] }) { index ->
                InfoCard(
                    faq = InfoItem(
                        question = questions[index],
                        answer = answers[index],
                    )

                )
            }

        }

    }

}


@Composable
fun InfoCard(
    faq: InfoItem,
) {
    AppCard(
        cornerRadius = 12.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.Top
            ) {
                if (faq.icon != null) {
                    Icon(
                        imageVector = faq.icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(text = faq.question, style = typography.headlineSmall)
            }

            Text(
                text = faq.answer,
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
