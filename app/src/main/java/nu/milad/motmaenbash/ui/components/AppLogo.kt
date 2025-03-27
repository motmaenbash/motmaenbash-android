package nu.milad.motmaenbash.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nu.milad.motmaenbash.R

@Composable
fun AppLogo(size: Dp = 120.dp) {

    Image(
        painter = painterResource(id = R.drawable.logo_transparent),
        contentDescription = "MotmaenBash Logo",
        modifier = Modifier.size(size),
        contentScale = ContentScale.FillBounds
    )

}

@Preview(showBackground = true)
@Composable
fun AppLogoPreview() {
    AppLogo()
}