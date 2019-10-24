package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.httprpc.RequestMethod
import org.httprpc.WebService
import java.net.InetAddress

@WebServlet(urlPatterns = ["/system-info/*"], loadOnStartup = 1)
class SystemInfoService : WebService() {
    class SystemInfo(
            val hostName: String,
            val hostAddress: String,
            val availableProcessors: Int,
            val freeMemory: Long,
            val totalMemory: Long
    )

    @RequestMethod("GET")
    fun getSystemInfo(): SystemInfo {
        val localHost = InetAddress.getLocalHost()
        val runtime = Runtime.getRuntime()
        return SystemInfo(
                localHost.hostName,
                localHost.hostAddress,
                runtime.availableProcessors(),
                runtime.freeMemory(),
                runtime.totalMemory()
        )
    }
}