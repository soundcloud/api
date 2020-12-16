const BANNER_NAME = 'banner-2018-05-15';

function initAnalytics() {
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-79535818-1', 'auto');
  ga('send', 'pageview');
}

/* Cookie banner code copied from developer portal */
function localStorageSupported() {
  return (window.localStorage && typeof window.localStorage.getItem === 'function');
}

function hasDismissedBanner() {
  return (localStorageSupported() && window.localStorage.getItem(BANNER_NAME)) ||
    (new RegExp(BANNER_NAME + '=1').test(document.cookie))
}

function storeDismiss() {
  if (localStorageSupported()) {
    window.localStorage.setItem(BANNER_NAME, '1');
  } else {
    document.cookie = BANNER_NAME + '=1; path=/';
  }
}

function dismissBanner(e) {
  e.preventDefault();
  document.getElementById('cookieBanner').classList.add('m-hidden')
  storeDismiss();
}

function addCookieBanner() {
  if (hasDismissedBanner()) { return }

  const cookieBanner = `
    <div id="cookieBanner" class="announcements">
      <div class="announcement">
        <a href="" class="announcement__dismiss" aria-role="button" title="Dismiss"></a>
        <span class="announcement__message">We use cookies for various purposes including analytics and personalized marketing. By continuing to use the service, you agree to our use of cookies as described in the <a href="https://soundcloud.com/pages/cookies/05-2018" target="_blank">Cookie Policy</a>.</span>
      </div>
    </div>`

  document.body.insertAdjacentHTML('beforeend', cookieBanner)

  const dismiss = document.getElementsByClassName('announcement__dismiss')[0]
  dismiss.addEventListener("click", dismissBanner)
}

if (window.location.host === 'developers.soundcloud.com') {
  initAnalytics()
  document.addEventListener("DOMContentLoaded", addCookieBanner)
}

