package nu.milad.motmaenbash.ui.activities


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import nu.milad.motmaenbash.ui.components.OverlayPermissionDialog
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme

class PermissionTutorialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MotmaenBashTheme {
                OverlayPermissionDialog()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewTutorialContent() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OverlayPermissionDialog()
        }
    }
}
