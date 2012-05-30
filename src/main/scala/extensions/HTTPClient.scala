package extensions

import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.{HttpClient, MultiThreadedHttpConnectionManager}
import java.security.cert.X509Certificate
import java.security.{KeyManagementException, NoSuchAlgorithmException, SecureRandom}
import javax.net.ssl.{KeyManager, SSLContext, X509TrustManager}

/**
 * HTTP Client. See if we can replace it by something provided by the framework.
 */
trait HTTPClient {

  TrustfulManager.install()
  val connectionParams = new HttpConnectionManagerParams
  connectionParams setDefaultMaxConnectionsPerHost (15)
  connectionParams setMaxTotalConnections (250)
  connectionParams setConnectionTimeout (20000)
  val multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
  multiThreadedHttpConnectionManager setParams (connectionParams)

  def getHttpClient: HttpClient = new HttpClient(multiThreadedHttpConnectionManager)
}

class TrustfulManager extends X509TrustManager {
  def checkServerTrusted(p1: Array[X509Certificate], p2: String) {}
  def checkClientTrusted(p1: Array[X509Certificate], p2: String) {}
  def getAcceptedIssuers: Array[X509Certificate] = null
}

object TrustfulManager {

  def install() {
    try {
      val ctx = SSLContext.getInstance("TLS")
      ctx.init(Array[KeyManager](), Array(new TrustfulManager()), new SecureRandom())
      SSLContext.setDefault(ctx)
    }
    catch {
      case e: NoSuchAlgorithmException =>
        throw new RuntimeException(e)
      case k: KeyManagementException =>
        throw new RuntimeException(k)
    }
  }
}