
import com.soundcloud.authorization.AuthorizeHttpResponse
import com.soundcloud.authorization.ContentAuthorizationFilter
import com.soundcloud.publicApiStrangler.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.PublicApiClientComponent
import com.soundcloud.bff.BazookaConfigComponent
import com.soundcloud.bff.BffApp
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController

class App extends BffApp {

  val app = new BazookaConfigComponent with BffController with PublicApiClientComponent with ContentAuthorizationComponent {
    val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
    val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)
    override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))
  }
}
