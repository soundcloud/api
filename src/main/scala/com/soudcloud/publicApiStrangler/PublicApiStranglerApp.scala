
import com.soundcloud.scalakit.framework.FinagleBasedServer
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.finagle.http.HttpServer
import com.soudcloud.rateLimiting.framework.RateLimitComponent
import com.soundcloud.bff.BffApp
import com.soundcloud.bff.BazookaConfigComponent
import com.soundcloud.bff.web.BffController
import com.soudcloud.publicApiStrangler.PublicApiClientComponent
import com.soudcloud.publicApiStrangler.DispatchToMothershipHandler

class App extends BffApp {

  val app = new BffController with PublicApiClientComponent with BazookaConfigComponent {
    override val fallbackHandler = Some(new DispatchToMothershipHandler(publicApiClient))
  }
}
