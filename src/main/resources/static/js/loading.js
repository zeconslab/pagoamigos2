(function(){
  const getEl = () => document.getElementById('global-loading');
  function show(){ const e = getEl(); if(e) e.classList.remove('hidden'); }
  function hide(){ const e = getEl(); if(e) e.classList.add('hidden'); }

  // Form submit
  document.addEventListener('submit', e => {
    const f = e.target;
    if(f && f.matches('form') && !f.hasAttribute('data-no-loading')) show();
  }, true);

  // Link navigation
  document.addEventListener('click', e => {
    const a = e.target.closest('a');
    if(!a) return;
    const href = a.getAttribute('href');
    if(!href) return;
    if(href.startsWith('#') || href.startsWith('javascript:')) return;
    if(a.hasAttribute('data-no-loading') || a.hasAttribute('download') || a.target) return;
    show();
  }, true);

  // Wrap fetch
  if(window.fetch){
    const _fetch = window.fetch;
    window.fetch = function(...args){
      show();
      return _fetch.apply(this,args).finally(hide);
    };
  }

  // Wrap XHR
  const XHR = window.XMLHttpRequest;
  if(XHR){
    const open = XHR.prototype.open;
    const send = XHR.prototype.send;
    XHR.prototype.open = function(){ this._showLoader = true; return open.apply(this, arguments); };
    XHR.prototype.send = function(){ if(this._showLoader) show(); this.addEventListener('loadend', hide); return send.apply(this, arguments); };
  }

  window.addEventListener('load', hide);
  document.addEventListener('DOMContentLoaded', hide);
})();