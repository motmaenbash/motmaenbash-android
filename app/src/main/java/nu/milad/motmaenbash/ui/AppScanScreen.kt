import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.viewmodels.AppScanViewModel
import nu.milad.motmaenbash.viewmodels.ScanState

@Composable


fun AppScanScreen(
    viewModel: AppScanViewModel = viewModel()

) {


    val currentScanState by viewModel.scanState.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()
    val detectedSuspiciousApps by viewModel.suspiciousApps.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()


    AppBar(
        title = stringResource(id = R.string.app_scan_activity_title),

        ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.last_scan_time, lastScanTime),

                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )



            Text(
                text = scanStatusMessage,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentScanState == ScanState.IN_PROGRESS) viewModel.stopScan() else viewModel.startScan()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentScanState == ScanState.IN_PROGRESS) Color.Red else ColorPrimary,
                    contentColor = Color.White
                ), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = if (currentScanState == ScanState.IN_PROGRESS) "توقف" else "اسکن برنامه‌ها")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentScanState == ScanState.IN_PROGRESS) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }


            if (currentScanState == ScanState.COMPLETED_SUCCESSFULLY && detectedSuspiciousApps.isEmpty()) {
                Text(text = "هیچ برنامه مشکوکی یافت نشد")
            } else {
                SuspiciousAppsList(detectedSuspiciousApps)
            }


        }
    }
}

@Composable
fun SuspiciousAppsList(apps: List<App>) {
    LazyColumn {
        items(apps) { app ->
            SuspiciousAppItem(app = app)
        }
    }
}

@Composable
fun SuspiciousAppItem(app: App) {
    val context = LocalContext.current
    // Track if this app has been uninstalled
    val (isUninstalled, setUninstalled) = remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appName = result.data?.getStringExtra("APP_NAME") ?: app.appName
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "برنامه '$appName' با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            // Mark as uninstalled when successful
            setUninstalled(true)
        } else {
            Toast.makeText(context, "حذف برنامه '$appName' لغو شد", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Display app icon if available
        app.appIcon?.let { icon ->
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = app.appName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

        Button(
            onClick = {
                val intent = PackageUtils.uninstallApp(context, app.packageName)
                uninstallLauncher.launch(intent)
            },
            enabled = !isUninstalled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isUninstalled) Color.Gray else MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Gray
            )
        ) {
            Text(text = if (isUninstalled) "حذف شد" else "حذف برنامه")
        }
    }
}
