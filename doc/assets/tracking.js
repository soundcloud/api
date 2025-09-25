function addConsentScripts() {
  const preloadScript = document.createElement('script');
  preloadScript.src = 'https://consent.sndcdn.com/v1/preload.js';
  
  const consentScript = document.createElement('script');
  consentScript.src = 'https://consent.sndcdn.com/v1/consent.js';

  consentScript.onload = function() {
    addClickHandlerToCookieManagerLink();
    initAnalyticsIfConsented(); 
  };
  
  document.head.appendChild(preloadScript);
  document.head.appendChild(consentScript);
}

function initAnalytics() {
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-79535818-1', 'auto');
  ga('send', 'pageview');
}

const CookieCategory = {
  StrictlyNecessary: 'C0001',
  Performance: 'C0002',
  Functional: 'C0003',
  Targeting: 'C0004',
  SocialMedia: 'C0005',
  Communications: 'C0007',
};

function initAnalyticsIfConsented() {
  if (window.location.host === 'developers.soundcloud.com') {
    Promise.all([
      window.SCConsent.waitForCategory(CookieCategory.Performance), 
      window.SCConsent.waitForCategory(CookieCategory.Targeting)  
    ]).then(() => {
      initAnalytics();
    });
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', addConsentScripts);
} else {
  addConsentScripts();
}

function addClickHandlerToCookieManagerLink() {
  document.addEventListener('click', (e) => {
    const link = e.target.closest('#cookie-manager');
    if (link && window?.SCConsent?.showConsentDialog) {
      e.preventDefault();
      window.SCConsent.showConsentDialog();
    }
  });
}
