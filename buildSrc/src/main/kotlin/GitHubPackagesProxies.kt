
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

fun startGitHubPackagesProxy() {
    CoroutineScope(IO).launch {
        try {
            println("Starting GitHubPackagesProxy...")

            Class
                .forName("GitHubPackagesProxy")
                .getDeclaredMethod("main", Array<String>::class.java)
                .invoke(null, arrayOf<String>())
        } catch (_: java.net.BindException) {
            println("Already running.") // Run `gradle --stop` if you are debugging locally
        }
    }
}
