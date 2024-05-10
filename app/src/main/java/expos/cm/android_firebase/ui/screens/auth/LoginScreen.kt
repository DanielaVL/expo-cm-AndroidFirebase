package expos.cm.android_firebase.ui.screens.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
@@ -44,7 +46,9 @@ import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.GoogleAuthProvider
import expos.cm.android_firebase.R
import expos.cm.android_firebase.ui.navigation.Routes
import expos.cm.android_firebase.ui.theme.Purple40
@@ -62,6 +66,33 @@ fun LoginScreen(analytics: AnalyticsManager, auth: AuthManager, navigation: NavC
                                   val context = LocalContext.current
                                   val scope = rememberCoroutineScope()

                                   val googleSignInLauncher = rememberLauncherForActivityResult(
                                       contract = ActivityResultContracts.StartActivityForResult()) { result ->
                                       when(val account = auth.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))) {
                                           is AuthRes.Success -> {
                                               val credential = GoogleAuthProvider.getCredential(account?.data?.idToken, null)
                                               scope.launch {
                                                   val fireUser = auth.signInWithGoogleCredential(credential)
                                                   if (fireUser != null){
                                                       Toast.makeText(context, "Bienvenidx", Toast.LENGTH_SHORT).show()
                                                       navigation.navigate(Routes.Home.route){
                                                           popUpTo(Routes.Login.route){
                                                               inclusive = true
                                                           }
                                                       }
                                                   }
                                               }
                                           }
                                           is AuthRes.Error -> {
                                               analytics.logError("Error SignIn: ${account.errorMessage}")
                                               Toast.makeText(context, "Error: ${account.errorMessage}", Toast.LENGTH_SHORT).show()
                                           }
                                           else -> {
                                               Toast.makeText(context, "Error desconocido", Toast.LENGTH_SHORT).show()
                                           }
                                       }
                                   }

                                           Box(modifier = Modifier.fillMaxSize()) {
                                       ClickableText(
                                           text = AnnotatedString("¿No tienes una cuenta? Regístrate"),
                                           @@ -155,7 +186,7 @@ fun LoginScreen(analytics: AnalyticsManager, auth: AuthManager, navigation: NavC
                                                                               Spacer(modifier = Modifier.height(15.dp))
                                       SocialMediaButton(
                                           onClick = {
                                               auth.signInWithGoogle(googleSignInLauncher)
                                           },
                                           text = "Continuar con Google",
                                           icon = R.drawable.ic_google,
                                           color = Color(0xFFF1F1F1)
                                       )
                                   }
}
private suspend fun incognitoSignIn(auth: AuthManager, analytics: AnalyticsManager, context: Context, navigation: NavController) {
    when(val result = auth.signInAnonymously()) {
        is AuthRes.Success -> {
            analytics.logButtonClicked("Click: Continuar como invitado")
            navigation.navigate(Routes.Home.route) {
                popUpTo(Routes.Login.route) {
                    inclusive = true
                }
            }
        }
        is AuthRes.Error -> {
            analytics.logError("Error SignIn Incognito: ${result.errorMessage}")
        }
    }
}
private suspend fun emailPassSignIn(email: String, password: String, auth: AuthManager, analytics: AnalyticsManager, context: Context, navigation: NavController) {
    if(email.isNotEmpty() && password.isNotEmpty()) {
        when (val result = auth.signInWithEmailAndPassword(email, password)) {
            is AuthRes.Success -> {
                analytics.logButtonClicked("Click: Iniciar sesión correo & contraseña")
                navigation.navigate(Routes.Home.route) {
                    popUpTo(Routes.Login.route) {
                        inclusive = true
                    }
                }
            }
            is AuthRes.Error -> {
                analytics.logButtonClicked("Error SignUp: ${result.errorMessage}")
                Toast.makeText(context, "Error SignUp: ${result.errorMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        Toast.makeText(context, "Existen campos vacios", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun SocialMediaButton(onClick: () -> Unit, text: String, icon: Int, color: Color, ) {
    var click by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(start = 40.dp, end = 40.dp).clickable { click = !click },
        shape = RoundedCornerShape(50),
        border = BorderStroke(width = 1.dp, color = if(icon == R.drawable.ic_incognito) color else Color.Gray),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                modifier = Modifier.size(24.dp),
                contentDescription = text,
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$text", color = if(icon == R.drawable.ic_incognito) Color.White else Color.Black)
            click = true
        }
    }
}