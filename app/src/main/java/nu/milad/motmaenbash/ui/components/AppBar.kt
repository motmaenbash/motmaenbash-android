package nu.milad.motmaenbash.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.ui.LocalNavController
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.ui.theme.VazirFontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val titleContentColor = ColorPrimary
    val navController = LocalNavController.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = titleContentColor,
                ),
                title = {
                    Text(
                        title, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = VazirFontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // navigate back
                        if (!navController.navigateUp()) {
                            // If no back stack exists, return to main screen
                            navController.navigate(NavRoutes.MAIN_SCREEN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }

                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        content(
            innerPadding
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppBarPreview() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        MotmaenBashTheme {
            AppBar(
                title = "مطمئن باش"
            ) { }
        }
    }
}