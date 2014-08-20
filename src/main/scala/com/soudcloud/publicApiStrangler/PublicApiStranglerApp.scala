
import com.soudcloud.authorization.AuthorizeContent
import com.soudcloud.authorization.ContentAuthorizationFilter
import com.soudcloud.publicApiStrangler.DispatchToMothershipHandler
import com.soudcloud.publicApiStrangler.PublicApiClientComponent
import com.soundcloud.bff.BazookaConfigComponent
import com.soundcloud.bff.BffApp
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController

class App extends BffApp {

  val app = new BazookaConfigComponent with BffController with PublicApiClientComponent with ContentAuthorizationComponent {
    val authorizeContent = new AuthorizeContent(contentAuthorizationService, this)
    val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)
    override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))
  }
}
