package nu.milad.motmaenbash.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.Pages
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme


@Composable
fun InfoListScreen(navController: NavController, page: String = Pages.FAQ) {


    val (questions, answers) = when (page) {
        Pages.FAQ -> stringArrayResource(id = R.array.faq_questions) to stringArrayResource(id = R.array.faq_answers)

        Pages.PERMISSION -> stringArrayResource(id = R.array.permission_titles) to stringArrayResource(
            id = R.array.permission_descriptions
        )

        else -> stringArrayResource(id = R.array.faq_questions) to stringArrayResource(id = R.array.faq_answers)
    }

//    CenterAlignedTopAppBar(
//        title = {
//            Text(
//                stringResource(id = R.string.faq_activity_title),
//                style = MaterialTheme.typography.titleLarge
//            )
//        },
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
//    )

    AppBar(
        title = stringResource(id = R.string.faq_activity_title),
        onNavigationIconClick = { navController.navigateUp() },
        onActionClick = { /* Handle menu action */ },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 2.dp), contentPadding = innerPadding

        ) {


            // FAQ Content
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FaqListScreenPreview() {
    MotmaenBashTheme {
        InfoListScreen(rememberNavController(), Pages.FAQ)
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionListScreenPreview() {
    MotmaenBashTheme {
        InfoListScreen(rememberNavController(), Pages.PERMISSION)
    }
}
