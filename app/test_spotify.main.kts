import java.net.URL
import java.net.HttpURLConnection
import java.io.InputStreamReader
import java.io.BufferedReader

fun main() {
    try {
        val url = URL("https://open.spotify.com/oembed?url=https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        var line: String?
        val response = StringBuilder()
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        println(response.toString())
    } catch (e: Exception) {
        println("Error: \${e.message}")
    }
}
