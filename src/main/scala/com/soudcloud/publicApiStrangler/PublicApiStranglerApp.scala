
import com.soudcloud.authorization.AuthorizeContent
import com.soudcloud.authorization.ContentAuthorizationFilter
import com.soudcloud.publicApiStrangler.DispatchToMothershipHandler
import com.soudcloud.publicApiStrangler.PublicApiClientComponent
import com.soundcloud.bff.BazookaConfigComponent
import com.soundcloud.bff.BffApp
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController
import com.soundcloud.rollout.RolloutClientComponent
import com.soundcloud.rollout.RolloutRepository

class App extends BffApp {

  val app = new BazookaConfigComponent with BffController with PublicApiClientComponent with ContentAuthorizationComponent with RolloutClientComponent {
    val authorizeContent = new AuthorizeContent(contentAuthorizationService, this)
    val rolloutRepository = new RolloutRepository(rolloutClient, cache)
    val authorizationFilter = new ContentAuthorizationFilter(authorizeContent, rolloutRepository)
    override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))
  }
}
